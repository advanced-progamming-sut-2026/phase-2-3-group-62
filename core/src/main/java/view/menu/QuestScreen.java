package view.menu;

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
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.quest.Quest;
import model.user.User;
import model.user.UserSession;
import util.FileManager;
import view.audio.AudioManager;
import view.ui.Toast;
import view.ui.WalletBar;

import java.util.ArrayList;
import java.util.List;

public class QuestScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private TextureRegion cardBgRegion;
    private TextureRegion scrollBarRegion;
    private TextureRegion redFlagRegion;

    private TextureRegion tabUpgradesRegion;
    private TextureRegion tabZombiesRegion;
    private TextureRegion tabPlantsRegion;

    private TextureRegion goldCoinRegion;
    private TextureRegion diamondRegion;
    private TextureRegion seedPacketIconRegion;

    private Texture roundedBrownBgTexture;
    private Table questListTable;
    private WalletBar walletBar;
    private Quest.QuestType currentTab = Quest.QuestType.DAILY;

    public QuestScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZCORP_BG_TEXTURE");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        }

        cardBgRegion = game.getTextureBank().region("IMAGE_UI_HUD_LOD_LOD_BEGHOULED_BEYOND_BG");
        scrollBarRegion = game.getTextureBank().region("IMAGE_UI_ALMANAC_CARD_ZOMBIE_SCROLLBAR");
        redFlagRegion = game.getTextureBank().region("IMAGE_UI_SEASONS_UNCOMPRESSED_RED_FLAG");

        tabUpgradesRegion = game.getTextureBank().region("IMAGE_UI_ALMANAC_TABS_UPGRADES_DOWN");
        tabZombiesRegion = game.getTextureBank().region("IMAGE_UI_ALMANAC_TABS_ZOMBIES_DOWN");
        tabPlantsRegion = game.getTextureBank().region("IMAGE_UI_ALMANAC_TABS_PLANTS_DOWN");

        goldCoinRegion = game.getTextureBank().region("IMAGE_EFFECTS_COIN_GOLD_COIN_GOLD_98X95");
        diamondRegion = game.getTextureBank().region("IMAGE_EFFECTS_COIN_DIAMOND_COIN_DIAMOND_141X146");
        seedPacketIconRegion = game.getTextureBank().region("IMAGE_UI_STOREMULTI_SEEDPACKETICON");

        roundedBrownBgTexture = createRoundedRectangleTexture(1220, 580, 26, new Color(0.24f, 0.14f, 0.08f, 0.96f));

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

    private Table createNavButton(String iconRegionName, String labelText, float size, ClickListener listener) {
        TextureRegion icon = game.getTextureBank().region(iconRegionName);

        Button.ButtonStyle style = new Button.ButtonStyle();
        if (icon != null) {
            style.up = new TextureRegionDrawable(icon);
        }

        Button btn = new Button(style);
        if (listener != null) {
            btn.addListener(listener);
        }

        attachHoverEffect(btn, 1.12f);

        Table container = new Table();
        container.add(btn).size(size, size).row();
        Label lbl = new Label(labelText, skin);
        lbl.setFontScale(0.9f);
        lbl.setColor(Color.WHITE);
        container.add(lbl).padTop(4).center();

        return container;
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topRow = new Table();
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.35f);
        backBtn.setOrigin(Align.center);
        backBtn.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    backBtn.clearActions();
                    backBtn.addAction(Actions.scaleTo(1.06f, 1.06f, 0.1f));
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    backBtn.clearActions();
                    backBtn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                game.setScreen(new PlayScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(145, 56).left();

        Label title = new Label("TRAVEL LOG", skin, "big_outline");
        title.setFontScale(1.3f);
        topRow.add(title).expandX().center().padLeft(60);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).right();

        root.add(topRow).fillX().pad(12, 30, 6, 30).row();

        Table dialogWrapper = new Table();
        dialogWrapper.center();

        Stack dialogStack = new Stack();

        if (roundedBrownBgTexture != null) {
            Image solidBg = new Image(roundedBrownBgTexture);
            solidBg.setScaling(Scaling.stretch);
            dialogStack.add(solidBg);
        }

        Table dialogContent = new Table();
        dialogContent.top().pad(20, 20, 20, 20);

        Table tabTable = new Table();
        tabTable.left();

        ImageButton dailyTabBtn = createTabButton(tabPlantsRegion, "Daily", Quest.QuestType.DAILY);
        ImageButton epicTabBtn = createTabButton(tabZombiesRegion, "Epic", Quest.QuestType.EPIC);
        ImageButton storyTabBtn = createTabButton(tabUpgradesRegion, "Story", Quest.QuestType.STORY);

        tabTable.add(dailyTabBtn).size(175, 58).padRight(14);
        tabTable.add(epicTabBtn).size(175, 58).padRight(14);
        tabTable.add(storyTabBtn).size(175, 58).padRight(14);

        TextButton claimAllBtn = new TextButton("Claim All", skin, "green");
        claimAllBtn.getLabel().setFontScale(1.05f);
        claimAllBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String res = controller.processClaimQuests(new util.ParsedCommand("claim"));
                Toast.show(stage, skin, res, res.startsWith("Error"));
                walletBar.updateValues();
                rebuildQuests();
            }
        });
        tabTable.add(claimAllBtn).size(165, 54).expandX().right().padRight(5);

        dialogContent.add(tabTable).fillX().padBottom(14).row();

        questListTable = new Table();
        rebuildQuests();

        ScrollPane scroll = new ScrollPane(questListTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        dialogContent.add(scroll).expand().fill().pad(4);

        dialogStack.add(dialogContent);

        dialogWrapper.add(dialogStack).size(1220, 580);
        root.add(dialogWrapper).expand().center().padBottom(6).row();

        Table bottomRow = new Table();

        Table bottomLeft = new Table();
        Table minigamesBtn = createNavButton(
            "IMAGE_UI_GENERIC_BUTTON_HUD_MINIGAMES_ALT_SELECTED",
            "Minigames",
            64,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    game.setScreen(new MiniGameSelectionScreen(game, controller, skin));
                    dispose();
                }
            }
        );
        bottomLeft.add(minigamesBtn);
        bottomRow.add(bottomLeft).left().expandX().padLeft(30);

        root.add(bottomRow).fillX().bottom().padBottom(10);
    }

    private ImageButton createTabButton(TextureRegion region, String labelText, Quest.QuestType type) {
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        if (region != null) {
            style.up = new TextureRegionDrawable(region);
        }

        ImageButton btn = new ImageButton(style);
        btn.setTransform(true);
        btn.setOrigin(Align.center);

        Label lbl = new Label(labelText, skin, "big");
        lbl.setFontScale(0.72f);
        lbl.setAlignment(Align.center);
        btn.add(lbl).center().padBottom(4);

        btn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                currentTab = type;
                rebuildQuests();
            }
        });

        return btn;
    }

    private void rebuildQuests() {
        questListTable.clear();
        questListTable.top();

        User user = UserSession.getCurrentUser();
        if (user == null || user.getQuests() == null || user.getQuests().isEmpty()) {
            Label emptyLbl = new Label("No Quests Available", skin, "big");
            emptyLbl.setFontScale(0.9f);
            emptyLbl.setColor(Color.LIGHT_GRAY);
            questListTable.add(emptyLbl).padTop(120);
            return;
        }

        List<Quest> filtered = new ArrayList<>();
        for (Quest q : user.getQuests()) {
            if (q.getType() == currentTab) {
                filtered.add(q);
            }
        }

        filtered.sort((q1, q2) -> Integer.compare(q1.getPriority().ordinal(), q2.getPriority().ordinal()));

        if (filtered.isEmpty()) {
            Label emptyLbl = new Label("No " + currentTab.name() + " Quests Active", skin, "big");
            emptyLbl.setFontScale(0.9f);
            emptyLbl.setColor(Color.LIGHT_GRAY);
            questListTable.add(emptyLbl).padTop(120);
            return;
        }

        for (Quest q : filtered) {
            QuestCard card = new QuestCard(q);
            questListTable.add(card).size(1160, 160).padBottom(16).row();
        }
    }

    private class QuestCard extends Stack {
        public QuestCard(Quest quest) {
            setTransform(true);
            setOrigin(Align.center);

            if (cardBgRegion != null) {
                Image cardBg = new Image(cardBgRegion);
                cardBg.setScaling(Scaling.stretch);
                add(cardBg);
            }

            Table inner = new Table();
            inner.pad(14, 28, 14, 28);

            Table textInfo = new Table();
            textInfo.left();

            Label titleLbl = new Label(quest.getTitle(), skin, "big");
            titleLbl.setFontScale(0.74f);
            titleLbl.setColor(Color.YELLOW);
            textInfo.add(titleLbl).left().row();

            Label descLbl = new Label(quest.getDescription(), skin, "default");
            descLbl.setFontScale(1.1f);
            descLbl.setColor(Color.WHITE);
            descLbl.setWrap(true);
            textInfo.add(descLbl).width(500).left().padTop(4).row();

            FlagProgressBar progressBar = new FlagProgressBar(redFlagRegion, scrollBarRegion, quest.getProgress(), quest.getTarget());
            textInfo.add(progressBar).padTop(8).left();

            inner.add(textInfo).expandX().left();

            Table rewardTable = new Table();
            rewardTable.right();

            if (quest.getRewardCoins() > 0) {
                Label cLbl = new Label("+" + quest.getRewardCoins(), skin, "big");
                cLbl.setFontScale(0.75f);
                cLbl.setColor(Color.YELLOW);
                rewardTable.add(cLbl).padRight(6);

                if (goldCoinRegion != null) {
                    Image cImg = new Image(goldCoinRegion);
                    cImg.setScaling(Scaling.fit);
                    rewardTable.add(cImg).size(36, 36).padRight(14);
                }
            }

            if (quest.getRewardDiamonds() > 0) {
                Label gLbl = new Label("+" + quest.getRewardDiamonds(), skin, "big");
                gLbl.setFontScale(0.75f);
                gLbl.setColor(Color.CYAN);
                rewardTable.add(gLbl).padRight(6);

                if (diamondRegion != null) {
                    Image gImg = new Image(diamondRegion);
                    gImg.setScaling(Scaling.fit);
                    rewardTable.add(gImg).size(36, 36).padRight(14);
                }
            }

            if (quest.getRewardSeedPackets() > 0) {
                String plantTarget = quest.getRewardSeedPlantType() != null ? " " + quest.getRewardSeedPlantType() : "";
                Label seedLbl = new Label("+" + quest.getRewardSeedPackets() + plantTarget, skin, "big");
                seedLbl.setFontScale(0.72f);
                seedLbl.setColor(Color.GREEN);
                rewardTable.add(seedLbl).padRight(6);

                if (seedPacketIconRegion != null) {
                    Image seedImg = new Image(seedPacketIconRegion);
                    seedImg.setScaling(Scaling.fit);
                    rewardTable.add(seedImg).size(34, 34).padRight(14);
                }
            }

            if (quest.getRewardUnlockable() != null && !quest.getRewardUnlockable().isEmpty()) {
                Label unlockLbl = new Label("Unlock: " + quest.getRewardUnlockable(), skin, "big");
                unlockLbl.setFontScale(0.72f);
                unlockLbl.setColor(Color.ORANGE);
                rewardTable.add(unlockLbl).padRight(14);
            }

            inner.add(rewardTable).padRight(20);

            if (quest.getStatus() == Quest.QuestStatus.COMPLETED) {
                TextButton claimBtn = new TextButton("CLAIM", skin, "green");
                claimBtn.getLabel().setFontScale(1.05f);
                claimBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        User user = UserSession.getCurrentUser();
                        if (user != null) {
                            quest.applyReward(user);
                            quest.setStatus(Quest.QuestStatus.CLAIMED);
                            FileManager.updateUser(user);
                            Toast.show(stage, skin, "Reward Claimed!", false);
                            walletBar.updateValues();
                            rebuildQuests();
                        }
                    }
                });
                inner.add(claimBtn).size(150, 58).right();
            } else if (quest.getStatus() == Quest.QuestStatus.CLAIMED) {
                Label claimedLbl = new Label("CLAIMED", skin, "big");
                claimedLbl.setFontScale(0.72f);
                claimedLbl.setColor(Color.GRAY);
                inner.add(claimedLbl).size(150, 58).right().padRight(10);
            } else {
                TextButton playBtn = new TextButton("PLAY", skin, "purple");
                playBtn.getLabel().setFontScale(1.05f);
                playBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        AudioManager.getInstance().playButtonClick();
                        game.setScreen(new PlayScreen(game, controller, skin));
                        dispose();
                    }
                });
                inner.add(playBtn).size(150, 58).right();
            }

            add(inner);

            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        clearActions();
                        addAction(Actions.scaleTo(1.015f, 1.015f, 0.08f));
                    }
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        clearActions();
                        addAction(Actions.scaleTo(1f, 1f, 0.08f));
                    }
                }
            });
        }
    }

    private class FlagProgressBar extends Table {
        public FlagProgressBar(TextureRegion flagRegion, TextureRegion barRegion, int current, int target) {
            Stack barStack = new Stack();

            if (flagRegion != null) {
                Image flagBg = new Image(flagRegion);
                flagBg.setScaling(Scaling.stretch);
                barStack.add(flagBg);
            }

            Table innerContent = new Table();
            innerContent.padLeft(16).padRight(16);

            if (barRegion != null) {
                Image barImg = new Image(barRegion);
                barImg.setScaling(Scaling.stretch);
                barImg.setOrigin(Align.center);
                barImg.setRotation(-90f);
                innerContent.add(barImg).size(290, 20).padRight(12);
            }

            Label progressLbl = new Label(current + " / " + target, skin, "default");
            progressLbl.setFontScale(0.95f);
            progressLbl.setColor(Color.WHITE);
            innerContent.add(progressLbl).center();

            barStack.add(innerContent);

            add(barStack).size(420, 42);
        }
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
