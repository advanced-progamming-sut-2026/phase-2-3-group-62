package view.game.couch;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import controller.menu.MenuController;
import main.Maini;
import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.plant.loader.PlantLoader;
import model.entities.zombie.Zombie;
import model.entities.zombie.factory.ZombieFactory;
import model.minigame.IZombie;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.game.hud.PauseOverlayDialog;
import view.game.mainGame.GameGrid;
import view.game.mainGame.GamePlayScreen;
import view.game.renderers.LawnRenderer;
import view.game.renderers.PlantRenderer;
import view.game.renderers.ProjectileRenderer;
import view.game.renderers.ZombieRenderer;
import view.menu.playMenu.PlayScreen;
import view.ui.CheatWidget;
import view.ui.PamActor;
import view.ui.PlantSeedCard;
import view.ui.Toast;
import view.ui.ZombieSeedCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouchPlayGameScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private final List<String> chosenPlants;
    private final List<String> chosenZombies;

    private Game modelGame;
    private GameController gameController;
    private IZombie izombieLogic;

    private int plantSunCount = 150;
    private int zombieSunCount = 150;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion brainRegion;
    private TextureRegion badgeRegion;
    private TextureRegion plantCardFaceRegion;

    private Texture seedBankBgTexture;
    private Texture greenHighlightTexture;
    private Texture redHighlightTexture;
    private Texture dimTexture;
    private ShapeRenderer shapeRenderer;

    private LawnRenderer lawnRenderer;
    private PlantRenderer plantRenderer;
    private ZombieRenderer zombieRenderer;
    private ProjectileRenderer projectileRenderer;

    private CouchPlayHud hud;
    private CouchPlayDialogHelper dialogHelper;
    private PauseOverlayDialog pauseDialog;
    private CheatWidget cheatWidget;

    private Image tileHighlightImage;
    private PamActor cursorGhostActor;

    private final Table plantSeedBankTable = new Table();
    private final Table zombieSeedBankTable = new Table();
    private final List<PlantSeedCard> plantCardWidgets = new ArrayList<>();
    private final List<ZombieSeedCard> zombieCardWidgets = new ArrayList<>();
    private final List<Table> zombieCardContainers = new ArrayList<>();
    private final Map<String, Float> plantCooldowns = new HashMap<>();
    private final Map<String, Float> zombieCooldowns = new HashMap<>();

    private float gameTime = 120.0f;
    private boolean isGameOver = false;
    private boolean isPaused = false;
    private float stateTime = 0f;
    private float tickAccumulator = 0f;

    private String selectedPlantName = null;
    private int selectedZombieIndex = 0;
    private int zombieCursorRow = 2;
    private int zombieCursorCol = 7;

    private int hoveredCol = -1;
    private int hoveredRow = -1;

    private class CouchGamePlayAdapter extends GamePlayScreen {
        public CouchGamePlayAdapter(Maini game, GameController gc) {
            super(game, gc, chosenPlants);
        }

        @Override
        public boolean isPaused() {
            return CouchPlayGameScreen.this.isPaused;
        }

        @Override
        public void togglePause() {
            CouchPlayGameScreen.this.togglePause();
        }

        @Override
        public void restartLevel() {
            CouchPlayGameScreen.this.restartLevel();
        }

        @Override
        public void saveAndExit() {
            CouchPlayGameScreen.this.saveAndExit();
        }
    }

    public CouchPlayGameScreen(Maini game, MenuController controller, Skin skin, List<String> chosenPlants, List<String> chosenZombies) {
        this.game = game;
        this.controller = controller;
        this.skin = skin;
        this.batch = game.getBatch();
        this.chosenPlants = chosenPlants != null ? chosenPlants : new ArrayList<>();
        this.chosenZombies = chosenZombies != null ? chosenZombies : new ArrayList<>();

        this.modelGame = new Game(5, 9, 1, 3);
        this.izombieLogic = new IZombie();
        this.modelGame.setActiveMiniGame(izombieLogic);
        this.modelGame.start();
        this.modelGame.setSunCount(150);

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                this.modelGame.getBoard().removeGrave(r, c);
            }
        }

        this.gameController = new GameController(this.modelGame);
        GameGrid.activeSeasonContext = null;
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        shapeRenderer = new ShapeRenderer();

        AudioManager.getInstance().playMiniGameMusic(izombieLogic);

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_BACKGROUND_LOD_BIRTHDAY_TEXTURE");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_LAWN_DAY");
        }

        brainRegion = game.getTextureBank().region("IMAGE_UI_CALENDAR_TIMER_DECO_BIGBRAINZ");
        badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        plantCardFaceRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_FACE");

        seedBankBgTexture = createSolidTexture(new Color(0.12f, 0.08f, 0.05f, 0.88f));
        greenHighlightTexture = createSolidTexture(new Color(0.15f, 0.9f, 0.2f, 0.38f));
        redHighlightTexture = createSolidTexture(new Color(0.95f, 0.15f, 0.15f, 0.42f));
        dimTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.72f));

        lawnRenderer = new LawnRenderer(game.getTextureBank(), bgRegion, shapeRenderer, pamPlayer);
        plantRenderer = new PlantRenderer(pamPlayer);
        zombieRenderer = new ZombieRenderer(pamPlayer, game.getTextureBank());
        projectileRenderer = new ProjectileRenderer(game.getTextureBank(), pamPlayer);

        dialogHelper = new CouchPlayDialogHelper(game, stage, skin, dimTexture);
        pauseDialog = new PauseOverlayDialog(skin, new CouchGamePlayAdapter(game, gameController));

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        tileHighlightImage = new Image(new TextureRegionDrawable(greenHighlightTexture));
        tileHighlightImage.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
        tileHighlightImage.setVisible(false);
        tileHighlightImage.setTouchable(Touchable.disabled);
        stage.addActor(tileHighlightImage);

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        stage.addActor(root);

        cheatWidget = new CheatWidget(skin, stage, CheatWidget.Context.INGAME, gameController, new Runnable() {
            @Override
            public void run() {
                plantSunCount = modelGame.getSunCount();
                zombieSunCount = modelGame.getSunCount();
                hud.updatePlantSun(plantSunCount);
                hud.updateZombieSun(zombieSunCount);
            }
        });

        hud = new CouchPlayHud(game, skin, cheatWidget, () -> {
            if (!isGameOver) {
                togglePause();
            }
        });
        root.add(hud.getRoot()).fillX().pad(14, 16, 0, 16).row();

        Table centerArea = new Table();
        centerArea.top().left();

        Table leftWrapper = new Table();
        if (seedBankBgTexture != null) leftWrapper.setBackground(new TextureRegionDrawable(seedBankBgTexture));
        rebuildPlantSeedBank();
        leftWrapper.add(plantSeedBankTable).pad(6);
        centerArea.add(leftWrapper).left().top().padLeft(12).padTop(8);

        centerArea.add().expandX();

        Table rightWrapper = new Table();
        if (seedBankBgTexture != null) rightWrapper.setBackground(new TextureRegionDrawable(seedBankBgTexture));
        rebuildZombieSeedBank();
        rightWrapper.add(zombieSeedBankTable).pad(6);
        centerArea.add(rightWrapper).right().top().padRight(12).padTop(8);

        root.add(centerArea).expand().fill().row();

        stage.addActor(pauseDialog.getRoot());

        stage.addListener(new InputListener() {
            @Override
            public boolean keyDown(InputEvent event, int keycode) {
                if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
                    if (!isGameOver) {
                        togglePause();
                        return true;
                    }
                }
                if (isGameOver || isPaused) return false;
                handleZombieKeyboardInput(keycode);
                return true;
            }

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (isGameOver || isPaused) return false;
                if (button == 1) {
                    clearPlantSelection();
                    return true;
                }

                Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
                for (Plant p : new ArrayList<>(modelGame.getActivePlants())) {
                    if (p.isHasSunToCollect()) {
                        Vector2 pCenter = GameGrid.getTileCenterPosition(p.getY(), p.getX());
                        if (mouseWorld.dst(pCenter) <= 65f) {
                            int sunProduced = (int) p.getSunProduce();
                            if (sunProduced <= 0) sunProduced = 25;
                            plantSunCount += sunProduced;
                            p.setHasSunToCollect(false);
                            hud.updatePlantSun(plantSunCount);
                            return true;
                        }
                    }
                }

                if (hoveredCol != -1 && hoveredRow != -1) {
                    onPlantGridClicked(hoveredRow, hoveredCol);
                    return true;
                }
                return false;
            }
        });
    }

    public void togglePause() {
        isPaused = !isPaused;
        if (pauseDialog != null) {
            pauseDialog.setVisible(isPaused);
        }
        if (isPaused) {
            clearPlantSelection();
        }
    }

    public void restartLevel() {
        Game newModelGame = new Game(5, 9, 1, 3);
        newModelGame.setActiveMiniGame(new IZombie());
        newModelGame.start();
        newModelGame.setSunCount(150);

        gameController.setGame(newModelGame);
        game.setScreen(new CouchPlayGameScreen(game, controller, skin, chosenPlants, chosenZombies));
        dispose();
    }

    public void saveAndExit() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            util.FileManager.updateUser(user);
        }
        game.setScreen(new PlayScreen(game, controller, skin));
        dispose();
    }

    private void handleZombieKeyboardInput(int keycode) {
        if (keycode == Input.Keys.NUM_1 || keycode == Input.Keys.NUMPAD_1) selectedZombieIndex = 0;
        else if (keycode == Input.Keys.NUM_2 || keycode == Input.Keys.NUMPAD_2) selectedZombieIndex = 1;
        else if (keycode == Input.Keys.NUM_3 || keycode == Input.Keys.NUMPAD_3) selectedZombieIndex = 2;
        else if (keycode == Input.Keys.NUM_4 || keycode == Input.Keys.NUMPAD_4) selectedZombieIndex = 3;
        else if (keycode == Input.Keys.NUM_5 || keycode == Input.Keys.NUMPAD_5) selectedZombieIndex = 4;
        else if (keycode == Input.Keys.N) {
            if (!chosenZombies.isEmpty()) {
                selectedZombieIndex = (selectedZombieIndex + 1) % chosenZombies.size();
            }
        }

        if (keycode == Input.Keys.W || keycode == Input.Keys.UP) zombieCursorRow = Math.max(0, zombieCursorRow - 1);
        if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) zombieCursorRow = Math.min(4, zombieCursorRow + 1);
        if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) zombieCursorCol = Math.max(5, zombieCursorCol - 1);
        if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) zombieCursorCol = Math.min(8, zombieCursorCol + 1);

        if (keycode == Input.Keys.SPACE || keycode == Input.Keys.ENTER) {
            spawnZombieAtCursor();
        }

        updateZombieCardSelectionVisuals();
    }

    private void updateZombieCardSelectionVisuals() {
        for (int i = 0; i < zombieCardWidgets.size(); i++) {
            zombieCardWidgets.get(i).setSelected(i == selectedZombieIndex);
        }
        for (int i = 0; i < zombieCardContainers.size(); i++) {
            Table container = zombieCardContainers.get(i);
            container.clearActions();
            if (i == selectedZombieIndex) {
                container.addAction(Actions.scaleTo(1.1f, 1.1f, 0.08f));
            } else {
                container.addAction(Actions.scaleTo(1.0f, 1.0f, 0.08f));
            }
        }
    }

    private void spawnZombieAtCursor() {
        if (selectedZombieIndex < 0 || selectedZombieIndex >= chosenZombies.size()) return;
        String zName = chosenZombies.get(selectedZombieIndex);

        if (!gameController.isCooldownCheatActive() && zombieCooldowns.getOrDefault(zName, 0f) > 0f) {
            Toast.show(stage, skin, "P2: Zombie is on cooldown!", true);
            return;
        }

        int cost = getZombieCost(zName);
        if (zombieSunCount < cost) {
            Toast.show(stage, skin, "P2: Not enough sun!", true);
            return;
        }

        zombieSunCount -= cost;
        hud.updateZombieSun(zombieSunCount);

        Zombie z = ZombieFactory.createZombieAtColumn(zName, zombieCursorRow, zombieCursorCol, 3);
        if (z != null) {
            if (zName.toLowerCase().contains("bucket")) {
                z.setArmorHealth(1100);
                z.setArmorType("BUCKET");
            }
            modelGame.addZombie(z);
            Tile tile = modelGame.getBoard().getTile(zombieCursorRow, zombieCursorCol);
            if (tile != null) tile.setZombie(z);

            AudioManager.getInstance().playPlantSound();

            float cd = 7.5f;
            if (zName.toLowerCase().contains("imp")) cd = 3.5f;
            else if (zName.toLowerCase().contains("normal")) cd = 5.0f;
            else if (zName.toLowerCase().contains("gargantuar")) cd = 30.0f;
            zombieCooldowns.put(zName, cd);
        }
    }

    private void rebuildPlantSeedBank() {
        plantSeedBankTable.clear();
        plantCardWidgets.clear();
        plantSeedBankTable.top().left();

        User user = UserSession.getCurrentUser();
        List<Plant> allPlants = PlantLoader.loadPlants();
        int cardIndex = 0;

        for (String pName : chosenPlants) {
            Plant plant = null;
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(pName)) {
                    plant = p;
                    break;
                }
            }
            if (plant == null) plant = PlantFactory.createPlant(pName);
            if (plant == null) continue;

            int lvl = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
            PlantSeedCard card = new PlantSeedCard(game, plant, lvl, false, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
            plantCardWidgets.add(card);

            final Plant finalPlant = plant;
            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (isGameOver || isPaused) return;
                    AudioManager.getInstance().playButtonClick();

                    if (!gameController.isCooldownCheatActive() && plantCooldowns.getOrDefault(finalPlant.getName(), 0f) > 0f) {
                        Toast.show(stage, skin, "P1: Plant is on cooldown!", true);
                        return;
                    }
                    if (plantSunCount < finalPlant.getCost()) {
                        Toast.show(stage, skin, "P1: Not enough sun!", true);
                        return;
                    }
                    if (selectedPlantName != null && selectedPlantName.equalsIgnoreCase(finalPlant.getName())) {
                        clearPlantSelection();
                        return;
                    }

                    selectedPlantName = finalPlant.getName();
                    for (PlantSeedCard c : plantCardWidgets) c.setSelected(c == card);

                    if (cursorGhostActor != null) cursorGhostActor.remove();
                    cursorGhostActor = new PamActor(pamPlayer, finalPlant.getPamPath(), "idle", 0.32f);
                    cursorGhostActor.getColor().a = 0.55f;
                    cursorGhostActor.setTouchable(Touchable.disabled);
                    cursorGhostActor.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                    stage.addActor(cursorGhostActor);
                }
            });

            plantSeedBankTable.add(card).size(105, 132).pad(2);
            cardIndex++;
            if (cardIndex % 2 == 0) plantSeedBankTable.row();
        }
    }

    private void rebuildZombieSeedBank() {
        zombieSeedBankTable.clear();
        zombieCardWidgets.clear();
        zombieCardContainers.clear();
        zombieSeedBankTable.top().right();

        for (int i = 0; i < chosenZombies.size(); i++) {
            String zName = chosenZombies.get(i);
            int cost = getZombieCost(zName);

            Table cardContainer = new Table();
            cardContainer.setTouchable(Touchable.disabled);
            cardContainer.setTransform(true);
            cardContainer.setOrigin(Align.center);

            Label keyLabel = new Label("[" + (i + 1) + "]", skin, "big_outline");
            keyLabel.setFontScale(0.85f);
            keyLabel.setColor(Color.YELLOW);
            keyLabel.setAlignment(Align.center);
            cardContainer.add(keyLabel).padBottom(2).center().row();

            ZombieSeedCard card = new ZombieSeedCard(game, zName, cost, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
            card.setTouchable(Touchable.disabled);
            zombieCardWidgets.add(card);
            cardContainer.add(card).size(95, 120).center();

            zombieCardContainers.add(cardContainer);
            zombieSeedBankTable.add(cardContainer).pad(3).row();
        }

        zombieSeedBankTable.setTouchable(Touchable.disabled);
        updateZombieCardSelectionVisuals();
    }

    private int getZombieCost(String type) {
        String clean = type.toLowerCase();
        if (clean.contains("imp")) return 25;
        if (clean.contains("normal")) return 50;
        if (clean.contains("cone")) return 75;
        if (clean.contains("bucket")) return 125;
        if (clean.contains("newspaper")) return 100;
        if (clean.contains("prospector")) return 110;
        if (clean.contains("crystalskull") || clean.contains("turquoise")) return 120;
        if (clean.contains("juggler")) return 125;
        if (clean.contains("piano")) return 130;
        if (clean.contains("barrel")) return 140;
        if (clean.contains("allstar") || clean.contains("football")) return 150;
        if (clean.contains("gargantuar")) return 300;
        return 50;
    }

    private void clearPlantSelection() {
        selectedPlantName = null;
        for (PlantSeedCard c : plantCardWidgets) c.setSelected(false);
        if (cursorGhostActor != null) {
            cursorGhostActor.remove();
            cursorGhostActor = null;
        }
    }

    private void onPlantGridClicked(int row, int col) {
        if (isGameOver || isPaused || selectedPlantName == null) return;
        if (col > 4) {
            Toast.show(stage, skin, "Plants must be placed on columns 0 to 4!", true);
            return;
        }

        Plant template = PlantFactory.createPlant(selectedPlantName);
        int cost = template != null ? template.getCost() : 50;

        if (plantSunCount >= cost) {
            Tile tile = modelGame.getBoard().getTile(row, col);
            if (tile != null && tile.getPlant() == null) {
                plantSunCount -= cost;
                hud.updatePlantSun(plantSunCount);

                Plant p = PlantFactory.createPlant(selectedPlantName);
                p.setX(col);
                p.setY(row);
                p.initHealth();
                modelGame.addPlant(p);
                tile.setPlant(p);

                AudioManager.getInstance().playPlantSound();

                plantCooldowns.put(selectedPlantName, (float) p.getRecharge());
                clearPlantSelection();
            }
        }
    }

    private void updateCooldowns(float delta) {
        for (String name : new ArrayList<>(plantCooldowns.keySet())) {
            float cd = plantCooldowns.get(name) - delta;
            if (cd <= 0f) plantCooldowns.remove(name);
            else plantCooldowns.put(name, cd);
        }
        for (String name : new ArrayList<>(zombieCooldowns.keySet())) {
            float cd = zombieCooldowns.get(name) - delta;
            if (cd <= 0f) zombieCooldowns.remove(name);
            else zombieCooldowns.put(name, cd);
        }

        for (PlantSeedCard card : plantCardWidgets) {
            float cd = gameController.isCooldownCheatActive() ? 0f : plantCooldowns.getOrDefault(card.getPlant().getName(), 0f);
            card.updateCooldownState(cd, plantSunCount);
        }
        for (ZombieSeedCard card : zombieCardWidgets) {
            float cd = gameController.isCooldownCheatActive() ? 0f : zombieCooldowns.getOrDefault(card.getZombieName(), 0f);
            card.updateCooldownState(cd, zombieSunCount);
        }
    }

    private void updateHoverAndHighlight() {
        if (isPaused) {
            tileHighlightImage.setVisible(false);
            return;
        }

        Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hoveredCol = GameGrid.getColumnAt(mouseWorld.x);
        hoveredRow = GameGrid.getRowAt(mouseWorld.y);

        if (cursorGhostActor != null) {
            cursorGhostActor.setPosition(mouseWorld.x - (GameGrid.TILE_WIDTH / 2f), mouseWorld.y - (GameGrid.TILE_HEIGHT / 2f));
            cursorGhostActor.toFront();
        }

        if (hoveredCol != -1 && hoveredRow != -1 && selectedPlantName != null) {
            float hx = GameGrid.getGridStartX() + (hoveredCol * GameGrid.TILE_WIDTH);
            float hy = GameGrid.getGridStartY() + ((4 - hoveredRow) * GameGrid.TILE_HEIGHT);

            boolean isValid = hoveredCol <= 4 && modelGame.getBoard().getTile(hoveredRow, hoveredCol).getPlant() == null;

            tileHighlightImage.setVisible(true);
            tileHighlightImage.setPosition(hx, hy);
            tileHighlightImage.setDrawable(new TextureRegionDrawable(isValid ? greenHighlightTexture : redHighlightTexture));
        } else {
            tileHighlightImage.setVisible(false);
        }
    }

    private void renderRedLineOverlay() {
        float curStartX = GameGrid.getGridStartX();
        float curStartY = GameGrid.getGridStartY();
        float redLineX = curStartX + (5 * GameGrid.TILE_WIDTH);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(game.getViewport().getCamera().combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 0.1f, 0.1f, 0.88f);
        shapeRenderer.rect(redLineX - 4f, curStartY, 8f, GameGrid.GRID_TOTAL_HEIGHT);
        shapeRenderer.end();

        float zx = curStartX + (zombieCursorCol * GameGrid.TILE_WIDTH);
        float zy = curStartY + ((4 - zombieCursorRow) * GameGrid.TILE_HEIGHT);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(1f, 1f, 1f, 0.22f);
        shapeRenderer.rect(zx, zy, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(1f, 0.12f, 0.12f, 0.95f);
        Gdx.gl.glLineWidth(3f);
        shapeRenderer.rect(zx + 2, zy + 2, GameGrid.TILE_WIDTH - 4, GameGrid.TILE_HEIGHT - 4);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    private void renderBrains(SpriteBatch batch) {
        if (brainRegion == null) return;
        for (int r = 0; r < GameGrid.ROWS; r++) {
            if (!izombieLogic.isBrainEaten(r)) {
                float bx = GameGrid.getGridStartX() - (GameGrid.TILE_WIDTH * 0.78f);
                float by = GameGrid.getGridStartY() + ((4 - r) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 5f);
                batch.draw(brainRegion, bx, by, 72f, 72f);
            }
        }
    }

    private void updateBrainCollisions() {
        for (Zombie z : new ArrayList<>(modelGame.getActiveZombies())) {
            if (z.getX() <= 0.2) {
                izombieLogic.eatBrain(z.getY());
                modelGame.removeZombie(z);
            }
        }
    }

    private void updateSunProduction() {
        for (Plant plant : modelGame.getActivePlants()) {
            if ("SUN_PRODUCER".equalsIgnoreCase(plant.getCategory()) && !plant.isFrozen() && !plant.isTransformedToSheep()) {
                if (plant.shouldShoot() && !plant.isHasSunToCollect()) {
                    plant.setHasSunToCollect(true);
                    plant.triggerAttack(0.8f);
                }
            }
        }

        for (Zombie z : modelGame.getActiveZombies()) {
            if (z.getArmorType() != null && z.getArmorType().equalsIgnoreCase("BUCKET") && z.isAlive()) {
                z.incrementIzombieSunTicks();
                int interval = Math.max(240 - (z.getIzombieSunProductionTicks() / 4), 100);
                if (modelGame.getTickCount() % interval == 0) {
                    zombieSunCount += 25;
                    hud.updateZombieSun(zombieSunCount);
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.10f, 0.12f, 1f);

        if (modelGame.getDroppedItems() != null && !modelGame.getDroppedItems().isEmpty()) {
            modelGame.getDroppedItems().clear();
        }

        if (!isGameOver && !isPaused) {
            gameTime -= delta;
            stateTime += delta;
            tickAccumulator += delta;

            if (gameTime <= 0) {
                gameTime = 0;
                int eaten = izombieLogic.getBrainsEaten();
                boolean plantsWon = eaten < 5;
                dialogHelper.showEndGameDialog(plantsWon ? "TIME'S UP! Plants defended the lawn!" : "TIME'S UP! Zombies failed to eat all brains.", plantsWon);
            }

            if (modelGame.getSunCount() != plantSunCount) {
                plantSunCount = modelGame.getSunCount();
                zombieSunCount = modelGame.getSunCount();
                hud.updatePlantSun(plantSunCount);
                hud.updateZombieSun(zombieSunCount);
            }

            hud.updateTimer(gameTime);

            while (tickAccumulator >= 0.1f) {
                gameController.advanceTime(1);
                if (modelGame.getDroppedItems() != null) {
                    modelGame.getDroppedItems().clear();
                }
                updateSunProduction();
                tickAccumulator -= 0.1f;
            }

            updateBrainCollisions();
            updateCooldowns(delta);

            int eaten = izombieLogic.getBrainsEaten();
            if (eaten >= 5) {
                dialogHelper.showEndGameDialog("VICTORY! All brains eaten!", false);
            }

            updateHoverAndHighlight();
        }

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        lawnRenderer.renderBackground(batch, game.getViewport().getWorldWidth(), game.getViewport().getWorldHeight());
        lawnRenderer.renderLawnElements(batch, modelGame, stateTime);

        for (int r = 0; r < GameGrid.ROWS; r++) {
            plantRenderer.renderRow(batch, modelGame, r, stateTime, isPaused ? 0 : delta);
            zombieRenderer.renderRow(batch, modelGame, r, stateTime, isPaused ? 0 : delta);
        }
        zombieRenderer.renderDyingZombies(batch, isPaused ? 0 : delta);
        projectileRenderer.render(batch, modelGame, stateTime);

        renderBrains(batch);
        batch.end();

        renderRedLineOverlay();

        stage.act(isPaused ? 0 : Math.min(delta, 1 / 30f));
        stage.draw();
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
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
        if (seedBankBgTexture != null) seedBankBgTexture.dispose();
        if (greenHighlightTexture != null) greenHighlightTexture.dispose();
        if (redHighlightTexture != null) redHighlightTexture.dispose();
        if (dimTexture != null) dimTexture.dispose();
        if (pauseDialog != null) pauseDialog.dispose();
        stage.dispose();
    }
}
