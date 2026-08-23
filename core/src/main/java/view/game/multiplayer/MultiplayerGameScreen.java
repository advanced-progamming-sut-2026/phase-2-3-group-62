package view.game.multiplayer;

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
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import controller.menu.MenuController;
import main.Maini;
import model.Game;
import model.board.Tile;
import model.entities.ZombieType;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.plant.loader.PlantLoader;
import model.entities.zombie.Zombie;
import model.entities.zombie.factory.ZombieFactory;
import model.minigame.IZombie;
import model.user.User;
import model.user.UserSession;
import network.Message;
import network.NetworkManager;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.game.mainGame.GameGrid;
import view.game.renderers.LawnRenderer;
import view.game.renderers.PlantRenderer;
import view.game.renderers.ProjectileRenderer;
import view.game.renderers.ZombieRenderer;
import view.ui.PamActor;
import view.ui.PlantSeedCard;
import view.ui.Toast;
import view.ui.ZombieSeedCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiplayerGameScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private final String opponentUsername;
    private final String myRole;
    private final boolean isPlantsRole;
    private final List<String> chosenCards;
    private final String myUsername;

    private Game modelGame;
    private GameController gameController;
    private IZombie izombieLogic;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion brainRegion;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion badgeRegion;

    private Texture seedBankBgTexture;
    private Texture greenHighlightTexture;
    private Texture redHighlightTexture;
    private Texture dimTexture;
    private ShapeRenderer shapeRenderer;

    private LawnRenderer lawnRenderer;
    private PlantRenderer plantRenderer;
    private ZombieRenderer zombieRenderer;
    private ProjectileRenderer projectileRenderer;

    private MultiplayerHud hud;
    private MultiplayerReactionManager reactionManager;
    private MultiplayerDialogHelper dialogHelper;

    private Image tileHighlightImage;
    private PamActor cursorGhostActor;

    private final Table seedBankTable = new Table();
    private final List<PlantSeedCard> seedCardWidgets = new ArrayList<>();
    private final List<ZombieSeedCard> zombieCardWidgets = new ArrayList<>();
    private final Map<String, Float> cooldownTimers = new HashMap<>();

    private float gameTime = 120.0f;
    private boolean isGameOver = false;
    private float stateTime = 0f;
    private float tickAccumulator = 0f;

    private String selectedPlantName = null;
    private String selectedZombieType = null;
    private int hoveredCol = -1;
    private int hoveredRow = -1;

    public MultiplayerGameScreen(Maini game, MenuController controller, Skin skin, String opponentUsername, String role, List<String> chosenCards) {
        this.game = game;
        this.controller = controller;
        this.skin = skin;
        this.batch = game.getBatch();
        this.opponentUsername = opponentUsername;
        this.myRole = role != null ? role.toUpperCase() : "PLANTS";
        this.isPlantsRole = "PLANTS".equalsIgnoreCase(this.myRole);
        this.chosenCards = chosenCards != null ? chosenCards : new ArrayList<>();

        User currentUser = UserSession.getCurrentUser();
        this.myUsername = currentUser != null ? currentUser.getUsername() : "Player";

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
        AudioManager.getInstance().stopMusic();

        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        shapeRenderer = new ShapeRenderer();

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        if (isPlantsRole) {
            bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_BACKGROUND_LOD_BIRTHDAY_TEXTURE");
            if (bgRegion == null) bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_LAWN_DAY");
        } else {
            bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_DARK_TEXTURE");
            if (bgRegion == null) bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_EGYPT_TEXTURE");
        }

        brainRegion = game.getTextureBank().region("IMAGE_UI_CALENDAR_TIMER_DECO_BIGBRAINZ");
        plantCardFaceRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_FACE");
        badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");

        seedBankBgTexture = createSolidTexture(new Color(0.12f, 0.08f, 0.05f, 0.88f));
        greenHighlightTexture = createSolidTexture(new Color(0.15f, 0.9f, 0.2f, 0.38f));
        redHighlightTexture = createSolidTexture(new Color(0.95f, 0.15f, 0.15f, 0.42f));
        dimTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.72f));

        lawnRenderer = new LawnRenderer(game.getTextureBank(), bgRegion, shapeRenderer, pamPlayer);
        plantRenderer = new PlantRenderer(pamPlayer);
        zombieRenderer = new ZombieRenderer(pamPlayer, game.getTextureBank());
        projectileRenderer = new ProjectileRenderer(game.getTextureBank(), pamPlayer);

        dialogHelper = new MultiplayerDialogHelper(game, stage, skin, dimTexture);

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

        hud = new MultiplayerHud(game, skin, isPlantsRole, myUsername, opponentUsername, this::showLeaveConfirmationDialog);
        root.add(hud.getRoot()).fillX().pad(14, 16, 0, 16).row();

        Table centerArea = new Table();
        centerArea.top().left();

        Table sideSeedBankWrapper = new Table();
        if (seedBankBgTexture != null) sideSeedBankWrapper.setBackground(new TextureRegionDrawable(seedBankBgTexture));
        rebuildSeedBank();
        sideSeedBankWrapper.add(seedBankTable).pad(6);
        centerArea.add(sideSeedBankWrapper).left().top().padLeft(12).padTop(8);

        root.add(centerArea).expand().fill().row();

        reactionManager = new MultiplayerReactionManager(stage, skin, pamPlayer, myUsername, opponentUsername, seedBankBgTexture, greenHighlightTexture);

        stage.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                if (isGameOver) return false;

                if (button == 1) {
                    clearSelection();
                    return true;
                }

                Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

                if (isPlantsRole) {
                    for (Plant p : new ArrayList<>(modelGame.getActivePlants())) {
                        if (p.isHasSunToCollect()) {
                            Vector2 pCenter = GameGrid.getTileCenterPosition(p.getY(), p.getX());
                            if (mouseWorld.dst(pCenter) <= 65f) {
                                int sunProduced = (int) p.getSunProduce();
                                if (sunProduced <= 0) sunProduced = 25;
                                modelGame.addSun(sunProduced);
                                p.setHasSunToCollect(false);
                                hud.updateSun(modelGame.getSunCount());
                                return true;
                            }
                        }
                    }
                }

                if (hoveredCol != -1 && hoveredRow != -1) {
                    onGridCellClicked(hoveredRow, hoveredCol);
                    return true;
                }
                return false;
            }
        });
    }

    private void showLeaveConfirmationDialog() {
        dialogHelper.showLeaveDialog(() -> {
            sendGameOver(false);
            showEndGameDialog("You left the match! " + opponentUsername + " won!", false);
        });
    }

    private void showEndGameDialog(String message, boolean won) {
        if (isGameOver) return;
        isGameOver = true;
        clearSelection();
        dialogHelper.showEndGameDialog(message, won, controller);
    }

    private void rebuildSeedBank() {
        seedBankTable.clear();
        seedCardWidgets.clear();
        zombieCardWidgets.clear();
        seedBankTable.top().left();

        User user = UserSession.getCurrentUser();
        List<Plant> allPlants = PlantLoader.loadPlants();

        int cardIndex = 0;
        if (isPlantsRole) {
            for (String pName : chosenCards) {
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
                seedCardWidgets.add(card);
                MultiplayerHud.attachHoverEffect(card, 1.06f);

                final Plant finalPlant = plant;
                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (isGameOver) return;
                        AudioManager.getInstance().playButtonClick();

                        if (cooldownTimers.getOrDefault(finalPlant.getName(), 0f) > 0f) {
                            Toast.show(stage, skin, "Plant is on cooldown!", true);
                            return;
                        }

                        if (modelGame.getSunCount() < finalPlant.getCost()) {
                            Toast.show(stage, skin, "Not enough sun!", true);
                            return;
                        }

                        if (selectedPlantName != null && selectedPlantName.equalsIgnoreCase(finalPlant.getName())) {
                            clearSelection();
                            return;
                        }

                        selectedPlantName = finalPlant.getName();
                        selectedZombieType = null;

                        for (PlantSeedCard c : seedCardWidgets) c.setSelected(c == card);

                        if (cursorGhostActor != null) cursorGhostActor.remove();
                        cursorGhostActor = new PamActor(pamPlayer, finalPlant.getPamPath(), "idle", 0.32f);
                        cursorGhostActor.getColor().a = 0.55f;
                        cursorGhostActor.setTouchable(Touchable.disabled);
                        cursorGhostActor.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                        stage.addActor(cursorGhostActor);
                    }
                });

                seedBankTable.add(card).size(110, 138).pad(2);
                cardIndex++;
                if (cardIndex % 2 == 0) seedBankTable.row();
            }
        } else {
            for (String zName : chosenCards) {
                int cost = getZombieCost(zName);
                ZombieSeedCard card = new ZombieSeedCard(game, zName, cost, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                zombieCardWidgets.add(card);
                MultiplayerHud.attachHoverEffect(card, 1.06f);

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (isGameOver) return;
                        AudioManager.getInstance().playButtonClick();

                        if (cooldownTimers.getOrDefault(zName, 0f) > 0f) {
                            Toast.show(stage, skin, "Zombie is on cooldown!", true);
                            return;
                        }

                        if (modelGame.getSunCount() < cost) {
                            Toast.show(stage, skin, "Not enough sun!", true);
                            return;
                        }

                        if (selectedZombieType != null && selectedZombieType.equalsIgnoreCase(zName)) {
                            clearSelection();
                            return;
                        }

                        selectedZombieType = zName;
                        selectedPlantName = null;

                        for (ZombieSeedCard c : zombieCardWidgets) c.setSelected(c == card);

                        if (cursorGhostActor != null) cursorGhostActor.remove();
                        ZombieType zType = ZombieType.fromId(zName);
                        cursorGhostActor = new PamActor(pamPlayer, zType.getPamPath(), "idle", 0.26f, zType.getOffsetX(), zType.getOffsetY());
                        cursorGhostActor.getColor().a = 0.55f;
                        cursorGhostActor.setTouchable(Touchable.disabled);
                        cursorGhostActor.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                        stage.addActor(cursorGhostActor);
                    }
                });

                seedBankTable.add(card).size(110, 138).pad(2);
                cardIndex++;
                if (cardIndex % 2 == 0) seedBankTable.row();
            }
        }
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

    private void clearSelection() {
        selectedPlantName = null;
        selectedZombieType = null;
        for (PlantSeedCard c : seedCardWidgets) c.setSelected(false);
        for (ZombieSeedCard c : zombieCardWidgets) c.setSelected(false);
        if (cursorGhostActor != null) {
            cursorGhostActor.remove();
            cursorGhostActor = null;
        }
    }

    private void onGridCellClicked(int row, int col) {
        if (isGameOver) return;

        if (isPlantsRole && selectedPlantName != null) {
            if (col > 4) {
                Toast.show(stage, skin, "Plants must be planted on columns 0 to 4!", true);
                return;
            }
            Plant template = PlantFactory.createPlant(selectedPlantName);
            int cost = template != null ? template.getCost() : 50;

            if (modelGame.getSunCount() >= cost) {
                Tile tile = modelGame.getBoard().getTile(row, col);
                if (tile != null && tile.getPlant() == null) {
                    modelGame.spendSun(cost);
                    hud.updateSun(modelGame.getSunCount());

                    Plant p = PlantFactory.createPlant(selectedPlantName);
                    p.setX(col);
                    p.setY(row);
                    p.initHealth();
                    modelGame.addPlant(p);
                    tile.setPlant(p);

                    AudioManager.getInstance().playPlantSound();
                    cooldownTimers.put(selectedPlantName, (float) p.getRecharge());

                    Message action = new Message(Message.Type.GAME_ACTION_PLANT)
                        .put("opponent_username", opponentUsername)
                        .put("plant_name", selectedPlantName)
                        .put("row", String.valueOf(row))
                        .put("col", String.valueOf(col));
                    NetworkManager.getInstance().sendAsync(action);
                    clearSelection();
                }
            }
        } else if (!isPlantsRole && selectedZombieType != null) {
            if (col < 5) {
                Toast.show(stage, skin, "Zombies must be spawned on columns 5 to 8!", true);
                return;
            }
            int cost = getZombieCost(selectedZombieType);

            if (modelGame.getSunCount() >= cost) {
                modelGame.spendSun(cost);
                hud.updateSun(modelGame.getSunCount());

                Zombie z = ZombieFactory.createZombieAtColumn(selectedZombieType, row, col, 3);
                if (z != null) {
                    if (selectedZombieType.toLowerCase().contains("bucket")) {
                        z.setArmorHealth(1100);
                        z.setArmorType("BUCKET");
                    }

                    modelGame.addZombie(z);
                    Tile tile = modelGame.getBoard().getTile(row, col);
                    if (tile != null) tile.setZombie(z);

                    AudioManager.getInstance().playPlantSound();

                    float cd = 7.5f;
                    if (selectedZombieType.toLowerCase().contains("imp")) cd = 3.5f;
                    else if (selectedZombieType.toLowerCase().contains("normal")) cd = 5.0f;
                    else if (selectedZombieType.toLowerCase().contains("gargantuar")) cd = 30.0f;
                    cooldownTimers.put(selectedZombieType, cd);

                    Message action = new Message(Message.Type.GAME_ACTION_SPAWN_ZOMBIE)
                        .put("opponent_username", opponentUsername)
                        .put("zombie_name", selectedZombieType)
                        .put("row", String.valueOf(row))
                        .put("col", String.valueOf(col));
                    NetworkManager.getInstance().sendAsync(action);
                    clearSelection();
                }
            }
        }
    }

    private void handleIncomingNetworkMessages() {
        Message msg;
        while ((msg = NetworkManager.getInstance().pollPushMessage()) != null) {
            if (msg.getType() == Message.Type.GAME_ACTION_PLANT) {
                String pName = msg.get("plant_name");
                int r = Integer.parseInt(msg.get("row"));
                int c = Integer.parseInt(msg.get("col"));

                Tile tile = modelGame.getBoard().getTile(r, c);
                if (tile != null && tile.getPlant() == null) {
                    Plant p = PlantFactory.createPlant(pName);
                    if (p != null) {
                        p.setX(c);
                        p.setY(r);
                        p.initHealth();
                        modelGame.addPlant(p);
                        tile.setPlant(p);
                        AudioManager.getInstance().playPlantSound();
                    }
                }
            } else if (msg.getType() == Message.Type.GAME_ACTION_SPAWN_ZOMBIE) {
                String zName = msg.get("zombie_name");
                int r = Integer.parseInt(msg.get("row"));
                int c = Integer.parseInt(msg.get("col"));

                Zombie z = ZombieFactory.createZombieAtColumn(zName, r, c, 3);
                if (z != null) {
                    if (zName.toLowerCase().contains("bucket")) {
                        z.setArmorHealth(1100);
                        z.setArmorType("BUCKET");
                    }
                    modelGame.addZombie(z);
                    Tile tile = modelGame.getBoard().getTile(r, c);
                    if (tile != null) tile.setZombie(z);
                    AudioManager.getInstance().playPlantSound();
                }
            } else if (msg.getType() == Message.Type.GAME_REACTION) {
                String cat = msg.get("category");
                String cnt = msg.get("content");
                String from = msg.get("from_username");
                if (reactionManager != null) {
                    reactionManager.triggerLocalReaction(cat, cnt, from != null ? from : opponentUsername);
                }
            } else if (msg.getType() == Message.Type.GAME_STATE_UPDATE) {
                if (msg.get("brains") != null) {
                    int brains = Integer.parseInt(msg.get("brains"));
                }
            } else if (msg.getType() == Message.Type.GAME_OVER) {
                String result = msg.get("result");
                boolean win = "WIN".equalsIgnoreCase(result);
                showEndGameDialog(win ? "VICTORY! Opponent left the match." : "DEFEAT! Opponent won.", win);
            }
        }
    }

    private void updateCooldowns(float delta) {
        for (String name : new ArrayList<>(cooldownTimers.keySet())) {
            float cd = cooldownTimers.get(name) - delta;
            if (cd <= 0f) cooldownTimers.remove(name);
            else cooldownTimers.put(name, cd);
        }

        int curSun = modelGame.getSunCount();
        if (isPlantsRole) {
            for (PlantSeedCard card : seedCardWidgets) {
                float cd = cooldownTimers.getOrDefault(card.getPlant().getName(), 0f);
                card.updateCooldownState(cd, curSun);
            }
        } else {
            for (ZombieSeedCard card : zombieCardWidgets) {
                float cd = cooldownTimers.getOrDefault(card.getZombieName(), 0f);
                card.updateCooldownState(cd, curSun);
            }
        }
    }

    private void sendGameOver(boolean won) {
        Message msg = new Message(Message.Type.GAME_OVER)
            .put("opponent_username", opponentUsername)
            .put("result", won ? "LOSE" : "WIN");
        NetworkManager.getInstance().sendAsync(msg);
    }

    private void updateHoverAndHighlight() {
        Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        hoveredCol = GameGrid.getColumnAt(mouseWorld.x);
        hoveredRow = GameGrid.getRowAt(mouseWorld.y);

        if (cursorGhostActor != null) {
            cursorGhostActor.setPosition(mouseWorld.x - (GameGrid.TILE_WIDTH / 2f), mouseWorld.y - (GameGrid.TILE_HEIGHT / 2f));
            cursorGhostActor.toFront();
        }

        boolean hasSelection = (isPlantsRole && selectedPlantName != null) || (!isPlantsRole && selectedZombieType != null);

        if (hoveredCol != -1 && hoveredRow != -1 && hasSelection) {
            float hx = GameGrid.getGridStartX() + (hoveredCol * GameGrid.TILE_WIDTH);
            float hy = GameGrid.getGridStartY() + ((4 - hoveredRow) * GameGrid.TILE_HEIGHT);

            boolean isValid = isPlantsRole ? (hoveredCol <= 4 && modelGame.getBoard().getTile(hoveredRow, hoveredCol).getPlant() == null)
                : (hoveredCol >= 5);

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

    private void updateMultiplayerSunProduction() {
        if (isPlantsRole) {
            for (Plant plant : modelGame.getActivePlants()) {
                if ("SUN_PRODUCER".equalsIgnoreCase(plant.getCategory()) && !plant.isFrozen() && !plant.isTransformedToSheep()) {
                    if (plant.shouldShoot() && !plant.isHasSunToCollect()) {
                        plant.setHasSunToCollect(true);
                        plant.triggerAttack(0.8f);
                    }
                }
            }
        } else {
            for (Zombie z : modelGame.getActiveZombies()) {
                if (z.getArmorType() != null && z.getArmorType().equalsIgnoreCase("BUCKET") && z.isAlive()) {
                    z.incrementIzombieSunTicks();
                    int interval = Math.max(240 - (z.getIzombieSunProductionTicks() / 4), 100);
                    if (modelGame.getTickCount() % interval == 0) {
                        modelGame.addSun(25);
                        hud.updateSun(modelGame.getSunCount());
                    }
                }
            }
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.08f, 0.10f, 0.12f, 1f);

        handleIncomingNetworkMessages();

        if (modelGame.getDroppedItems() != null && !modelGame.getDroppedItems().isEmpty()) {
            modelGame.getDroppedItems().clear();
        }

        if (!isGameOver) {
            gameTime -= delta;
            stateTime += delta;
            tickAccumulator += delta;

            if (gameTime <= 0) {
                gameTime = 0;
                int eaten = izombieLogic.getBrainsEaten();
                boolean plantsWon = eaten < 5;
                boolean isWinner = (isPlantsRole && plantsWon) || (!isPlantsRole && !plantsWon);
                showEndGameDialog(isWinner ? "TIME'S UP! Plants defended the lawn!" : "TIME'S UP! Zombies failed to eat all brains.", isWinner);
            }

            hud.updateTimer(gameTime);

            while (tickAccumulator >= 0.1f) {
                gameController.advanceTime(1);
                if (modelGame.getDroppedItems() != null) {
                    modelGame.getDroppedItems().clear();
                }
                updateMultiplayerSunProduction();
                tickAccumulator -= 0.1f;
            }

            updateBrainCollisions();
            updateCooldowns(delta);

            hud.updateSun(modelGame.getSunCount());

            int eaten = izombieLogic.getBrainsEaten();
            if (eaten >= 5) {
                boolean isWinner = !isPlantsRole;
                showEndGameDialog(isWinner ? "VICTORY! All brains eaten!" : "DEFEAT! The zombies ate your brains!", isWinner);
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
            plantRenderer.renderRow(batch, modelGame, r, stateTime, delta);
            zombieRenderer.renderRow(batch, modelGame, r, stateTime, delta);
        }
        zombieRenderer.renderDyingZombies(batch, delta);
        projectileRenderer.render(batch, modelGame, stateTime);

        renderBrains(batch);
        batch.end();

        renderRedLineOverlay();

        stage.act(Math.min(delta, 1 / 30f));
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
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (seedBankBgTexture != null) seedBankBgTexture.dispose();
        if (greenHighlightTexture != null) greenHighlightTexture.dispose();
        if (redHighlightTexture != null) redHighlightTexture.dispose();
        if (dimTexture != null) dimTexture.dispose();
        if (reactionManager != null) reactionManager.dispose();
        stage.dispose();
    }
}
