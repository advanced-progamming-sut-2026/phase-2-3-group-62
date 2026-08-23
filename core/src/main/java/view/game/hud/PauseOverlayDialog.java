package view.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import model.user.Settings;
import util.FileManager;
import view.audio.AudioManager;
import view.game.mainGame.GamePlayScreen;

public class PauseOverlayDialog {
    private final Table pauseOverlay = new Table();
    private Texture dimOverlayTexture;
    private Texture pauseDialogBgTexture;

    public PauseOverlayDialog(Skin skin, GamePlayScreen screen) {
        initTextures();
        buildUI(skin, screen);
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

    private void initTextures() {
        dimOverlayTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.65f));
        pauseDialogBgTexture = createRoundedRectangleTexture(new Color(0.24f, 0.14f, 0.08f, 0.98f));
    }

    private void buildUI(Skin skin, GamePlayScreen screen) {
        pauseOverlay.setFillParent(true);
        pauseOverlay.setVisible(false);
        pauseOverlay.setTouchable(Touchable.enabled);

        if (dimOverlayTexture != null) {
            pauseOverlay.setBackground(new TextureRegionDrawable(dimOverlayTexture));
        }

        Stack dialogStack = new Stack();
        if (pauseDialogBgTexture != null) {
            Image dialogBg = new Image(pauseDialogBgTexture);
            dialogBg.setScaling(com.badlogic.gdx.utils.Scaling.stretch);
            dialogStack.add(dialogBg);
        }

        Table dialogContent = new Table();
        dialogContent.top().pad(24);

        Label title = new Label("GAME PAUSED", skin, "big_outline");
        title.setFontScale(1.35f);
        title.setColor(Color.YELLOW);
        dialogContent.add(title).padTop(8).padBottom(16).center().row();

        Settings settings = FileManager.loadSettings();
        if (settings == null) settings = new Settings();

        Table audioControls = new Table();
        audioControls.padBottom(16);

        Label mLabel = new Label("Music:", skin, "big");
        mLabel.setFontScale(0.8f);
        Slider mSlider = new Slider(0f, 1f, 0.05f, false, skin);
        mSlider.setValue(AudioManager.getInstance().getMusicVolume());
        Label mVal = new Label(String.format("%.0f%%", mSlider.getValue() * 100), skin, "big");
        mVal.setFontScale(0.75f);
        mVal.setColor(Color.CYAN);

        final Settings finalSettings = settings;
        mSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = mSlider.getValue();
                AudioManager.getInstance().setMusicVolume(val);
                finalSettings.setMusicVolume(val);
                mVal.setText(String.format("%.0f%%", val * 100));
                FileManager.saveSettings(finalSettings);
            }
        });

        audioControls.add(mLabel).width(110).left();
        audioControls.add(mSlider).width(240).padRight(12);
        audioControls.add(mVal).width(60).left().row();

        Label sLabel = new Label("SFX:", skin, "big");
        sLabel.setFontScale(0.8f);
        Slider sSlider = new Slider(0f, 1f, 0.05f, false, skin);
        sSlider.setValue(AudioManager.getInstance().getSoundVolume());
        Label sVal = new Label(String.format("%.0f%%", sSlider.getValue() * 100), skin, "big");
        sVal.setFontScale(0.75f);
        sVal.setColor(Color.CYAN);

        sSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = sSlider.getValue();
                AudioManager.getInstance().setSoundVolume(val);
                finalSettings.setSfxVolume(val);
                sVal.setText(String.format("%.0f%%", val * 100));
                FileManager.saveSettings(finalSettings);
            }
        });

        audioControls.add(sLabel).width(110).left().padTop(8);
        audioControls.add(sSlider).width(240).padRight(12).padTop(8);
        audioControls.add(sVal).width(60).left().padTop(8);

        dialogContent.add(audioControls).center().row();

        TextButton resumeBtn = new TextButton("RESUME", skin, "green");
        resumeBtn.getLabel().setFontScale(1.15f);
        attachHoverEffect(resumeBtn, 1.06f);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                screen.togglePause();
            }
        });
        dialogContent.add(resumeBtn).size(340, 58).padBottom(12).center().row();

        TextButton restartBtn = new TextButton("RESTART", skin, "brown");
        restartBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(restartBtn, 1.06f);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                screen.restartLevel();
            }
        });
        dialogContent.add(restartBtn).size(340, 58).padBottom(12).center().row();

        TextButton exitBtn = new TextButton("SAVE & EXIT", skin);
        exitBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(exitBtn, 1.06f);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                screen.saveAndExit();
            }
        });
        dialogContent.add(exitBtn).size(340, 58).center();

        dialogStack.add(dialogContent);
        pauseOverlay.add(dialogStack).size(560, 540).center();
    }

    public void setVisible(boolean visible) {
        pauseOverlay.setVisible(visible);
        if (visible) pauseOverlay.toFront();
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createRoundedRectangleTexture(Color color) {
        int width = 560;
        int height = 540;
        int radius = 28;
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

    public Table getRoot() {
        return pauseOverlay;
    }

    public void dispose() {
        if (dimOverlayTexture != null) dimOverlayTexture.dispose();
        if (pauseDialogBgTexture != null) pauseDialogBgTexture.dispose();
    }
}
