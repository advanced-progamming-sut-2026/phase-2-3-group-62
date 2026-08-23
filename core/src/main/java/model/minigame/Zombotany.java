package model.minigame;

import model.Game;
import model.board.Bullet;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.entities.zombie.ZombieEffect;
import model.entities.zombie.factory.ZombieFactory;
import view.game.mainGame.GameGrid;
import view.game.renderers.ProjectileRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Zombotany extends MiniGame {
    private int stageDifficulty;
    private int maxStage;
    private int zombiesDefeated;
    private int targetZombies;

    public Zombotany() {
        super("Zombotany");
        this.stageDifficulty = 1;
        this.maxStage = 3;
        this.zombiesDefeated = 0;
        this.targetZombies = 15;
    }

    public int getStageDifficulty() {
        return stageDifficulty;
    }

    public void setStageDifficulty(int stageDifficulty) {
        this.stageDifficulty = Math.min(stageDifficulty, maxStage);
        updateStageParameters();
    }

    private void updateStageParameters() {
        switch (stageDifficulty) {
            case 1:
                targetZombies = 15;
                break;
            case 2:
                targetZombies = 25;
                break;
            case 3:
                targetZombies = 35;
                break;
            default:
                targetZombies = 15;
        }
    }

    public void updateMiniGame(Game game) {
        if (game.getTickCount() == 1) {
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                Tile t = game.getBoard().getTile(z.getY(), (int) Math.round(z.getX()));
                if (t != null) t.setZombie(null);
                game.removeZombie(z);
            }
            spawnHybridWave(game);
            game.getGameLogMessages().add("Zombotany: Stage " + stageDifficulty + " started! Target: " + targetZombies + " kills");
        }

        if (zombiesDefeated >= targetZombies) {
            if (stageDifficulty < maxStage) {
                completeLevel(stageDifficulty, zombiesDefeated);
                stageDifficulty++;
                updateStageParameters();
                zombiesDefeated = 0;
                game.getGameLogMessages().add("Zombotany: Stage " + (stageDifficulty - 1) + " complete! Moving to Stage " + stageDifficulty);
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    Tile t = game.getBoard().getTile(z.getY(), (int) Math.round(z.getX()));
                    if (t != null) t.setZombie(null);
                    game.removeZombie(z);
                }
                spawnHybridWave(game);
                return;
            } else {
                game.setWon(true);
                game.stop();
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                return;
            }
        }

        for (Zombie zombie : new ArrayList<>(game.getActiveZombies())) {
            String zName = zombie.getName().toLowerCase();

            if (zName.contains("peashooter") && game.getTickCount() % 25 == 0) {
                boolean hasPlantAhead = false;
                for (Plant p : game.getActivePlants()) {
                    if (p.getY() == zombie.getY() && p.getX() < zombie.getX()) {
                        hasPlantAhead = true;
                        break;
                    }
                }
                if (hasPlantAhead) {
                    Bullet pea = new Bullet(20, zombie.getY(), zombie.getX() - 0.2, Bullet.BulletType.NORMAL, false, false, 0);
                    pea.setDx(-0.35);
                    pea.setPlantName("ZombiePea");
                    game.addBullet(pea);
                    game.getGameLogMessages().add("Zombotany: Peashooter Zombie fired a pea down row " + zombie.getY() + "!");
                }
            }

            if (zName.contains("jalapeno")) {
                zombie.incrementJalapenoTimer();
                if (zombie.getZombotanyJalapenoTimer() >= 100) {
                    float fx = GameGrid.getGridStartX() + (GameGrid.GRID_TOTAL_WIDTH / 2f);
                    float fy = GameGrid.getGridStartY() + ((4 - zombie.getY()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                    ProjectileRenderer.triggerStaticImpact("768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", fx, fy);

                    List<Plant> toBurn = new ArrayList<>();
                    for (Plant p : game.getActivePlants()) {
                        if (p.getY() == zombie.getY()) {
                            toBurn.add(p);
                        }
                    }
                    for (Plant bp : toBurn) {
                        Tile t = game.getBoard().getTile(bp.getY(), bp.getX());
                        if (t != null) t.setPlant(null);
                        game.removePlant(bp);
                    }

                    game.getGameLogMessages().add("Zombotany: Jalapeno Zombie detonated and incinerated row " + zombie.getY() + "!");
                    Tile zTile = game.getBoard().getTile(zombie.getY(), (int) Math.round(zombie.getX()));
                    if (zTile != null) zTile.setZombie(null);
                    game.removeZombie(zombie);
                    continue;
                }
            }

            if (zName.contains("squash") && !zombie.hasEffect(ZombieEffect.FROZEN)) {
                Plant targetPlant = null;
                for (Plant p : game.getActivePlants()) {
                    if (p.getY() == zombie.getY() && Math.abs(p.getX() - zombie.getX()) <= 0.45) {
                        targetPlant = p;
                        break;
                    }
                }

                if (targetPlant != null) {
                    float hitPx = GameGrid.getGridStartX() + (targetPlant.getX() * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                    float hitPy = GameGrid.getGridStartY() + ((4 - targetPlant.getY()) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                    ProjectileRenderer.triggerStaticImpact("768/INITIAL/EFFECTS/MELON_EXPLODE/MELON_EXPLODE.PAM", hitPx, hitPy);

                    game.getGameLogMessages().add("Zombotany: Squash Zombie squashed " + targetPlant.getName() + " at (" + targetPlant.getX() + ", " + targetPlant.getY() + ")!");
                    Tile pTile = game.getBoard().getTile(targetPlant.getY(), targetPlant.getX());
                    if (pTile != null) pTile.setPlant(null);
                    game.removePlant(targetPlant);

                    Tile zTile = game.getBoard().getTile(zombie.getY(), (int) Math.round(zombie.getX()));
                    if (zTile != null) zTile.setZombie(null);
                    game.removeZombie(zombie);
                    continue;
                }
            }
        }

        List<Zombie> toRemove = new ArrayList<>();
        for (Zombie z : game.getActiveZombies()) {
            if (!z.isAlive()) {
                toRemove.add(z);
                zombiesDefeated++;
                game.getScoreGame().onZombieKilled(z, game);
                game.getGameLogMessages().add("Zombotany: " + z.getName() + " defeated! (" + zombiesDefeated + "/" + targetZombies + ")");
            }
        }
        for (Zombie z : toRemove) {
            game.getActiveZombies().remove(z);
            Tile t = game.getBoard().getTile(z.getY(), (int) Math.round(z.getX()));
            if (t != null) t.setZombie(null);
        }

        if (game.getTickCount() % 150 == 0 && game.getActiveZombies().size() < 5) {
            spawnHybridWave(game);
        }

        for (Zombie z : game.getActiveZombies()) {
            if (z.getX() <= 0) {
                game.setLost(true);
                game.stop();
                game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
                return;
            }
        }
    }

    private void spawnHybridWave(Game game) {
        Random rand = new Random();
        String[] hybridTypes = {"PeashooterZombie", "WallnutZombie", "JalapenoZombie", "SquashZombie"};
        int numZombies = 2 + stageDifficulty + rand.nextInt(stageDifficulty + 1);

        for (int i = 0; i < numZombies; i++) {
            String type = hybridTypes[rand.nextInt(hybridTypes.length)];
            int lane = rand.nextInt(game.getBoard().getRows());
            int spawnCol = game.getBoard().getColumns() - 1;

            Zombie z = ZombieFactory.createZombieAtColumn(type, lane, spawnCol);
            if (z == null) {
                int hp = 200;
                double speed = 0.185;
                int damage = 20;
                if (type.equalsIgnoreCase("WallnutZombie")) {
                    hp = 1200;
                    speed = 0.08;
                } else if (type.equalsIgnoreCase("SquashZombie")) {
                    speed = 0.38;
                    damage = 50;
                } else if (type.equalsIgnoreCase("JalapenoZombie")) {
                    hp = 250;
                    speed = 0.20;
                }
                z = new Zombie(type, hp, speed, damage);
                z.setX(spawnCol);
                z.setY(lane);
            }

            game.addZombie(z);
            Tile t = game.getBoard().getTile(lane, spawnCol);
            if (t != null) t.setZombie(z);
        }
        game.getGameLogMessages().add("Zombotany: Hybrid zombie wave spawned!");
    }
}
