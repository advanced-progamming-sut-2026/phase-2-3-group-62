package view.game.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;
import model.entities.zombie.boss.ZombossType;
import pvz.libpvz.pam.PamPlayer;
import view.game.GameGrid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ZombossRenderer {
    private final PamPlayer pamPlayer;
    private final Map<Zomboss, Float> smoothPixelX = new HashMap<>();
    private final Map<Zomboss, Float> smoothPixelY = new HashMap<>();

    public static class BossVisualEffect {
        String pamPath;
        String clipName;
        float x;
        float y;
        float elapsed;
        float duration;
        float delay;

        public BossVisualEffect(String pamPath, String clipName, float x, float y, float duration, float delay) {
            this.pamPath = pamPath;
            this.clipName = clipName;
            this.x = x;
            this.y = y;
            this.elapsed = 0f;
            this.duration = duration;
            this.delay = delay;
        }
    }

    public static class FallingProjectileEffect {
        String pamPath;
        String clipName;
        float targetX;
        float targetY;
        float currentY;
        float elapsed;
        float duration;
        float delay;

        public FallingProjectileEffect(String pamPath, String clipName, float targetX, float targetY, float duration, float delay) {
            this.pamPath = pamPath;
            this.clipName = clipName;
            this.targetX = targetX;
            this.targetY = targetY;
            this.currentY = targetY + 650f;
            this.elapsed = 0f;
            this.duration = duration;
            this.delay = delay;
        }
    }

    public static class SharkProjectileEffect {
        String pamPath;
        int row;
        float x;
        float elapsed;
        String state;
        float stateTimer;
        boolean hasEaten;

        public SharkProjectileEffect(String pamPath, int row, float startX) {
            this.pamPath = pamPath;
            this.row = row;
            this.x = startX;
            this.elapsed = 0f;
            this.state = "submerge";
            this.stateTimer = 0f;
            this.hasEaten = false;
        }
    }

    private static final List<BossVisualEffect> activeBossEffects = new ArrayList<>();
    private static final List<FallingProjectileEffect> activeProjectiles = new ArrayList<>();
    private static final List<SharkProjectileEffect> activeSharks = new ArrayList<>();

    public ZombossRenderer(PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
    }

    public static void triggerBossEffect(String pamPath, String clipName, float pixelX, float pixelY, float duration, float delay) {
        if (pamPath != null && !pamPath.isEmpty()) {
            activeBossEffects.add(new BossVisualEffect(pamPath, clipName, pixelX, pixelY, duration, delay));
        }
    }

    public static void triggerFallingProjectile(String pamPath, String clipName, float targetX, float targetY, float duration, float delay) {
        if (pamPath != null && !pamPath.isEmpty()) {
            activeProjectiles.add(new FallingProjectileEffect(pamPath, clipName, targetX, targetY, duration, delay));
        }
    }

    public static void triggerSharkProjectile(String pamPath, int row, float startX) {
        if (pamPath != null && !pamPath.isEmpty()) {
            activeSharks.add(new SharkProjectileEffect(pamPath, row, startX));
        }
    }

    public void render(SpriteBatch batch, Game game, float stateTime, float delta) {
        if (game == null) return;

        renderScorchedTiles(batch, game, stateTime);

        float startX = GameGrid.getGridStartX();
        float startY = GameGrid.getGridStartY();

        for (Zombie z : game.getActiveZombies()) {
            if (z instanceof Zomboss) {
                Zomboss boss = (Zomboss) z;
                boss.updateAnimationTimer(delta);

                float targetPx = startX + ((float) boss.getX() * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH * 0.5f);
                float curPx = smoothPixelX.getOrDefault(boss, targetPx);
                curPx = MathUtils.lerp(curPx, targetPx, Math.min(delta * 10f, 1f));
                smoothPixelX.put(boss, curPx);

                float yOffsetRatio = (boss.getZombossType() == ZombossType.TUSKMASTER) ? 0.95f : 0.15f;
                float targetPy = startY + ((4 - boss.getSecondaryRow()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT * yOffsetRatio);
                float curPy = smoothPixelY.getOrDefault(boss, targetPy);
                curPy = MathUtils.lerp(curPy, targetPy, Math.min(delta * 8f, 1f));
                smoothPixelY.put(boss, curPy);

                String pamPath = getPamPathForBoss(boss);
                String clip = getClipForBoss(boss);

                if (boss.isStunned()) {
                    batch.setColor(0.75f, 0.75f, 0.85f, 1f);
                } else {
                    batch.setColor(Color.WHITE);
                }

                try {
                    pamPlayer.draw(batch, pamPath, clip, stateTime, curPx, curPy, true);
                } catch (Exception e) {
                    try {
                        pamPlayer.draw(batch, pamPath, "idle", stateTime, curPx, curPy, true);
                    } catch (Exception ignored) {
                        try {
                            pamPlayer.draw(batch, pamPath, "", stateTime, curPx, curPy, true);
                        } catch (Exception ignored2) {}
                    }
                }

                batch.setColor(Color.WHITE);

                if (boss.isTurbineActive()) {
                    try {
                        float windY1 = startY + ((4 - boss.getPrimaryRow()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                        float windY2 = startY + ((4 - boss.getSecondaryRow()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                        pamPlayer.draw(batch, "768/FULL/EFFECTS/ZOMBOSS_TURBINE_WIND/ZOMBOSS_TURBINE_WIND.PAM", "animation", stateTime, curPx - 260f, windY1, true);
                        pamPlayer.draw(batch, "768/FULL/EFFECTS/ZOMBOSS_TURBINE_WIND/ZOMBOSS_TURBINE_WIND.PAM", "animation", stateTime, curPx - 260f, windY2, true);
                    } catch (Exception ignored) {}
                }
            }
        }

        Iterator<SharkProjectileEffect> sharkIt = activeSharks.iterator();
        while (sharkIt.hasNext()) {
            SharkProjectileEffect shark = sharkIt.next();
            shark.elapsed += delta;
            shark.stateTimer += delta;

            float sharkPy = startY + ((4 - shark.row) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

            if (shark.state.equals("submerge")) {
                if (shark.stateTimer >= 0.5f) {
                    shark.state = "walk";
                    shark.stateTimer = 0f;
                }
            } else if (shark.state.equals("walk")) {
                shark.x -= 280f * delta;

                int currentGridCol = (int) Math.floor((shark.x - startX) / GameGrid.TILE_WIDTH);
                if (currentGridCol >= 0 && currentGridCol < game.getBoard().getColumns()) {
                    Tile tile = game.getBoard().getTile(shark.row, currentGridCol);
                    if (tile != null && tile.getPlant() != null && !shark.hasEaten) {
                        Plant plant = tile.getPlant();
                        float plantCenterX = startX + (currentGridCol * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                        if (Math.abs(shark.x - plantCenterX) <= GameGrid.TILE_WIDTH * 0.45f) {
                            shark.x = plantCenterX;
                            shark.state = "attack";
                            shark.stateTimer = 0f;
                            shark.hasEaten = true;
                            game.removePlant(plant);
                            tile.setPlant(null);
                            ScreenShake.shake(3.5f, 0.25f);
                            game.getGameLogMessages().add("Baby shark devoured " + plant.getName() + " at (" + currentGridCol + ", " + shark.row + ")!");
                        }
                    }
                }
            }

            try {
                pamPlayer.draw(batch, shark.pamPath, shark.state, shark.elapsed, shark.x, sharkPy, true);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, shark.pamPath, "walk", shark.elapsed, shark.x, sharkPy, true);
                } catch (Exception ignored) {}
            }

            if (shark.state.equals("attack") && shark.stateTimer >= 0.9f) {
                sharkIt.remove();
            } else if (shark.x < startX - 100f) {
                sharkIt.remove();
            }
        }

        Iterator<FallingProjectileEffect> projIt = activeProjectiles.iterator();
        while (projIt.hasNext()) {
            FallingProjectileEffect proj = projIt.next();
            if (proj.delay > 0) {
                proj.delay -= delta;
                continue;
            }

            proj.elapsed += delta;
            float progress = Math.min(1.0f, proj.elapsed / proj.duration);
            proj.currentY = (proj.targetY + 650f) - (650f * Interpolation.pow2In.apply(progress));

            try {
                pamPlayer.draw(batch, proj.pamPath, proj.clipName, proj.elapsed, proj.targetX, proj.currentY, false);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, proj.pamPath, "animation", proj.elapsed, proj.targetX, proj.currentY, false);
                } catch (Exception ignored) {}
            }

            if (proj.elapsed >= proj.duration) {
                projIt.remove();
            }
        }

        Iterator<BossVisualEffect> it = activeBossEffects.iterator();
        while (it.hasNext()) {
            BossVisualEffect eff = it.next();
            if (eff.delay > 0) {
                eff.delay -= delta;
                continue;
            }

            eff.elapsed += delta;
            try {
                pamPlayer.draw(batch, eff.pamPath, eff.clipName, eff.elapsed, eff.x, eff.y, false);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, eff.pamPath, "animation", eff.elapsed, eff.x, eff.y, false);
                } catch (Exception ignored) {
                    try {
                        pamPlayer.draw(batch, eff.pamPath, "", eff.elapsed, eff.x, eff.y, false);
                    } catch (Exception ignored2) {}
                }
            }
            if (eff.elapsed >= eff.duration) {
                it.remove();
            }
        }
    }

    private void renderScorchedTiles(SpriteBatch batch, Game game, float stateTime) {
        float startX = GameGrid.getGridStartX();
        float startY = GameGrid.getGridStartY();

        for (int r = 0; r < game.getBoard().getRows(); r++) {
            for (int c = 0; c < game.getBoard().getColumns(); c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile != null && (tile.isOnFire() || tile.getFireTimerTicks() > 0)) {
                    float tx = startX + (c * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                    float ty = startY + ((4 - r) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                    try {
                        pamPlayer.draw(batch, "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", "animation", stateTime, tx, ty, true);
                    } catch (Exception e) {
                        try {
                            pamPlayer.draw(batch, "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", "", stateTime, tx, ty, true);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }

    private String getPamPathForBoss(Zomboss boss) {
        switch (boss.getZombossType()) {
            case SPHINX_INATOR:
                return "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_ZOMBOSS/ZOMBIE_EGYPT_ZOMBOSS.PAM";
            case DARK_DRAGON:
                return "768/FULL/ZOMBIE/ZOMBIE_DARK_ZOMBOSS/ZOMBIE_DARK_ZOMBOSS.PAM";
            case TUSKMASTER:
                return "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_ZOMBOSS/ZOMBIE_ICEAGE_ZOMBOSS.PAM";
            case SHARKTRONIC:
            default:
                return "768/FULL/ZOMBIE/ZOMBIE_BEACH_ZOMBOSS/ZOMBIE_BEACH_ZOMBOSS.PAM";
        }
    }

    private String getClipForBoss(Zomboss boss) {
        if (!boss.isAlive()) return "die";
        if (boss.isStunned()) {
            if (boss.getZombossType() == ZombossType.SPHINX_INATOR || boss.getZombossType() == ZombossType.SHARKTRONIC) {
                return "stun_loop";
            }
            return "stun";
        }

        if (boss.isBossCharging()) {
            return boss.isReturningFromCharge() ? "walk_bacwards" : "walk_forward";
        }

        return boss.getActiveAnimation();
    }
}
