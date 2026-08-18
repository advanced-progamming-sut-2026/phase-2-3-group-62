package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import controller.menu.MenuController;
import main.Maini;
import model.entities.ZombieType;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import model.entities.zombie.Zombie;
import model.entities.zombie.loader.ZombieLoader;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import util.ParsedCommand;
import view.audio.AudioManager;
import view.ui.PamActor;
import view.ui.Toast;
import view.ui.WalletBar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CollectionScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion scrollBottomRegion;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion zombieCardBackRegion;
    private TextureRegion progressBarBgRegion;

    private boolean isPlantsTab = true;
    private String currentFilter = "ALL";
    private Table contentGrid;
    private ScrollPane scrollPane;
    private Table detailPane;
    private Label filterLabel;
    private WalletBar walletBar;

    public CollectionScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_CARDS_STORE_STORE_CARD_GREEN");
        scrollBottomRegion = game.getTextureBank().region("IMAGE_UI_JOUST_LEADERBOARD_LEADERBOARD_SCROLL_BOTTOM");
        plantCardFaceRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_FACE");
        zombieCardBackRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_BACK");
        progressBarBgRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_GUARANTEED_BG");

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        buildUI();
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
                AudioManager.getInstance().playButtonClick();
                game.setScreen(new PlayScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(110, 46).left();

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).expandX().right();

        root.add(topRow).fillX().pad(8, 20, 0, 20).row();

        Table mainContainer = new Table();

        Table leftSection = new Table();

        Table leftTopHeader = new Table();

        Table tabsTable = new Table();
        Button plantTabBtn = createTabButton("IMAGE_UI_ALMANAC_TABS_PLANTS_ACTIVE", "IMAGE_UI_STORE_TABICONS_PLANTS", () -> {
            isPlantsTab = true;
            refreshView();
        });
        tabsTable.add(plantTabBtn).size(125, 52).padRight(10);

        Button zombieTabBtn = createTabButton("IMAGE_UI_ALMANAC_TABS_UPGRADES_ACTIVE", "IMAGE_UI_STORE_TABICONS_ZOMBIES", () -> {
            isPlantsTab = false;
            refreshView();
        });
        tabsTable.add(zombieTabBtn).size(125, 52);

        leftTopHeader.add(tabsTable).left();

        Table filterContainer = new Table();
        TextureRegion filterIcon = game.getTextureBank().region("IMAGE_UI_ALMANAC_FILTER_BUTTON_DOWN");
        ImageButton filterBtn = null;
        if (filterIcon != null) {
            ImageButton.ImageButtonStyle fStyle = new ImageButton.ImageButtonStyle();
            fStyle.imageUp = new TextureRegionDrawable(filterIcon);
            filterBtn = new ImageButton(fStyle);
            filterBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    toggleFilter();
                }
            });
            attachHoverEffect(filterBtn, 1.1f);
        }

        filterLabel = new Label("Filter: " + currentFilter, skin, "big");
        filterLabel.setFontScale(0.75f);
        filterLabel.setColor(Color.WHITE);

        if (filterBtn != null) filterContainer.add(filterBtn).size(38, 38).padRight(8);
        filterContainer.add(filterLabel).left();

        leftTopHeader.add(filterContainer).expandX().right().padRight(12);

        leftSection.add(leftTopHeader).fillX().padBottom(6).row();

        Table leftBox = new Table();
        if (scrollBottomRegion != null) {
            TextureRegionDrawable leftBg = new TextureRegionDrawable(scrollBottomRegion);
            leftBg.setPadding(-22, -26, -22, -26);
            leftBox.setBackground(leftBg);
        }

        contentGrid = new Table();
        contentGrid.top().left();
        scrollPane = new ScrollPane(contentGrid, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        leftBox.add(scrollPane).width(800).height(515).pad(15);
        leftSection.add(leftBox).size(830, 545);

        mainContainer.add(leftSection).padRight(32);

        detailPane = new Table();
        if (scrollBottomRegion != null) {
            TextureRegionDrawable detailBg = new TextureRegionDrawable(scrollBottomRegion);
            detailBg.setPadding(-24, -28, -24, -28);
            detailPane.setBackground(detailBg);
        }
        mainContainer.add(detailPane).size(480, 603);

        root.add(mainContainer).expand().center().padBottom(10);

        refreshView();
    }

    private Button createTabButton(String bgRegionName, String iconRegionName, Runnable onClick) {
        TextureRegion tabBg = game.getTextureBank().region(bgRegionName);
        TextureRegion tabIcon = game.getTextureBank().region(iconRegionName);

        Button.ButtonStyle style = new Button.ButtonStyle();
        if (tabBg != null) style.up = new TextureRegionDrawable(tabBg);

        Button btn = new Button(style);
        if (tabIcon != null) {
            Image iconImg = new Image(tabIcon);
            iconImg.setScaling(Scaling.fit);
            btn.add(iconImg).size(40, 40).center();
        }

        attachHoverEffect(btn, 1.08f);
        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                if (onClick != null) onClick.run();
            }
        });

        return btn;
    }

    private void toggleFilter() {
        if (!isPlantsTab) {
            currentFilter = "ALL";
            if (filterLabel != null) filterLabel.setText("Filter: " + currentFilter);
            refreshView();
            return;
        }

        if (currentFilter.equals("ALL")) currentFilter = "UNLOCKED";
        else if (currentFilter.equals("UNLOCKED")) currentFilter = "LOCKED";
        else if (currentFilter.equals("LOCKED")) currentFilter = "UPGRADE_READY";
        else currentFilter = "ALL";

        if (filterLabel != null) filterLabel.setText("Filter: " + currentFilter);
        refreshView();
    }

    private void refreshView() {
        contentGrid.clear();
        detailPane.clear();

        if (isPlantsTab) {
            populatePlants();
        } else {
            populateZombies();
        }
    }

    private void populatePlants() {
        User user = UserSession.getCurrentUser();
        List<String> unlocked = user != null ? user.getUnlockedPlants() : new ArrayList<>();
        List<Plant> allPlants = PlantLoader.loadPlants();

        int colCount = 0;
        Plant firstPlantToSelect = null;

        for (Plant plant : allPlants) {
            boolean isUnlocked = false;
            for (String u : unlocked) {
                if (u.replaceAll("[\\s_-]", "").equalsIgnoreCase(plant.getName().replaceAll("[\\s_-]", ""))) {
                    isUnlocked = true;
                    break;
                }
            }

            int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
            int packets = user != null ? user.getSeedPackets().getOrDefault(plant.getName(), 0) : 0;
            int requiredPackets = currentLevel * 20;
            boolean canUpgrade = packets >= requiredPackets;

            if (currentFilter.equals("UNLOCKED") && !isUnlocked) continue;
            if (currentFilter.equals("LOCKED") && isUnlocked) continue;
            if (currentFilter.equals("UPGRADE_READY") && (!isUnlocked || !canUpgrade)) continue;

            if (firstPlantToSelect == null) {
                firstPlantToSelect = plant;
            }

            Table card = createPlantCardWidget(plant, isUnlocked, currentLevel, packets, requiredPackets);
            contentGrid.add(card).size(144, 168).pad(6);
            colCount++;
            if (colCount % 5 == 0) {
                contentGrid.row();
            }
        }

        if (firstPlantToSelect != null) {
            showPlantDetail(firstPlantToSelect);
        }
    }

    private Table createPlantCardWidget(Plant plant, boolean isUnlocked, int level, int packets, int requiredPackets) {
        Table card = new Table();
        card.setTransform(true);
        card.setTouchable(Touchable.enabled);

        if (plantCardFaceRegion != null) {
            card.setBackground(new TextureRegionDrawable(plantCardFaceRegion));
        }

        if (!isUnlocked) {
            card.setColor(0.55f, 0.55f, 0.55f, 0.85f);
        } else {
            card.setColor(Color.WHITE);
        }

        Stack stack = new Stack();
        stack.setSize(136, 158);

        Table contentTable = new Table();
        contentTable.setFillParent(true);

        PamActor plantAnim = new PamActor(pamPlayer, plant.getPamPath(), null, 0.392f) {
            @Override
            public void act(float delta) {
                super.act(0f);
            }
        };
        plantAnim.setSize(140, 140);
        plantAnim.setTouchable(Touchable.disabled);

        if (!isUnlocked) {
            plantAnim.setColor(0.38f, 0.38f, 0.38f, 0.7f);
        }
        contentTable.add(plantAnim).size(140, 140).padTop(2).center().row();

        if (isUnlocked) {
            Label lvlBadge = new Label("Lvl " + level, skin, "big");
            lvlBadge.setFontScale(0.55f);
            lvlBadge.setColor(Color.WHITE);
            lvlBadge.setTouchable(Touchable.disabled);
            contentTable.add(lvlBadge).padTop(1).center().row();

            Table progTable = new Table();
            if (progressBarBgRegion != null) {
                progTable.setBackground(new TextureRegionDrawable(progressBarBgRegion));
            }
            Label progLabel = new Label(packets + "/" + requiredPackets, skin, "big");
            progLabel.setFontScale(0.48f);
            progLabel.setColor(packets >= requiredPackets ? Color.GREEN : Color.YELLOW);
            progTable.add(progLabel).center();
            contentTable.add(progTable).width(112).height(22).padTop(2).center();
        } else {
            Label lockedLabel = new Label("LOCKED", skin, "big");
            lockedLabel.setFontScale(0.6f);
            lockedLabel.setColor(Color.LIGHT_GRAY);
            lockedLabel.setTouchable(Touchable.disabled);
            contentTable.add(lockedLabel).padTop(8).center();
        }

        stack.add(contentTable);

        if (!isUnlocked) {
            Table lockOverlay = new Table();
            lockOverlay.setFillParent(true);
            PamActor lockAnim = new PamActor(
                pamPlayer,
                "768/FULL/UI/LOCK_ANIMS/LOCK_ANIMS.PAM",
                null,
                0.65f,
                200f,
                0f
            ) {
                @Override
                public void act(float delta) {
                    super.act(0f);
                }
            };
            lockAnim.setSize(72, 72);
            lockAnim.setTouchable(Touchable.disabled);
            lockOverlay.add(lockAnim).size(72, 72).center();
            stack.add(lockOverlay);
        }

        card.add(stack).size(136, 158);
        card.setOrigin(Align.center);

        card.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    card.clearActions();
                    card.addAction(Actions.scaleTo(1.06f, 1.06f, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    card.clearActions();
                    card.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                showPlantDetail(plant);
            }
        });

        return card;
    }

    private void addStatRow(Table table, String title, String value) {
        Table row = new Table();
        Label tLbl = new Label(title, skin, "big");
        tLbl.setFontScale(0.68f);
        tLbl.setColor(new Color(0.85f, 0.9f, 1f, 1f));

        Label vLbl = new Label(value, skin, "big");
        vLbl.setFontScale(0.68f);
        vLbl.setColor(Color.WHITE);

        row.add(tLbl).left();
        row.add(vLbl).expandX().right();
        table.add(row).fillX().padBottom(5).row();
    }

    private void showPlantDetail(Plant plant) {
        detailPane.clear();
        User user = UserSession.getCurrentUser();
        List<String> unlocked = user != null ? user.getUnlockedPlants() : new ArrayList<>();

        boolean isUnlocked = false;
        for (String u : unlocked) {
            if (u.replaceAll("[\\s_-]", "").equalsIgnoreCase(plant.getName().replaceAll("[\\s_-]", ""))) {
                isUnlocked = true;
                break;
            }
        }

        int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;

        Label nameTitle = new Label(plant.getName(), skin, "big");
        nameTitle.setFontScale(1.3f);
        nameTitle.setColor(Color.YELLOW);
        detailPane.add(nameTitle).padTop(16).center().row();

        PamActor bigAnim = new PamActor(pamPlayer, plant.getPamPath(), "anim_idle", 0.63f);
        bigAnim.setSize(220, 220);
        detailPane.add(bigAnim).size(220, 220).padTop(6).center().row();

        Table stats = new Table();
        stats.left();

        addStatRow(stats, "Category:", plant.getCategory() != null ? plant.getCategory() : "-");
        addStatRow(stats, "Sun Cost:", String.valueOf(plant.getCost()));
        addStatRow(stats, "Recharge:", plant.getRecharge() + "s");
        addStatRow(stats, "Toughness (HP):", String.valueOf(plant.getBaseHp()));
        addStatRow(stats, "Damage:", String.valueOf(plant.getDamage()));
        addStatRow(stats, "Action Interval:", plant.getActionInterval() + "s");
        addStatRow(stats, "Current Level:", String.valueOf(currentLevel));

        detailPane.add(stats).pad(10, 24, 6, 24).fillX().row();

        if (isUnlocked) {
            int upgradeCost = currentLevel * 1000;
            TextButton upgradeBtn = new TextButton("Upgrade (" + upgradeCost + " Coins)", skin);
            upgradeBtn.getLabel().setFontScale(0.95f);
            attachHoverEffect(upgradeBtn, 1.05f);
            upgradeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    ParsedCommand cmd = new ParsedCommand("menu collection upgrade-plant");
                    cmd.addArg("-p", plant.getName());
                    String result = controller.processCollection(cmd, "upgrade-plant");
                    Toast.show(stage, skin, result, result.startsWith("Error"));
                    if (walletBar != null) {
                        walletBar.updateValues();
                    }
                    refreshView();
                    showPlantDetail(plant);
                }
            });
            detailPane.add(upgradeBtn).size(260, 48).padBottom(15).center();
        } else {
            TextButton purchaseBtn = new TextButton("Unlock (2000 Coins)", skin);
            purchaseBtn.getLabel().setFontScale(0.95f);
            attachHoverEffect(purchaseBtn, 1.05f);
            purchaseBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    ParsedCommand cmd = new ParsedCommand("menu collection purchase-plant");
                    cmd.addArg("-p", plant.getName());
                    String result = controller.processCollection(cmd, "purchase-plant");
                    Toast.show(stage, skin, result, result.startsWith("Error"));
                    if (walletBar != null) {
                        walletBar.updateValues();
                    }
                    refreshView();
                    showPlantDetail(plant);
                }
            });
            detailPane.add(purchaseBtn).size(260, 48).padBottom(15).center();
        }
    }

    private void populateZombies() {
        User user = UserSession.getCurrentUser();
        List<String> observed = user != null ? user.getObservedZombies() : new ArrayList<>();
        List<Zombie> allZombies = ZombieLoader.loadZombies();

        int colCount = 0;
        Zombie firstObserved = null;

        for (Zombie zombie : allZombies) {
            boolean isObserved = false;
            for (String o : observed) {
                if (o.replaceAll("[\\s_-]", "").equalsIgnoreCase(zombie.getName().replaceAll("[\\s_-]", ""))) {
                    isObserved = true;
                    break;
                }
            }

            if (isObserved && firstObserved == null) {
                firstObserved = zombie;
            }

            Table card = createZombieCardWidget(zombie, isObserved);
            contentGrid.add(card).size(144, 168).pad(6);
            colCount++;
            if (colCount % 5 == 0) {
                contentGrid.row();
            }
        }

        if (firstObserved != null) {
            showZombieDetail(firstObserved);
        } else {
            detailPane.clear();
            Label emptyLbl = new Label("No zombies discovered yet.", skin, "big");
            emptyLbl.setFontScale(0.9f);
            emptyLbl.setColor(Color.LIGHT_GRAY);
            detailPane.add(emptyLbl).expand().center();
        }
    }

    private Table createZombieCardWidget(Zombie zombie, boolean isObserved) {
        Table card = new Table();
        card.setTransform(true);
        card.setTouchable(Touchable.enabled);

        if (zombieCardBackRegion != null) {
            card.setBackground(new TextureRegionDrawable(zombieCardBackRegion));
        }

        if (!isObserved) {
            card.setColor(0.55f, 0.55f, 0.55f, 0.85f);
        } else {
            card.setColor(Color.WHITE);
        }

        Table content = new Table();

        if (isObserved) {
            ZombieType zType = ZombieType.fromZombie(zombie);
            String pamPath = zType != null ? zType.getPamPath() : "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM";
            float scale = zType != null ? zType.getScale() : 0.28f;
            float offX = zType != null ? zType.getOffsetX() : 0f;
            float offY = zType != null ? zType.getOffsetY() : 0f;

            Map<String, Boolean> visibility = getArmorVisibilityForType(zType);

            PamActor zAnim = new PamActor(
                pamPlayer,
                pamPath,
                null,
                scale,
                offX,
                offY,
                visibility
            ) {
                @Override
                public void act(float delta) {
                    super.act(0f);
                }
            };
            zAnim.setSize(108, 108);
            zAnim.setTouchable(Touchable.disabled);
            content.add(zAnim).size(108, 108).center().row();

            Label nameLbl = new Label(zType != null ? zType.getDisplayName() : zombie.getName(), skin, "big");
            nameLbl.setFontScale(0.55f);
            nameLbl.setEllipsis(true);
            nameLbl.setAlignment(Align.center);
            nameLbl.setColor(Color.WHITE);
            nameLbl.setTouchable(Touchable.disabled);
            content.add(nameLbl).width(128).padTop(4).center();
        } else {
            Label unk = new Label("???", skin, "big");
            unk.setFontScale(1.5f);
            unk.setColor(Color.DARK_GRAY);
            unk.setTouchable(Touchable.disabled);
            content.add(unk).expand().center();
        }

        card.add(content).expand().center();
        card.setOrigin(Align.center);

        if (isObserved) {
            card.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        card.clearActions();
                        card.addAction(Actions.scaleTo(1.06f, 1.06f, 0.1f));
                    }
                }

                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        card.clearActions();
                        card.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                    }
                }
            });

            card.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    showZombieDetail(zombie);
                }
            });
        }

        return card;
    }

    private Map<String, Boolean> getArmorVisibilityForType(ZombieType zType) {
        Map<String, Boolean> visibility = new HashMap<>();
        if (zType == null) return visibility;

        String aType = zType.getArmorType();
        String cleanId = zType.getId().toLowerCase();

        if (cleanId.contains("cone") || cleanId.contains("armor1") || (aType != null && aType.equalsIgnoreCase("Cone"))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_cone_states", true);
            visibility.put("zombie_armor_cone_states", true);
            visibility.put("_zombie_armor_cone", true);
            visibility.put("zombie_armor_cone", true);
            visibility.put("zombie_armor_cone_norm", true);
            visibility.put("_zombie_armor_cone_norm", true);
            visibility.put("cone", true);
            visibility.put("_cone", true);
        } else if (cleanId.contains("bucket") || cleanId.contains("armor2") || (aType != null && aType.equalsIgnoreCase("Bucket"))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_bucket_states", true);
            visibility.put("zombie_armor_bucket_states", true);
            visibility.put("_zombie_armor_bucket", true);
            visibility.put("zombie_armor_bucket", true);
            visibility.put("zombie_armor_bucket_norm", true);
            visibility.put("_zombie_armor_bucket_norm", true);
            visibility.put("bucket", true);
            visibility.put("_bucket", true);
        } else if (cleanId.contains("brick") || cleanId.contains("armor4") || (aType != null && aType.equalsIgnoreCase("Brick"))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_brick_states", true);
            visibility.put("zombie_armor_brick_states", true);
            visibility.put("_zombie_armor_brick", true);
            visibility.put("zombie_armor_brick", true);
            visibility.put("zombie_armor_brick_norm", true);
            visibility.put("_zombie_armor_brick_norm", true);
            visibility.put("brick", true);
            visibility.put("_brick", true);
        } else if (cleanId.contains("knight") || cleanId.contains("armor3") || (aType != null && (aType.equalsIgnoreCase("Crown") || aType.equalsIgnoreCase("Knight")))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_crown_states", true);
            visibility.put("zombie_armor_crown_states", true);
            visibility.put("_zombie_armor_crown", true);
            visibility.put("zombie_armor_crown", true);
            visibility.put("zombie_armor_crown_norm", true);
            visibility.put("_zombie_armor_crown_norm", true);
            visibility.put("crown", true);
            visibility.put("_crown", true);
        } else if (cleanId.contains("newspaper") || (aType != null && aType.equalsIgnoreCase("Newspaper"))) {
            visibility.put("_zombie_newspaper_states", true);
            visibility.put("zombie_newspaper_states", true);
            visibility.put("_zombie_newspaper", true);
            visibility.put("zombie_newspaper", true);
        }

        return visibility;
    }

    private void showZombieDetail(Zombie zombie) {
        detailPane.clear();

        ZombieType zType = ZombieType.fromZombie(zombie);
        String displayName = zType != null ? zType.getDisplayName() : zombie.getName();
        String pamPath = zType != null ? zType.getPamPath() : "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM";
        float scale = zType != null ? zType.getScale() * 1.6f : 0.45f;
        float offX = zType != null ? zType.getOffsetX() : 0f;
        float offY = zType != null ? zType.getOffsetY() : 0f;

        Label nameTitle = new Label(displayName, skin, "big");
        nameTitle.setFontScale(1.3f);
        nameTitle.setColor(Color.RED);
        detailPane.add(nameTitle).padTop(16).center().row();

        Map<String, Boolean> visibility = getArmorVisibilityForType(zType);

        PamActor bigAnim = new PamActor(
            pamPlayer,
            pamPath,
            "anim_idle",
            scale,
            offX,
            offY,
            visibility
        );
        bigAnim.setSize(190, 190);
        detailPane.add(bigAnim).size(190, 190).padTop(6).center().row();

        Table stats = new Table();
        stats.left();

        addStatRow(stats, "Hitpoints (HP):", String.valueOf(zombie.getHealth()));
        addStatRow(stats, "Damage:", String.valueOf(zombie.getDamage()));
        addStatRow(stats, "Speed:", String.valueOf(zombie.getSpeed()));
        addStatRow(stats, "Wave Cost:", String.valueOf(zombie.getWaveCost()));
        addStatRow(stats, "Armor Type:", zombie.getArmorType() != null && !zombie.getArmorType().equalsIgnoreCase("none") ? zombie.getArmorType() : (zType != null ? zType.getArmorType() : "none"));
        if (zombie.getArmorHealth() > 0 || (zType != null && zType.getArmorHp() > 0)) {
            int aHp = zombie.getArmorHealth() > 0 ? zombie.getArmorHealth() : (zType != null ? zType.getArmorHp() : 0);
            addStatRow(stats, "Armor HP:", String.valueOf(aHp));
        }

        detailPane.add(stats).pad(10, 24, 15, 24).fillX().expandY().top();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.12f, 0.14f, 0.18f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
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
        stage.dispose();
    }
}
