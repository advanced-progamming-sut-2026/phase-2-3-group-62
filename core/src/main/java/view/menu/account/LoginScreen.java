package view.menu.account;

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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import network.NetworkManager;
import util.ParsedCommand;
import view.menu.mainMenu.MainMenuScreen;

public class LoginScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private Texture roundedBrownBgTexture;

    private Table root;
    private TextField usernameField;
    private TextField passwordField;
    private CheckBox stayLoggedInBox;
    private Label statusLabel;
    private Label networkStatusLabel;
    private float networkCheckTimer = 0f;
    private boolean isTransitioning = false;

    public LoginScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_STORE_MINISTORE_BG");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_MAINMENU_BACKGROUND");
            if (bgRegion == null) {
                bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
            }
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(640, 530, 24, new Color(0.18f, 0.11f, 0.06f, 0.96f));

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

    private void buildUI() {
        stage.clear();

        root = new Table();
        root.setFillParent(true);
        root.setTransform(true);
        root.setOrigin(Align.center);
        root.setScale(0.96f);
        root.setColor(1f, 1f, 1f, 0f);
        stage.addActor(root);

        Table centerWrapper = new Table();
        centerWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        Table formTable = new Table();
        formTable.top().pad(25, 40, 25, 40);

        Label titleLabel = new Label("LOGIN", skin, "big_outline");
        titleLabel.setFontScale(1.35f);
        formTable.add(titleLabel).padBottom(4).colspan(2).center().row();

        boolean isOnline = NetworkManager.getInstance().isConnected();
        networkStatusLabel = new Label(isOnline ? "● Server Online" : "● Server Offline", skin);
        networkStatusLabel.setFontScale(0.85f);
        networkStatusLabel.setColor(isOnline ? Color.GREEN : Color.RED);
        formTable.add(networkStatusLabel).padBottom(8).colspan(2).center().row();

        statusLabel = new Label("", skin, "big");
        statusLabel.setFontScale(0.8f);
        statusLabel.setColor(Color.RED);
        formTable.add(statusLabel).padBottom(10).colspan(2).center().row();

        Label userLbl = new Label("Username:", skin, "big");
        userLbl.setFontScale(0.85f);
        userLbl.setColor(Color.YELLOW);
        usernameField = new TextField("", skin);
        formTable.add(userLbl).right().padRight(12).padBottom(10);
        formTable.add(usernameField).width(260).height(42).left().padBottom(10).row();

        Label passLbl = new Label("Password:", skin, "big");
        passLbl.setFontScale(0.85f);
        passLbl.setColor(Color.YELLOW);
        passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');
        formTable.add(passLbl).right().padRight(12).padBottom(10);
        formTable.add(passwordField).width(260).height(42).left().padBottom(10).row();

        stayLoggedInBox = new CheckBox(" Stay logged in", skin);
        stayLoggedInBox.getLabel().setFontScale(0.85f);
        formTable.add(stayLoggedInBox).colspan(2).center().padTop(4).padBottom(18).row();

        TextButton loginBtn = new TextButton("Login", skin, "green");
        loginBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(loginBtn, 1.06f);
        loginBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleLogin();
            }
        });

        TextButton registerBtn = new TextButton("Register", skin, "brown");
        registerBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(registerBtn, 1.06f);
        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changeScreenWithTransition(new RegisterScreen(game, controller, skin));
            }
        });

        TextButton forgotBtn = new TextButton("Forgot Password?", skin);
        forgotBtn.getLabel().setFontScale(0.9f);
        attachHoverEffect(forgotBtn, 1.06f);
        forgotBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changeScreenWithTransition(new ForgotPasswordScreen(game, controller, skin));
            }
        });

        Table btnRow1 = new Table();
        btnRow1.add(loginBtn).size(150, 48).padRight(15);
        btnRow1.add(registerBtn).size(150, 48);
        formTable.add(btnRow1).colspan(2).center().padBottom(12).row();

        formTable.add(forgotBtn).colspan(2).size(200, 38).center().row();

        boxStack.add(formTable);
        centerWrapper.add(boxStack).size(640, 530);
        root.add(centerWrapper).expand().center();

        root.addAction(Actions.parallel(
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.sineOut),
            Actions.fadeIn(0.18f, Interpolation.fade)
        ));
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setColor(Color.RED);
            statusLabel.setText("Please fill all fields.");
            return;
        }

        ParsedCommand cmd = new ParsedCommand("login");
        cmd.addArg("-u", username);
        cmd.addArg("-p", password);
        if (stayLoggedInBox.isChecked()) {
            cmd.addArg("-stay-logged-in", "true");
        }

        String result = controller.processLogin(cmd);

        if ("Login successful!".equals(result)) {
            statusLabel.setColor(Color.GREEN);
            statusLabel.setText("Login successful!");
            changeScreenWithTransition(new MainMenuScreen(game, controller, skin));
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText(result);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.07f, 0.09f, 1f);

        networkCheckTimer += delta;
        if (networkCheckTimer >= 1.0f) {
            networkCheckTimer = 0f;
            if (networkStatusLabel != null) {
                boolean online = NetworkManager.getInstance().isConnected();
                networkStatusLabel.setText(online ? "● Server Online" : "● Server Offline");
                networkStatusLabel.setColor(online ? Color.GREEN : Color.RED);
            }
        }

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        batch.setColor(0.38f, 0.38f, 0.38f, 1f);
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            float bgW = bgRegion.getRegionWidth();
            float bgH = bgRegion.getRegionHeight();

            float zoomFactor = 1.15f;
            float scale = Math.max(worldW / bgW, worldH / bgH) * zoomFactor;
            float drawW = bgW * scale;
            float drawH = bgH * scale;
            float drawX = (worldW - drawW) / 2f;
            float drawY = (worldH - drawH) / 2f;

            batch.draw(bgRegion, drawX, drawY, drawW, drawH);
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
