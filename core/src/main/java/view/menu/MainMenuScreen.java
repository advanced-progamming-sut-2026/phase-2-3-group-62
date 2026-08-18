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
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.user.NewsItem;
import model.user.User;
import model.user.UserSession;
import util.ParsedCommand;
import view.ui.WalletBar;

public class MainMenuScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final SpriteBatch batch;
    private final Skin skin;
    private Stage stage;

    private TextureRegion bgRegion;
    private Image logoImg;
    private TextButton playBtn;
    private Table topLeft;
    private WalletBar walletBar;
    private Table bottomLeft;
    private Table bottomRight;
    private boolean isTransitioning = false;

    public MainMenuScreen(Maini game) {
        this(game, game.getMenuController(), game.getSkin());
    }

    public MainMenuScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);
        view.audio.AudioManager.getInstance().playTitleMusic();
        bgRegion = game.getTextureBank().region("IMAGE_MAINMENU_BACKGROUND");
        buildUI();
    }

    private void changeScreenWithTransition(Screen nextScreen) {
        if (isTransitioning) return;
        isTransitioning = true;
        stage.getRoot().setTouchable(Touchable.disabled);

        if (logoImg != null) {
            logoImg.clearActions();
            logoImg.setOrigin(Align.center);
            logoImg.addAction(Actions.parallel(
                Actions.scaleTo(0.7f, 0.7f, 0.22f, Interpolation.sineIn),
                Actions.moveBy(0, 300f, 0.24f, Interpolation.pow2In),
                Actions.fadeOut(0.2f)
            ));
        }

        if (playBtn != null) {
            playBtn.setOrigin(Align.center);
            playBtn.addAction(Actions.sequence(
                Actions.scaleTo(1.15f, 0.85f, 0.08f),
                Actions.parallel(
                    Actions.scaleTo(0f, 0f, 0.2f, Interpolation.swingIn),
                    Actions.fadeOut(0.18f)
                )
            ));
        }

        if (topLeft != null) {
            topLeft.setOrigin(Align.center);
            topLeft.addAction(Actions.parallel(
                Actions.moveBy(-200f, 0, 0.22f, Interpolation.sineIn),
                Actions.fadeOut(0.18f)
            ));
        }

        if (walletBar != null) {
            walletBar.setOrigin(Align.center);
            walletBar.addAction(Actions.parallel(
                Actions.moveBy(200f, 0, 0.22f, Interpolation.sineIn),
                Actions.fadeOut(0.18f)
            ));
        }

        if (bottomLeft != null) {
            bottomLeft.setOrigin(Align.center);
            bottomLeft.addAction(Actions.parallel(
                Actions.moveBy(-150f, -150f, 0.22f, Interpolation.sineIn),
                Actions.fadeOut(0.18f)
            ));
        }

        if (bottomRight != null) {
            bottomRight.setOrigin(Align.center);
            bottomRight.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.moveBy(150f, -150f, 0.22f, Interpolation.sineIn),
                    Actions.fadeOut(0.18f)
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

    private Table createNamedIconButton(String bgRegionName, String iconRegionName, String labelText, float size, ClickListener listener) {
        TextureRegion bg = game.getTextureBank().region(bgRegionName);
        TextureRegion icon = iconRegionName != null ? game.getTextureBank().region(iconRegionName) : null;

        Button.ButtonStyle style = new Button.ButtonStyle();
        if (bg != null) {
            style.up = new TextureRegionDrawable(bg);
        }

        Button btn = new Button(style);
        if (icon != null) {
            Image iconImg = new Image(new TextureRegionDrawable(icon));
            iconImg.setScaling(Scaling.fit);
            btn.add(iconImg).size(size * 0.58f).center();
        }
        if (listener != null) {
            btn.addListener(listener);
        }

        attachHoverEffect(btn, 1.12f);

        Table container = new Table();
        container.add(btn).size(size, size).row();
        Label lbl = new Label(labelText, skin);
        container.add(lbl).padTop(4).center();

        return container;
    }

    private Button createTextFrameButton(String bgRegionName, String text, float width, float height, ClickListener listener) {
        TextureRegion bg = game.getTextureBank().region(bgRegionName);

        Button.ButtonStyle style = new Button.ButtonStyle();
        if (bg != null) {
            style.up = new TextureRegionDrawable(bg);
        }

        Button btn = new Button(style);
        Label lbl = new Label(text, skin);
        btn.add(lbl).center();

        if (listener != null) {
            btn.addListener(listener);
        }

        attachHoverEffect(btn, 1.1f);
        return btn;
    }

    private void buildUI() {
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        Table topRow = new Table();

        topLeft = new Table();
        Table profileContainer = createNamedIconButton(
            "IMAGE_UI_GENERIC_BLUEBUTTON_DOWN",
            "IMAGE_UI_MAINMENU_MM_PLAYERICON",
            "Profile",
            78,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    view.audio.AudioManager.getInstance().playButtonClick();
                    changeScreenWithTransition(new ProfileScreen(game, controller, skin));
                }
            }
        );
        topLeft.add(profileContainer).padRight(14).top();

        Button logoutBtn = createTextFrameButton(
            "IMAGE_UI_GENERIC_PURPLEBUTTON",
            "Logout",
            105,
            52,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    view.audio.AudioManager.getInstance().playButtonClick();
                    controller.processLogOut(new ParsedCommand("logout"));
                    changeScreenWithTransition(new LoginScreen(game, controller, skin));
                }
            }
        );
        topLeft.add(logoutBtn).size(105, 52).padTop(13).top();

        topRow.add(topLeft).left().expandX().pad(15);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).right().pad(15);
        root.add(topRow).fillX().top().row();

        Table centerArea = new Table();

        TextureRegion logoRegion = game.getTextureBank().region("IMAGE_UI_MAINMENU_PVZ2_LOGO_HORIZONTAL");
        if (logoRegion != null) {
            logoImg = new Image(new TextureRegionDrawable(logoRegion));
            logoImg.setScaling(Scaling.fit);
            centerArea.add(logoImg).size(520, 150).center().padBottom(25).row();
        }

        playBtn = new TextButton("Play", skin, "green");
        playBtn.getLabel().setFontScale(1.35f);
        playBtn.setTransform(true);
        playBtn.setOrigin(Align.center);
        attachHoverEffect(playBtn, 1.08f);
        playBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                view.audio.AudioManager.getInstance().playButtonClick();
                changeScreenWithTransition(new PlayScreen(game, controller, skin));
            }
        });
        centerArea.add(playBtn).width(360).height(85).center();

        root.add(centerArea).expand().center().padBottom(30).row();

        Table bottomRow = new Table();

        bottomLeft = new Table();
        Table leaderboardContainer = createNamedIconButton(
            "IMAGE_UI_GENERIC_GREENBUTTON",
            "IMAGE_UI_GAMECENTER_ICON",
            "Leaderboard",
            78,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    view.audio.AudioManager.getInstance().playButtonClick();
                    changeScreenWithTransition(new LeaderboardScreen(game, controller, skin));
                }
            }
        );
        bottomLeft.add(leaderboardContainer);
        bottomRow.add(bottomLeft).left().expandX().pad(15);

        bottomRight = new Table();
        User currentUser = UserSession.getCurrentUser();
        int unreadCount = 0;
        if (currentUser != null && currentUser.getNews() != null) {
            for (NewsItem item : currentUser.getNews()) {
                if (!item.isRead()) unreadCount++;
            }
        }

        TextureRegion newsBg = game.getTextureBank().region("IMAGE_UI_GENERIC_PURPLEBUTTON");
        TextureRegion newsIcon = game.getTextureBank().region("IMAGE_UI_MAINMENU_MM_NEWSICON");

        Button.ButtonStyle newsStyle = new Button.ButtonStyle();
        if (newsBg != null) {
            newsStyle.up = new TextureRegionDrawable(newsBg);
        }

        Button rawNewsBtn = new Button(newsStyle);
        if (newsIcon != null) {
            Image iconImg = new Image(new TextureRegionDrawable(newsIcon));
            iconImg.setScaling(Scaling.fit);
            rawNewsBtn.add(iconImg).size(78 * 0.58f).center();
        }
        rawNewsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                view.audio.AudioManager.getInstance().playButtonClick();
                changeScreenWithTransition(new NewsScreen(game, controller, skin));
            }
        });

        attachHoverEffect(rawNewsBtn, 1.12f);

        Stack newsStack = new Stack();
        newsStack.add(rawNewsBtn);

        if (unreadCount > 0) {
            Table badgeTable = new Table();
            badgeTable.top().right();
            Label badgeLabel = new Label("!", skin, "big");
            badgeLabel.setColor(Color.YELLOW);
            badgeTable.add(badgeLabel).padRight(6).padTop(2);
            newsStack.add(badgeTable);
        }

        Table newsContainer = new Table();
        newsContainer.add(newsStack).size(78, 78).row();
        Label newsLabel = new Label("News", skin);
        if (unreadCount > 0) {
            newsLabel.setColor(Color.YELLOW);
        }
        newsContainer.add(newsLabel).padTop(4).center();
        bottomRight.add(newsContainer).padRight(16);

        Table settingsContainer = createNamedIconButton(
            "IMAGE_UI_GENERIC_PURPLEBUTTON",
            "IMAGE_UI_MAINMENU_MM_SETTINGS",
            "Settings",
            78,
            new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    view.audio.AudioManager.getInstance().playButtonClick();
                    changeScreenWithTransition(new SettingScreen(game, controller, skin));
                }
            }
        );
        bottomRight.add(settingsContainer);
        bottomRow.add(bottomRight).right().pad(15);

        root.add(bottomRow).fillX().bottom();

        if (logoImg != null) {
            logoImg.setOrigin(Align.center);
            logoImg.setScale(0.85f);
            logoImg.setColor(1f, 1f, 1f, 0f);
            logoImg.addAction(Actions.sequence(
                Actions.moveBy(0, 360f),
                Actions.parallel(
                    Actions.moveBy(0, -360f, 0.65f, Interpolation.bounceOut),
                    Actions.scaleTo(1f, 1f, 0.65f, Interpolation.bounceOut),
                    Actions.fadeIn(0.2f)
                ),
                Actions.forever(
                    Actions.sequence(
                        Actions.scaleTo(1.03f, 0.97f, 1.2f, Interpolation.sine),
                        Actions.scaleTo(0.97f, 1.03f, 1.2f, Interpolation.sine)
                    )
                )
            ));
        }

        playBtn.setScale(0.1f, 0.1f);
        playBtn.setColor(1f, 1f, 1f, 0f);
        playBtn.addAction(Actions.sequence(
            Actions.delay(0.25f),
            Actions.parallel(
                Actions.scaleTo(1f, 1f, 0.45f, Interpolation.swingOut),
                Actions.fadeIn(0.15f)
            ),
            Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.05f, 1.05f, 0.8f, Interpolation.sine),
                    Actions.scaleTo(1f, 1f, 0.8f, Interpolation.sine)
                )
            )
        ));

        topLeft.setTransform(true);
        topLeft.setColor(1f, 1f, 1f, 0f);
        topLeft.addAction(Actions.sequence(
            Actions.moveBy(-180f, 0),
            Actions.delay(0.12f),
            Actions.parallel(
                Actions.moveBy(180f, 0, 0.45f, Interpolation.swingOut),
                Actions.fadeIn(0.25f)
            )
        ));

        walletBar.setTransform(true);
        walletBar.setColor(1f, 1f, 1f, 0f);
        walletBar.addAction(Actions.sequence(
            Actions.moveBy(180f, 0),
            Actions.delay(0.12f),
            Actions.parallel(
                Actions.moveBy(-180f, 0, 0.45f, Interpolation.swingOut),
                Actions.fadeIn(0.25f)
            )
        ));

        bottomLeft.setTransform(true);
        bottomLeft.setColor(1f, 1f, 1f, 0f);
        bottomLeft.addAction(Actions.sequence(
            Actions.moveBy(-120f, -120f),
            Actions.delay(0.2f),
            Actions.parallel(
                Actions.moveBy(120f, 120f, 0.45f, Interpolation.swingOut),
                Actions.fadeIn(0.25f)
            )
        ));

        bottomRight.setTransform(true);
        bottomRight.setColor(1f, 1f, 1f, 0f);
        bottomRight.addAction(Actions.sequence(
            Actions.moveBy(120f, -120f),
            Actions.delay(0.2f),
            Actions.parallel(
                Actions.moveBy(-120f, 120f, 0.45f, Interpolation.swingOut),
                Actions.fadeIn(0.25f)
            )
        ));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        if (bgRegion != null) {
            batch.draw(bgRegion, 0, 0, stage.getWidth(), stage.getHeight());
        }
        batch.end();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        stage.dispose();
    }
}
