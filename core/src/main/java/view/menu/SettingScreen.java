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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.user.Settings;
import util.FileManager;
import util.ParsedCommand;
import view.audio.AudioManager;

public class SettingScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private Texture roundedBrownBgTexture;
    private Settings settings;

    public SettingScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        settings = FileManager.loadSettings();
        if (settings == null) {
            settings = new Settings();
        }

        bgRegion = game.getTextureBank().region("IMAGE_UI_STORE_MINISTORE_BG");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_UI_THYMED_EVENTS_SPRINGENING_EVENT_BG");
            if (bgRegion == null) {
                bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
            }
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(1120, 680, 28, new Color(0.18f, 0.11f, 0.06f, 0.96f));

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
        backBtn.getLabel().setFontScale(1.3f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                FileManager.saveSettings(settings);
                game.setScreen(new MainMenuScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(145, 58).left();

        Label title = new Label("SETTINGS", skin, "big_outline");
        title.setFontScale(1.45f);
        topRow.add(title).expandX().center().padRight(145);

        root.add(topRow).fillX().pad(12, 30, 4, 30).row();

        Table centerWrapper = new Table();
        centerWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        Table contentTable = new Table();
        contentTable.top().pad(28, 36, 28, 36);

        Table gridSetting = new Table();
        gridSetting.top().left();

        addSectionHeader(gridSetting, "GAMEPLAY SETTINGS");

        Table diffRow = new Table();
        Label diffLabel = createLabel("Difficulty (1-5):");
        diffRow.add(diffLabel).width(300).left();

        final TextButton[] diffButtons = new TextButton[5];
        for (int i = 1; i <= 5; i++) {
            final int d = i;
            final int index = i - 1;
            TextButton dBtn = new TextButton(String.valueOf(d), skin, (settings.getDifficulty() == d) ? "green" : "brown");
            dBtn.getLabel().setFontScale(0.9f);
            diffButtons[index] = dBtn;
            attachHoverEffect(dBtn, 1.08f);

            dBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    ParsedCommand cmd = new ParsedCommand("menu settings change-difficulty");
                    cmd.addArg("-l", String.valueOf(d));
                    controller.processSetting(cmd);
                    settings.setDifficulty(d);
                    FileManager.saveSettings(settings);

                    for (int j = 0; j < 5; j++) {
                        TextButton btn = diffButtons[j];
                        String styleName = (j == index) ? "green" : "brown";
                        TextButton.TextButtonStyle style = skin.has(styleName, TextButton.TextButtonStyle.class) ? skin.get(styleName, TextButton.TextButtonStyle.class) : skin.get(TextButton.TextButtonStyle.class);
                        btn.setStyle(style);
                    }
                }
            });
            diffRow.add(dBtn).size(85, 48).padRight(12);
        }
        gridSetting.add(diffRow).fillX().padBottom(16).row();

        Table speedRow = new Table();
        Label speedLabel = createLabel("Game Speed:");
        speedRow.add(speedLabel).width(300).left();

        final TextButton[] speedButtons = new TextButton[3];
        for (int i = 1; i <= 3; i++) {
            final int s = i;
            final int index = i - 1;
            TextButton sBtn = new TextButton(s + "x", skin, (settings.getGameSpeed() == s) ? "green" : "brown");
            sBtn.getLabel().setFontScale(0.9f);
            speedButtons[index] = sBtn;
            attachHoverEffect(sBtn, 1.08f);

            sBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    settings.setGameSpeed(s);
                    FileManager.saveSettings(settings);

                    for (int j = 0; j < 3; j++) {
                        TextButton btn = speedButtons[j];
                        String styleName = (j == index) ? "green" : "brown";
                        TextButton.TextButtonStyle style = skin.has(styleName, TextButton.TextButtonStyle.class) ? skin.get(styleName, TextButton.TextButtonStyle.class) : skin.get(TextButton.TextButtonStyle.class);
                        btn.setStyle(style);
                    }
                }
            });
            speedRow.add(sBtn).size(135, 48).padRight(14);
        }
        gridSetting.add(speedRow).fillX().padBottom(16).row();

        addSectionHeader(gridSetting, "AUDIO SETTINGS");

        Table musicRow = new Table();
        musicRow.add(createLabel("Music Volume:")).width(300).left();
        Slider musicSlider = new Slider(0f, 1f, 0.05f, false, skin);
        musicSlider.setValue(settings.getMusicVolume());
        Label musicVal = createValueLabel(String.format("%.0f%%", musicSlider.getValue() * 100));
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = musicSlider.getValue();
                settings.setMusicVolume(val);
                AudioManager.getInstance().setMusicVolume(val);
                musicVal.setText(String.format("%.0f%%", val * 100));
                FileManager.saveSettings(settings);
            }
        });
        musicRow.add(musicSlider).width(360).padRight(18);
        musicRow.add(musicVal).left();
        gridSetting.add(musicRow).fillX().padBottom(14).row();

        Table sfxRow = new Table();
        sfxRow.add(createLabel("SFX Volume:")).width(300).left();
        Slider sfxSlider = new Slider(0f, 1f, 0.05f, false, skin);
        sfxSlider.setValue(settings.getSfxVolume());
        Label sfxVal = createValueLabel(String.format("%.0f%%", sfxSlider.getValue() * 100));
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = sfxSlider.getValue();
                settings.setSfxVolume(val);
                AudioManager.getInstance().setSoundVolume(val);
                sfxVal.setText(String.format("%.0f%%", val * 100));
                FileManager.saveSettings(settings);
            }
        });
        sfxRow.add(sfxSlider).width(360).padRight(18);
        sfxRow.add(sfxVal).left();
        gridSetting.add(sfxRow).fillX().padBottom(16).row();

        addSectionHeader(gridSetting, "DEVELOPER & DEBUG");

        CheckBox gridBox = new CheckBox("  Show Tile Grid Lines", skin);
        gridBox.getLabel().setFontScale(0.88f);
        gridBox.setChecked(settings.isShowGrid());
        gridBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().playButtonClick();
                settings.setShowGrid(gridBox.isChecked());
                FileManager.saveSettings(settings);
            }
        });
        gridSetting.add(gridBox).left().padBottom(12).row();

        CheckBox debugBox = new CheckBox("  Enable In-Game Debug Mode (Cheats)", skin);
        debugBox.getLabel().setFontScale(0.88f);
        debugBox.setChecked(settings.isDebugMode());
        debugBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                AudioManager.getInstance().playButtonClick();
                settings.setDebugMode(debugBox.isChecked());
                FileManager.saveSettings(settings);
            }
        });
        gridSetting.add(debugBox).left().padBottom(16).row();

        contentTable.add(gridSetting).expand().fill();
        boxStack.add(contentTable);

        centerWrapper.add(boxStack).size(1120, 680);
        root.add(centerWrapper).expand().center().padBottom(14);
    }

    private void addSectionHeader(Table table, String text) {
        Label header = new Label(text, skin, "big");
        header.setFontScale(0.95f);
        header.setColor(Color.YELLOW);
        table.add(header).left().padTop(8).padBottom(10).row();
    }

    private Label createLabel(String text) {
        Label label = new Label(text, skin, "big");
        label.setFontScale(0.82f);
        label.setColor(Color.WHITE);
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text, skin, "big");
        label.setFontScale(0.78f);
        label.setColor(Color.CYAN);
        return label;
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
