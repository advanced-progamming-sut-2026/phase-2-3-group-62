package model.handler;

import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;
import model.entities.zombie.boss.ZombossType;
import model.entities.zombie.factory.ZombieFactory;
import view.game.GameGrid;
import view.game.renderers.ScreenShake;
import view.game.renderers.ZombossRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombossAbilityHandler {
    private final Random random = new Random();

    public static class ScheduledAction {
        int row;
        int col;
        int delayTicks;
        int actionType;

        public ScheduledAction(int row, int col, int delayTicks, int actionType) {
            this.row = row;
            this.col = col;
            this.delayTicks = delayTicks;
            this.actionType = actionType;
        }
    }

    private final List<ScheduledAction> scheduledActions = new ArrayList<>();

    public void processZomboss(Zomboss zomboss, Game game) {
        if (zomboss == null || !zomboss.isAlive()) return;

        updateScheduledActions(game);
        zomboss.updateBossState();

        if (zomboss.isStunned()) {
            return;
        }

        handleChargeMovement(zomboss, game);
        handleTurbineSuctionPull(zomboss, game);

        if (zomboss.getActionTimer() >= zomboss.getActionCooldownTicks()) {
            zomboss.resetActionTimer();
            executeRandomBossMove(zomboss, game);
        }
    }

    private void updateScheduledActions(Game game) {
        for (int i = scheduledActions.size() - 1; i >= 0; i--) {
            ScheduledAction task = scheduledActions.get(i);
            task.delayTicks--;
            if (task.delayTicks <= 0) {
                Tile targetTile = game.getBoard().getTile(task.row, task.col);
                if (task.actionType == 1) {
                    if (targetTile != null && targetTile.getPlant() != null) {
                        game.removePlant(targetTile.getPlant());
                        targetTile.setPlant(null);
                    }
                    ScreenShake.shake(4f, 0.3f);
                } else if (task.actionType == 2) {
                    if (targetTile != null) {
                        if (targetTile.getPlant() != null) {
                            game.removePlant(targetTile.getPlant());
                            targetTile.setPlant(null);
                        }
                        targetTile.ignite(40);
                    }
                    ScreenShake.shake(3f, 0.25f);
                    Zombie imp = ZombieFactory.createZombie("ZombieDarkImpDragon", game.getDifficultyLevel());
                    if (imp == null) imp = ZombieFactory.createZombie("ZombieImp", game.getDifficultyLevel());
                    if (imp != null) {
                        imp.setX(task.col);
                        imp.setY(task.row);
                        game.getActiveZombies().add(imp);
                    }
                } else if (task.actionType == 3) {
                    if (targetTile != null) {
                        if (targetTile.getPlant() != null) {
                            game.removePlant(targetTile.getPlant());
                            targetTile.setPlant(null);
                        }
                        targetTile.ignite(40);
                    }
                } else if (task.actionType == 4) {
                    if (targetTile != null && targetTile.getPlant() != null) {
                        game.removePlant(targetTile.getPlant());
                        targetTile.setPlant(null);
                    }
                    ScreenShake.shake(4f, 0.3f);
                }
                scheduledActions.remove(i);
            }
        }
    }

    private void executeRandomBossMove(Zomboss zomboss, Game game) {
        if (zomboss.isBossCharging()) return;

        if (zomboss.getZombossType() != ZombossType.TUSKMASTER && random.nextInt(100) < 30) {
            int newTopRow = random.nextInt(4);
            zomboss.setOccupiedRows(newTopRow);
            game.getGameLogMessages().add(zomboss.getName() + " shifted to lanes " + newTopRow + " & " + (newTopRow + 1));
        }

        switch (zomboss.getZombossType()) {
            case SPHINX_INATOR:
                executeSphinxInatorMove(zomboss, game);
                break;
            case DARK_DRAGON:
                executeDarkDragonMove(zomboss, game);
                break;
            case TUSKMASTER:
                executeTuskmasterMove(zomboss, game);
                break;
            case SHARKTRONIC:
                executeSharktronicMove(zomboss, game);
                break;
            default:
                break;
        }
    }

    private void executeSphinxInatorMove(Zomboss zomboss, Game game) {
        int roll = random.nextInt(100);
        if (roll < 45) {
            zomboss.playBossAnimation("rocket_launch", 1.8f);
            int targetCol = random.nextInt(game.getBoard().getColumns() - 2);
            int targetRow = random.nextInt(game.getBoard().getRows());

            float px = GameGrid.getGridStartX() + (targetCol * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
            float py = GameGrid.getGridStartY() + ((4 - targetRow) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

            String pamCowboy = "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_COWBOY/ZOMBOSS_MISSILE_EXPLOSION_COWBOY.PAM";

            ZombossRenderer.triggerBossEffect(pamCowboy, "missile_lock_reticle", px, py, 1.2f, 0.0f);
            ZombossRenderer.triggerFallingProjectile(pamCowboy, "missile", px, py, 0.65f, 1.0f);
            ZombossRenderer.triggerBossEffect(pamCowboy, "messile_explosion", px, py, 0.9f, 1.65f);

            scheduledActions.add(new ScheduledAction(targetRow, targetCol, 17, 1));
            game.getGameLogMessages().add("Sphinx-inator locked target and fired missile at (" + targetCol + ", " + targetRow + ")!");

            for (int k = 0; k < 2; k++) {
                int grX = random.nextInt(game.getBoard().getColumns());
                int grY = random.nextInt(game.getBoard().getRows());
                Tile gt = game.getBoard().getTile(grY, grX);
                if (gt != null && gt.isEmpty()) {
                    game.getBoard().setupGrave(grY, grX, 600, 0, false);
                }
            }
        } else if (roll < 75) {
            zomboss.setBossCharging(true);
            zomboss.setReturningFromCharge(false);
            game.getGameLogMessages().add("Sphinx-inator is charging forward across lanes " + zomboss.getPrimaryRow() + " & " + zomboss.getSecondaryRow() + "!");
        } else {
            zomboss.playBossAnimation("stomp", 1.8f);
            ScreenShake.shake(3.0f, 0.4f);
            spawnZombiesForBoss(zomboss, game, new String[]{"ZombieConehead", "ZombieRa", "NormalZombie"});
        }
    }

    private void executeDarkDragonMove(Zomboss zomboss, Game game) {
        int roll = random.nextInt(100);
        if (roll < 50) {
            zomboss.playBossAnimation("fire_bomb", 1.8f);
            int fireballsCount = 1 + random.nextInt(2);
            String fireballPam = "768/FULL/EFFECTS/ZOMBOSS_DARK_FIREBALL/ZOMBOSS_DARK_FIREBALL.PAM";

            for (int i = 0; i < fireballsCount; i++) {
                int fx = 2 + random.nextInt(game.getBoard().getColumns() - 4);
                int fy = random.nextInt(game.getBoard().getRows());

                float px = GameGrid.getGridStartX() + (fx * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                float py = GameGrid.getGridStartY() + ((4 - fy) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

                ZombossRenderer.triggerFallingProjectile(fireballPam, "fall", px, py, 0.65f, 0.2f * i);
                ZombossRenderer.triggerBossEffect(fireballPam, "impact", px, py, 0.8f, 0.65f + (0.2f * i));

                scheduledActions.add(new ScheduledAction(fy, fx, (int) Math.round(6.5 + (2.0 * i)), 2));
            }
            game.getGameLogMessages().add("Dark Dragon spat fireballs scorching tiles and summoning Dragon Imps!");
        } else if (roll < 80) {
            zomboss.playBossAnimation("fire_attack", 2.2f);
            int r1 = zomboss.getPrimaryRow();
            int r2 = zomboss.getSecondaryRow();
            String firePam = "768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM";

            int maxFireCol = Math.min((int) Math.floor(zomboss.getX()) - 1, game.getBoard().getColumns() - 3);

            for (int c = maxFireCol; c >= 0; c--) {
                float delaySec = (maxFireCol - c) * 0.08f;
                int delayTicks = (int) Math.round(delaySec * 10);

                float px = GameGrid.getGridStartX() + (c * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                float py1 = GameGrid.getGridStartY() + ((4 - r1) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                float py2 = GameGrid.getGridStartY() + ((4 - r2) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

                ZombossRenderer.triggerBossEffect(firePam, "animation", px, py1, 4.0f, delaySec);
                ZombossRenderer.triggerBossEffect(firePam, "animation", px, py2, 4.0f, delaySec);

                scheduledActions.add(new ScheduledAction(r1, c, delayTicks, 3));
                scheduledActions.add(new ScheduledAction(r2, c, delayTicks, 3));
            }

            ScreenShake.shake(5f, 0.6f);
            game.getGameLogMessages().add("Dark Dragon incinerated lanes " + r1 + " & " + r2 + "!");
        } else {
            spawnZombiesForBoss(zomboss, game, new String[]{"ZombieDarkImpDragon", "ZombieDarkBasic", "ZombieKnight"});
        }
    }

    private void executeTuskmasterMove(Zomboss zomboss, Game game) {
        int roll = random.nextInt(100);
        if (roll < 35) {
            zomboss.playBossAnimation("slingshot", 2.0f);
            int targetCol = 2 + random.nextInt(game.getBoard().getColumns() - 4);
            int targetRow = random.nextInt(game.getBoard().getRows());

            float px = GameGrid.getGridStartX() + (targetCol * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
            float py = GameGrid.getGridStartY() + ((4 - targetRow) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

            String pamIceMissile = "768/FULL/EFFECTS/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE/ZOMBOSS_MISSILE_EXPLOSION_ICEAGE.PAM";

            ZombossRenderer.triggerBossEffect(pamIceMissile, "missile_lock_reticle", px, py, 1.2f, 0.0f);
            ZombossRenderer.triggerFallingProjectile(pamIceMissile, "missile", px, py, 0.65f, 1.0f);
            ZombossRenderer.triggerBossEffect(pamIceMissile, "messile_explosion", px, py, 0.9f, 1.65f);

            scheduledActions.add(new ScheduledAction(targetRow, targetCol, 17, 4));
            game.getGameLogMessages().add("Tuskmaster fired an ice missile at (" + targetCol + ", " + targetRow + ")!");
        } else if (roll < 70) {
            int r1 = random.nextInt(game.getBoard().getRows());
            int r2 = (r1 + 1) % game.getBoard().getRows();
            int chosenRow = Math.min(r1, r2);
            int windIndex = Math.min(4, Math.max(1, chosenRow + 1));
            zomboss.playBossAnimation("wind_" + windIndex, 2.0f);

            for (Plant p : game.getActivePlants()) {
                if (p.getY() == r1 || p.getY() == r2) {
                    p.setFreezeLevel(3);
                }
            }

            float cx = GameGrid.getGridStartX() + (GameGrid.GRID_TOTAL_WIDTH / 2f);
            float cy1 = GameGrid.getGridStartY() + ((4 - r1) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
            float cy2 = GameGrid.getGridStartY() + ((4 - r2) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

            String windPam = "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";
            ZombossRenderer.triggerBossEffect(windPam, "animation", cx, cy1, 1.8f, 0.1f);
            ZombossRenderer.triggerBossEffect(windPam, "animation", cx, cy2, 1.8f, 0.1f);

            game.getGameLogMessages().add("Tuskmaster blew freezing wind on lanes " + r1 + " & " + r2 + "!");
        } else if (roll < 88) {
            zomboss.playBossAnimation("slingshot", 2.0f);
            int count = 1 + random.nextInt(2);
            for (int i = 0; i < count; i++) {
                int spawnCol = 2 + random.nextInt(game.getBoard().getColumns() - 4);
                int spawnRow = random.nextInt(game.getBoard().getRows());
                Tile t = game.getBoard().getTile(spawnRow, spawnCol);
                if (t != null && t.getPlant() != null) {
                    t.getPlant().setFreezeLevel(3);
                }

                Zombie frozenZombie = ZombieFactory.createZombie("ZombieIceAgeHunter", game.getDifficultyLevel());
                if (frozenZombie != null) {
                    frozenZombie.setX(spawnCol);
                    frozenZombie.setY(spawnRow);
                    frozenZombie.setFrozenIceHealth(0);
                    frozenZombie.applyFrozen(5.0);
                    game.getActiveZombies().add(frozenZombie);
                }
            }
            ScreenShake.shake(2.5f, 0.2f);
            game.getGameLogMessages().add("Tuskmaster summoned frozen Hunter Zombies onto the lawn!");
        } else {
            int targetCol = 2 + random.nextInt(game.getBoard().getColumns() - 4);
            int glacierIndex = Math.min(6, Math.max(1, 8 - targetCol));
            zomboss.playBossAnimation("glacier_column_" + glacierIndex, 2.2f);

            for (int r = 0; r < game.getBoard().getRows(); r++) {
                Tile t = game.getBoard().getTile(r, targetCol);
                if (t != null && t.getPlant() != null) {
                    t.getPlant().setFreezeLevel(3);
                }

                Zombie frozenZombie = ZombieFactory.createZombie("ZombieIceAgeHunter", game.getDifficultyLevel());
                if (frozenZombie != null) {
                    frozenZombie.setX(targetCol);
                    frozenZombie.setY(r);
                    frozenZombie.setFrozenIceHealth(0);
                    frozenZombie.applyFrozen(5.0);
                    game.getActiveZombies().add(frozenZombie);
                }
            }

            ScreenShake.shake(3.5f, 0.3f);
            game.getGameLogMessages().add("Tuskmaster froze entire column " + targetCol + " and summoned frozen Hunter Zombies in all lanes!");
        }
    }

    private void executeSharktronicMove(Zomboss zomboss, Game game) {
        int roll = random.nextInt(100);
        if (roll < 45) {
            zomboss.playBossAnimation("suction_loop", 3.0f);
            zomboss.setTurbineActive(true, 30);
            ScreenShake.shake(3.5f, 3.0f);
            game.getGameLogMessages().add("Sharktronic Sub activated turbine suction across lanes " + zomboss.getPrimaryRow() + " & " + zomboss.getSecondaryRow() + "!");
        } else if (roll < 80) {
            zomboss.playBossAnimation("tangled_off", 1.8f);
            String sharkPam = "768/FULL/EFFECTS/ZOMBOSS_SHARK_PROJECTILE/ZOMBOSS_SHARK_PROJECTILE.PAM";
            int sharksCount = 1 + random.nextInt(2);

            for (int i = 0; i < sharksCount; i++) {
                int targetRow = random.nextInt(game.getBoard().getRows());
                float startX = GameGrid.getGridStartX() + ((float) zomboss.getX() * GameGrid.TILE_WIDTH);
                ZombossRenderer.triggerSharkProjectile(sharkPam, targetRow, startX);
            }
            game.getGameLogMessages().add("Sharktronic Sub released baby shark projectiles!");
        } else {
            spawnZombiesForBoss(zomboss, game, new String[]{"ZombieBeachOctopus", "ZombieBeachFisherman", "ZombieBeachSnorkel", "NormalZombie"});
        }
    }

    private void handleTurbineSuctionPull(Zomboss zomboss, Game game) {
        if (!zomboss.isTurbineActive()) return;

        int r1 = zomboss.getPrimaryRow();
        int r2 = zomboss.getSecondaryRow();
        double bossX = zomboss.getX();

        if (game.getTickCount() % 4 == 0) {
            for (Plant p : new ArrayList<>(game.getActivePlants())) {
                if (p.getY() == r1 || p.getY() == r2) {
                    Tile curTile = game.getBoard().getTile(p.getY(), p.getX());
                    int nextX = p.getX() + 1;
                    if (nextX >= (int) Math.floor(bossX)) {
                        game.removePlant(p);
                        if (curTile != null) {
                            if (curTile.getPlant() == p) {
                                curTile.setPlant(null);
                            }
                            if (curTile.getPumpkinPlant() == p) {
                                curTile.setPumpkinPlant(null);
                            }
                        }
                        game.incrementPlantsLost();
                        game.getGameLogMessages().add("Sharktronic Sub devoured plant " + p.getName() + "!");
                    } else {
                        Tile nextTile = game.getBoard().getTile(p.getY(), nextX);
                        boolean isPumpkin = p.getName().replace(" ", "").replace("-", "").equalsIgnoreCase("Pumpkin");
                        if (isPumpkin) {
                            if (nextTile != null && nextTile.getPumpkinPlant() == null) {
                                if (curTile != null && curTile.getPumpkinPlant() == p) curTile.setPumpkinPlant(null);
                                p.setX(nextX);
                                nextTile.setPumpkinPlant(p);
                            }
                        } else {
                            if (nextTile != null && nextTile.getPlant() == null) {
                                if (curTile != null && curTile.getPlant() == p) curTile.setPlant(null);
                                p.setX(nextX);
                                nextTile.setPlant(p);
                            }
                        }
                    }
                }
            }
        }
    }

    private void handleChargeMovement(Zomboss zomboss, Game game) {
        if (!zomboss.isBossCharging()) return;

        double speed = 0.25;
        if (!zomboss.isReturningFromCharge()) {
            zomboss.setX(zomboss.getX() - speed);
            int curCol = (int) Math.round(zomboss.getX());
            int r1 = zomboss.getPrimaryRow();
            int r2 = zomboss.getSecondaryRow();

            for (int c = Math.max(0, curCol); c < game.getBoard().getColumns(); c++) {
                Tile t1 = game.getBoard().getTile(r1, c);
                Tile t2 = game.getBoard().getTile(r2, c);
                if (t1 != null) {
                    if (t1.getPlant() != null) {
                        game.removePlant(t1.getPlant());
                        t1.setPlant(null);
                    }
                    if (t1.getPumpkinPlant() != null) {
                        game.removePlant(t1.getPumpkinPlant());
                        t1.setPumpkinPlant(null);
                    }
                }
                if (t2 != null) {
                    if (t2.getPlant() != null) {
                        game.removePlant(t2.getPlant());
                        t2.setPlant(null);
                    }
                    if (t2.getPumpkinPlant() != null) {
                        game.removePlant(t2.getPumpkinPlant());
                        t2.setPumpkinPlant(null);
                    }
                }
            }

            if (zomboss.getX() <= 1.0) {
                zomboss.setReturningFromCharge(true);
            }
        } else {
            zomboss.setX(zomboss.getX() + speed * 1.5);
            if (zomboss.getX() >= zomboss.getOriginalX()) {
                zomboss.setX(zomboss.getOriginalX());
                zomboss.setBossCharging(false);
                zomboss.setReturningFromCharge(false);
            }
        }
    }

    private void spawnZombiesForBoss(Zomboss zomboss, Game game, String[] pool) {
        int count = 1 + random.nextInt(2);
        for (int i = 0; i < count; i++) {
            String zName = pool[random.nextInt(pool.length)];
            Zombie z = ZombieFactory.createZombie(zName, game.getDifficultyLevel());
            if (z != null) {
                z.setX(8.0);
                z.setY(random.nextInt(game.getBoard().getRows()));
                game.getActiveZombies().add(z);
            }
        }
        game.getGameLogMessages().add(zomboss.getName() + " summoned reinforcements!");
    }
}
