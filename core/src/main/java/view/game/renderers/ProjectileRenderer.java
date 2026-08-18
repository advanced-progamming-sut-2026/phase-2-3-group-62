package view.game.renderers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import model.Game;
import model.board.Bullet;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import view.game.GameGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ProjectileRenderer {
    private final PamPlayer pamPlayer;
    private final TextureBank textureBank;
    private final Map<Bullet, Float> smoothPixelX = new HashMap<>();

    private static class ImpactEffect {
        String pamPath;
        String clipName;
        float x;
        float y;
        float duration;
        float elapsed;

        ImpactEffect(String pamPath, String clipName, float x, float y, float duration) {
            this.pamPath = pamPath;
            this.clipName = clipName;
            this.x = x;
            this.y = y;
            this.duration = duration;
            this.elapsed = 0f;
        }
    }

    private static final List<ImpactEffect> activeImpacts = new ArrayList<>();

    public ProjectileRenderer(TextureBank textureBank, PamPlayer pamPlayer) {
        this.textureBank = textureBank;
        this.pamPlayer = pamPlayer;
    }

    public static void triggerStaticImpact(String pamPath, float pixelX, float pixelY) {
        triggerStaticImpact(pamPath, "animation", pixelX, pixelY);
    }

    public static void triggerStaticImpact(String pamPath, String clipName, float pixelX, float pixelY) {
        if (pamPath != null && !pamPath.isEmpty()) {
            activeImpacts.add(new ImpactEffect(pamPath, clipName, pixelX, pixelY, 0.45f));
        }
    }

    public void render(SpriteBatch batch, Game game, float stateTime) {
        if (game == null) return;

        smoothPixelX.keySet().removeIf(b -> !game.getBullets().contains(b));

        float startX = GameGrid.getGridStartX();
        float startY = GameGrid.getGridStartY();

        for (Bullet bullet : new ArrayList<>(game.getBullets())) {
            int row = bullet.getRow();
            double col = bullet.getColumn();

            float targetPx = startX + ((float) col * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
            float currentPx = smoothPixelX.getOrDefault(bullet, targetPx);
            currentPx = MathUtils.lerp(currentPx, targetPx, 0.35f);
            smoothPixelX.put(bullet, currentPx);

            float basePy = startY + ((4 - row) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

            Bullet.BulletType type = bullet.getType();
            boolean isLobbed = (type == Bullet.BulletType.LOB || (bullet.getPlantName() != null && (
                bullet.getPlantName().equalsIgnoreCase("Winter Melon") ||
                    bullet.getPlantName().equalsIgnoreCase("Melon-pult") ||
                    bullet.getPlantName().equalsIgnoreCase("Cabbage-pult") ||
                    bullet.getPlantName().equalsIgnoreCase("Pepper-pult") ||
                    bullet.getPlantName().equalsIgnoreCase("Kernel-pult")
            )));

            float py = basePy;
            if (isLobbed) {
                double startC = bullet.getStartColumn();
                double targetC = Math.max(startC + 1.0, bullet.getTargetColumn());
                float progress = (float) Math.min(1.0, Math.max(0.0, (col - startC) / (targetC - startC)));
                float arcHeight = 160f * (4f * progress * (1f - progress));
                py += arcHeight;
            }

            String pamPath = getPamPathForBullet(bullet);
            String clipName = getClipNameForBullet(bullet);

            try {
                pamPlayer.draw(batch, pamPath, clipName, stateTime, currentPx, py, true);
            } catch (Exception e1) {
                try {
                    pamPlayer.draw(batch, pamPath, "animation", stateTime, currentPx, py, true);
                } catch (Exception e2) {
                    try {
                        pamPlayer.draw(batch, pamPath, "idle", stateTime, currentPx, py, true);
                    } catch (Exception e3) {
                        try {
                            pamPlayer.draw(batch, pamPath, "", stateTime, currentPx, py, true);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        Iterator<ImpactEffect> it = activeImpacts.iterator();
        while (it.hasNext()) {
            ImpactEffect effect = it.next();
            effect.elapsed += 0.016f;

            try {
                pamPlayer.draw(batch, effect.pamPath, effect.clipName, effect.elapsed, effect.x, effect.y, false);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, effect.pamPath, "animation", effect.elapsed, effect.x, effect.y, false);
                } catch (Exception ignored) {
                    try {
                        pamPlayer.draw(batch, effect.pamPath, "", effect.elapsed, effect.x, effect.y, false);
                    } catch (Exception ignored2) {}
                }
            }

            if (effect.elapsed >= effect.duration) {
                it.remove();
            }
        }
    }

    private String getPamPathForBullet(Bullet bullet) {
        if (bullet == null) {
            return "768/INITIAL/EFFECTS/SLINGPEA_PROJECTILE/SLINGPEA_PROJECTILE.PAM";
        }

        String plantName = bullet.getPlantName() != null ? bullet.getPlantName().toLowerCase() : "";

        if (plantName.contains("winter melon") || plantName.contains("wintermelon")) {
            return "768/FULL/EFFECTS/T_WINTERMELON_PROJECTILE/T_WINTERMELON_PROJECTILE.PAM";
        }
        if (plantName.contains("melon")) {
            return "768/INITIAL/EFFECTS/T_MELON_PROJECTILE/T_MELON_PROJECTILE.PAM";
        }
        if (plantName.contains("pepper")) {
            return "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE/PEPPERPULT_PROJECTILE.PAM";
        }
        if (plantName.contains("cabbage")) {
            return "768/INITIAL/EFFECTS/CABBAGEPULT_PLANTFOOD_PROJECTILE/CABBAGEPULT_PLANTFOOD_PROJECTILE.PAM";
        }
        if (plantName.contains("kernel")) {
            return "768/FULL/EFFECTS/CORNFETTIPOPPER_PROJECTILE/CORNFETTIPOPPER_PROJECTILE.PAM";
        }
        if (plantName.contains("goo")) {
            return "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
        }
        if (plantName.contains("cactus")) {
            return "768/INITIAL/EFFECTS/CACTUS_PROJECTILE/CACTUS_PROJECTILE.PAM";
        }
        if (plantName.contains("laser")) {
            return "768/FULL/EFFECTS/LASERBEAN_LASER/LASERBEAN_LASER.PAM";
        }
        if (plantName.contains("starfruit") || plantName.contains("star")) {
            return "768/INITIAL/EFFECTS/T_STARFRUIT_PROJECTILE/T_STARFRUIT_PROJECTILE.PAM";
        }
        if (plantName.contains("caulipower")) {
            return "768/INITIAL/EFFECTS/CAULIPOWER_PROJECTILE/CAULIPOWER_PROJECTILE.PAM";
        }
        if (plantName.contains("electric") || plantName.contains("blueberry")) {
            return "768/FULL/EFFECTS/ZOMBIE_GATE_ELECTRICITY/ZOMBIE_GATE_ELECTRICITY.PAM";
        }
        if (plantName.contains("bowling")) {
            int dmg = bullet.getDamage();
            if (dmg <= 40) {
                return "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE1/BOWLINGBULB_PROJECTILE1.PAM";
            } else if (dmg <= 120) {
                return "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE2/BOWLINGBULB_PROJECTILE2.PAM";
            } else {
                return "768/FULL/EFFECTS/BOWLINGBULB_PROJECTILE3/BOWLINGBULB_PROJECTILE3.PAM";
            }
        }

        Bullet.BulletType type = bullet.getType();
        if (type == Bullet.BulletType.FIRE) {
            return "768/INITIAL/EFFECTS/T_FIRE_PEA/T_FIRE_PEA.PAM";
        }
        if (type == Bullet.BulletType.ICE) {
            return "768/FULL/EFFECTS/SEASHOOTER_PROJECTILE/SEASHOOTER_PROJECTILE.PAM";
        }
        if (type == Bullet.BulletType.POISON) {
            return "768/INITIAL/EFFECTS/GOOPEASHOOTER_PROJECTILES/GOOPEASHOOTER_PROJECTILES.PAM";
        }
        if (type == Bullet.BulletType.STRIKE_THROUGH) {
            return "768/INITIAL/EFFECTS/CACTUS_PROJECTILE/CACTUS_PROJECTILE.PAM";
        }
        if (type == Bullet.BulletType.LASER) {
            return "768/FULL/EFFECTS/LASERBEAN_LASER/LASERBEAN_LASER.PAM";
        }
        if (type == Bullet.BulletType.ELECTRIC) {
            return "768/FULL/EFFECTS/ZOMBIE_GATE_ELECTRICITY/ZOMBIE_GATE_ELECTRICITY.PAM";
        }
        if (type == Bullet.BulletType.MAGIC || type == Bullet.BulletType.HOMING) {
            return "768/INITIAL/EFFECTS/CAULIPOWER_PROJECTILE/CAULIPOWER_PROJECTILE.PAM";
        }
        if (type == Bullet.BulletType.LOB) {
            return "768/INITIAL/EFFECTS/CABBAGEPULT_PLANTFOOD_PROJECTILE/CABBAGEPULT_PLANTFOOD_PROJECTILE.PAM";
        }

        return "768/INITIAL/EFFECTS/SLINGPEA_PROJECTILE/SLINGPEA_PROJECTILE.PAM";
    }

    private String getClipNameForBullet(Bullet bullet) {
        if (bullet == null) return "animation";
        String plantName = bullet.getPlantName() != null ? bullet.getPlantName().toLowerCase() : "";
        if (plantName.contains("goo") || bullet.getType() == Bullet.BulletType.POISON) {
            return "projectile_t1";
        }
        return "animation";
    }

    public static String getExplosionPamForBullet(Bullet bullet) {
        if (bullet == null) return "768/FULL/EFFECTS/REDSTINGER_PROJECTILE_HIT/REDSTINGER_PROJECTILE_HIT.PAM";
        String plantName = bullet.getPlantName() != null ? bullet.getPlantName().toLowerCase() : "";

        if (plantName.contains("winter melon") || plantName.contains("wintermelon")) {
            return "768/FULL/EFFECTS/WINTERMELON_EXPLODE/WINTERMELON_EXPLODE.PAM";
        }
        if (plantName.contains("melon")) {
            return "768/INITIAL/EFFECTS/MELON_EXPLODE/MELON_EXPLODE.PAM";
        }
        if (plantName.contains("pepper")) {
            return "768/FULL/EFFECTS/PEPPERPULT_PROJECTILE_PF_SPLAT/PEPPERPULT_PROJECTILE_PF_SPLAT.PAM";
        }
        if (plantName.contains("cabbage")) {
            return "768/INITIAL/EFFECTS/SPLAT_CABBAGEPULT/SPLAT_CABBAGEPULT.PAM";
        }
        if (bullet.getType() == Bullet.BulletType.FIRE) {
            return "768/INITIAL/EFFECTS/TUMBLEWEED_FIRE_EFFECT/TUMBLEWEED_FIRE_EFFECT.PAM";
        }

        return "768/FULL/EFFECTS/REDSTINGER_PROJECTILE_HIT/REDSTINGER_PROJECTILE_HIT.PAM";
    }
}
