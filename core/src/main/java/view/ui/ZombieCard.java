package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import main.Maini;
import model.entities.ZombieType;
import pvz.libpvz.pam.PamPlayer;

public class ZombieCard extends Table {
    private final ZombieType zombieType;
    private final PamActor animActor;
    private final Label nameLabel;
    private final Label costLabel;

    public ZombieCard(Maini game, PamPlayer pamPlayer, Skin skin, ZombieType zombieType) {
        this.zombieType = zombieType;

        setTransform(true);
        setTouchable(Touchable.enabled);
        setSize(96, 130);
        setOrigin(Align.center);

        TextureRegion cardBg = game.getTextureBank().region("IMAGE_UI_CARDS_STORE_STORE_CARD_PURPLE");
        if (cardBg == null) {
            cardBg = game.getTextureBank().region("IMAGE_UI_CARDS_STORE_STORE_CARD_GREEN");
        }
        if (cardBg != null) {
            setBackground(new TextureRegionDrawable(cardBg));
        }

        animActor = new PamActor(
            pamPlayer,
            zombieType.getPamPath(),
            null,
            zombieType.getScale() * 0.75f,
            zombieType.getOffsetX(),
            zombieType.getOffsetY()
        );
        animActor.setSize(76, 76);
        animActor.setTouchable(Touchable.disabled);

        nameLabel = new Label(zombieType.getDisplayName(), skin);
        nameLabel.setFontScale(0.55f);
        nameLabel.setEllipsis(true);
        nameLabel.setAlignment(Align.center);
        nameLabel.setColor(Color.WHITE);
        nameLabel.setTouchable(Touchable.disabled);

        costLabel = new Label(String.valueOf(zombieType.getWaveCost()), skin, "big");
        costLabel.setFontScale(0.65f);
        costLabel.setColor(new Color(1f, 0.4f, 0.4f, 1f));
        costLabel.setTouchable(Touchable.disabled);

        Table topRow = new Table();
        topRow.add(animActor).size(76, 76).center();
        add(topRow).size(76, 76).padTop(6).center().row();

        add(nameLabel).width(88).padTop(2).center().row();

        Table bottomRow = new Table();
        bottomRow.add(costLabel).expandX().right().padRight(6).padBottom(4);
        add(bottomRow).fillX();

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    animActor.setHovered(true);
                    clearActions();
                    addAction(Actions.scaleTo(1.08f, 1.08f, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    animActor.setHovered(false);
                    clearActions();
                    addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }

    public ZombieType getZombieType() {
        return zombieType;
    }

    public PamActor getAnimActor() {
        return animActor;
    }
}
