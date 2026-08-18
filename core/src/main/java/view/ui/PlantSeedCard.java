package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import model.entities.plant.Plant;
import pvz.libpvz.pam.PamPlayer;

public class PlantSeedCard extends Table {
    private final Plant plant;
    private final float maxCooldown;
    private float currentCooldown = 0f;
    private int currentSun = 0;
    private boolean isSelected = false;
    private boolean inGameMode = false;

    private final Label costLbl;
    private final Label lvlLbl;
    private final Label cooldownLabel;

    private static Texture darkTexture;
    private static Texture borderTexture;
    private static Texture badgeBgTexture;

    public PlantSeedCard(Plant plant, int level, boolean boosted, PamPlayer pamPlayer, TextureRegion cardFace, TextureRegion badgeRegion, Skin skin) {
        this.plant = plant;
        this.maxCooldown = plant.getRecharge() > 0 ? (float) plant.getRecharge() : 5f;

        setTransform(true);
        setTouchable(Touchable.enabled);
        setSize(110, 138);
        setOrigin(Align.center);
        setClip(true);

        if (cardFace != null) {
            setBackground(new TextureRegionDrawable(cardFace));
        }

        if (darkTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(0f, 0f, 0f, 0.75f);
            p.fill();
            darkTexture = new Texture(p);
            p.dispose();
        }

        if (borderTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(1f, 1f, 0.2f, 0.95f);
            p.fill();
            borderTexture = new Texture(p);
            p.dispose();
        }

        if (badgeBgTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(0.05f, 0.03f, 0.02f, 0.92f);
            p.fill();
            badgeBgTexture = new Texture(p);
            p.dispose();
        }

        Stack stack = new Stack();
        stack.setSize(106, 134);

        Table content = new Table();
        content.setFillParent(true);
        content.top();

        if (boosted) {
            Table boostIndicator = new Table();
            boostIndicator.top().right();
            boostIndicator.setFillParent(true);
            Label boostIcon = new Label("★", skin, "big_outline");
            boostIcon.setFontScale(0.9f);
            boostIcon.setColor(Color.MAGENTA);
            boostIcon.setTouchable(Touchable.disabled);
            boostIndicator.add(boostIcon).padTop(2).padRight(4);
            stack.add(boostIndicator);
        }

        PamActor anim = new PamActor(pamPlayer, plant.getPamPath(), null, 0.42f) {
            @Override
            public void act(float delta) {
                super.act(0f);
            }
        };
        anim.setSize(100, 94);
        anim.setTouchable(Touchable.disabled);
        content.add(anim).size(100, 94).padTop(2).center().row();

        Table bottomRow = new Table();
        bottomRow.setFillParent(false);

        Table lvlBadge = new Table();
        if (badgeRegion != null) {
            lvlBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        } else {
            lvlBadge.setBackground(new TextureRegionDrawable(badgeBgTexture));
        }
        lvlLbl = new Label("L" + level, skin, "big_outline");
        lvlLbl.setFontScale(0.85f);
        lvlLbl.setColor(boosted ? Color.CYAN : Color.WHITE);
        lvlLbl.setTouchable(Touchable.disabled);
        lvlBadge.add(lvlLbl).pad(2, 6, 2, 6);

        Table costBadge = new Table();
        if (badgeRegion != null) {
            costBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        } else {
            costBadge.setBackground(new TextureRegionDrawable(badgeBgTexture));
        }
        costLbl = new Label(String.valueOf(plant.getCost()), skin, "big_outline");
        costLbl.setFontScale(0.95f);
        costLbl.setColor(Color.YELLOW);
        costLbl.setTouchable(Touchable.disabled);
        costBadge.add(costLbl).pad(2, 6, 2, 6);

        bottomRow.add(lvlBadge).height(28).left().padLeft(3);
        bottomRow.add(costBadge).height(28).expandX().right().padRight(3);
        content.add(bottomRow).fillX().padTop(2).padBottom(3);

        stack.add(content);

        Table labelTable = new Table();
        labelTable.setFillParent(true);
        cooldownLabel = new Label("", skin, "big_outline");
        cooldownLabel.setFontScale(1.15f);
        cooldownLabel.setColor(Color.WHITE);
        cooldownLabel.setTouchable(Touchable.disabled);
        labelTable.add(cooldownLabel).center();
        stack.add(labelTable);

        add(stack).size(106, 134);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        if (!inGameMode) {
            return;
        }

        float progress = maxCooldown > 0 ? Math.min(1.0f, Math.max(0.0f, currentCooldown / maxCooldown)) : 0f;

        if (currentCooldown > 0f) {
            float overlayH = getHeight() * progress;
            batch.draw(darkTexture, getX(), getY(), getWidth(), overlayH);
        } else if (currentSun < plant.getCost()) {
            batch.draw(darkTexture, getX(), getY(), getWidth(), getHeight());
        }

        if (isSelected) {
            float bw = 4f;
            batch.draw(borderTexture, getX(), getY(), getWidth(), bw);
            batch.draw(borderTexture, getX(), getY() + getHeight() - bw, getWidth(), bw);
            batch.draw(borderTexture, getX(), getY(), bw, getHeight());
            batch.draw(borderTexture, getX() + getWidth() - bw, getY(), bw, getHeight());
        }
    }

    public void updateCooldownState(float currentCd, int currentSun) {
        this.inGameMode = true;
        this.currentCooldown = Math.max(0f, currentCd);
        this.currentSun = currentSun;

        if (this.currentCooldown > 0f) {
            cooldownLabel.setText(String.format("%.1f", this.currentCooldown));
            this.isSelected = false;
        } else {
            cooldownLabel.setText("");
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public Plant getPlant() {
        return plant;
    }
}
