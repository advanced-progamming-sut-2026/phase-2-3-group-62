package view.game.couch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import main.Maini;

public class CouchPlayHud {
    private final Maini game;
    private final Skin skin;
    private final Table topHud = new Table();

    private Label plantSunCountLabel;
    private Label zombieSunCountLabel;
    private Label timerLabel;

    public CouchPlayHud(Maini game, Skin skin, Runnable onLeaveClick) {
        this.game = game;
        this.skin = skin;
        build(onLeaveClick);
    }

    private void build(Runnable onLeaveClick) {
        topHud.left();

        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        TextureRegion sunIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SUN");
        TextureRegion blueGloveRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_GLOVE_BLUE_JOUST_GLOVE_BLUE_211X138");
        TextureRegion redGloveRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_GLOVE_RED_JOUST_GLOVE_RED_211X138");
        TextureRegion shieldRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_METER_JOUST_METER_SHIELD");
        TextureRegion timerBgRegion = game.getTextureBank().region("IMAGE_UI_JOUST_JOUST_METER_ASSETS_JOUST_TIMER_BG");
        TextureRegion teamCounterBgRegion = game.getTextureBank().region("IMAGE_UI_HUD_WORLDMAP_LEVEL_COUNTER");

        Table plantSunBadge = new Table();
        plantSunBadge.setTouchable(Touchable.disabled);
        if (badgeRegion != null) plantSunBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        if (sunIconRegion != null) {
            Image sunIcon = new Image(sunIconRegion);
            sunIcon.setScaling(Scaling.fit);
            plantSunBadge.add(sunIcon).size(52, 52).padLeft(8).padRight(6);
        }
        plantSunCountLabel = new Label("150", skin, "big_outline");
        plantSunCountLabel.setFontScale(1.1f);
        plantSunCountLabel.setColor(Color.YELLOW);
        plantSunBadge.add(plantSunCountLabel).padRight(16);
        topHud.add(plantSunBadge).height(64).padRight(18);

        Table joustBar = new Table();
        joustBar.center();

        Table plantTeamBadge = new Table();
        if (teamCounterBgRegion != null) plantTeamBadge.setBackground(new TextureRegionDrawable(teamCounterBgRegion));
        if (blueGloveRegion != null) {
            Image blueGlove = new Image(new TextureRegionDrawable(blueGloveRegion));
            blueGlove.setScaling(Scaling.fit);
            plantTeamBadge.add(blueGlove).size(62, 42).padLeft(12).padRight(8);
        }
        Label plantNameLbl = new Label("P1 (MOUSE)", skin, "big_outline");
        plantNameLbl.setColor(Color.CYAN);
        plantNameLbl.setFontScale(1.1f);
        plantTeamBadge.add(plantNameLbl).padRight(18);
        joustBar.add(plantTeamBadge).height(58).padRight(10);

        if (shieldRegion != null) {
            Image shield = new Image(new TextureRegionDrawable(shieldRegion));
            shield.setScaling(Scaling.fit);
            joustBar.add(shield).size(58, 58).padRight(10);
        }

        Table zombieTeamBadge = new Table();
        if (teamCounterBgRegion != null) zombieTeamBadge.setBackground(new TextureRegionDrawable(teamCounterBgRegion));
        Label zombieNameLbl = new Label("P2 (KEYS 1-5 / N)", skin, "big_outline");
        zombieNameLbl.setColor(Color.SCARLET);
        zombieNameLbl.setFontScale(1.1f);
        zombieTeamBadge.add(zombieNameLbl).padLeft(18).padRight(8);
        if (redGloveRegion != null) {
            Image redGlove = new Image(new TextureRegionDrawable(redGloveRegion));
            redGlove.setScaling(Scaling.fit);
            zombieTeamBadge.add(redGlove).size(62, 42).padRight(12);
        }
        joustBar.add(zombieTeamBadge).height(58);

        topHud.add(joustBar).expandX().center();

        Table timerBadge = new Table();
        if (timerBgRegion != null) timerBadge.setBackground(new TextureRegionDrawable(timerBgRegion));
        timerLabel = new Label("02:00", skin, "big_outline");
        timerLabel.setFontScale(1.25f);
        timerLabel.setColor(Color.YELLOW);
        timerBadge.add(timerLabel).pad(8, 22, 8, 22).center();
        topHud.add(timerBadge).size(160, 60).padRight(14);

        Table zombieSunBadge = new Table();
        zombieSunBadge.setTouchable(Touchable.disabled);
        if (badgeRegion != null) zombieSunBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        if (sunIconRegion != null) {
            Image sunIcon = new Image(sunIconRegion);
            sunIcon.setScaling(Scaling.fit);
            zombieSunBadge.add(sunIcon).size(52, 52).padLeft(8).padRight(6);
        }
        zombieSunCountLabel = new Label("150", skin, "big_outline");
        zombieSunCountLabel.setFontScale(1.1f);
        zombieSunCountLabel.setColor(Color.YELLOW);
        zombieSunBadge.add(zombieSunCountLabel).padRight(16);
        topHud.add(zombieSunBadge).height(64).padRight(14);

        TextButton leftGameBtn = new TextButton("Left Game", skin);
        leftGameBtn.addListener(new ClickListener() {
            @Override
            public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) {
                if (onLeaveClick != null) onLeaveClick.run();
            }
        });
        topHud.add(leftGameBtn).size(130, 44).padRight(20);
    }

    public void updatePlantSun(int count) {
        plantSunCountLabel.setText(String.valueOf(count));
    }

    public void updateZombieSun(int count) {
        zombieSunCountLabel.setText(String.valueOf(count));
    }

    public void updateTimer(float timeRemaining) {
        int mins = (int) (timeRemaining / 60);
        int secs = (int) (timeRemaining % 60);
        timerLabel.setText(String.format("%02d:%02d", mins, secs));
    }

    public Table getRoot() {
        return topHud;
    }
}
