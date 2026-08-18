package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.game.ChapterType;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.ui.CheatWidget;
import view.ui.PamActor;
import view.ui.WalletBar;

public class PlayScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private WalletBar walletBar;

    private Table root;
    private boolean isTransitioning = false;

    public PlayScreen(Maini game, MenuController controller, Skin skin) {
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
        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        buildUI();
    }

    private void changeScreenWithTransition(Screen nextScreen) {
        if (isTransitioning) return;
        isTransitioning = true;
        stage.getRoot().setTouchable(Touchable.disabled);

        if (root != null) {
            root.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.scaleTo(0.96f, 0.96f, 0.16f, Interpolation.sineIn),
                    Actions.fadeOut(0.15f, Interpolation.fade)
                ),
                Actions.run(() -> {
                    game.setScreen(nextScreen);
                    dispose();
                })
            ));
        }
    }

    private void attachHoverEffect(Actor actor, float targetScale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(targetScale, targetScale, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
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

    private Table createChapterCard(String title, String pamPath, String clipName, float pamScale, float offX, float offY, Runnable onSelect) {
        Table card = new Table();
        card.setTransform(true);
        card.setTouchable(Touchable.enabled);

        PamActor pamActor = new PamActor(pamPlayer, pamPath, clipName, pamScale, offX, offY);
        pamActor.setSize(140, 140);
        pamActor.setTouchable(Touchable.disabled);

        Table nameBadge = new Table();
        nameBadge.setTouchable(Touchable.disabled);
        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        if (badgeRegion != null) {
            nameBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        }

        Label nameLabel = new Label(title, skin, "big");
        nameLabel.setFontScale(0.78f);
        nameLabel.setColor(new Color(0.9f, 0.95f, 1f, 1f));
        nameLabel.setTouchable(Touchable.disabled);
        nameBadge.add(nameLabel).padLeft(14).padRight(14).center();

        card.add(pamActor).size(140, 140).center().row();
        card.add(nameBadge).height(46).width(210).padTop(14).center();
        card.pack();
        card.setOrigin(Align.center);

        card.addListener(new InputListener() {
            private float defaultY = -1;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    if (fromActor != null && (fromActor == card || card.isAscendantOf(fromActor))) {
                        return;
                    }
                    if (defaultY == -1) {
                        defaultY = card.getY();
                    }
                    pamActor.setHovered(true);
                    nameLabel.setColor(Color.YELLOW);
                    card.clearActions();
                    card.addAction(Actions.parallel(
                        Actions.scaleTo(1.08f, 1.08f, 0.1f),
                        Actions.moveTo(card.getX(), defaultY + 12f, 0.1f)
                    ));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    if (toActor != null && (toActor == card || card.isAscendantOf(toActor))) {
                        return;
                    }
                    if (defaultY != -1) {
                        pamActor.setHovered(false);
                        nameLabel.setColor(new Color(0.9f, 0.95f, 1f, 1f));
                        card.clearActions();
                        card.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.1f),
                            Actions.moveTo(card.getX(), defaultY, 0.1f)
                        ));
                    }
                }
            }
        });

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                if (onSelect != null && !isTransitioning) {
                    onSelect.run();
                }
            }
        });

        return card;
    }

    private void buildUI() {
        root = new Table();
        root.setFillParent(true);
        root.top();
        root.setTransform(true);
        root.setOrigin(Align.center);
        root.setScale(0.96f);
        root.setColor(1f, 1f, 1f, 0f);
        stage.addActor(root);

        Table topRow = new Table();
        topRow.left();

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                changeScreenWithTransition(new MainMenuScreen(game, controller, skin));
            }
        });
        topRow.add(backBtn).size(100, 42).left();

        CheatWidget cheatWidget = new CheatWidget(skin, stage, CheatWidget.Context.MENU, null, () -> {
            if (walletBar != null) walletBar.updateBalances();
        });
        topRow.add(cheatWidget).padLeft(20);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).expandX().right();
        root.add(topRow).fillX().pad(15).row();

        Label selectTitle = new Label("Select Chapter", skin, "big");
        selectTitle.setFontScale(1.3f);
        root.add(selectTitle).padTop(0).padBottom(15).center().row();

        Table chaptersRow = new Table();

        Table egyptCard = createChapterCard(
            "Ancient Egypt",
            "768/INITIAL/WORLDMAP/ZOMBOSS_NODE_EGYPT/ZOMBOSS_NODE_EGYPT.PAM",
            null,
            0.26f,
            0f,
            0f,
            () -> changeScreenWithTransition(new LevelSelectScreen(game, controller, skin, ChapterType.ANCIENT_EGYPT))
        );
        chaptersRow.add(egyptCard).width(210).padLeft(55).padRight(55);

        Table iceAgeCard = createChapterCard(
            "Frostbite Caves",
            "768/FULL/WORLDMAP/ZOMBOSS_NODE_ICEAGE/ZOMBOSS_NODE_ICEAGE.PAM",
            null,
            0.26f,
            0f,
            0f,
            () -> changeScreenWithTransition(new LevelSelectScreen(game, controller, skin, ChapterType.FROSTBITE_CAVES))
        );
        chaptersRow.add(iceAgeCard).width(210).padLeft(55).padRight(55);

        Table darkAgesCard = createChapterCard(
            "Dark Ages",
            "768/FULL/WORLDMAP/DARK/ANIM1/ANIM1.PAM",
            null,
            0.26f,
            0f,
            0f,
            () -> changeScreenWithTransition(new LevelSelectScreen(game, controller, skin, ChapterType.DARK_AGES))
        );
        chaptersRow.add(darkAgesCard).width(210).padLeft(55).padRight(55);

        Table beachCard = createChapterCard(
            "Big Wave Beach",
            "768/FULL/WORLDMAP/BEACH/ANIM27/ANIM27.PAM",
            null,
            0.23f,
            -1300f,
            1260f,
            () -> changeScreenWithTransition(new LevelSelectScreen(game, controller, skin, ChapterType.BIG_WAVE_BEACH))
        );
        chaptersRow.add(beachCard).width(210).padLeft(55).padRight(55);

        root.add(chaptersRow).expand().center().padBottom(15).row();

        Table bottomRow = new Table();

        Table bottomLeft = new Table();
        Table minigamesBtn = createNavButton(
            "IMAGE_UI_GENERIC_BUTTON_HUD_MINIGAMES_ALT_SELECTED",
            "Minigames",
            68,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                }
            }
        );
        bottomLeft.add(minigamesBtn).padRight(16);

        Table zenGardenBtn = createNavButton(
            "IMAGE_UI_GENERIC_BUTTONS_HUD_ZG_NORMAL",
            "Zen Garden",
            68,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    changeScreenWithTransition(new GreenhouseScreen(game, controller, skin));
                }
            }
        );
        bottomLeft.add(zenGardenBtn);
        bottomRow.add(bottomLeft).left().expandX().pad(15);

        Table bottomRight = new Table();
        Table travelLogBtn = createNavButton(
            "IMAGE_UI_QUESTS_QUEST_ICON_BROWN",
            "Travel Log",
            68,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    changeScreenWithTransition(new QuestScreen(game, controller, skin));
                }
            }
        );
        bottomRight.add(travelLogBtn).padRight(16);

        Table collectionBtn = createNavButton(
            "IMAGE_UI_GENERIC_BUTTON_HUD_MINIGAMES_SELECTED",
            "Collection",
            68,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    changeScreenWithTransition(new CollectionScreen(game, controller, skin));
                }
            }
        );
        bottomRight.add(collectionBtn);
        bottomRow.add(bottomRight).right().pad(15);

        root.add(bottomRow).fillX().bottom();

        root.addAction(Actions.parallel(
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.sineOut),
            Actions.fadeIn(0.18f, Interpolation.fade)
        ));
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
