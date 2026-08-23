package view.game.mainGame;

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
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import controller.menu.PreGameController;
import main.Maini;
import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Spawner;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;
import model.enums.SpecialLevelType;
import model.enums.TileType;
import model.handler.ZombossAbilityHandler;
import model.minigame.Vasebreaker;
import model.season.AncientEgypt;
import model.season.FrostbiteCaves;
import model.season.Season;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import util.FileManager;
import view.audio.AudioManager;
import view.game.renderers.*;
import view.menu.playMenu.PlayScreen;
import view.ui.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class GamePlayScreen implements Screen {
    public static float ICE_OFFSET_X = 0.0f;
    public static float ICE_OFFSET_Y = 0.0f;

    public static final float GRID_START_X = GameGrid.GRID_START_X;
    public static final float GRID_START_Y = GameGrid.GRID_START_Y;
    public static final float TILE_WIDTH = GameGrid.TILE_WIDTH;
    public static final float TILE_HEIGHT = GameGrid.TILE_HEIGHT;
    public static final float GRID_TOTAL_WIDTH = GameGrid.GRID_TOTAL_WIDTH;
    public static final float GRID_TOTAL_HEIGHT = GameGrid.GRID_TOTAL_HEIGHT;
    public static final int COLS = GameGrid.COLS;
    public static final int ROWS = GameGrid.ROWS;

    public enum ToolMode { NONE, PLANTING, SHOVEL, PLANT_FOOD }

    private final Maini game;
    private final GameController gameController;
    private final SpriteBatch batch;
    private Stage stage;

    private final List<String> initialChosenPlants;
    private final List<String> selectedPlants = new ArrayList<>();

    private ShapeRenderer shapeRenderer;
    private PamPlayer pamPlayer;
    private Settings settings;

    private LawnRenderer lawnRenderer;
    private PlantRenderer plantRenderer;
    private ZombieRenderer zombieRenderer;
    private ProjectileRenderer projectileRenderer;
    private ZombossRenderer zombossRenderer;
    private VaseRenderer vaseRenderer;
    private final ZombossAbilityHandler zombossAbilityHandler = new ZombossAbilityHandler();

    private GamePlayHud hud;
    private Texture dimNightTexture;

    private final List<WindEffect> activeWindEffects = new ArrayList<>();
    private final Map<Integer, Float> chilledRowAuras = new HashMap<>();

    private boolean isPaused = false;
    private float stateTime = 0f;
    private float tickAccumulator = 0f;
    private int lastRenderedWave = 1;
    private int previousZombieCount = 0;

    private ToolMode currentToolMode = ToolMode.NONE;
    private Plant selectedPlantToPlant = null;

    private int hoveredCol = -1;
    private int hoveredRow = -1;

    private static class ToastMessage {
        String text;
        boolean isError;
        ToastMessage(String text, boolean isError) {
            this.text = text;
            this.isError = isError;
        }
    }

    private final Queue<ToastMessage> toastQueue = new LinkedList<>();
    private float toastTimer = 0f;
    private static final float TOAST_DELAY = 0.85f;

    public GamePlayScreen(Maini game, GameController gameController) {
        this(game, gameController, null);
    }

    public GamePlayScreen(Maini game, GameController gameController, List<String> chosenPlants) {
        this.game = game;
        this.gameController = gameController;
        this.batch = game.getBatch();
        this.initialChosenPlants = chosenPlants != null ? new ArrayList<>(chosenPlants) : null;
        if (gameController != null && gameController.getGame() != null) {
            GameGrid.activeSeasonContext = gameController.getGame().getCurrentSeason();
        }
    }

    public static float getGridStartX() {
        return GameGrid.getGridStartX();
    }

    public static float getGridStartY() {
        return GameGrid.getGridStartY();
    }

    public static Vector2 getTileCenterPosition(int row, int col) {
        return GameGrid.getTileCenterPosition(row, col);
    }

    public void triggerScreenWind(float duration) {
        float cx = GameGrid.getGridStartX() + (GameGrid.GRID_TOTAL_WIDTH / 2f);
        float cy = GameGrid.getGridStartY() + (GameGrid.GRID_TOTAL_HEIGHT / 2f);
        activeWindEffects.add(new WindEffect(cx, cy, duration));
    }

    public void triggerRowWind(int row, float duration) {
        float cx = GameGrid.getGridStartX() + (GameGrid.GRID_TOTAL_WIDTH / 2f);
        float ry = GameGrid.getGridStartY() + ((4 - row) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
        activeWindEffects.add(new WindEffect(cx, ry, duration));
        chilledRowAuras.put(row, duration);
    }

    public void triggerTileWind(int row, int col, float duration) {
        Vector2 pos = GameGrid.getTileCenterPosition(row, col);
        activeWindEffects.add(new WindEffect(pos.x, pos.y, duration));
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        shapeRenderer = new ShapeRenderer();
        if (gameController != null && gameController.getGame() != null) {
            AudioManager.getInstance().playSeasonMusic(gameController.getGame().getCurrentSeason());
        }
        settings = FileManager.loadSettings();
        if (settings == null) {
            settings = new Settings();
        }

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        TextureRegion bgRegion = loadSeasonBackground();
        dimNightTexture = createSolidTexture(new Color(0.05f, 0.05f, 0.25f, 0.35f));

        lawnRenderer = new LawnRenderer(game.getTextureBank(), bgRegion, shapeRenderer, pamPlayer);
        plantRenderer = new PlantRenderer(pamPlayer);
        zombieRenderer = new ZombieRenderer(pamPlayer, game.getTextureBank());
        projectileRenderer = new ProjectileRenderer(game.getTextureBank(), pamPlayer);
        zombossRenderer = new ZombossRenderer(pamPlayer);
        vaseRenderer = new VaseRenderer();

        loadSelectedPlants();

        hud = new GamePlayHud(this, stage, game.getSkin(), pamPlayer, game, gameController);
        stage.addListener(new GamePlayInputHandler(this, game, gameController));

        if (gameController != null && gameController.getGame() != null && gameController.getGame().getActiveMiniGame() instanceof Vasebreaker) {
            enqueueLog("Vasebreaker Started! Smash vases to find plants or zombies.", false);
        } else {
            enqueueLog("Wave 1 started!", false);
        }
    }

    public void setToolMode(ToolMode mode) {
        this.currentToolMode = mode;
        if (mode != ToolMode.PLANTING) {
            selectedPlantToPlant = null;
        }
        hud.updateToolModeVisuals(mode);
    }

    private TextureRegion loadSeasonBackground() {
        Game modelGame = gameController.getGame();
        Season season = modelGame != null ? modelGame.getCurrentSeason() : null;
        String seasonName = season != null ? season.getName() : (PreGameController.activeChapterName != null ? PreGameController.activeChapterName : "");

        TextureRegion bg = null;
        String sNameLower = seasonName.toLowerCase();
        if (sNameLower.contains("egypt")) {
            bg = game.getTextureBank().region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
        } else if (sNameLower.contains("dark")) {
            bg = game.getTextureBank().region("IMAGE_BACKGROUNDS_DARK_TEXTURE");
        } else if (sNameLower.contains("caves") || sNameLower.contains("ice") || sNameLower.contains("frost")) {
            bg = game.getTextureBank().region("IMAGE_BACKGROUNDS_ICEAGE_TEXTURE");
        } else if (sNameLower.contains("beach")) {
            bg = game.getTextureBank().region("IMAGE_BACKGROUNDS_BEACH_TEXTURE");
        }

        if (bg == null) {
            bg = game.getTextureBank().region("IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_COWBOY");
        }
        if (bg == null) {
            bg = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        }
        return bg;
    }

    private void loadSelectedPlants() {
        selectedPlants.clear();
        if (initialChosenPlants != null && !initialChosenPlants.isEmpty()) {
            selectedPlants.addAll(initialChosenPlants);
            return;
        }

        try {
            PreGameController preGame = new PreGameController();
            if (preGame.getSelectedPlants() != null && !preGame.getSelectedPlants().isEmpty()) {
                selectedPlants.addAll(preGame.getSelectedPlants());
                return;
            }
        } catch (Exception ignored) {}

        User user = UserSession.getCurrentUser();
        if (user != null && user.getUnlockedPlants() != null) {
            for (int i = 0; i < Math.min(8, user.getUnlockedPlants().size()); i++) {
                selectedPlants.add(user.getUnlockedPlants().get(i));
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

    public void togglePause() {
        if (hud.getGameOverOverlay() != null && hud.getGameOverOverlay().isShown()) return;
        isPaused = !isPaused;
        hud.showPauseOverlay(isPaused);
        if (isPaused) {
            setToolMode(ToolMode.NONE);
        }
    }

    public void restartLevel() {
        Game current = gameController.getGame();
        int lvl = current != null ? current.getLevel().getNumber() : 1;
        int diffVal = settings.getDifficulty();
        Game newModelGame = new Game(5, 9, lvl, diffVal);
        if (current != null && current.getCurrentSeason() != null) {
            newModelGame.setCurrentSeason(current.getCurrentSeason());
        } else {
            newModelGame.setCurrentSeason(new AncientEgypt());
        }
        if (current != null && current.getLevel() != null) {
            newModelGame.getLevel().setSpecialLevelType(current.getLevel().getSpecialLevelType());
        }
        if (current != null && current.getActiveMiniGame() != null) {
            newModelGame.setActiveMiniGame(new Vasebreaker());
        }
        newModelGame.start();
        newModelGame.setupSpecialLevelFeatures();
        newModelGame.setSunCount(newModelGame.getLevel().getInitialSunAmount());

        gameController.setGame(newModelGame);
        game.setScreen(new GamePlayScreen(game, gameController, new ArrayList<>(selectedPlants)));
        dispose();
    }

    public void saveAndExit() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            FileManager.updateUser(user);
        }
        game.setScreen(new PlayScreen(game, game.getMenuController(), game.getSkin()));
        dispose();
    }

    private boolean canPlantOnTile(int row, int col) {
        Game modelGame = gameController.getGame();
        if (modelGame == null || row < 0 || row >= GameGrid.ROWS || col < 0 || col >= GameGrid.COLS) return false;
        Tile t = modelGame.getBoard().getTile(row, col);
        if (t == null) return false;
        if (t.isCrater() || t.isOnFire()) return false;

        String selectedName = selectedPlantToPlant != null ? selectedPlantToPlant.getName().replace(" ", "").replace("-", "").toLowerCase() : "";
        boolean isGraveBuster = selectedName.equalsIgnoreCase("gravebuster");
        boolean isPumpkin = selectedName.equalsIgnoreCase("pumpkin");
        boolean isPeaPod = selectedName.equalsIgnoreCase("peapod");

        if (isGraveBuster) {
            return t.getType() == TileType.GRAVE && t.getPlant() == null;
        }

        if (t.getType() == TileType.GRAVE) return false;

        if (isPeaPod && t.getPlant() != null && t.getPlant().getName().replace(" ", "").replace("-", "").equalsIgnoreCase("peapod")) {
            return t.getPlant().getPeaPodHeads() < 5;
        }

        if (isPumpkin) {
            return t.getPumpkinPlant() == null;
        } else {
            return t.getPlant() == null;
        }
    }

    private void updateHoverAndHighlight() {
        if (isPaused || (hud.getGameOverOverlay() != null && hud.getGameOverOverlay().isShown())) {
            hud.setHighlight(false, 0, 0, false);
            return;
        }

        Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hoveredCol = GameGrid.getColumnAt(mouseWorld.x);
        hoveredRow = GameGrid.getRowAt(mouseWorld.y);

        hud.updateCursorPositions(mouseWorld.x, mouseWorld.y);

        if (hoveredCol != -1 && hoveredRow != -1 && (currentToolMode != ToolMode.NONE)) {
            float hx = GameGrid.getGridStartX() + (hoveredCol * GameGrid.TILE_WIDTH);
            float hy = GameGrid.getGridStartY() + ((4 - hoveredRow) * GameGrid.TILE_HEIGHT);

            boolean isValid = false;
            if (currentToolMode == ToolMode.PLANTING) {
                isValid = canPlantOnTile(hoveredRow, hoveredCol);
            } else if (currentToolMode == ToolMode.SHOVEL || currentToolMode == ToolMode.PLANT_FOOD) {
                Game mg = gameController.getGame();
                Tile t = mg != null ? mg.getBoard().getTile(hoveredRow, hoveredCol) : null;
                isValid = t != null && (t.getPlant() != null || t.getPumpkinPlant() != null || t.getSupportPlant() != null);
            }
            hud.setHighlight(true, hx, hy, isValid);
        } else {
            hud.setHighlight(false, 0, 0, false);
        }
    }

    private void renderSpecialLevelOverlays() {
        Game modelGame = gameController.getGame();
        if (modelGame == null || modelGame.getLevel() == null) return;

        SpecialLevelType type = modelGame.getLevel().getSpecialLevelType();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getViewport().getCamera().combined);

        float curStartX = GameGrid.getGridStartX();
        float curStartY = GameGrid.getGridStartY();

        for (Map.Entry<Integer, Float> entry : chilledRowAuras.entrySet()) {
            int row = entry.getKey();
            float remTime = entry.getValue();
            float alpha = Math.min(0.45f, remTime * 0.25f);
            float ry = curStartY + ((4 - row) * GameGrid.TILE_HEIGHT);

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(0.2f, 0.6f, 1.0f, alpha);
            shapeRenderer.rect(curStartX, ry, GameGrid.GRID_TOTAL_WIDTH, GameGrid.TILE_HEIGHT);
            shapeRenderer.end();
        }

        if (type == SpecialLevelType.SAVE_OUR_SEEDS) {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.82f, 0.1f, 0.28f);
            for (int[] pos : modelGame.getLevel().getSeedProtectionPositions()) {
                float px = curStartX + pos[1] * GameGrid.TILE_WIDTH;
                float py = curStartY + (4 - pos[0]) * GameGrid.TILE_HEIGHT;
                shapeRenderer.rect(px, py, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
            }
            shapeRenderer.end();

            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
            shapeRenderer.setColor(Color.ORANGE);
            Gdx.gl.glLineWidth(4);
            for (int[] pos : modelGame.getLevel().getSeedProtectionPositions()) {
                float px = curStartX + pos[1] * GameGrid.TILE_WIDTH;
                float py = curStartY + (4 - pos[0]) * GameGrid.TILE_HEIGHT;
                shapeRenderer.rect(px + 2, py + 2, GameGrid.TILE_WIDTH - 4, GameGrid.TILE_HEIGHT - 4);
            }
            shapeRenderer.end();
        }

        if (type == SpecialLevelType.DEAD_LINE) {
            int lineCol = modelGame.getLevel().getDeadlineColumn();
            float lineX = curStartX + (lineCol + 1) * GameGrid.TILE_WIDTH;

            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            shapeRenderer.setColor(1f, 0.1f, 0.1f, 0.88f);
            shapeRenderer.rect(lineX - 4f, curStartY, 8f, GameGrid.GRID_TOTAL_HEIGHT);
            shapeRenderer.end();
        }

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void checkWaveAndSpawns() {
        Game modelGame = gameController.getGame();
        if (modelGame == null) return;

        Spawner spawner = modelGame.getSpawner();
        if (spawner != null) {
            int currentWave = spawner.getCurrentWave();
            if (currentWave > lastRenderedWave) {
                lastRenderedWave = currentWave;

                if (spawner.isFinalWave()) {
                    enqueueLog("THE FINAL WAVE HAS COME!", true);
                } else {
                    enqueueLog("Wave " + currentWave + " started!", false);
                }

                Season season = modelGame.getCurrentSeason();
                if (season instanceof FrostbiteCaves) {
                    List<Integer> chilledRows = ((FrostbiteCaves) season).getLastChilledRows();
                    for (int row : chilledRows) {
                        triggerRowWind(row, 2.2f);
                    }
                }
            }

            if (spawner.isFinalWave()) {
                Season season = modelGame.getCurrentSeason();
                boolean isEgypt = season != null && season.getName() != null && season.getName().toLowerCase().contains("egypt");
                int currentZombieCount = modelGame.getActiveZombies().size();

                if (isEgypt && currentZombieCount > previousZombieCount) {
                    for (int i = previousZombieCount; i < currentZombieCount; i++) {
                        if (i < modelGame.getActiveZombies().size()) {
                            Zombie newlySpawned = modelGame.getActiveZombies().get(i);
                            triggerTileWind(newlySpawned.getY(), (int) Math.round(newlySpawned.getX()), 1.8f);
                        }
                    }
                }
                previousZombieCount = currentZombieCount;
            } else {
                previousZombieCount = modelGame.getActiveZombies().size();
            }
        }
    }

    public void enqueueLog(String message, boolean isError) {
        if (message == null || message.trim().isEmpty()) return;
        System.out.println("[GAME LOG] " + message);
        toastQueue.offer(new ToastMessage(message, isError));
    }

    private void processTurnLogs() {
        List<String> logs = gameController.extractAccumulatedTurnLogs();
        Game modelGame = gameController.getGame();
        if (modelGame != null) {
            logs.addAll(modelGame.getGameLogMessages());
        }

        for (String log : logs) {
            if (log != null && !log.trim().isEmpty()) {
                boolean isErrorOrLoss = log.contains("LOSER") || log.contains("Error") || log.contains("destroyed");
                enqueueLog(log, isErrorOrLoss);
            }
        }
    }

    private void updateToastQueue(float delta) {
        toastTimer -= delta;
        if (toastTimer <= 0f && !toastQueue.isEmpty()) {
            ToastMessage nextToast = toastQueue.poll();
            if (nextToast != null) {
                Toast.show(stage, game.getSkin(), nextToast.text, nextToast.isError);
                toastTimer = TOAST_DELAY;
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.10f, 0.12f, 1f);
        ScreenShake.update(game.getViewport().getCamera(), delta);
        float speedMultiplier = settings.getGameSpeed();
        float effectiveDelta = isPaused ? 0f : delta * speedMultiplier;

        Game modelGame = gameController.getGame();
        if (modelGame != null && (modelGame.isWon() || modelGame.isLost())) {
            if (hud.getGameOverOverlay() != null && !hud.getGameOverOverlay().isShown()) {
                int earnedScore = modelGame.getScoreGame() != null ? modelGame.getScoreGame().getFinalScore() : 0;
                hud.getGameOverOverlay().showResult(modelGame.isWon(), earnedScore);
            }
        }

        if (!isPaused && (hud.getGameOverOverlay() == null || !hud.getGameOverOverlay().isShown())) {
            stateTime += effectiveDelta;
            tickAccumulator += effectiveDelta;

            float tickRate = 0.1f;
            while (tickAccumulator >= tickRate) {
                gameController.advanceTime(1);
                if (modelGame != null && modelGame.getActiveZombies() != null) {
                    for (Zombie z : new ArrayList<>(modelGame.getActiveZombies())) {
                        if (z instanceof Zomboss) {
                            zombossAbilityHandler.processZomboss((Zomboss) z, modelGame);
                        }
                    }
                }
                tickAccumulator -= tickRate;
            }

            processTurnLogs();
            updateToastQueue(delta);
            hud.updateCooldowns(delta, speedMultiplier);
            updateHoverAndHighlight();

            for (int i = activeWindEffects.size() - 1; i >= 0; i--) {
                if (!activeWindEffects.get(i).update(effectiveDelta)) {
                    activeWindEffects.remove(i);
                }
            }

            for (Integer row : new ArrayList<>(chilledRowAuras.keySet())) {
                float rem = chilledRowAuras.get(row) - effectiveDelta;
                if (rem <= 0) {
                    chilledRowAuras.remove(row);
                } else {
                    chilledRowAuras.put(row, rem);
                }
            }
        }

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        lawnRenderer.renderBackground(batch, game.getViewport().getWorldWidth(), game.getViewport().getWorldHeight());

        if (modelGame != null && modelGame.getLevel().getSpecialLevelType() == SpecialLevelType.NIGHT_OPS && dimNightTexture != null) {
            batch.draw(dimNightTexture, 0, 0, game.getViewport().getWorldWidth(), game.getViewport().getWorldHeight());
        }

        lawnRenderer.renderLawnElements(batch, gameController.getGame(), stateTime);

        if (modelGame != null) {
            for (int r = 0; r < GameGrid.ROWS; r++) {
                plantRenderer.renderRow(batch, modelGame, r, stateTime, effectiveDelta);
                zombieRenderer.renderRow(batch, modelGame, r, stateTime, effectiveDelta);
            }
            zombossRenderer.render(batch, modelGame, stateTime, effectiveDelta);
            zombieRenderer.renderDyingZombies(batch, effectiveDelta);
            vaseRenderer.render(batch, pamPlayer, game.getTextureBank(), modelGame, effectiveDelta);
        }

        projectileRenderer.render(batch, gameController.getGame(), stateTime);

        for (WindEffect wind : activeWindEffects) {
            wind.render(batch, pamPlayer, stateTime);
        }
        batch.end();

        renderSpecialLevelOverlays();
        lawnRenderer.renderBeachAndSpecialOverlays(gameController.getGame(), game.getViewport().getCamera().combined);
        lawnRenderer.renderDebugGrid(settings.isShowGrid(), hoveredCol, hoveredRow);

        if (!isPaused) {
            checkWaveAndSpawns();
            hud.updateGameStateUI();
        }

        stage.act(isPaused ? 0 : Math.min(effectiveDelta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.getViewport().update(width, height, true);
        stage.getViewport().update(width, height, true);
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

    public boolean isPaused() { return isPaused; }
    public ToolMode getCurrentToolMode() { return currentToolMode; }
    public Plant getSelectedPlantToPlant() { return selectedPlantToPlant; }
    public void setSelectedPlantToPlant(Plant plant) { this.selectedPlantToPlant = plant; }
    public List<String> getSelectedPlants() { return selectedPlants; }
    public int getHoveredCol() { return hoveredCol; }
    public int getHoveredRow() { return hoveredRow; }
    public GamePlayHud getHud() { return hud; }
}
