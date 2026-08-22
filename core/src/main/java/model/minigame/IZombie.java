package model.minigame;

import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.zombie.Zombie;
import model.entities.zombie.factory.ZombieFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IZombie extends MiniGame {
    private int brainsEaten;
    private boolean[] brainRowEaten;
    private int stageLevel;
    private int maxStageLevel;
    private List<String> stageZombiePool;
    private int redLineColumn;

    public IZombie() {
        super("IZombie");
        this.brainsEaten = 0;
        this.brainRowEaten = new boolean[5];
        this.stageLevel = 1;
        this.maxStageLevel = 3;
        this.stageZombiePool = new ArrayList<>();
        this.redLineColumn = 3;
    }

    public void setupStage(Game game, int level) {
        this.stageLevel = level;
        this.brainsEaten = 0;
        this.brainRowEaten = new boolean[5];
        game.setSunCount(150);

        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            Tile t = game.getBoard().getTile(p.getY(), p.getX());
            if (t != null) t.setPlant(null);
            game.removePlant(p);
        }
        for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
            Tile t = game.getBoard().getTile(z.getY(), (int) Math.round(z.getX()));
            if (t != null) t.setZombie(null);
            game.removeZombie(z);
        }

        setupStageZombiePool(level);

        for (int row = 0; row < 5; row++) {
            Zombie sunZombie = ZombieFactory.createZombieAtColumn("BucketZombie", row, 8, game.getDifficultyLevel());
            if (sunZombie == null) {
                sunZombie = new Zombie("BucketZombie", 190, 0.05, 20);
                sunZombie.setArmorHealth(1100);
                sunZombie.setArmorType("BUCKET");
                sunZombie.setX(8.0);
                sunZombie.setY(row);
            } else {
                sunZombie.setSpeed(0.05);
                sunZombie.setX(8.0);
                sunZombie.setY(row);
            }
            game.addZombie(sunZombie);
            Tile t = game.getBoard().getTile(row, 8);
            if (t != null) t.setZombie(sunZombie);
        }

        Random rand = new Random();
        String[] plantTypes;

        if (level == 1) {
            plantTypes = new String[]{"Peashooter", "Sunflower", "Wall-nut", "Snow Pea"};
        } else if (level == 2) {
            plantTypes = new String[]{"Peashooter", "Sunflower", "Wall-nut", "Snow Pea", "Repeater", "Squash"};
        } else {
            plantTypes = new String[]{"Repeater", "Snow Pea", "Wall-nut", "Tall-nut", "Cabbage-pult", "Melon-pult", "Squash", "Potato Mine"};
        }

        for (int row = 0; row < 5; row++) {
            for (int col = 0; col <= redLineColumn; col++) {
                int spawnChance = (level == 1) ? 75 : (level == 2 ? 85 : 95);
                if (rand.nextInt(100) < spawnChance) {
                    Tile t = game.getBoard().getTile(row, col);
                    if (t != null && t.getPlant() == null) {
                        String type = plantTypes[rand.nextInt(plantTypes.length)];
                        Plant p = PlantFactory.createPlant(type);
                        if (p != null) {
                            p.setX(col);
                            p.setY(row);
                            game.addPlant(p);
                            t.setPlant(p);
                        }
                    }
                }
            }
        }
    }

    private void setupStageZombiePool(int level) {
        stageZombiePool = new ArrayList<>();
        String[][] stagePools = {
            {"NormalZombie", "ConeZombie", "ZombieNewspaper", "ZombieModernAllStar", "ZombieImp"},
            {"NormalZombie", "ConeZombie", "ZombieProspector", "ZombieCrystalSkull", "ZombieModernAllStar"},
            {"ZombieModernAllStar", "ZombiePiano", "BarrelRollerZombie", "ZombieDarkJuggler", "ZombieGargantuar"}
        };

        String[] pool = stagePools[Math.min(level - 1, stagePools.length - 1)];
        for (String type : pool) {
            stageZombiePool.add(type);
        }
    }

    public boolean placeZombie(String type, int lane, Game game) {
        return placeZombie(type, 8, lane, game);
    }

    public boolean placeZombie(String type, int col, int lane, Game game) {
        if (lane < 0 || lane >= 5) return false;
        if (col <= redLineColumn || col >= 9) {
            game.getGameLogMessages().add("IZombie: Cannot place zombie past or on the plant boundary (Red Line at column " + redLineColumn + ")!");
            return false;
        }

        if (type.toLowerCase().contains("bucket")) {
            game.getGameLogMessages().add("IZombie: Bucket Zombie cannot be deployed!");
            return false;
        }

        boolean valid = false;
        for (String z : stageZombiePool) {
            if (z.equalsIgnoreCase(type)) {
                valid = true;
                type = z;
                break;
            }
        }
        if (!valid) return false;

        int cost = getZombieCost(type);
        if (game.getSunCount() < cost) return false;

        game.spendSun(cost);

        Zombie z = ZombieFactory.createZombieAtColumn(type, lane, col, game.getDifficultyLevel());
        if (z == null) {
            z = new Zombie(type, 200, 0.185, 20);
            z.setX(col);
            z.setY(lane);
        } else {
            z.setX((double) col);
            z.setY(lane);
        }
        game.addZombie(z);
        Tile t = game.getBoard().getTile(lane, col);
        if (t != null) t.setZombie(z);
        game.getGameLogMessages().add("IZombie: Placed " + type + " at (" + col + ", " + lane + ") for " + cost + " suns.");
        return true;
    }

    public int getZombieCost(String type) {
        String clean = type.toLowerCase();
        if (clean.contains("imp")) return 25;
        if (clean.contains("normal")) return 50;
        if (clean.contains("cone")) return 75;
        if (clean.contains("newspaper")) return 100;
        if (clean.contains("prospector")) return 110;
        if (clean.contains("crystalskull") || clean.contains("turquoise")) return 120;
        if (clean.contains("juggler")) return 125;
        if (clean.contains("piano")) return 130;
        if (clean.contains("barrel")) return 140;
        if (clean.contains("allstar") || clean.contains("football")) return 150;
        if (clean.contains("gargantuar")) return 300;
        return 50;
    }

    public int getMinZombieCostInPool() {
        int min = Integer.MAX_VALUE;
        for (String z : stageZombiePool) {
            min = Math.min(min, getZombieCost(z));
        }
        return min == Integer.MAX_VALUE ? 50 : min;
    }

    public int getBrainsEaten() { return brainsEaten; }
    public int getRedLineColumn() { return redLineColumn; }

    public List<String> getAvailableZombieTypes() {
        return getStageZombiePool();
    }

    public boolean isBrainEaten(int row) {
        return isBrainRowEaten(row);
    }

    public boolean isBrainRowEaten(int row) {
        if (row >= 0 && row < 5) return brainRowEaten[row];
        return true;
    }

    public void eatBrain(int row) {
        if (row >= 0 && row < 5 && !brainRowEaten[row]) {
            brainRowEaten[row] = true;
            brainsEaten++;
        }
    }

    public boolean isVictoryConditionMet() { return brainsEaten >= 5; }
    public int getStageLevel() { return stageLevel; }
    public List<String> getStageZombiePool() { return new ArrayList<>(stageZombiePool); }

    public int getZombieSunCount() {
        return 150;
    }

    public void updateMiniGame(Game game) {
        if (game == null) return;

        for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
            if (z.getArmorType() != null && z.getArmorType().equalsIgnoreCase("BUCKET")) {
                z.incrementIzombieSunTicks();
                int interval = Math.max(150 - (z.getIzombieSunProductionTicks() / 4), 30);
                if (game.getTickCount() % interval == 0) {
                    game.addSun(25);
                    game.getGameLogMessages().add("IZombie: Buckethead Zombie in lane " + z.getY() + " generated 25 suns.");
                }
            }
        }

        if (isVictoryConditionMet()) {
            completeLevel(stageLevel, brainsEaten);
            if (stageLevel < maxStageLevel) {
                stageLevel++;
                game.getGameLogMessages().add("IZombie: Stage " + (stageLevel - 1) + " complete! Advancing to Stage " + stageLevel);
                setupStage(game, stageLevel);
                return;
            } else {
                game.setWon(true);
                game.stop();
                game.getGameLogMessages().add("IZombie: All 3 Stages Completed Successfully! Victory!");
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                return;
            }
        }

        if (game.getSunCount() < getMinZombieCostInPool() && game.getActiveZombies().isEmpty() && !isVictoryConditionMet()) {
            game.setLost(true);
            game.stop();
            game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
        }
    }
}
