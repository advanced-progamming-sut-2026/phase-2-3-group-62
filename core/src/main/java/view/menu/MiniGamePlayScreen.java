package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import main.Maini;
import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.handler.PlantAbilityHandler;
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
    private ShapeRenderer shapeRenderer;

    private LawnRenderer lawnRenderer;
    private PlantRenderer plantRenderer;
    private ZombieRenderer zombieRenderer;
    private ProjectileRenderer projectileRenderer;
    private ZombossRenderer zombossRenderer;
    private VaseRenderer vaseRenderer;

    private GamePlayHud hud;
    private MiniGameHudAdapter hudAdapter;
    private boolean isPaused = false;
    private float stateTime = 0f;
    private float tickAccumulator = 0f;
    private boolean isInitialized = false;

    private GamePlayScreen.ToolMode currentToolMode = GamePlayScreen.ToolMode.NONE;
    private Plant selectedPlantToPlant = null;
    private String selectedZombieToPlace = null;
    private int lastPlantCountInHand = -1;

    private int beghoulSelectedRow = -1;
    private int beghoulSelectedCol = -1;
    private Vector2 dragStartPos = null;
    private Table beghoulUpgradeTable = null;

    private class MiniGameHudAdapter extends GamePlayScreen {
        public MiniGameHudAdapter(Maini game, GameController gc, List<String> plants) {
            super(game, gc, plants);
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
        public List<String> getSelectedPlants() {
            Game g = gameController.getGame();
            if (g != null && (g.getActiveMiniGame() instanceof Vasebreaker || g.getActiveMiniGame() instanceof WallnutBowling)) {
                return new ArrayList<>(g.getConveyorBeltPlants());
            }
            if (g != null && g.getActiveMiniGame() instanceof IZombie) {
                return ((IZombie) g.getActiveMiniGame()).getAvailableZombieTypes();
            }
            return MiniGamePlayScreen.this.chosenPlants;
        }

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
        public GamePlayScreen.ToolMode getCurrentToolMode() {
            return MiniGamePlayScreen.this.currentToolMode;
        }

        @Override
        public Plant getSelectedPlantToPlant() {
            return MiniGamePlayScreen.this.selectedPlantToPlant;
        }

        @Override
        public void setSelectedPlantToPlant(Plant plant) {
            MiniGamePlayScreen.this.selectedPlantToPlant = plant;
            if (plant != null) {
                MiniGamePlayScreen.this.selectedZombieToPlace = plant.getName();
            } else {
                MiniGamePlayScreen.this.selectedZombieToPlace = null;
            }
        }

        @Override
        public void setToolMode(GamePlayScreen.ToolMode mode) {
            MiniGamePlayScreen.this.currentToolMode = mode;
            if (mode != GamePlayScreen.ToolMode.PLANTING) {
                MiniGamePlayScreen.this.selectedPlantToPlant = null;
                MiniGamePlayScreen.this.selectedZombieToPlace = null;
            }
            if (MiniGamePlayScreen.this.hud != null) {
                MiniGamePlayScreen.this.hud.updateToolModeVisuals(mode);
            }
        }
    }

    private class MiniGameInputAdapter extends GamePlayInputHandler {
        public MiniGameInputAdapter(Maini game, GameController gc, MiniGameHudAdapter adapter) {
            super(adapter, game, gc);
        }

        @Override
        public boolean touchDown(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y, int pointer, int button) {
            if (isPaused || (hud != null && hud.getGameOverOverlay() != null && hud.getGameOverOverlay().isShown())) {
                return false;
            }

            Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            Game modelGame = gameController.getGame();

            if (button == 1) {
                currentToolMode = GamePlayScreen.ToolMode.NONE;
                selectedPlantToPlant = null;
                selectedZombieToPlace = null;
                beghoulSelectedRow = -1;
                beghoulSelectedCol = -1;
                dragStartPos = null;
                if (hud != null) hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                return true;
            }

            int hoveredCol = GameGrid.getColumnAt(mouseWorld.x);
            int hoveredRow = GameGrid.getRowAt(mouseWorld.y);

            if (hoveredCol != -1 && hoveredRow != -1 && modelGame != null) {
                MiniGame mg = modelGame.getActiveMiniGame();

                if (mg instanceof Beghoul) {
                    dragStartPos = new Vector2(mouseWorld.x, mouseWorld.y);
                    if (beghoulSelectedRow == -1 && beghoulSelectedCol == -1) {
                        beghoulSelectedRow = hoveredRow;
                        beghoulSelectedCol = hoveredCol;
                        AudioManager.getInstance().playButtonClick();
                    } else {
                        executeSmoothBeghoulSwap((Beghoul) mg, beghoulSelectedRow, beghoulSelectedCol, hoveredRow, hoveredCol, modelGame);
                        beghoulSelectedRow = -1;
                        beghoulSelectedCol = -1;
                    }
                    return true;
                } else if (mg instanceof IZombie) {
                    if (currentToolMode == GamePlayScreen.ToolMode.PLANTING && selectedZombieToPlace != null) {
                        IZombie iz = (IZombie) mg;
                        if (hoveredCol > iz.getRedLineColumn()) {
                            String zTypeToPlace = selectedZombieToPlace;
                            String res = gameController.placeZombie(zTypeToPlace, hoveredCol, hoveredRow);
                            if (!res.startsWith("Error")) {
                                enqueueLog(res, false);
                                float cd = 7.5f;
                                if (zTypeToPlace.toLowerCase().contains("imp")) cd = 3.5f;
                                else if (zTypeToPlace.toLowerCase().contains("normal")) cd = 5.0f;
                                else if (zTypeToPlace.toLowerCase().contains("cone")) cd = 7.5f;
                                else if (zTypeToPlace.toLowerCase().contains("newspaper")) cd = 8.0f;
                                else if (zTypeToPlace.toLowerCase().contains("prospector")) cd = 10.0f;
                                else if (zTypeToPlace.toLowerCase().contains("gargantuar")) cd = 30.0f;
                                hud.putCooldown(zTypeToPlace, cd);

                                currentToolMode = GamePlayScreen.ToolMode.NONE;
                                selectedPlantToPlant = null;
                                selectedZombieToPlace = null;
                                hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                                return true;
                            } else {
                                enqueueLog(res, true);
                                return true;
                            }
                        } else {
                            enqueueLog("Must place zombie behind red line (Column > " + iz.getRedLineColumn() + ")!", true);
                            return true;
                        }
                    }
                } else if (mg instanceof Vasebreaker) {
                    Tile t = modelGame.getBoard().getTile(hoveredRow, hoveredCol);
                    if (t != null && t.getTemporarySeedPacket() != null) {
                        String result = gameController.pickupPacket(hoveredCol, hoveredRow);
                        enqueueLog(result, result.startsWith("Error"));
                        return true;
                    }

                    if (currentToolMode == GamePlayScreen.ToolMode.PLANTING && selectedPlantToPlant != null) {
                        if (t != null && t.getPlant() == null && !t.isCrater() && !t.isGrave()) {
                            String pName = selectedPlantToPlant.getName();
                            String plantRes = gameController.plantPlant(pName, hoveredCol, hoveredRow);
                            if (!plantRes.startsWith("Error")) {
                                modelGame.getConveyorBeltPlants().remove(pName);
                                enqueueLog(plantRes, false);
                                currentToolMode = GamePlayScreen.ToolMode.NONE;
                                selectedPlantToPlant = null;
                                hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                                lastPlantCountInHand = -1;
                                return true;
                            } else {
                                enqueueLog(plantRes, true);
                                return true;
                            }
                        }
                    }

                    Vasebreaker vb = (Vasebreaker) mg;
                    if (vb.hasVase(hoveredRow, hoveredCol) && !vb.isVaseBroken(hoveredRow, hoveredCol)) {
                        String result = gameController.smashVase(hoveredCol, hoveredRow);
                        enqueueLog(result, result.startsWith("Error"));
                        return true;
                    }
                } else if (mg instanceof WallnutBowling) {
                    if (currentToolMode == GamePlayScreen.ToolMode.PLANTING && selectedPlantToPlant != null) {
                        WallnutBowling wb = (WallnutBowling) mg;
                        if (hoveredCol <= wb.getRedLineX()) {
                            String pName = selectedPlantToPlant.getName();
                            String launchRes = gameController.plantPlant(pName, hoveredCol, hoveredRow);
                            if (!launchRes.startsWith("Error")) {
                                enqueueLog(launchRes, false);
                                currentToolMode = GamePlayScreen.ToolMode.NONE;
                                selectedPlantToPlant = null;
                                hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                                lastPlantCountInHand = -1;
                                return true;
                            } else {
                                enqueueLog(launchRes, true);
                                return true;
                            }
                        } else {
                            enqueueLog("Must place bowling walnut behind red line (Column <= " + wb.getRedLineX() + ")!", true);
                            return true;
                        }
                    }
                } else if (mg instanceof Zombotany || mg == null) {
                    Tile t = modelGame.getBoard().getTile(hoveredRow, hoveredCol);
                    if (currentToolMode == GamePlayScreen.ToolMode.PLANTING && selectedPlantToPlant != null) {
                        if (t != null && t.getPlant() == null && !t.isCrater() && !t.isGrave()) {
                            String pName = selectedPlantToPlant.getName();
                            String plantRes = gameController.plantPlant(pName, hoveredCol, hoveredRow);
                            if (!plantRes.startsWith("Error")) {
                                hud.putCooldown(pName, (float) selectedPlantToPlant.getRecharge());
                                enqueueLog(plantRes, false);
                                currentToolMode = GamePlayScreen.ToolMode.NONE;
                                selectedPlantToPlant = null;
                                hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                                return true;
                            } else {
                                enqueueLog(plantRes, true);
                                return true;
                            }
                        }
                    } else if (currentToolMode == GamePlayScreen.ToolMode.SHOVEL) {
                        if (t != null && t.getPlant() != null) {
                            Plant p = t.getPlant();
                            modelGame.removePlant(p);
                            t.setPlant(null);
                            enqueueLog("Removed plant " + p.getName() + " with shovel.", false);
                            currentToolMode = GamePlayScreen.ToolMode.NONE;
                            hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                            return true;
                        }
                    } else if (currentToolMode == GamePlayScreen.ToolMode.PLANT_FOOD) {
                        if (t != null && t.getPlant() != null) {
                            Plant p = t.getPlant();
                            if (modelGame.getPlantFoodCount() > 0) {
                                modelGame.usePlantFood();
                                new PlantAbilityHandler().applyPlantFood(p, modelGame);
                                enqueueLog("Applied Plant Food to " + p.getName() + "!", false);
                            } else {
                                enqueueLog("No Plant Food available!", true);
                            }
                            currentToolMode = GamePlayScreen.ToolMode.NONE;
                            hud.updateToolModeVisuals(GamePlayScreen.ToolMode.NONE);
                            return true;
                        }
                    }
                }
            }

            return super.touchDown(event, x, y, pointer, button);
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            Game modelGame = gameController.getGame();

            if (dragStartPos != null && modelGame != null && modelGame.getActiveMiniGame() instanceof Beghoul) {
                float dx = mouseWorld.x - dragStartPos.x;
                float dy = mouseWorld.y - dragStartPos.y;
                float threshold = 35f;

                if (Math.abs(dx) > threshold || Math.abs(dy) > threshold) {
                    int startCol = GameGrid.getColumnAt(dragStartPos.x);
                    int startRow = GameGrid.getRowAt(dragStartPos.y);

                    if (startCol != -1 && startRow != -1) {
                        int targetCol = startCol;
                        int targetRow = startRow;

                        if (Math.abs(dx) > Math.abs(dy)) {
                            targetCol += (dx > 0) ? 1 : -1;
                        } else {
                            targetRow += (dy > 0) ? -1 : 1;
                        }

                        if (targetCol >= 0 && targetCol < GameGrid.COLS && targetRow >= 0 && targetRow < GameGrid.ROWS) {
                            executeSmoothBeghoulSwap((Beghoul) modelGame.getActiveMiniGame(), startRow, startCol, targetRow, targetCol, modelGame);
                        }
                    }

                    dragStartPos = null;
                    beghoulSelectedRow = -1;
                    beghoulSelectedCol = -1;
                }
            }
            super.touchDragged(event, x, y, pointer);
        }
    }

    private void executeSmoothBeghoulSwap(Beghoul bg, int r1, int c1, int r2, int c2, Game modelGame) {
        Tile t1 = modelGame.getBoard().getTile(r1, c1);
        Tile t2 = modelGame.getBoard().getTile(r2, c2);
        Plant p1 = t1 != null ? t1.getPlant() : null;
        Plant p2 = t2 != null ? t2.getPlant() : null;

        boolean swapped = bg.swapPlants(r1, c1, r2, c2, modelGame);
        if (swapped) {
            if (p1 != null) {
                p1.setVisualOffsetX((c1 - c2) * GameGrid.TILE_WIDTH);
                p1.setVisualOffsetY((r2 - r1) * GameGrid.TILE_HEIGHT);
            }
            if (p2 != null) {
                p2.setVisualOffsetX((c2 - c1) * GameGrid.TILE_WIDTH);
                p2.setVisualOffsetY((r1 - r2) * GameGrid.TILE_HEIGHT);
            }
            enqueueLog("Match found!", false);
            AudioManager.getInstance().playButtonClick();
        } else {
            enqueueLog("Invalid match move!", true);
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
        shapeRenderer = new ShapeRenderer();

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

        lawnRenderer = new LawnRenderer(game.getTextureBank(), bgRegion, shapeRenderer, pamPlayer);
        plantRenderer = new PlantRenderer(pamPlayer);
        zombieRenderer = new ZombieRenderer(pamPlayer, game.getTextureBank());
        projectileRenderer = new ProjectileRenderer(game.getTextureBank(), pamPlayer);
        zombossRenderer = new ZombossRenderer(pamPlayer);
        vaseRenderer = new VaseRenderer();

        hudAdapter = new MiniGameHudAdapter(game, gameController, chosenPlants);
        hud = new GamePlayHud(hudAdapter, stage, game.getSkin(), pamPlayer, game, gameController);

        buildBeghoulUpgradePanel();

        stage.addListener(new MiniGameInputAdapter(game, gameController, hudAdapter));

        isInitialized = true;
        enqueueLog(minigameName + " started!", false);
    }

    private void buildBeghoulUpgradePanel() {
        Game modelGame = gameController.getGame();
        if (modelGame == null || !(modelGame.getActiveMiniGame() instanceof Beghoul)) return;

        beghoulUpgradeTable = new Table();
        beghoulUpgradeTable.left().top();
        beghoulUpgradeTable.setPosition(18, game.getViewport().getWorldHeight() - 170);

        String[][] upgrades = Beghoul.getUpgradePaths();
        int[] costs = Beghoul.getUpgradeCosts();

        for (int i = 0; i < upgrades.length; i++) {
            final String from = upgrades[i][0];
            final String to = upgrades[i][1];
            final int cost = costs[i];

            TextButton btn = new TextButton(from + " -> " + to + " (" + cost + "S)", game.getSkin(), "green");
            btn.getLabel().setFontScale(0.70f);
            btn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isPaused) return;
                    Beghoul bg = (Beghoul) modelGame.getActiveMiniGame();
                    boolean res = bg.upgradePlants(from, to, modelGame);
                    if (res) {
                        enqueueLog("Upgraded " + from + " to " + to + "!", false);
                        AudioManager.getInstance().playButtonClick();
                    } else {
                        enqueueLog("Cannot upgrade! Not enough sun (" + cost + " required) or no " + from + " on board.", true);
                    }
                }
            });
            beghoulUpgradeTable.add(btn).size(235, 42).padBottom(5).left().row();
        }
        stage.addActor(beghoulUpgradeTable);
    }

    private void renderRedLineOverlay() {
        Game modelGame = gameController.getGame();
        if (modelGame == null) return;

        float curStartX = GameGrid.getGridStartX();
        float curStartY = GameGrid.getGridStartY();
        float redLineXCoord = -1f;

        if (modelGame.getActiveMiniGame() instanceof WallnutBowling) {
            WallnutBowling wb = (WallnutBowling) modelGame.getActiveMiniGame();
            redLineXCoord = curStartX + (wb.getRedLineX() + 1) * GameGrid.TILE_WIDTH;
        } else if (modelGame.getActiveMiniGame() instanceof IZombie) {
            IZombie iz = (IZombie) modelGame.getActiveMiniGame();
            redLineXCoord = curStartX + (iz.getRedLineColumn() + 1) * GameGrid.TILE_WIDTH;
        }

        if (redLineXCoord != -1f) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            shapeRenderer.setProjectionMatrix(game.getViewport().getCamera().combined);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.1f, 0.1f, 0.88f);
            shapeRenderer.rect(redLineXCoord - 4f, curStartY, 8f, GameGrid.GRID_TOTAL_HEIGHT);
            shapeRenderer.end();

            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    private void renderBeghoulSelection() {
        if (beghoulSelectedRow != -1 && beghoulSelectedCol != -1) {
            float hx = GameGrid.getGridStartX() + (beghoulSelectedCol * GameGrid.TILE_WIDTH);
            float hy = GameGrid.getGridStartY() + ((4 - beghoulSelectedRow) * GameGrid.TILE_HEIGHT);
            shapeRenderer.setProjectionMatrix(game.getViewport().getCamera().combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.YELLOW);
            shapeRenderer.rect(hx + 2, hy + 2, GameGrid.TILE_WIDTH - 4, GameGrid.TILE_HEIGHT - 4);
            shapeRenderer.end();
        }
    }

    private void renderBrains(SpriteBatch batch) {
        Game modelGame = gameController.getGame();
        if (modelGame == null || !(modelGame.getActiveMiniGame() instanceof IZombie)) return;

        IZombie iz = (IZombie) modelGame.getActiveMiniGame();
        TextureRegion brainRegion = game.getTextureBank().region("IMAGE_UI_CALENDAR_TIMER_DECO_BIGBRAINZ");
        if (brainRegion == null) {
            brainRegion = game.getTextureBank().region("IMAGE_UI_ALMANAC_BRAIN");
        }

        for (int r = 0; r < GameGrid.ROWS; r++) {
            if (!iz.isBrainEaten(r) && brainRegion != null) {
                float px = GameGrid.getGridStartX() - (GameGrid.TILE_WIDTH * 0.78f);
                float py = GameGrid.getGridStartY() + ((4 - r) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 5f);
                batch.draw(brainRegion, px, py, 72f, 72f);
            }
        }
    }

    private void updateBrainCollisions(Game modelGame) {
        if (modelGame == null || !(modelGame.getActiveMiniGame() instanceof IZombie)) return;

        IZombie iz = (IZombie) modelGame.getActiveMiniGame();
        for (Zombie z : new ArrayList<>(modelGame.getActiveZombies())) {
            if (z.getX() <= 0.2) {
                iz.eatBrain(z.getY());
                enqueueLog("A brain has been eaten in row " + (z.getY() + 1) + "!", false);
                modelGame.removeZombie(z);
                if (iz.isVictoryConditionMet()) {
                    modelGame.setWon(true);
                    modelGame.stop();
                }
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

            List<Plant> plantsToRemove = new ArrayList<>();
            for (Plant p : modelGame.getActivePlants()) {
                boolean readyToRemove = p.updateVisualTransition(delta);
                if (readyToRemove) {
                    plantsToRemove.add(p);
                }
            }
            for (Plant p : plantsToRemove) {
                modelGame.removePlant(p);
            }

            updateBrainCollisions(modelGame);

            if (modelGame.getActiveMiniGame() instanceof Vasebreaker || modelGame.getActiveMiniGame() instanceof WallnutBowling) {
                int curCount = modelGame.getConveyorBeltPlants().size();
                if (curCount != lastPlantCountInHand) {
                    lastPlantCountInHand = curCount;
                    hud.rebuildSeedBank();
                }
            }

            Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            hud.updateCursorPositions(mouseWorld.x, mouseWorld.y);

            int hCol = GameGrid.getColumnAt(mouseWorld.x);
            int hRow = GameGrid.getRowAt(mouseWorld.y);
            if (hCol != -1 && hRow != -1 && currentToolMode == GamePlayScreen.ToolMode.PLANTING) {
                float hx = GameGrid.getGridStartX() + (hCol * GameGrid.TILE_WIDTH);
                float hy = GameGrid.getGridStartY() + ((4 - hRow) * GameGrid.TILE_HEIGHT);
                Tile t = modelGame.getBoard().getTile(hRow, hCol);
                boolean canPlant = false;
                if (modelGame.getActiveMiniGame() instanceof WallnutBowling) {
                    WallnutBowling wb = (WallnutBowling) modelGame.getActiveMiniGame();
                    canPlant = hCol <= wb.getRedLineX();
                } else if (modelGame.getActiveMiniGame() instanceof IZombie) {
                    IZombie iz = (IZombie) modelGame.getActiveMiniGame();
                    canPlant = hCol > iz.getRedLineColumn();
                } else {
                    canPlant = t != null && t.getPlant() == null && !t.isCrater() && !t.isGrave();
                }
                hud.setHighlight(true, hx, hy, canPlant);
            } else {
                hud.setHighlight(false, 0, 0, false);
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

        if (modelGame != null && plantRenderer != null && zombieRenderer != null) {
            for (int r = 0; r < GameGrid.ROWS; r++) {
                plantRenderer.renderRow(batch, modelGame, r, stateTime, delta);
                zombieRenderer.renderRow(batch, modelGame, r, stateTime, delta);
            }
            if (zombossRenderer != null) {
                zombossRenderer.render(batch, modelGame, stateTime, delta);
            }
            zombieRenderer.renderDyingZombies(batch, delta);
            if (vaseRenderer != null) {
                vaseRenderer.render(batch, pamPlayer, game.getTextureBank(), modelGame, delta);
            }
        }

        if (projectileRenderer != null) {
            projectileRenderer.render(batch, modelGame, stateTime);
        }

        renderBrains(batch);
        batch.end();

        renderRedLineOverlay();
        renderBeghoulSelection();

        if (hud != null && isInitialized) {
            hud.updateGameStateUI();
        }

        stage.act(isPaused ? 0 : Math.min(delta, 1 / 30f));
        stage.draw();
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
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (dimNightTexture != null) dimNightTexture.dispose();
        if (hud != null) hud.dispose();
        if (stage != null) stage.dispose();
    }
}
