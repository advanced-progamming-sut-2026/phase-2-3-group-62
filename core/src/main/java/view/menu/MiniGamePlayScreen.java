package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import controller.menu.MenuController;
import controller.menu.PreGameController;
import main.Maini;
import model.Game;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.minigame.*;
import model.season.AncientEgypt;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import util.FileManager;
import view.audio.AudioManager;
import view.game.GameGrid;
import view.game.GamePlayHud;
import view.game.GamePlayInputHandler;
import view.game.GamePlayScreen;
import view.game.renderers.*;
import view.ui.Toast;
import view.ui.WalletBar;

import java.util.ArrayList;
import java.util.List;

public class MiniGamePlayScreen implements Screen {
    private final Maini game;
    private final GameController gameController;
    private final String minigameName;
    private final List<String> chosenPlants;
    private final SpriteBatch batch;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private Texture dimNightTexture;

    private LawnRenderer lawnRenderer;
    private PlantRenderer plantRenderer;
    private ZombieRenderer zombieRenderer;
    private ProjectileRenderer projectileRenderer;
    private ZombossRenderer zombossRenderer;

    private GamePlayHud hud;
    private boolean isPaused = false;
    private float stateTime = 0f;
    private float tickAccumulator = 0f;
    private boolean isInitialized = false;

    // Custom HUD adapter for minigames - full plant planting support
    private class MiniGameHudAdapter extends GamePlayScreen {
        private final List<String> plants;

        public MiniGameHudAdapter(Maini game, GameController gc, List<String> plants) {
            super(game, gc, plants);
            this.plants = plants != null ? plants : new ArrayList<>();
        }

        @Override
        public void enqueueLog(String message, boolean isError) {
            MiniGamePlayScreen.this.enqueueLog(message, isError);
        }

        @Override
        public boolean isPaused() { return MiniGamePlayScreen.this.isPaused; }

        @Override
        public void togglePause() { MiniGamePlayScreen.this.togglePause(); }

        @Override
        public void restartLevel() { MiniGamePlayScreen.this.restartLevel(); }

        @Override
        public void saveAndExit() { MiniGamePlayScreen.this.saveAndExit(); }

        @Override
        public List<String> getSelectedPlants() { return this.plants; }

        @Override
        public int getHoveredCol() {
            Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            return GameGrid.getColumnAt(mouseWorld.x);
        }

        @Override
        public int getHoveredRow() {
            Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            return GameGrid.getRowAt(mouseWorld.y);
        }

        @Override
        public GamePlayHud getHud() { return MiniGamePlayScreen.this.hud; }

        @Override
        public ToolMode getCurrentToolMode() {
            // For Zombotany, we need to support planting
            if ("Zombotany".equalsIgnoreCase(minigameName)) {
                return ToolMode.PLANTING;
            }
            return ToolMode.NONE;
        }

        @Override
        public Plant getSelectedPlantToPlant() {
            // For Zombotany, return the first available plant from selected plants
            if ("Zombotany".equalsIgnoreCase(minigameName) && !this.plants.isEmpty()) {
                Plant p = PlantFactory.createPlant(this.plants.get(0));
                if (p != null) {
                    // Set default position
                    p.setX(0);
                    p.setY(0);
                }
                return p;
            }
            return null;
        }

        @Override
        public void setSelectedPlantToPlant(Plant plant) {}

        @Override
        public void setToolMode(ToolMode mode) {}
    }

    // Custom input handler adapter with full planting support
    private class MiniGameInputAdapter extends GamePlayInputHandler {
        private final MiniGameHudAdapter hudAdapter;

        public MiniGameInputAdapter(Maini game, GameController gc, MiniGameHudAdapter adapter) {
            super(adapter, game, gc);
            this.hudAdapter = adapter;
        }

        @Override
        public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
            if (isPaused || (hud != null && hud.getGameOverOverlay() != null && hud.getGameOverOverlay().isShown())) {
                return false;
            }

            Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            Game modelGame = gameController.getGame();

            int hoveredCol = GameGrid.getColumnAt(mouseWorld.x);
            int hoveredRow = GameGrid.getRowAt(mouseWorld.y);

            if (hoveredCol != -1 && hoveredRow != -1 && modelGame != null) {
                MiniGame mg = modelGame.getActiveMiniGame();

                if (mg instanceof Vasebreaker) {
                    String result = gameController.smashVase(hoveredCol, hoveredRow);
                    enqueueLog(result, result.startsWith("Error"));
                    return true;
                } else if (mg instanceof WallnutBowling) {
                    if (modelGame.getConveyorBeltPlants() != null && !modelGame.getConveyorBeltPlants().isEmpty()) {
                        String plantType = modelGame.getConveyorBeltPlants().get(0);
                        String result = gameController.plantPlant(plantType, hoveredCol, hoveredRow);
                        enqueueLog(result, result.startsWith("Error"));
                        return true;
                    }
                } else if (mg instanceof Zombotany) {
                    // Full plant planting for Zombotany
                    if (!hudAdapter.getSelectedPlants().isEmpty()) {
                        String plantType = hudAdapter.getSelectedPlants().get(0);
                        String result = gameController.plantPlant(plantType, hoveredCol, hoveredRow);
                        enqueueLog(result, result.startsWith("Error"));
                        return true;
                    } else {
                        enqueueLog("No plants selected! Use seed chooser.", true);
                        return true;
                    }
                } else if (mg instanceof Beghoul) {
                    // Beghoul - swap plants
                    // This is handled through the game controller
                } else if (mg instanceof IZombie) {
                    // IZombie - place zombies
                    // This could be handled via a separate UI
                }
            }

            return super.touchDown(event, x, y, pointer, button);
        }
    }

    public MiniGamePlayScreen(Maini game, GameController gameController, String minigameName, List<String> chosenPlants) {
        this.game = game;
        this.gameController = gameController;
        this.minigameName = minigameName;
        this.batch = game.getBatch();
        this.chosenPlants = chosenPlants != null ? new ArrayList<>(chosenPlants) : new ArrayList<>();

        GameGrid.activeSeasonContext = new AncientEgypt();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        AudioManager.getInstance().playSeasonMusic(new AncientEgypt());

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_COWBOY");
        }

        dimNightTexture = createSolidTexture(new Color(0.05f, 0.05f, 0.25f, 0.35f));

        lawnRenderer = new LawnRenderer(game.getTextureBank(), bgRegion, new com.badlogic.gdx.graphics.glutils.ShapeRenderer(), pamPlayer);
        plantRenderer = new PlantRenderer(pamPlayer);
        zombieRenderer = new ZombieRenderer(pamPlayer, game.getTextureBank());
        projectileRenderer = new ProjectileRenderer(game.getTextureBank(), pamPlayer);
        zombossRenderer = new ZombossRenderer(pamPlayer);

        setupMinigamePlants();

        // Create HUD with proper plant list for Zombotany
        MiniGameHudAdapter hudAdapter = new MiniGameHudAdapter(game, gameController, chosenPlants);
        hud = new GamePlayHud(hudAdapter, stage, game.getSkin(), pamPlayer, game, gameController);

        stage.addListener(new MiniGameInputAdapter(game, gameController, hudAdapter));

        isInitialized = true;
        enqueueLog(minigameName + " started!", false);
    }

    private void setupMinigamePlants() {
        Game modelGame = gameController.getGame();
        if (modelGame == null) return;

        MiniGame mg = modelGame.getActiveMiniGame();

        if (mg instanceof Beghoul) {
            // Beghoul fills grid with random plants - already done in setupStage
        } else if (mg instanceof IZombie) {
            // IZombie has plants placed on the board via setupStage
        } else if (mg instanceof Vasebreaker) {
            // Vasebreaker has vases on the board - setup done in setupVaseGrid
        } else if (mg instanceof WallnutBowling) {
            // Wallnut Bowling uses conveyor belt
            modelGame.getConveyorBeltPlants().add("WallNut");
            modelGame.getConveyorBeltPlants().add("WallNut");
            modelGame.getConveyorBeltPlants().add("WallNut");
        } else if (mg instanceof Zombotany) {
            // Zombotany - plants are chosen by the player via seed chooser
            // The plants will be placed by the player clicking on the board
            if (!chosenPlants.isEmpty()) {
                // Pre-populate with some initial plants so the player can start
                for (int row = 0; row < Math.min(3, GameGrid.ROWS); row++) {
                    String plantType = chosenPlants.get(row % chosenPlants.size());
                    Plant p = PlantFactory.createPlant(plantType);
                    if (p != null) {
                        p.setX(2);
                        p.setY(row);
                        modelGame.addPlant(p);
                        modelGame.getBoard().getTile(row, 2).setPlant(p);
                    }
                }
                modelGame.getGameLogMessages().add("Zombotany: Initial plants placed. Click to plant more!");
            }
        }
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void enqueueLog(String message, boolean isError) {
        if (message == null || message.trim().isEmpty()) return;
        System.out.println("[MINIGAME LOG] " + message);
        if (stage != null) {
            Toast.show(stage, game.getSkin(), message, isError);
        }
    }

    public void togglePause() {
        isPaused = !isPaused;
        if (hud != null) {
            hud.showPauseOverlay(isPaused);
        }
    }

    public void restartLevel() {
        Game current = gameController.getGame();
        int diffVal = 3;
        Game newModelGame = new Game(5, 9, 1, diffVal);
        newModelGame.setCurrentSeason(new AncientEgypt());

        MiniGame mg = null;
        if ("Vasebreaker".equalsIgnoreCase(minigameName)) {
            mg = new Vasebreaker();
            ((Vasebreaker) mg).setupVaseGrid(5, 9, 1);
        } else if ("IZombie".equalsIgnoreCase(minigameName)) {
            mg = new IZombie();
            ((IZombie) mg).setupStage(newModelGame, 1);
        } else if ("Beghoul".equalsIgnoreCase(minigameName)) {
            mg = new Beghoul();
            ((Beghoul) mg).setupStage(newModelGame, 1);
        } else if ("Zombotany".equalsIgnoreCase(minigameName)) {
            mg = new Zombotany();
        } else if ("WallnutBowling".equalsIgnoreCase(minigameName)) {
            mg = new WallnutBowling();
        }

        newModelGame.setActiveMiniGame(mg);
        newModelGame.start();
        newModelGame.setupSpecialLevelFeatures();
        newModelGame.setSunCount(newModelGame.getLevel().getInitialSunAmount());

        gameController.setGame(newModelGame);
        game.setScreen(new MiniGamePlayScreen(game, gameController, minigameName, new ArrayList<>(chosenPlants)));
        dispose();
    }

    public void saveAndExit() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            FileManager.updateUser(user);
        }
        game.setScreen(new MiniGameSelectionScreen(game, game.getMenuController(), game.getSkin()));
        dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.10f, 0.12f, 1f);

        ScreenShake.update(game.getViewport().getCamera(), delta);

        Game modelGame = gameController.getGame();
        if (modelGame != null && (modelGame.isWon() || modelGame.isLost())) {
            if (hud != null && hud.getGameOverOverlay() != null && !hud.getGameOverOverlay().isShown()) {
                int earnedScore = modelGame.getScoreGame() != null ? modelGame.getScoreGame().getFinalScore() : 0;
                hud.getGameOverOverlay().showResult(modelGame.isWon(), earnedScore);
            }
        }

        if (!isPaused && isInitialized && modelGame != null) {
            stateTime += delta;
            tickAccumulator += delta;

            float tickRate = 0.1f;
            while (tickAccumulator >= tickRate) {
                gameController.advanceTime(1);
                tickAccumulator -= tickRate;
            }

            if (hud != null) {
                hud.updateCooldowns(delta, 1.0f);
            }
        }

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        if (lawnRenderer != null) {
            lawnRenderer.renderBackground(batch, game.getViewport().getWorldWidth(), game.getViewport().getWorldHeight());
        }

        if (modelGame != null && modelGame.getLevel().getSpecialLevelType() == model.enums.SpecialLevelType.NIGHT_OPS && dimNightTexture != null) {
            batch.draw(dimNightTexture, 0, 0, game.getViewport().getWorldWidth(), game.getViewport().getWorldHeight());
        }

        if (lawnRenderer != null) {
            lawnRenderer.renderLawnElements(batch, modelGame, stateTime);
        }

        // Render plants and zombies by row
        if (modelGame != null && plantRenderer != null && zombieRenderer != null) {
            for (int r = 0; r < GameGrid.ROWS; r++) {
                plantRenderer.renderRow(batch, modelGame, r, stateTime, delta);
                zombieRenderer.renderRow(batch, modelGame, r, stateTime, delta);
            }
            if (zombossRenderer != null) {
                zombossRenderer.render(batch, modelGame, stateTime, delta);
            }
            zombieRenderer.renderDyingZombies(batch, delta);
        }

        if (projectileRenderer != null) {
            projectileRenderer.render(batch, modelGame, stateTime);
        }
        batch.end();

        if (hud != null && isInitialized) {
            hud.updateGameStateUI();
        }

        if (stage != null) {
            stage.act(isPaused ? 0 : Math.min(delta, 1 / 30f));
            stage.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        game.getViewport().update(width, height, true);
        if (stage != null) {
            stage.getViewport().update(width, height, true);
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { AudioManager.getInstance().stopMusic(); }

    @Override
    public void dispose() {
        if (dimNightTexture != null) dimNightTexture.dispose();
        if (hud != null) hud.dispose();
        if (stage != null) stage.dispose();
    }
}
