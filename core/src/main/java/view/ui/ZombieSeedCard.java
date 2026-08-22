package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import main.Maini;
import model.entities.ZombieType;
import pvz.libpvz.pam.PamPlayer;

import java.util.HashMap;
import java.util.Map;

public class ZombieSeedCard extends Table {
    private final String zombieName;
    private final int cost;
    private final float maxCooldown;
    private float currentCooldown = 0f;
    private int currentSun = 0;
    private boolean isSelected = false;
    private boolean inGameMode = false;

    private final Label costLbl;
    private final Label cooldownLabel;

    private static Texture darkTexture;
    private static Texture borderTexture;
    private static Texture badgeBgTexture;

    public ZombieSeedCard(Maini game, String zombieName, int cost, PamPlayer pamPlayer, TextureRegion cardFace, TextureRegion badgeRegion, Skin skin) {
        this.zombieName = zombieName;
        this.cost = cost;
        this.maxCooldown = calculateDefaultCooldown(zombieName);

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

        ZombieType zType = ZombieType.fromId(zombieName);
        Actor visualActor = null;

        if (zType != null) {
            String pamPath = zType.getPamPath() != null ? zType.getPamPath() : "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM";
            float scale = (zType.getScale() > 0 ? zType.getScale() : 0.28f) * 0.95f;
            float offX = zType.getOffsetX();
            float offY = zType.getOffsetY() - 10f;
            Map<String, Boolean> visibility = getArmorVisibilityForType(zType);

            PamActor anim = new PamActor(
                pamPlayer,
                pamPath,
                "idle",
                scale,
                offX,
                offY,
                visibility
            ) {
                @Override
                public void act(float delta) {
                    super.act(0f);
                }
            };
            anim.act(1.4f);
            visualActor = anim;
        }

        if (visualActor != null) {
            visualActor.setTouchable(Touchable.disabled);
            Container<Actor> iconContainer = new Container<>(visualActor);
            iconContainer.size(76, 76);
            iconContainer.fill();
            content.add(iconContainer).size(76, 76).padTop(10).center().row();
        }

        Table bottomRow = new Table();
        bottomRow.setFillParent(false);

        Table costBadge = new Table();
        if (badgeRegion != null) {
            costBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        } else {
            costBadge.setBackground(new TextureRegionDrawable(badgeBgTexture));
        }

        TextureRegion sunIcon = game != null ? game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SUN") : null;
        if (sunIcon != null) {
            Image sIcon = new Image(sunIcon);
            sIcon.setScaling(Scaling.fit);
            costBadge.add(sIcon).size(20, 20).padLeft(3).padRight(2);
        }

        costLbl = new Label(String.valueOf(cost), skin, "big_outline");
        costLbl.setFontScale(0.95f);
        costLbl.setColor(Color.YELLOW);
        costLbl.setTouchable(Touchable.disabled);
        costBadge.add(costLbl).pad(2, 4, 2, 6);

        bottomRow.add(costBadge).height(28).expandX().right().padRight(4);
        content.add(bottomRow).fillX().padTop(6).padBottom(3);

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

    private float calculateDefaultCooldown(String name) {
        String clean = name.toLowerCase();
        if (clean.contains("imp")) return 3.5f;
        if (clean.contains("normal")) return 5.0f;
        if (clean.contains("cone")) return 7.5f;
        if (clean.contains("newspaper")) return 8.0f;
        if (clean.contains("prospector")) return 10.0f;
        if (clean.contains("crystalskull") || clean.contains("turquoise")) return 12.0f;
        if (clean.contains("juggler") || clean.contains("piano")) return 12.0f;
        if (clean.contains("barrel")) return 15.0f;
        if (clean.contains("allstar") || clean.contains("football")) return 15.0f;
        if (clean.contains("gargantuar")) return 30.0f;
        return 7.5f;
    }

    private static Map<String, Boolean> getArmorVisibilityForType(ZombieType zType) {
        Map<String, Boolean> visibility = new HashMap<>();
        if (zType == null) return visibility;

        String aType = zType.getArmorType();
        String cleanId = zType.getId().toLowerCase();

        if (cleanId.contains("cone") || cleanId.contains("armor1") || (aType != null && aType.equalsIgnoreCase("Cone"))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_cone_states", true);
            visibility.put("zombie_armor_cone_states", true);
            visibility.put("_zombie_armor_cone", true);
            visibility.put("zombie_armor_cone", true);
            visibility.put("zombie_armor_cone_norm", true);
            visibility.put("_zombie_armor_cone_norm", true);
            visibility.put("cone", true);
            visibility.put("_cone", true);
        } else if (cleanId.contains("bucket") || cleanId.contains("armor2") || (aType != null && aType.equalsIgnoreCase("Bucket"))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_bucket_states", true);
            visibility.put("zombie_armor_bucket_states", true);
            visibility.put("_zombie_armor_bucket", true);
            visibility.put("zombie_armor_bucket", true);
            visibility.put("zombie_armor_bucket_norm", true);
            visibility.put("_zombie_armor_bucket_norm", true);
            visibility.put("bucket", true);
            visibility.put("_bucket", true);
        } else if (cleanId.contains("brick") || cleanId.contains("armor4") || (aType != null && aType.equalsIgnoreCase("Brick"))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_brick_states", true);
            visibility.put("zombie_armor_brick_states", true);
            visibility.put("_zombie_armor_brick", true);
            visibility.put("zombie_armor_brick", true);
            visibility.put("zombie_armor_brick_norm", true);
            visibility.put("_zombie_armor_brick_norm", true);
            visibility.put("brick", true);
            visibility.put("_brick", true);
        } else if (cleanId.contains("knight") || cleanId.contains("armor3") || (aType != null && (aType.equalsIgnoreCase("Crown") || aType.equalsIgnoreCase("Knight")))) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_crown_states", true);
            visibility.put("zombie_armor_crown_states", true);
            visibility.put("_zombie_armor_crown", true);
            visibility.put("zombie_armor_crown", true);
            visibility.put("zombie_armor_crown_norm", true);
            visibility.put("_zombie_armor_crown_norm", true);
            visibility.put("crown", true);
            visibility.put("_crown", true);
        } else if (cleanId.contains("newspaper") || (aType != null && aType.equalsIgnoreCase("Newspaper"))) {
            visibility.put("_zombie_newspaper_states", true);
            visibility.put("zombie_newspaper_states", true);
            visibility.put("_zombie_newspaper", true);
            visibility.put("zombie_newspaper", true);
        }

        return visibility;
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
        } else if (currentSun < cost) {
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

        if (currentSun < cost) {
            costLbl.setColor(Color.RED);
        } else {
            costLbl.setColor(Color.YELLOW);
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public String getZombieName() {
        return zombieName;
    }

    public int getCost() {
        return cost;
    }

    public float getMaxCooldown() {
        return maxCooldown;
    }
}
