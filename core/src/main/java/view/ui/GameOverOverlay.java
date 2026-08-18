package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import model.game.ChapterType;
import model.user.User;
import model.user.UserSession;
import util.FileManager;
import view.audio.AudioManager;

public class GameOverOverlay extends Table {
    private final Skin skin;
    private final Runnable onRestart;
    private final Runnable onExit;
    private final Table dialogContent;

    private final Texture dimTexture;
    private final Texture dialogBgTexture;
    private boolean isShown = false;

    public GameOverOverlay(Skin skin, Runnable onRestart, Runnable onExit) {
        this.skin = skin;
        this.onRestart = onRestart;
        this.onExit = onExit;

        setFillParent(true);
        setVisible(false);
        setTouchable(Touchable.enabled);

        dimTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.75f));
        setBackground(new TextureRegionDrawable(dimTexture));

        dialogBgTexture = createRoundedRectangleTexture(new Color(0.22f, 0.14f, 0.08f, 0.98f));

        Stack stack = new Stack();
        Image bgImg = new Image(dialogBgTexture);
        bgImg.setScaling(Scaling.stretch);
        stack.add(bgImg);

        dialogContent = new Table();
        dialogContent.top().pad(24);
        stack.add(dialogContent);

        add(stack).size(540, 420).center();
    }

    private void attachHoverEffect(Actor actor) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1.06f, 1.06f, 0.1f));
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

    private Texture createSolidTexture(Color color) {
        Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        p.setColor(color);
        p.fill();
        Texture t = new Texture(p);
        p.dispose();
        return t;
    }

    private Texture createRoundedRectangleTexture(Color color) {
        int width = 540;
        int height = 420;
        int radius = 24;
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

    public void showResult(boolean won, int score) {
        showResult(won, score, null, 1);
    }

    public void showResult(boolean won, int score, ChapterType chapter, int currentLevel) {
        if (isShown) return;
        isShown = true;

        if (won && chapter != null) {
            User u = UserSession.getCurrentUser();
            if (u != null) {
                int seasonIndex = chapter.getSeasonIndex();
                int currentSeasonComp = u.getLastSeasonCompleted();
                int currentLvlComp = u.getLastLevelCompleted();

                if (seasonIndex > currentSeasonComp || (seasonIndex == currentSeasonComp && currentLevel > currentLvlComp)) {
                    u.setLastSeasonCompleted(seasonIndex);
                    u.setLastLevelCompleted(currentLevel);
                    FileManager.updateUser(u);
                }
            }
        }

        dialogContent.clear();

        Label title = new Label(won ? "VICTORY!" : "GAME OVER", skin, "big_outline");
        title.setFontScale(1.35f);
        title.setColor(won ? Color.YELLOW : Color.RED);
        dialogContent.add(title).padTop(10).padBottom(16).center().row();

        Label sub = new Label(won ? "Zombies Defeated!" : "The Zombies Ate Your Brains!", skin, "big");
        sub.setFontScale(0.85f);
        sub.setColor(Color.WHITE);
        dialogContent.add(sub).padBottom(14).center().row();

        Label scoreLbl = new Label("Score: " + score, skin, "big_outline");
        scoreLbl.setFontScale(0.95f);
        scoreLbl.setColor(Color.CYAN);
        dialogContent.add(scoreLbl).padBottom(24).center().row();

        Table btnRow = new Table();
        TextButton restartBtn = new TextButton(won ? "PLAY AGAIN" : "RETRY", skin, won ? "green" : "brown");
        restartBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(restartBtn);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                if (onRestart != null) onRestart.run();
            }
        });
        btnRow.add(restartBtn).size(200, 56).padRight(16);

        TextButton exitBtn = new TextButton("MAP", skin);
        exitBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(exitBtn);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                if (onExit != null) onExit.run();
            }
        });
        btnRow.add(exitBtn).size(200, 56);

        dialogContent.add(btnRow).center();

        setVisible(true);
        toFront();
    }

    public boolean isShown() {
        return isShown;
    }

    public void dispose() {
        dimTexture.dispose();
        dialogBgTexture.dispose();
    }
}
