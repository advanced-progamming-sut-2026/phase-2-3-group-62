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
import model.user.User;
import model.user.UserSession;
import util.ParsedCommand;
import view.ui.Toast;
import view.ui.WalletBar;

public class ProfileScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private Texture roundedBrownBgTexture;
    private Texture modalBgTexture;

    private Table root;
    private Table statsGrid;
    private Table editModalContainer;
    private boolean isEditOpen = false;

    public ProfileScreen(Maini game, MenuController controller, Skin skin) {
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
            bgRegion = game.getTextureBank().region("IMAGE_UI_THYMED_EVENTS_SPRINGENING_EVENT_BG");
            if (bgRegion == null) {
                bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
            }
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(1120, 520, 24, new Color(0.18f, 0.11f, 0.06f, 0.96f));
        modalBgTexture = createRoundedRectangleTexture(960, 260, 18, new Color(0.12f, 0.08f, 0.04f, 0.98f));

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

        root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topBar = new Table();
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.2f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, controller, skin));
                dispose();
            }
        });
        topBar.add(backBtn).size(130, 50).left();

        Label title = new Label("USER PROFILE", skin, "big_outline");
        title.setFontScale(1.35f);
        topBar.add(title).expandX().center();

        WalletBar walletBar = new WalletBar(game, skin);
        topBar.add(walletBar).right();

        root.add(topBar).fillX().pad(12, 30, 8, 30).row();

        Table centerWrapper = new Table();
        centerWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        Table contentTable = new Table();
        contentTable.top().pad(22, 30, 20, 30);

        statsGrid = new Table();
        refreshStatsGrid();
        contentTable.add(statsGrid).expandX().fillX().padBottom(14).row();

        Table editActionTable = new Table();
        TextureRegion editBtnRegion = game.getTextureBank().region("IMAGE_UI_MAINMENU_EDIT_BTN_NORMAL");
        Button.ButtonStyle editStyle = new Button.ButtonStyle();
        if (editBtnRegion != null) {
            editStyle.up = new TextureRegionDrawable(editBtnRegion);
        }
        Button editBtn = new Button(editStyle);
        attachHoverEffect(editBtn, 1.12f);
        editBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleEditModal();
            }
        });

        editActionTable.add(editBtn).size(48, 48).padRight(10);
        Label editLabel = new Label("Edit Account Details", skin, "big");
        editLabel.setFontScale(0.9f);
        editLabel.setColor(Color.YELLOW);
        editActionTable.add(editLabel);

        contentTable.add(editActionTable).padBottom(10).center().row();

        editModalContainer = new Table();
        editModalContainer.setVisible(false);
        buildEditModal(editModalContainer);
        contentTable.add(editModalContainer).expandX().fillX().center().row();

        boxStack.add(contentTable);
        centerWrapper.add(boxStack).size(1120, 540);
        root.add(centerWrapper).expand().center().padBottom(15);
    }

    private void toggleEditModal() {
        isEditOpen = !isEditOpen;
        editModalContainer.setVisible(isEditOpen);
    }

    private void refreshStatsGrid() {
        statsGrid.clear();
        User user = UserSession.getCurrentUser();
        if (user == null) return;

        statsGrid.add(createStatCell("Username:", user.getUsername(), 160)).left().expandX().padBottom(12);
        statsGrid.add(createStatCell("Nickname:", user.getNickname(), 160)).left().expandX().padBottom(12).row();

        statsGrid.add(createStatCell("Email:", user.getEmail(), 160)).left().expandX().padBottom(12);
        statsGrid.add(createStatCell("Gender:", user.getGender().name(), 160)).left().expandX().padBottom(12).row();

        statsGrid.add(createStatCell("Total Score:", String.valueOf(user.getScore()), 160)).left().expandX().padBottom(12);
        String progressStr = "S" + user.getLastSeasonCompleted() + " - L" + user.getLastLevelCompleted();
        statsGrid.add(createStatCell("Progress:", progressStr, 160)).left().expandX().padBottom(12).row();

        statsGrid.add(createStatCell("Mini-games:", String.valueOf(user.getCompletedMiniGames()), 160)).left().expandX();
        statsGrid.add(createStatCell("High Score:", String.valueOf(user.getHighestScoreInScoringGame()), 160)).left().expandX().row();
    }

    private Table createStatCell(String title, String val, float titleBadgeWidth) {
        Table cell = new Table();

        Table titleBadge = new Table();
        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        if (badgeRegion != null) {
            titleBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        }

        Label titleLbl = new Label(title, skin, "big");
        titleLbl.setColor(Color.YELLOW);
        titleLbl.setFontScale(0.85f);
        titleBadge.add(titleLbl).padLeft(10).padRight(10).center();

        cell.add(titleBadge).height(42).width(titleBadgeWidth).padRight(14).left();

        Label valLbl = new Label(val, skin, "big");
        valLbl.setColor(Color.WHITE);

        if (val.length() > 24) {
            valLbl.setFontScale(0.8f);
        } else if (val.length() > 16) {
            valLbl.setFontScale(0.9f);
        } else {
            valLbl.setFontScale(1.0f);
        }

        cell.add(valLbl).left().expandX();

        return cell;
    }

    private void buildEditModal(Table modal) {
        modal.clear();

        Stack modalStack = new Stack();
        if (modalBgTexture != null) {
            Image modalBg = new Image(modalBgTexture);
            modalBg.setScaling(Scaling.stretch);
            modalStack.add(modalBg);
        }

        Table innerBox = new Table();
        innerBox.top().pad(14, 20, 14, 20);

        TextField changeUserField = new TextField("", skin);
        TextButton changeUserBtn = new TextButton("Update", skin, "green");
        changeUserBtn.getLabel().setFontScale(0.85f);
        attachHoverEffect(changeUserBtn, 1.06f);
        changeUserBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String val = changeUserField.getText().trim();
                if (val.isEmpty()) {
                    Toast.show(stage, skin, "Username cannot be empty.", true);
                    return;
                }
                ParsedCommand cmd = new ParsedCommand("change-username");
                cmd.addArg("-u", val);
                String res = controller.processProfile(cmd, "change-username");
                boolean isError = !res.toLowerCase().contains("successfully");
                Toast.show(stage, skin, res, isError);
                if (!isError) {
                    changeUserField.setText("");
                    refreshStatsGrid();
                }
            }
        });
        Label lblUser = new Label("New User:", skin, "big");
        lblUser.setFontScale(0.8f);
        innerBox.add(lblUser).right().padRight(6);
        innerBox.add(changeUserField).width(160).height(38).padRight(8);
        innerBox.add(changeUserBtn).size(90, 38).padRight(20);

        TextField changeNickField = new TextField("", skin);
        TextButton changeNickBtn = new TextButton("Update", skin, "green");
        changeNickBtn.getLabel().setFontScale(0.85f);
        attachHoverEffect(changeNickBtn, 1.06f);
        changeNickBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String val = changeNickField.getText().trim();
                if (val.isEmpty()) {
                    Toast.show(stage, skin, "Nickname cannot be empty.", true);
                    return;
                }
                ParsedCommand cmd = new ParsedCommand("change-nickname");
                cmd.addArg("-n", val);
                String res = controller.processProfile(cmd, "change-nickname");
                boolean isError = !res.toLowerCase().contains("successfully");
                Toast.show(stage, skin, res, isError);
                if (!isError) {
                    changeNickField.setText("");
                    refreshStatsGrid();
                }
            }
        });
        Label lblNick = new Label("New Nick:", skin, "big");
        lblNick.setFontScale(0.8f);
        innerBox.add(lblNick).right().padRight(6);
        innerBox.add(changeNickField).width(160).height(38).padRight(8);
        innerBox.add(changeNickBtn).size(90, 38).row();

        TextField changeEmailField = new TextField("", skin);
        TextButton changeEmailBtn = new TextButton("Update", skin, "green");
        changeEmailBtn.getLabel().setFontScale(0.85f);
        attachHoverEffect(changeEmailBtn, 1.06f);
        changeEmailBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String val = changeEmailField.getText().trim();
                if (val.isEmpty()) {
                    Toast.show(stage, skin, "Email cannot be empty.", true);
                    return;
                }
                ParsedCommand cmd = new ParsedCommand("change-email");
                cmd.addArg("-e", val);
                String res = controller.processProfile(cmd, "change-email");
                boolean isError = !res.toLowerCase().contains("successfully");
                Toast.show(stage, skin, res, isError);
                if (!isError) {
                    changeEmailField.setText("");
                    refreshStatsGrid();
                }
            }
        });
        Label lblEmail = new Label("New Email:", skin, "big");
        lblEmail.setFontScale(0.8f);
        innerBox.add(lblEmail).right().padTop(8).padRight(6);
        innerBox.add(changeEmailField).width(160).height(38).padTop(8).padRight(8);
        innerBox.add(changeEmailBtn).size(90, 38).padTop(8).padRight(20);

        TextField oldPassField = new TextField("", skin);
        oldPassField.setPasswordMode(true);
        oldPassField.setPasswordCharacter('*');

        TextField newPassField = new TextField("", skin);
        newPassField.setPasswordMode(true);
        newPassField.setPasswordCharacter('*');

        TextButton changePassBtn = new TextButton("Change Pass", skin, "brown");
        changePassBtn.getLabel().setFontScale(0.82f);
        attachHoverEffect(changePassBtn, 1.06f);
        changePassBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String oldP = oldPassField.getText().trim();
                String newP = newPassField.getText().trim();
                if (oldP.isEmpty() || newP.isEmpty()) {
                    Toast.show(stage, skin, "Both passwords are required.", true);
                    return;
                }
                ParsedCommand cmd = new ParsedCommand("change-password");
                cmd.addArg("-o", oldP);
                cmd.addArg("-p", newP);
                String res = controller.processProfile(cmd, "change-password");
                boolean isError = !res.toLowerCase().contains("successfully");
                Toast.show(stage, skin, res, isError);
                if (!isError) {
                    oldPassField.setText("");
                    newPassField.setText("");
                }
            }
        });

        Table passRow = new Table();
        Label lblOld = new Label("Old:", skin, "big");
        lblOld.setFontScale(0.78f);
        passRow.add(lblOld).padRight(4);
        passRow.add(oldPassField).width(100).height(38).padRight(8);
        Label lblNew = new Label("New:", skin, "big");
        lblNew.setFontScale(0.78f);
        passRow.add(lblNew).padRight(4);
        passRow.add(newPassField).width(100).height(38).padRight(8);
        passRow.add(changePassBtn).size(115, 38);

        innerBox.add(passRow).colspan(3).padTop(8).left().row();

        modalStack.add(innerBox);
        modal.add(modalStack).size(960, 160).center();
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
        if (modalBgTexture != null) modalBgTexture.dispose();
        if (stage != null) stage.dispose();
    }
}
