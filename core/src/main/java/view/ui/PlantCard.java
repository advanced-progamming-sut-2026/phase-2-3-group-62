package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import main.Maini;
import model.entities.PlantType;
import model.entities.plant.Plant;
import pvz.libpvz.pam.PamPlayer;

public class PlantCard extends Table {
    private final Plant plant;
    private final Actor visualActor;
    private final Label costLabel;
    private final Label rechargeLabel;
    private float currentCooldown = 0f;
    private float maxCooldown = 5f;
    private boolean isAvailable = true;

    public PlantCard(Maini game, PamPlayer pamPlayer, Skin skin, Plant plant) {
        this.plant = plant;
        this.maxCooldown = plant.getRecharge() > 0 ? (float) plant.getRecharge() : 5f;

        setTransform(true);
        setTouchable(Touchable.enabled);
        setSize(86, 115);
        setOrigin(Align.center);

        TextureRegion cardBg = game.getTextureBank().region("IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_DEFAULT");
        if (cardBg == null) {
            cardBg = game.getTextureBank().region("IMAGE_UI_CARDS_STORE_STORE_CARD_GREEN");
        }
        if (cardBg != null) {
            setBackground(new TextureRegionDrawable(cardBg));
        }

        PlantType pType = PlantType.fromName(plant.getName());
        Actor visual = null;

        if (pType != null && pType.getIconRegionName() != null) {
            TextureRegion iconReg = game.getTextureBank().region(pType.getIconRegionName());
            if (iconReg != null) {
                Image iconImg = new Image(iconReg);
                iconImg.setScaling(Scaling.fit);
                iconImg.setAlign(Align.center);
                visual = iconImg;
            }
        }

        if (visual == null) {
            PamActor animActor = new PamActor(pamPlayer, plant.getPamPath(), "idle", 0.24f) {
                @Override
                public void act(float delta) {
                    super.act(0f);
                }
            };
            animActor.act(1.4f);
            visual = animActor;
        }

        this.visualActor = visual;
        this.visualActor.setTouchable(Touchable.disabled);

        Container<Actor> iconContainer = new Container<>(visualActor);
        iconContainer.size(56, 56);
        iconContainer.fill();

        costLabel = new Label(String.valueOf(plant.getCost()), skin, "big");
        costLabel.setFontScale(0.65f);
        costLabel.setColor(Color.YELLOW);
        costLabel.setTouchable(Touchable.disabled);

        rechargeLabel = new Label("", skin);
        rechargeLabel.setFontScale(0.7f);
        rechargeLabel.setColor(Color.WHITE);
        rechargeLabel.setTouchable(Touchable.disabled);

        Table topRow = new Table();
        topRow.add(iconContainer).size(56, 56).center();
        add(topRow).size(72, 72).padTop(4).center().row();

        Table bottomRow = new Table();
        bottomRow.add(rechargeLabel).left().padLeft(6);
        bottomRow.add(costLabel).expandX().right().padRight(6);
        add(bottomRow).fillX().padBottom(4);

        addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && isAvailable) {
                    clearActions();
                    addAction(Actions.scaleTo(1.08f, 1.08f, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    clearActions();
                    addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }

    public void startCooldown() {
        this.currentCooldown = maxCooldown;
        this.isAvailable = false;
    }

    public void updateCooldown(float delta) {
        if (currentCooldown > 0f) {
            currentCooldown -= delta;
            if (currentCooldown <= 0f) {
                currentCooldown = 0f;
                isAvailable = true;
                rechargeLabel.setText("");
                setColor(1f, 1f, 1f, 1f);
            } else {
                rechargeLabel.setText(String.format("%.1f", currentCooldown));
                setColor(0.5f, 0.5f, 0.5f, 0.85f);
            }
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updateCooldown(delta);
    }

    public Plant getPlant() {
        return plant;
    }

    public Actor getVisualActor() {
        return visualActor;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
