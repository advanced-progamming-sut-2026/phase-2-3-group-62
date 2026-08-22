package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import controller.menu.MenuController;
import main.Maini;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import model.minigame.Zombotany;
import model.user.User;
import model.Game;
import model.season.AncientEgypt;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.ui.PamActor;
import view.ui.PlantSeedCard;
import view.ui.Toast;
import view.ui.WalletBar;

import java.util.ArrayList;
import java.util.List;

public class SeedChooserForMinigameScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private final GameController gameController;
    private final String minigameName;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion emptyPacketRegion;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion badgeRegion;
    private Texture roundedBrownBgTexture;

    private final List<String> selectedPlants = new ArrayList<>();
    private final Table slotsTable = new Table();
    private final Table availablePlantsTable = new Table();
    private Table detailPane;
    private WalletBar walletBar;
    private Plant currentInspectedPlant;

    private static final int MAX_SLOTS = 8;

    public SeedChooserForMinigameScreen(Maini game, MenuController controller, Skin skin, GameController gameController, String minigameName) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
        this.gameController = gameController;
        this.minigameName = minigameName;
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_CARDS_STORE_STORE_CARD_GREEN");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        }

        emptyPacketRegion = game.getTextureBank().region("IMAGE_UI_PACKETS_EMPTY_PACKET");
        plantCardFaceRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_FACE");
        badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(1220, 460, 24, new Color(0.24f, 0.14f, 0.08f, 0.96f));

        buildUI();
    }

    private Texture createRoundedRectangleTexture(int width, int height, int radius, Color color) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);

        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius - 1, radius, radius);
        pixmap.fillCircle(radius, height - radius - 1, radius);
        pixmap.fillCircle(width - radius - 1, height - radius - 1, radius);

        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void attachHoverEffect(Actor actor, float targetScale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(targetScale, targetScale, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topRow = new Table();
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.2f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                game.setScreen(new MiniGameSelectionScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(130, 52).left();

        Label title = new Label("CHOOSE YOUR PLANTS", skin, "big_outline");
        title.setFontScale(1.25f);
        topRow.add(title).expandX().center().padLeft(40);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).right();

        root.add(topRow).fillX().pad(8, 25, 2, 25).row();

        Table topSlotsContainer = new Table();
        topSlotsContainer.center();

        rebuildSelectedSlots();
        topSlotsContainer.add(slotsTable).center().padRight(20);

        TextButton startBtn = new TextButton("START GAME", skin, "green");
        startBtn.getLabel().setFontScale(1.15f);
        attachHoverEffect(startBtn, 1.08f);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (selectedPlants.isEmpty()) {
                    Toast.show(stage, skin, "Error: Select at least 1 plant to start!", true);
                    return;
                }
                launchMiniGameWithPlants();
            }
        });
        topSlotsContainer.add(startBtn).size(160, 64).center();

        root.add(topSlotsContainer).fillX().padTop(4).padBottom(6).row();

        Table chooserBoxWrapper = new Table();
        chooserBoxWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        Table boxContent = new Table();
        boxContent.top().pad(12);

        Table mainSplit = new Table();
        mainSplit.top().left();

        detailPane = new Table();
        detailPane.top();

        rebuildAvailablePlants();
        ScrollPane scroll = new ScrollPane(availablePlantsTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        mainSplit.add(scroll).width(820).height(420).padRight(20);
        mainSplit.add(detailPane).width(350).height(420);

        boxContent.add(mainSplit).expand().fill();
        boxStack.add(boxContent);

        chooserBoxWrapper.add(boxStack).size(1220, 460);
        root.add(chooserBoxWrapper).expand().center().padBottom(8);

        if (currentInspectedPlant != null) {
            showPlantDetail(currentInspectedPlant);
        }
    }

    private void rebuildSelectedSlots() {
        slotsTable.clear();
        User user = UserSession.getCurrentUser();
        List<Plant> allPlants = PlantLoader.loadPlants();

        for (int i = 0; i < MAX_SLOTS; i++) {
            final int slotIndex = i;
            Stack slotStack = new Stack();
            slotStack.setTransform(true);
            slotStack.setOrigin(Align.center);

            if (emptyPacketRegion != null) {
                Image emptyImg = new Image(emptyPacketRegion);
                emptyImg.setScaling(Scaling.stretch);
                slotStack.add(emptyImg);
            }

            if (slotIndex < selectedPlants.size()) {
                String plantName = selectedPlants.get(slotIndex);
                Plant plant = null;
                for (Plant p : allPlants) {
                    if (p.getName().equalsIgnoreCase(plantName)) {
                        plant = p;
                        break;
                    }
                }

                if (plant != null) {
                    int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
                    PlantSeedCard card = new PlantSeedCard(game, plant, currentLevel, false, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                    card.setSize(95, 125);
                    slotStack.add(card);

                    final Plant finalPlant = plant;
                    slotStack.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            selectedPlants.remove(slotIndex);
                            showPlantDetail(finalPlant);
                            rebuildSelectedSlots();
                            rebuildAvailablePlants();
                        }
                    });

                    attachHoverEffect(slotStack, 1.06f);
                }
            }

            slotsTable.add(slotStack).size(95, 125).pad(4);
        }
    }

    private void rebuildAvailablePlants() {
        availablePlantsTable.clear();
        availablePlantsTable.top().left();

        User user = UserSession.getCurrentUser();
        List<String> unlocked = user != null ? user.getUnlockedPlants() : new ArrayList<>();
        List<Plant> allPlants = PlantLoader.loadPlants();

        int colCount = 0;
        Plant firstPlant = null;

        for (Plant plant : allPlants) {
            boolean isUnlocked = false;
            for (String u : unlocked) {
                if (u.replaceAll("[\\s_-]", "").equalsIgnoreCase(plant.getName().replaceAll("[\\s_-]", ""))) {
                    isUnlocked = true;
                    break;
                }
            }

            if (!isUnlocked) continue;

            if (firstPlant == null) firstPlant = plant;
            if (selectedPlants.contains(plant.getName())) continue;

            int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
            PlantSeedCard card = new PlantSeedCard(game, plant, currentLevel, false, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
            attachHoverEffect(card, 1.06f);

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    showPlantDetail(plant);
                    if (selectedPlants.size() >= MAX_SLOTS) {
                        Toast.show(stage, skin, "Error: You can only select up to " + MAX_SLOTS + " plants!", true);
                        return;
                    }
                    selectedPlants.add(plant.getName());
                    rebuildSelectedSlots();
                    rebuildAvailablePlants();
                }
            });

            availablePlantsTable.add(card).size(144, 168).pad(6);
            colCount++;
            if (colCount % 5 == 0) {
                availablePlantsTable.row();
            }
        }

        if (currentInspectedPlant == null && firstPlant != null) {
            showPlantDetail(firstPlant);
        }
    }

    private void showPlantDetail(Plant plant) {
        if (detailPane == null || plant == null) return;

        currentInspectedPlant = plant;
        detailPane.clear();

        User user = UserSession.getCurrentUser();
        int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;

        Label nameTitle = new Label(plant.getName(), skin, "big");
        nameTitle.setFontScale(1.0f);
        nameTitle.setColor(Color.YELLOW);
        detailPane.add(nameTitle).padTop(4).center().row();

        PamActor bigAnim = new PamActor(pamPlayer, plant.getPamPath(), "idle", 0.45f);
        bigAnim.setSize(130, 130);
        detailPane.add(bigAnim).size(130, 130).padTop(2).center().row();

        Table stats = new Table();
        stats.left();
        addStatRow(stats, "Sun Cost:", String.valueOf(plant.getCost()));
        addStatRow(stats, "Cooldown:", plant.getCooldown() + "s");
        addStatRow(stats, "Level:", String.valueOf(currentLevel));
        detailPane.add(stats).fillX().pad(6, 12, 6, 12).row();
    }

    private void addStatRow(Table table, String title, String value) {
        Table row = new Table();
        Label tLbl = new Label(title, skin, "big");
        tLbl.setFontScale(0.58f);
        tLbl.setColor(new Color(0.85f, 0.9f, 1f, 1f));

        Label vLbl = new Label(value, skin, "big");
        vLbl.setFontScale(0.58f);
        vLbl.setColor(Color.WHITE);

        row.add(tLbl).left();
        row.add(vLbl).expandX().right();
        table.add(row).fillX().padBottom(3).row();
    }

    private void launchMiniGameWithPlants() {
        // Create the game with Zombotany minigame
        int diffVal = 3;
        Game modelGame = new Game(5, 9, 1, diffVal);
        modelGame.setCurrentSeason(new AncientEgypt());

        Zombotany mg = new Zombotany();
        modelGame.setActiveMiniGame(mg);
        modelGame.start();
        modelGame.setupSpecialLevelFeatures();
        modelGame.setSunCount(modelGame.getLevel().getInitialSunAmount());

        GameController gc = new GameController(controller);
        gc.setGame(modelGame);

        // Launch the minigame with selected plants
        game.setScreen(new MiniGamePlayScreen(game, gc, "Zombotany", new ArrayList<>(selectedPlants)));
        dispose();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.08f, 0.10f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        batch.setColor(Color.WHITE);
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            float zoomOffset = 140f;
            batch.draw(bgRegion, -zoomOffset, -zoomOffset, worldW + (zoomOffset * 2f), worldH + (zoomOffset * 2f));
        }
        batch.end();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
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
        if (roundedBrownBgTexture != null) roundedBrownBgTexture.dispose();
        if (stage != null) stage.dispose();
    }
}
