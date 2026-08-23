package view.game.couch;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import main.Maini;
import view.audio.AudioManager;
import view.menu.playMenu.PlayScreen;

public class CouchPlayDialogHelper {
    private final Maini game;
    private final Stage stage;
    private final Skin skin;
    private final Texture dimTexture;

    public CouchPlayDialogHelper(Maini game, Stage stage, Skin skin, Texture dimTexture) {
        this.game = game;
        this.stage = stage;
        this.skin = skin;
        this.dimTexture = dimTexture;
    }

    public void showLeaveDialog(Runnable onConfirmLeave) {
        Table confirmOverlay = new Table();
        confirmOverlay.setFillParent(true);
        confirmOverlay.setTouchable(Touchable.enabled);

        Image dim = new Image(dimTexture);
        dim.setFillParent(true);
        confirmOverlay.addActor(dim);

        Stack stack = new Stack();
        TextureRegion border = game.getTextureBank().region("IMAGE_UI_QUESTS_QUESTBORDER");
        if (border != null) {
            Image borderImg = new Image(new TextureRegionDrawable(border));
            borderImg.setScaling(Scaling.stretch);
            stack.add(borderImg);
        }

        Table box = new Table();
        box.pad(35);

        Label title = new Label("LEAVE MATCH", skin, "big_outline");
        title.setColor(Color.RED);
        title.setFontScale(1.2f);
        box.add(title).padBottom(15).row();

        Label msg = new Label("Are you sure you want to exit to menu?", skin, "big");
        msg.setFontScale(0.85f);
        msg.setAlignment(Align.center);
        box.add(msg).padBottom(25).row();

        Table btnRow = new Table();

        TextButton yesBtn = new TextButton("Leave", skin, "brown");
        yesBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                confirmOverlay.remove();
                if (onConfirmLeave != null) onConfirmLeave.run();
            }
        });
        btnRow.add(yesBtn).size(140, 46).padRight(20);

        TextButton noBtn = new TextButton("Cancel", skin, "green");
        noBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                confirmOverlay.remove();
            }
        });
        btnRow.add(noBtn).size(140, 46);

        box.add(btnRow).row();
        stack.add(box);
        confirmOverlay.add(stack).size(560, 310).center();
        stage.addActor(confirmOverlay);
    }

    public void showEndGameDialog(String message, boolean plantsWon) {
        Table endOverlay = new Table();
        endOverlay.setFillParent(true);

        Image dim = new Image(dimTexture);
        dim.setFillParent(true);
        endOverlay.addActor(dim);

        Stack stack = new Stack();
        TextureRegion border = game.getTextureBank().region("IMAGE_UI_QUESTS_QUESTBORDER");
        if (border != null) {
            Image borderImg = new Image(new TextureRegionDrawable(border));
            borderImg.setScaling(Scaling.stretch);
            stack.add(borderImg);
        }

        Table box = new Table();
        box.pad(40);

        Label endTitle = new Label(plantsWon ? "PLANTS WIN!" : "ZOMBIES WIN!", skin, "big_outline");
        endTitle.setColor(plantsWon ? Color.GREEN : Color.RED);
        endTitle.setFontScale(1.3f);
        box.add(endTitle).padBottom(15).row();

        Label msgLbl = new Label(message, skin, "big");
        msgLbl.setFontScale(0.9f);
        msgLbl.setAlignment(Align.center);
        box.add(msgLbl).padBottom(25).row();

        TextButton returnBtn = new TextButton("Return to Menu", skin, "green");
        returnBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                game.setScreen(new PlayScreen(game, game.getMenuController(), skin));
            }
        });
        box.add(returnBtn).size(200, 50).row();

        stack.add(box);
        endOverlay.add(stack).size(560, 320).center();
        stage.addActor(endOverlay);
    }
}
