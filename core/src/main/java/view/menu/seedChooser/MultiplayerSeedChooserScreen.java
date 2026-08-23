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
import network.Message;
import network.NetworkManager;
import pvz.libpvz.pam.PamPlayer;
import view.game.multiplayer.MultiplayerGameScreen;
import view.menu.playMenu.PlayScreen;
import view.ui.PlantSeedCard;
import view.ui.Toast;
import view.ui.WalletBar;
import view.ui.ZombieSeedCard;

import java.util.ArrayList;
import java.util.List;

public class MultiplayerSeedChooserScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private final String opponentUsername;
    private final String role;
    private final boolean isPlantsRole;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion emptyPacketRegion;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion badgeRegion;
    private Texture roundedBrownBgTexture;

    private final List<String> selectedCards = new ArrayList<>();
    private final Table slotsTable = new Table();
    private final Table availableCardsTable = new Table();
    private WalletBar walletBar;
    private TextButton startBtn;
    private Label statusLabel;

    private boolean isMyReady = false;
    private boolean isOpponentReady = false;
    private boolean isCountingDown = false;
    private float countdownTimer = 5.0f;
    private int lastDisplayedSecond = 5;

    private static final int MAX_SLOTS = 5;

    public MultiplayerSeedChooserScreen(Maini game, MenuController controller, Skin skin, String opponentUsername, String role) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
        this.opponentUsername = opponentUsername;
        this.role = role != null ? role.toUpperCase() : "PLANTS";
        this.isPlantsRole = "PLANTS".equalsIgnoreCase(this.role);
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
                if (isCountingDown) return;
                game.setScreen(new PlayScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(120, 48).left();

        Label title = new Label("CHOOSE 5 " + (isPlantsRole ? "PLANTS" : "ZOMBIES"), skin, "big_outline");
        title.setFontScale(1.2f);
        title.setColor(isPlantsRole ? Color.GREEN : Color.SALMON);
        topRow.add(title).expandX().center().padLeft(40);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).right();

        root.add(topRow).fillX().pad(8, 25, 2, 25).row();

        Table topSlotsContainer = new Table();
        topSlotsContainer.center();

        rebuildSelectedSlots();
        topSlotsContainer.add(slotsTable).center().padRight(20);

        Table startBtnWrapper = new Table();
        startBtn = new TextButton("READY!", skin, "green");
        startBtn.getLabel().setFontScale(1.15f);
        attachHoverEffect(startBtn, 1.08f);
        startBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (isMyReady || isCountingDown) return;

                if (selectedCards.size() < MAX_SLOTS) {
                    Toast.show(stage, skin, "Error: Select exactly 5 cards first!", true);
                    return;
                }

                isMyReady = true;
                startBtn.setText("WAITING...");
                startBtn.setColor(Color.YELLOW);
                statusLabel.setText("Waiting for " + opponentUsername + "...");
                statusLabel.setColor(Color.YELLOW);

                Message readyMsg = new Message(Message.Type.GAME_PLAYER_READY)
                    .put("opponent_username", opponentUsername);
                NetworkManager.getInstance().sendAsync(readyMsg);

                checkStartGame();
            }
        });
        startBtnWrapper.add(startBtn).size(175, 58).row();

        statusLabel = new Label("", skin, "big");
        statusLabel.setFontScale(0.7f);
        statusLabel.setColor(Color.WHITE);
        startBtnWrapper.add(statusLabel).padTop(4).center();

        topSlotsContainer.add(startBtnWrapper).center();
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

    private synchronized void checkStartGame() {
        if (isCountingDown) return;
        if (isMyReady && isOpponentReady) {
            isCountingDown = true;
            countdownTimer = 5.0f;
            lastDisplayedSecond = 5;
            startBtn.setText("STARTING IN 5...");
            startBtn.setColor(Color.CYAN);
            statusLabel.setText("Both players ready! Match starts in 5s...");
            statusLabel.setColor(Color.GREEN);
        }
    }

    private void updateCountdown(float delta) {
        if (!isCountingDown) return;

        countdownTimer -= delta;
        int currentSec = (int) Math.ceil(countdownTimer);

        if (currentSec != lastDisplayedSecond && currentSec > 0) {
            lastDisplayedSecond = currentSec;
            startBtn.setText("STARTING IN " + currentSec + "...");
            statusLabel.setText("Match starts in " + currentSec + "s...");
        }

        if (countdownTimer <= 0) {
            isCountingDown = false;
            Gdx.app.postRunnable(() -> {
                game.setScreen(new MultiplayerGameScreen(game, controller, skin, opponentUsername, role, new ArrayList<>(selectedCards)));
                dispose();
            });
        }
    }

    private void checkNetworkMessages() {
        Message msg;
        while ((msg = NetworkManager.getInstance().pollPushMessage()) != null) {
            if (msg.getType() == Message.Type.GAME_PLAYER_READY) {
                isOpponentReady = true;
                if (!isMyReady && statusLabel != null) {
                    statusLabel.setText(opponentUsername + " is ready! Press READY!");
                    statusLabel.setColor(Color.GREEN);
                }
                checkStartGame();
            }
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

            if (slotIndex < selectedCards.size()) {
                String cardName = selectedCards.get(slotIndex);

                if (isPlantsRole) {
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
                        if (isMyReady || isCountingDown) return;
                        selectedCards.remove(slotIndex);
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

        if (isPlantsRole) {
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
                if (!isUnlocked || selectedCards.contains(plant.getName())) continue;

                int lvl = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
                PlantSeedCard card = new PlantSeedCard(game, plant, lvl, false, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                attachHoverEffect(card, 1.06f);

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (isMyReady || isCountingDown) return;
                        if (selectedCards.size() >= MAX_SLOTS) {
                            Toast.show(stage, skin, "Error: You can only select " + MAX_SLOTS + " cards!", true);
                            return;
                        }
                        selectedCards.add(plant.getName());
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
                if (selectedCards.contains(zName)) continue;

                int cost = getZombieCost(zName);
                ZombieSeedCard card = new ZombieSeedCard(game, zName, cost, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                attachHoverEffect(card, 1.06f);

                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (isMyReady || isCountingDown) return;
                        if (selectedCards.size() >= MAX_SLOTS) {
                            Toast.show(stage, skin, "Error: You can only select " + MAX_SLOTS + " cards!", true);
                            return;
                        }
                        selectedCards.add(zName);
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

        checkNetworkMessages();
        updateCountdown(delta);

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
