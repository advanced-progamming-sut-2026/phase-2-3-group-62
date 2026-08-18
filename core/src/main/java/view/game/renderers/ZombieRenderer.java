package view.game.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import model.Game;
import model.entities.ZombieType;
import model.entities.zombie.Zombie;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import view.game.GameGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ZombieRenderer {
    private final PamPlayer pamPlayer;
    private final TextureRegion frozenIceBlockRegion;
    private final Map<Zombie, Float> smoothXPositions = new HashMap<>();

    private static class DyingZombie {
        ZombieType type;
        float x;
        float y;
        float elapsed;
        float duration;

        DyingZombie(ZombieType type, float x, float y, float duration) {
            this.type = type;
            this.x = x;
            this.y = y;
            this.elapsed = 0f;
            this.duration = duration;
        }
    }

    private static final List<DyingZombie> dyingZombies = new ArrayList<>();

    public ZombieRenderer(PamPlayer pamPlayer, TextureBank textureBank) {
        this.pamPlayer = pamPlayer;
        this.frozenIceBlockRegion = textureBank != null
            ? textureBank.region("IMAGE_EFFECTS_ICEBLOOM_ICE_BLOCK_ZOMBIE_ICEBLOOM_ICE_BLOCK_ZOMBIE_135X247_4")
            : null;
    }

    public ZombieRenderer(PamPlayer pamPlayer) {
        this(pamPlayer, null);
    }

    public static void triggerDeathAnimation(Zombie zombie, float pixelX, float pixelY) {
        ZombieType type = ZombieType.fromZombie(zombie);
        dyingZombies.add(new DyingZombie(type, pixelX, pixelY, 1.2f));
    }

    public void render(SpriteBatch batch, Game game, float stateTime, float delta) {
        if (game == null) return;

        if (game.getActiveZombies() != null) {
            for (Zombie prevZombie : new ArrayList<>(smoothXPositions.keySet())) {
                if (!game.getActiveZombies().contains(prevZombie)) {
                    float px = smoothXPositions.getOrDefault(prevZombie, 0f);
                    float py = GameGrid.getGridStartY() + ((4 - prevZombie.getY()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                    triggerDeathAnimation(prevZombie, px, py);
                    smoothXPositions.remove(prevZombie);
                }
            }

            float startX = GameGrid.getGridStartX();
            float startY = GameGrid.getGridStartY();

            for (Zombie zombie : game.getActiveZombies()) {
                zombie.updateCustomAnim(delta);

                ZombieType type = ZombieType.fromZombie(zombie);
                float offsetX = type != null ? type.getOffsetX() : 0f;
                float offsetY = type != null ? type.getOffsetY() : 0f;

                float targetPixelX = startX + ((float) zombie.getX() * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f) + offsetX;
                float currentPixelX = smoothXPositions.getOrDefault(zombie, targetPixelX);

                if (zombie.isBoss() && Math.abs(targetPixelX - currentPixelX) > 1.5f) {
                    ScreenShake.shake(3.5f, 0.15f);
                }

                currentPixelX = MathUtils.lerp(currentPixelX, targetPixelX, Math.min(delta * 12f, 1f));
                smoothXPositions.put(zombie, currentPixelX);

                float zy = startY + ((4 - zombie.getY()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f) + offsetY;

                String pamPath = type != null ? type.getPamPath() : "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM";
                String clipName = zombie.getCustomAnim() != null ? zombie.getCustomAnim() : (zombie.isEating() ? "eat" : "walk");

                boolean isChilled = zombie.getChilledDuration() > 0;
                boolean isFullyFrozen = zombie.getFrozenIceHealth() > 0 || zombie.getFrozenDuration() > 0;

                if (isFullyFrozen) {
                    batch.setColor(0.35f, 0.70f, 1.0f, 1f);
                } else if (isChilled) {
                    batch.setColor(0.65f, 0.85f, 1.0f, 1f);
                }

                float currentAnimTime = isFullyFrozen ? 0f : stateTime;
                Map<String, Boolean> visibility = buildArmorVisibility(zombie, type);

                try {
                    pamPlayer.draw(batch, pamPath, clipName, currentAnimTime, currentPixelX, zy, true, visibility);
                } catch (Exception e) {
                    try {
                        pamPlayer.draw(batch, pamPath, "walk", currentAnimTime, currentPixelX, zy, true, visibility);
                    } catch (Exception ignored) {
                        try {
                            pamPlayer.draw(batch, pamPath, null, currentAnimTime, currentPixelX, zy, true, visibility);
                        } catch (Exception ignored2) {}
                    }
                }

                batch.setColor(Color.WHITE);

                if (isFullyFrozen && frozenIceBlockRegion != null) {
                    float blockW = 135f;
                    float blockH = 200f;
                    batch.draw(frozenIceBlockRegion, currentPixelX - blockW / 2f, zy - 40f, blockW, blockH);
                }
            }
        }

        Iterator<DyingZombie> it = dyingZombies.iterator();
        while (it.hasNext()) {
            DyingZombie dz = it.next();
            dz.elapsed += delta;
            String pamPath = dz.type != null ? dz.type.getPamPath() : "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM";

            try {
                pamPlayer.draw(batch, pamPath, "die", dz.elapsed, dz.x, dz.y, false);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, pamPath, "death", dz.elapsed, dz.x, dz.y, false);
                } catch (Exception ignored) {}
            }

            if (dz.elapsed >= dz.duration) {
                it.remove();
            }
        }
    }

    private Map<String, Boolean> buildArmorVisibility(Zombie zombie, ZombieType type) {
        Map<String, Boolean> visibility = new HashMap<>();
        String armorType = zombie.getArmorType();
        if ((armorType == null || armorType.equalsIgnoreCase("none") || armorType.equals("-")) && type != null) {
            armorType = type.getArmorType();
        }

        int curHp = zombie.getArmorHealth();
        int maxHp = zombie.getMaxArmorHealth() > 0 ? zombie.getMaxArmorHealth() : (type != null ? type.getArmorHp() : curHp);

        if (armorType == null || armorType.equalsIgnoreCase("none") || armorType.equals("-") || curHp <= 0) {
            visibility.put("_zombie_armor_states", false);
            visibility.put("zombie_armor_states", false);
            visibility.put("_zombie_armor_cone_states", false);
            visibility.put("zombie_armor_cone_states", false);
            visibility.put("_zombie_armor_bucket_states", false);
            visibility.put("zombie_armor_bucket_states", false);
            visibility.put("_zombie_armor_brick_states", false);
            visibility.put("zombie_armor_brick_states", false);
            visibility.put("_zombie_armor_crown_states", false);
            visibility.put("zombie_armor_crown_states", false);
            visibility.put("_zombie_newspaper_states", false);
            visibility.put("zombie_newspaper_states", false);
            visibility.put("_zombie_newspaper", false);
            visibility.put("zombie_newspaper", false);
            visibility.put("_zombie_newspaper_dmg1", false);
            visibility.put("zombie_newspaper_dmg1", false);
            visibility.put("_zombie_newspaper_dmg2", false);
            visibility.put("zombie_newspaper_dmg2", false);
            return visibility;
        }

        float ratio = maxHp > 0 ? (float) curHp / (float) maxHp : 1.0f;

        if (armorType.equalsIgnoreCase("Cone")) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_cone_states", true);
            visibility.put("zombie_armor_cone_states", true);
            visibility.put("_zombie_armor_cone", true);
            visibility.put("zombie_armor_cone", true);
            if (ratio > 0.66f) {
                visibility.put("zombie_armor_cone_norm", true);
                visibility.put("_zombie_armor_cone_norm", true);
            } else if (ratio > 0.33f) {
                visibility.put("zombie_armor_cone_damage_01", true);
                visibility.put("_zombie_armor_cone_damage_01", true);
            } else {
                visibility.put("zombie_armor_cone_damage_02", true);
                visibility.put("_zombie_armor_cone_damage_02", true);
            }
        } else if (armorType.equalsIgnoreCase("Bucket")) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_bucket_states", true);
            visibility.put("zombie_armor_bucket_states", true);
            visibility.put("_zombie_armor_bucket", true);
            visibility.put("zombie_armor_bucket", true);
            if (ratio > 0.66f) {
                visibility.put("zombie_armor_bucket_norm", true);
                visibility.put("_zombie_armor_bucket_norm", true);
            } else if (ratio > 0.33f) {
                visibility.put("zombie_armor_bucket_damage_01", true);
                visibility.put("_zombie_armor_bucket_damage_01", true);
            } else {
                visibility.put("zombie_armor_bucket_damage_02", true);
                visibility.put("_zombie_armor_bucket_damage_02", true);
            }
        } else if (armorType.equalsIgnoreCase("Brick")) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_brick_states", true);
            visibility.put("zombie_armor_brick_states", true);
            visibility.put("_zombie_armor_brick", true);
            visibility.put("zombie_armor_brick", true);
            if (ratio > 0.66f) {
                visibility.put("zombie_armor_brick_norm", true);
                visibility.put("_zombie_armor_brick_norm", true);
            } else if (ratio > 0.33f) {
                visibility.put("zombie_armor_brick_damage_01", true);
                visibility.put("_zombie_armor_brick_damage_01", true);
            } else {
                visibility.put("zombie_armor_brick_damage_02", true);
                visibility.put("_zombie_armor_brick_damage_02", true);
            }
        } else if (armorType.equalsIgnoreCase("Crown") || armorType.equalsIgnoreCase("Knight")) {
            visibility.put("_zombie_armor_states", true);
            visibility.put("zombie_armor_states", true);
            visibility.put("_zombie_armor_crown_states", true);
            visibility.put("zombie_armor_crown_states", true);
            visibility.put("_zombie_armor_crown", true);
            visibility.put("zombie_armor_crown", true);
            visibility.put("zombie_armor_crown_norm", true);
            visibility.put("_zombie_armor_crown_norm", true);
        } else if (armorType.equalsIgnoreCase("Newspaper")) {
            visibility.put("_zombie_newspaper_states", true);
            visibility.put("zombie_newspaper_states", true);
            visibility.put("zombie_newspaper", true);
            visibility.put("_zombie_newspaper", true);

            if (ratio > 0.66f) {
                visibility.put("_zombie_newspaper", true);
                visibility.put("zombie_newspaper", true);
                visibility.put("_zombie_newspaper_dmg1", false);
                visibility.put("zombie_newspaper_dmg1", false);
                visibility.put("_zombie_newspaper_dmg2", false);
                visibility.put("zombie_newspaper_dmg2", false);
            } else if (ratio > 0.33f) {
                visibility.put("_zombie_newspaper", false);
                visibility.put("zombie_newspaper", false);
                visibility.put("_zombie_newspaper_dmg1", true);
                visibility.put("zombie_newspaper_dmg1", true);
                visibility.put("_zombie_newspaper_dmg2", false);
                visibility.put("zombie_newspaper_dmg2", false);
            } else {
                visibility.put("_zombie_newspaper", false);
                visibility.put("zombie_newspaper", false);
                visibility.put("_zombie_newspaper_dmg1", false);
                visibility.put("zombie_newspaper_dmg1", false);
                visibility.put("_zombie_newspaper_dmg2", true);
                visibility.put("zombie_newspaper_dmg2", true);
            }
        }

        return visibility;
    }
}
