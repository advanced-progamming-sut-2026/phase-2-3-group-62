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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.user.SecurityQuestions;
import util.ParsedCommand;

public class RegisterScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private Texture roundedBrownBgTexture;

    private Table root;
    private TextField usernameField;
    private TextField nicknameField;
    private TextField emailField;
    private SelectBox<String> genderBox;
    private TextField passwordField;
    private TextField confirmPasswordField;
    private SelectBox<String> questionBox;
    private TextField answerField;
    private TextField confirmAnswerField;
    private Label statusLabel;
    private boolean isTransitioning = false;

    public RegisterScreen(Maini game, MenuController controller, Skin skin) {
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

        roundedBrownBgTexture = createRoundedRectangleTexture(820, 680, 24, new Color(0.18f, 0.11f, 0.06f, 0.96f));

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
        formTable.top().pad(24, 36, 24, 36);

        Label titleLabel = new Label("REGISTER", skin, "big_outline");
        titleLabel.setFontScale(1.35f);
        formTable.add(titleLabel).padBottom(6).colspan(2).center().row();

        statusLabel = new Label("", skin, "big");
        statusLabel.setFontScale(0.8f);
        statusLabel.setColor(Color.RED);
        formTable.add(statusLabel).padBottom(10).colspan(2).center().row();

        usernameField = new TextField("", skin);
        nicknameField = new TextField("", skin);
        emailField = new TextField("", skin);

        genderBox = new SelectBox<>(skin);
        genderBox.setItems("Male", "Female");

        passwordField = new TextField("", skin);
        passwordField.setPasswordMode(true);
        passwordField.setPasswordCharacter('*');

        confirmPasswordField = new TextField("", skin);
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');

        questionBox = new SelectBox<>(skin);
        questionBox.setItems(SecurityQuestions.getAll().toArray(new String[0]));

        answerField = new TextField("", skin);
        confirmAnswerField = new TextField("", skin);

        addFormField(formTable, "Username:", usernameField);
        addFormField(formTable, "Nickname:", nicknameField);
        addFormField(formTable, "Email:", emailField);
        addFormField(formTable, "Gender:", genderBox);
        addFormField(formTable, "Password:", passwordField);
        addFormField(formTable, "Confirm Pass:", confirmPasswordField);
        addFormField(formTable, "Security Q:", questionBox);
        addFormField(formTable, "Answer:", answerField);
        addFormField(formTable, "Confirm Ans:", confirmAnswerField);

        TextButton registerBtn = new TextButton("Submit", skin, "green");
        registerBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(registerBtn, 1.06f);
        registerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleRegister();
            }
        });

        TextButton backBtn = new TextButton("Back to Login", skin, "brown");
        backBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                changeScreenWithTransition(new LoginScreen(game, controller, skin));
            }
        });

        Table btnTable = new Table();
        btnTable.add(registerBtn).size(150, 46).padRight(16);
        btnTable.add(backBtn).size(160, 46);

        formTable.add(btnTable).colspan(2).padTop(14).center().row();

        boxStack.add(formTable);
        centerWrapper.add(boxStack).size(820, 680);
        root.add(centerWrapper).expand().center();

        root.addAction(Actions.parallel(
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.sineOut),
            Actions.fadeIn(0.18f, Interpolation.fade)
        ));
    }

    private void addFormField(Table table, String labelText, Actor fieldActor) {
        Label label = new Label(labelText, skin, "big");
        label.setFontScale(0.82f);
        label.setColor(Color.YELLOW);
        table.add(label).right().padRight(12).padBottom(6);
        table.add(fieldActor).width(360).height(38).left().padBottom(6).row();
    }

    private void handleRegister() {
        String u = usernameField.getText().trim();
        String n = nicknameField.getText().trim();
        String e = emailField.getText().trim();
        String g = genderBox.getSelected().toLowerCase();
        String p1 = passwordField.getText().trim();
        String p2 = confirmPasswordField.getText().trim();
        int qIndex = questionBox.getSelectedIndex() + 1;
        String a1 = answerField.getText().trim();
        String a2 = confirmAnswerField.getText().trim();

        if (u.isEmpty() || n.isEmpty() || e.isEmpty() || p1.isEmpty() || p2.isEmpty() || a1.isEmpty() || a2.isEmpty()) {
            statusLabel.setColor(Color.RED);
            statusLabel.setText("Please fill all fields.");
            return;
        }

        ParsedCommand cmd = new ParsedCommand("register");
        cmd.addArg("-u", u);
        cmd.addArg("-n", n);
        cmd.addArg("-e", e);
        cmd.addArg("-g", g);
        cmd.addArg("-p", p1 + " " + p2);
        cmd.addArg("-q", String.valueOf(qIndex));
        cmd.addArg("-a", a1);
        cmd.addArg("-c", a2);

        String result = controller.processRegister(cmd);

        if ("SUCCESS".equals(result)) {
            changeScreenWithTransition(new LoginScreen(game, controller, skin));
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText(result);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.07f, 0.09f, 1f);

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
