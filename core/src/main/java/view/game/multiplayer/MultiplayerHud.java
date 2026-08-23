package view.game.multiplayer;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import main.Maini;
import view.audio.AudioManager;

public class MultiplayerHud {
    private final Table root;
    private final Label sunCountLabel;
    private final Label timerLabel;

    public MultiplayerHud(Maini game, Skin skin, boolean isPlantsRole, String myUsername, String opponentUsername, Runnable onLeaveClicked) {
        root = new Table();
        root.left();

        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        TextureRegion sunIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SUN");
        TextureRegion blueGloveRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_GLOVE_BLUE_JOUST_GLOVE_BLUE_211X138");
        TextureRegion redGloveRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_GLOVE_RED_JOUST_GLOVE_RED_211X138");
        TextureRegion shieldRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_METER_JOUST_METER_SHIELD");
        TextureRegion timerBgRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_TIMER_BG");
        TextureRegion teamCounterBgRegion = game.getTextureBank().region("IMAGE_UI_HUD_WORLDMAP_LEVEL_COUNTER");

        Table sunBadge = new Table();
        if (badgeRegion != null) sunBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        if (sunIconRegion != null) {
            Image sunIcon = new Image(sunIconRegion);
            sunIcon.setScaling(Scaling.fit);
            sunBadge.add(sunIcon).size(52, 52).padLeft(8).padRight(6);
        }
        sunCountLabel = new Label("150", skin, "big_outline");
        sunCountLabel.setFontScale(1.1f);
        sunCountLabel.setColor(Color.YELLOW);
        sunBadge.add(sunCountLabel).padRight(16);
        root.add(sunBadge).height(64).padRight(18);

        Table joustBar = new Table();
        joustBar.center();

        String plantsUser = isPlantsRole ? myUsername : opponentUsername;
        String zombiesUser = isPlantsRole ? opponentUsername : myUsername;

        Table plantTeamBadge = new Table();
        if (teamCounterBgRegion != null) plantTeamBadge.setBackground(new TextureRegionDrawable(teamCounterBgRegion));
        if (blueGloveRegion != null) {
            Image blueGlove = new Image(new TextureRegionDrawable(blueGloveRegion));
            blueGlove.setScaling(Scaling.fit);
            plantTeamBadge.add(blueGlove).size(62, 42).padLeft(12).padRight(8);
        }
        Label plantNameLbl = new Label(plantsUser, skin, "big_outline");
        plantNameLbl.setColor(Color.CYAN);
        plantNameLbl.setFontScale(1.2f);
        plantTeamBadge.add(plantNameLbl).padRight(18);
        joustBar.add(plantTeamBadge).height(58).padRight(10);

        if (shieldRegion != null) {
            Image shield = new Image(new TextureRegionDrawable(shieldRegion));
            shield.setScaling(Scaling.fit);
            joustBar.add(shield).size(58, 58).padRight(10);
        } else {
            Label vsLbl = new Label("VS", skin, "big_outline");
            vsLbl.setColor(Color.YELLOW);
            vsLbl.setFontScale(1.25f);
            joustBar.add(vsLbl).padRight(10);
        }

        Table zombieTeamBadge = new Table();
        if (teamCounterBgRegion != null) zombieTeamBadge.setBackground(new TextureRegionDrawable(teamCounterBgRegion));
        Label zombieNameLbl = new Label(zombiesUser, skin, "big_outline");
        zombieNameLbl.setColor(Color.SCARLET);
        zombieNameLbl.setFontScale(1.2f);
        zombieTeamBadge.add(zombieNameLbl).padLeft(18).padRight(8);
        if (redGloveRegion != null) {
            Image redGlove = new Image(new TextureRegionDrawable(redGloveRegion));
            redGlove.setScaling(Scaling.fit);
            zombieTeamBadge.add(redGlove).size(62, 42).padRight(12);
        }
        joustBar.add(zombieTeamBadge).height(58);
        root.add(joustBar).expandX().center();

        Table timerBadge = new Table();
        if (timerBgRegion != null) timerBadge.setBackground(new TextureRegionDrawable(timerBgRegion));
        timerLabel = new Label("02:00", skin, "big_outline");
        timerLabel.setFontScale(1.25f);
        timerLabel.setColor(Color.YELLOW);
        timerBadge.add(timerLabel).pad(8, 22, 8, 22).center();
        root.add(timerBadge).size(160, 60).padRight(20);

        TextButton leftGameBtn = new TextButton("Left Game", skin);
        attachHoverEffect(leftGameBtn, 1.08f);
        leftGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                if (onLeaveClicked != null) onLeaveClicked.run();
            }
        });
        root.add(leftGameBtn).size(130, 44).padRight(20);
    }

    public void updateSun(int sun) {
        sunCountLabel.setText(String.valueOf(sun));
    }

    public void updateTimer(float gameTime) {
        int mins = (int) (gameTime / 60);
        int secs = (int) (gameTime % 60);
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
    }

    public Table getRoot() {
        return root;
    }

    public static void attachHoverEffect(Actor actor, float targetScale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(targetScale, targetScale, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(com.badlogic.gdx.scenes.scene2d.actions.Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }
}
