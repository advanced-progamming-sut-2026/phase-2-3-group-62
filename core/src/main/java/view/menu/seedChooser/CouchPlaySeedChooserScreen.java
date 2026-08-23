package view.menu.seedChooser;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.plant.loader.PlantLoader;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import view.game.couch.CouchPlayGameScreen;
import view.menu.playMenu.PlayScreen;
import view.ui.PlantSeedCard;
import view.ui.Toast;
import view.ui.ZombieSeedCard;

import java.util.ArrayList;
import java.util.List;

public class CouchPlaySeedChooserScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion emptyPacketRegion;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion badgeRegion;
    private Texture roundedBrownBgTexture;

    private boolean isPlantTurn = true;
    private final List<String> chosenPlants = new ArrayList<>();
    private final List<String> chosenZombies = new ArrayList<>();

    private final Table slotsTable = new Table();
    private final Table availableCardsTable = new Table();
    private Label titleLabel;
    private TextButton nextBtn;

    private static final int MAX_SLOTS = 5;

    public CouchPlaySeedChooserScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_STORE_GACHA_PINATA_RARE_CARD");
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
        backBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new PlayScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(120, 48).left();

        titleLabel = new Label(isPlantTurn ? "PLAYER 1 (PLANTS): CHOOSE 5 SEEDS" : "PLAYER 2 (ZOMBIES): CHOOSE 5 SEEDS", skin, "big_outline");
        titleLabel.setFontScale(1.15f);
        titleLabel.setColor(isPlantTurn ? Color.GREEN : Color.SALMON);
        topRow.add(titleLabel).expandX().center();

        root.add(topRow).fillX().pad(8, 25, 2, 25).row();

        Table topSlotsContainer = new Table();
        topSlotsContainer.center();

        rebuildSelectedSlots();
        topSlotsContainer.add(slotsTable).center().padRight(20);

        nextBtn = new TextButton(isPlantTurn ? "NEXT (ZOMBIES)" : "START GAME!", skin, "green");
        nextBtn.getLabel().setFontScale(1.15f);
        attachHoverEffect(nextBtn, 1.08f);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                List<String> curList = isPlantTurn ? chosenPlants : chosenZombies;
                if (curList.size() < MAX_SLOTS) {
                    Toast.show(stage, skin, "Error: Select exactly 5 cards!", true);
                    return;
                }

                if (isPlantTurn) {
                    isPlantTurn = false;
                    buildUI();
                } else {
                    game.setScreen(new CouchPlayGameScreen(game, controller, skin, new ArrayList<>(chosenPlants), new ArrayList<>(chosenZombies)));
                    dispose();
                }
            }
        });
        topSlotsContainer.add(nextBtn).size(200, 64).center();

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

        rebuildAvailableCards();
        ScrollPane scroll = new ScrollPane(availableCardsTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        boxContent.add(scroll).expand().fill();
        boxStack.add(boxContent);

        chooserBoxWrapper.add(boxStack).size(1220, 460);
        root.add(chooserBoxWrapper).expand().center().padBottom(8);
    }

    private void rebuildSelectedSlots() {
        slotsTable.clear();
        User user = UserSession.getCurrentUser();
        List<Plant> allPlants = PlantLoader.loadPlants();
        List<String> curList = isPlantTurn ? chosenPlants : chosenZombies;

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

            if (slotIndex < curList.size()) {
                String cardName = curList.get(slotIndex);

                if (isPlantTurn) {
                    Plant plant = null;
                    for (Plant p : allPlants) {
                        if (p.getName().equalsIgnoreCase(cardName)) {
                            plant = p;
                            break;
                        }
                    }
                    if (plant == null) plant = PlantFactory.createPlant(cardName);

                    if (plant != null) {
                        int lvl = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
                        PlantSeedCard cardWidget = new PlantSeedCard(game, plant, lvl, false, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                        cardWidget.setSize(95, 125);
                        slotStack.add(cardWidget);
                    }
                } else {
                    int cost = getZombieCost(cardName);
                    ZombieSeedCard cardWidget = new ZombieSeedCard(game, cardName, cost, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                    cardWidget.setSize(95, 125);
                    slotStack.add(cardWidget);
                }

                slotStack.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        curList.remove(slotIndex);
                        rebuildSelectedSlots();
                        rebuildAvailableCards();
                    }
                });
                attachHoverEffect(slotStack, 1.06f);
            }

            slotsTable.add(slotStack).size(95, 125).pad(4);
        }
    }

    private void rebuildAvailableCards() {
        availableCardsTable.clear();
        availableCardsTable.top().left();

        User user = UserSession.getCurrentUser();
        int colCount = 0;

        if (isPlantTurn) {
            List<String> unlocked = user != null ? user.getUnlockedPlants() : new ArrayList<>();
            List<Plant> allPlants = PlantLoader.loadPlants();

            for (Plant plant : allPlants) {
                boolean isUnlocked = false;
                for (String u : unlocked) {
                    if (u.replaceAll("[\\s_-]", "").equalsIgnoreCase(plant.getName().replaceAll("[\\s_-]", ""))) {
                        isUnlocked = true;
                        break;
                    }
                }
                if (!isUnlocked || chosenPlants.contains(plant.getName())) continue;

                int lvl = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
                PlantSeedCard card = new PlantSeedCard(game, plant, lvl, false, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                attachHoverEffect(card, 1.06f);

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (chosenPlants.size() >= MAX_SLOTS) {
                            Toast.show(stage, skin, "Error: You can only select " + MAX_SLOTS + " cards!", true);
                            return;
                        }
                        chosenPlants.add(plant.getName());
                        rebuildSelectedSlots();
                        rebuildAvailableCards();
                    }
                });

                availableCardsTable.add(card).size(140, 165).pad(6);
                colCount++;
                if (colCount % 7 == 0) availableCardsTable.row();
            }
        } else {
            String[] pool = {
                "NormalZombie", "ConeZombie", "BucketZombie", "ZombieNewspaper",
                "ZombieModernAllStar", "ZombieImp", "ZombieProspector", "ZombieCrystalSkull",
                "ZombieDarkJuggler", "ZombiePiano", "BarrelRollerZombie", "ZombieGargantuar"
            };

            for (String zName : pool) {
                if (chosenZombies.contains(zName)) continue;

                int cost = getZombieCost(zName);
                ZombieSeedCard card = new ZombieSeedCard(game, zName, cost, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                attachHoverEffect(card, 1.06f);

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (chosenZombies.size() >= MAX_SLOTS) {
                            Toast.show(stage, skin, "Error: You can only select " + MAX_SLOTS + " cards!", true);
                            return;
                        }
                        chosenZombies.add(zName);
                        rebuildSelectedSlots();
                        rebuildAvailableCards();
                    }
                });

                availableCardsTable.add(card).size(140, 165).pad(6);
                colCount++;
                if (colCount % 7 == 0) availableCardsTable.row();
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

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.08f, 0.10f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        batch.setColor(0.55f, 0.55f, 0.55f, 1f);
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            batch.draw(bgRegion, 0, 0, worldW, worldH);
        }
        batch.setColor(Color.WHITE);
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
