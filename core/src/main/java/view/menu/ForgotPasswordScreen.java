package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import controller.menu.MenuController;
import main.Maini;
import model.user.SecurityQuestions;
import model.user.User;
import util.FileManager;
import util.ParsedCommand;

public class ForgotPasswordScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private Stage stage;

    private Table rootTable;
    private Label statusLabel;
    private int currentStep = 1;
    private String verifiedUsername = null;

    private TextField usernameField;
    private TextField emailField;
    private TextField answerField;
    private TextField newPasswordField;
    private TextField confirmPasswordField;

    public ForgotPasswordScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller;
        this.skin = skin;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        rootTable = new Table();
        rootTable.setFillParent(true);
        stage.addActor(rootTable);

        renderCurrentStep();
    }

    private void renderCurrentStep() {
        rootTable.clear();

        Label titleLabel = new Label("Forgot Password", skin, "big");
        statusLabel = new Label("", skin);
        statusLabel.setColor(Color.RED);

        rootTable.add(titleLabel).padBottom(15).colspan(2).row();
        rootTable.add(statusLabel).padBottom(10).colspan(2).row();

        if (currentStep == 1) {
            renderStep1();
        } else if (currentStep == 2) {
            renderStep2();
        } else if (currentStep == 3) {
            renderStep3();
        }
    }

    private void renderStep1() {
        usernameField = new TextField("", skin);
        emailField = new TextField("", skin);

        rootTable.add(new Label("Username:", skin)).right().pad(5);
        rootTable.add(usernameField).width(240).pad(5).row();

        rootTable.add(new Label("Email:", skin)).right().pad(5);
        rootTable.add(emailField).width(240).pad(5).row();

        Table btnTable = new Table();
        TextButton nextBtn = new TextButton("Next", skin);
        nextBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleStep1Submit();
            }
        });

        TextButton backBtn = new TextButton("Back to Login", skin);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoginScreen(game, controller, skin));
                dispose();
            }
        });

        btnTable.add(nextBtn).width(120).pad(5);
        btnTable.add(backBtn).width(140).pad(5);
        rootTable.add(btnTable).colspan(2).padTop(15).row();
    }

    private void handleStep1Submit() {
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            statusLabel.setColor(Color.RED);
            statusLabel.setText("Please enter both username and email.");
            return;
        }

        ParsedCommand cmd = new ParsedCommand("forget password");
        cmd.addArg("-u", username);
        cmd.addArg("-e", email);

        String result = controller.processForgetPassword(cmd);

        if (result.contains("SUCCESS")) {
            verifiedUsername = username;
            currentStep = 2;
            renderCurrentStep();
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText(result);
        }
    }

    private void renderStep2() {
        String questionText = "Security Question";
        User user = FileManager.getUser(verifiedUsername);
        if (user != null) {
            try {
                int qIdx = Integer.parseInt(user.getSecurityQuestion()) - 1;
                questionText = SecurityQuestions.getQuestionByIndex(qIdx);
            } catch (Exception ignored) {}
        }

        Label questionLabel = new Label(questionText, skin);
        questionLabel.setWrap(true);
        answerField = new TextField("", skin);

        rootTable.add(questionLabel).width(350).colspan(2).padBottom(15).center().row();

        rootTable.add(new Label("Your Answer:", skin)).right().pad(5);
        rootTable.add(answerField).width(240).pad(5).row();

        Table btnTable = new Table();
        TextButton submitAnswerBtn = new TextButton("Verify Answer", skin);
        submitAnswerBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleStep2Submit();
            }
        });

        TextButton cancelBtn = new TextButton("Cancel", skin);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new LoginScreen(game, controller, skin));
                dispose();
            }
        });

        btnTable.add(submitAnswerBtn).width(150).pad(5);
        btnTable.add(cancelBtn).width(120).pad(5);
        rootTable.add(btnTable).colspan(2).padTop(15).row();
    }

    private void handleStep2Submit() {
        String answer = answerField.getText().trim();
        if (answer.isEmpty()) {
            statusLabel.setColor(Color.RED);
            statusLabel.setText("Please enter your answer.");
            return;
        }

        ParsedCommand cmd = new ParsedCommand("answer");
        cmd.addArg("-a", answer);

        String result = controller.processForgetPassword(cmd);

        if (result.contains("SUCCESS")) {
            currentStep = 3;
            renderCurrentStep();
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText(result);
        }
    }

    private void renderStep3() {
        newPasswordField = new TextField("", skin);
        confirmPasswordField = new TextField("", skin);
        newPasswordField.setPasswordMode(true);
        newPasswordField.setPasswordCharacter('*');
        confirmPasswordField.setPasswordMode(true);
        confirmPasswordField.setPasswordCharacter('*');

        rootTable.add(new Label("New Password:", skin)).right().pad(5);
        rootTable.add(newPasswordField).width(240).pad(5).row();

        rootTable.add(new Label("Confirm Password:", skin)).right().pad(5);
        rootTable.add(confirmPasswordField).width(240).pad(5).row();

        Table btnTable = new Table();
        TextButton changePasswordBtn = new TextButton("Change Password", skin);
        changePasswordBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                handleStep3Submit();
            }
        });

        btnTable.add(changePasswordBtn).width(180).pad(5);
        rootTable.add(btnTable).colspan(2).padTop(15).row();
    }

    private void handleStep3Submit() {
        String pass1 = newPasswordField.getText().trim();
        String pass2 = confirmPasswordField.getText().trim();

        if (pass1.isEmpty() || pass2.isEmpty()) {
            statusLabel.setColor(Color.RED);
            statusLabel.setText("Please enter and confirm your new password.");
            return;
        }

        ParsedCommand cmd = new ParsedCommand("new password");
        cmd.addArg("-p", pass1);
        cmd.addArg("-c", pass2);

        String result = controller.processForgetPassword(cmd);

        if (result.contains("SUCCESS")) {
            game.setScreen(new LoginScreen(game, controller, skin));
            dispose();
        } else {
            statusLabel.setColor(Color.RED);
            statusLabel.setText(result);
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.12f, 0.12f, 0.15f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
