package model.minigame;

import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.zombie.Zombie;
import java.util.ArrayList;
import java.util.Random;

public class Beghoul extends MiniGame {
    private int matchesFormed;
    private int targetMatches;
    private boolean[][] craters;
    private int stageLevel;
    private int maxStageLevel;
    private boolean isSetup;


    private static final String[][] UPGRADE_PATHS = {
        {"PeaShooter", "Repeater", "Threepeater"},
        {"WallNut", "TallNut"},
        {"Cabbagepult", "Melonpult", "WinterMelon"},
        {"PuffShroom", "FumeShroom"},
        {"SnowPea", "Repeater"}
    };

    private static final int[][] UPGRADE_COSTS = {
        {500, 1500},
        {500},
        {1000, 750},
        {250},
        {400}
    };

    public Beghoul() {
        super("Beghoul");
        this.matchesFormed = 0;
        this.targetMatches = 10;
        this.craters = new boolean[5][9];
        this.stageLevel = 1;
        this.maxStageLevel = 3;
        this.isSetup = false;
    }

    public void setupStage(Game game, int level) {
        this.stageLevel = level;
        this.matchesFormed = 0;
        this.targetMatches = 5 + level * 5;
        this.craters = new boolean[5][9];
        this.isSetup = true;


        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            game.getBoard().getTile(p.getY(), p.getX()).setPlant(null);
            game.removePlant(p);
        }
        for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
            game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
            game.removeZombie(z);
        }


        String[][] stagePlants = {
            {"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Cabbagepult"},
            {"PeaShooter", "Repeater", "TallNut", "SnowPea", "Melonpult", "PuffShroom"},
            {"Repeater", "Threepeater", "TallNut", "WinterMelon", "FumeShroom", "BonkChoy"}
        };

        Random rand = new Random();
        String[] types = stagePlants[Math.min(level - 1, stagePlants.length - 1)];
        fillGridRandomly(game, types);
        

        while (checkAndProcessMatches(game, false)) {}
        
        game.getGameLogMessages().add("Beghoul: Stage " + level + " started!");
        game.getGameLogMessages().add("Target matches: " + targetMatches);
    }

    public void fillGridRandomly(Game game, String[] types) {
    Random rand = new Random();
    for (int r = 0; r < 5; r++) {
        for (int c = 0; c < 9; c++) {
            if (!hasCrater(r, c)) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile.getPlant() != null) {
                    game.removePlant(tile.getPlant());
                }
                String type = types[rand.nextInt(types.length)];
                Plant p = PlantFactory.createPlant(type);
                if (p == null) {

                    p = new Plant(rand.nextInt(1000) + 200, type, "BEGHOULD", null, 0, 300, 20, 2.0, 0, null, 0, null, 0);
                    p.initHealth();
                }
                if (p != null) {
                    p.setX(c);
                    p.setY(r);
                    game.addPlant(p);
                    tile.setPlant(p);
                }
            }
        }
    }
}


    public boolean upgradePlants(String fromType, String toType, Game game) {
    int cost = getUpgradeCost(fromType, toType);
    if (cost < 0) return false;
    if (game.getSunCount() < cost) {
        game.getGameLogMessages().add("Beghoul: Not enough suns! Required: " + cost);
        return false;
    }

    game.spendSun(cost);
    int upgradedCount = 0;

    for (int r = 0; r < 5; r++) {
        for (int c = 0; c < 9; c++) {
            Tile tile = game.getBoard().getTile(r, c);
            if (!hasCrater(r, c) && tile.getPlant() != null && tile.getPlant().getName().equalsIgnoreCase(fromType)) {
                game.removePlant(tile.getPlant());
                Plant up = PlantFactory.createPlant(toType);
                if (up == null) {
                    up = new Plant(999, toType, "BEGHOULD", null, 0, 400, 40, 1.5, 0, null, 0, null, 0);
                }
                up.setX(c);
                up.setY(r);
                game.addPlant(up);
                tile.setPlant(up);
                upgradedCount++;
            }
        }
    }

    game.getGameLogMessages().add("Beghoul: Upgraded " + upgradedCount + " plants from " + fromType + " to " + toType + ".");
    return upgradedCount > 0;
}

    private int getUpgradeCost(String fromType, String toType) {
        for (int i = 0; i < UPGRADE_PATHS.length; i++) {
            String[] path = UPGRADE_PATHS[i];
            for (int j = 0; j < path.length - 1; j++) {
                if (path[j].equalsIgnoreCase(fromType) && path[j + 1].equalsIgnoreCase(toType)) {
                    if (i < UPGRADE_COSTS.length && j < UPGRADE_COSTS[i].length) {
                        return UPGRADE_COSTS[i][j];
                    }

                    return stageLevel * 300 + 200;
                }
            }
        }
        return -1;
    }


    public void addMatch() { this.matchesFormed++; }
    public int getStageLevel() { return stageLevel; }

    public boolean hasCrater(int r, int c) {
        if (r >= 0 && r < 5 && c >= 0 && c < 9) return craters[r][c];
        return false;
    }

    public void createCrater(int r, int c) {
        if (r >= 0 && r < 5 && c >= 0 && c < 9) {
            craters[r][c] = true;
        }
    }

    public void resetGridCraters() {
        this.craters = new boolean[5][9];
    }

    public boolean isVictoryConditionMet() {
        return matchesFormed >= targetMatches;
    }

    public void updateMiniGame(Game game) {

    if (!isSetup) {
        setupStage(game, stageLevel);
        return;
    }

    if (isVictoryConditionMet()) {
        if (stageLevel < maxStageLevel) {
            completeLevel(stageLevel, matchesFormed);
            stageLevel++;
            isSetup = false;
            game.getGameLogMessages().add("Beghoul: Stage " + (stageLevel - 1) + " complete! Moving to Stage " + stageLevel);
            setupStage(game, stageLevel);
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
                game.removeZombie(z);
            }
            return;
        } else {
            game.setWon(true);
            game.stop();
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                game.getBoard().getTile(z.getY(), (int) z.getX()).setZombie(null);
            }
            game.getActiveZombies().clear();
            game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
            return;
        }
    }


    if (!hasAnyPossibleMoves(game)) {
        String[] types = {"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Repeater"};
        fillGridRandomly(game, types);
        game.getGameLogMessages().add("Beghoul: No possible moves! Grid reset.");
        while (checkAndProcessMatches(game, false)) {}
    }


    if (game.getTickCount() % 80 == 0) {
        Zombie z = model.entities.zombie.factory.ZombieFactory.createZombie("NormalZombie");
        if (z != null) {
            int r = new Random().nextInt(5);
            z.setX(8.0);
            z.setY(r);

            if (stageLevel >= 2) {

                Zombie stronger = new Zombie(
                    z.getName(), 
                    z.getMaxHealth() + 100, 
                    z.getSpeed(), 
                    z.getDamage()
                );
                stronger.setX(z.getX());
                stronger.setY(z.getY());
                z = stronger;
            }
            if (stageLevel >= 3) {

                Zombie stronger = new Zombie(
                    z.getName(), 
                    z.getMaxHealth(), 
                    z.getSpeed(), 
                    z.getDamage() + 10
                );
                stronger.setX(z.getX());
                stronger.setY(z.getY());
                z = stronger;
            }
            
            game.addZombie(z);
            game.getBoard().getTile(r, 8).setZombie(z);
        }
    }
}

    public void fillGridRandomly(Game game) {
        String[] types = {"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Repeater"};
        fillGridRandomly(game, types);
    }

    public boolean checkAndProcessMatches(Game game, boolean isCascade) {
        boolean[][] toRemove = new boolean[5][9];
        boolean foundMatch = false;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 7; c++) {
                Tile t1 = game.getBoard().getTile(r, c);
                Tile t2 = game.getBoard().getTile(r, c + 1);
                Tile t3 = game.getBoard().getTile(r, c + 2);
                if (!hasCrater(r, c) && !hasCrater(r, c + 1) && !hasCrater(r, c + 2) &&
                    t1.getPlant() != null && t2.getPlant() != null && t3.getPlant() != null) {
                    String name1 = t1.getPlant().getName();
                    String name2 = t2.getPlant().getName();
                    String name3 = t3.getPlant().getName();
                    if (name1.equalsIgnoreCase(name2) && name2.equalsIgnoreCase(name3)) {
                        toRemove[r][c] = true;
                        toRemove[r][c + 1] = true;
                        toRemove[r][c + 2] = true;
                        foundMatch = true;
                    }
                }
            }
        }

        for (int c = 0; c < 9; c++) {
            for (int r = 0; r < 3; r++) {
                Tile t1 = game.getBoard().getTile(r, c);
                Tile t2 = game.getBoard().getTile(r + 1, c);
                Tile t3 = game.getBoard().getTile(r + 2, c);
                if (!hasCrater(r, c) && !hasCrater(r + 1, c) && !hasCrater(r + 2, c) &&
                    t1.getPlant() != null && t2.getPlant() != null && t3.getPlant() != null) {
                    String name1 = t1.getPlant().getName();
                    String name2 = t2.getPlant().getName();
                    String name3 = t3.getPlant().getName();
                    if (name1.equalsIgnoreCase(name2) && name2.equalsIgnoreCase(name3)) {
                        toRemove[r][c] = true;
                        toRemove[r + 1][c] = true;
                        toRemove[r + 2][c] = true;
                        foundMatch = true;
                    }
                }
            }
        }

        if (!foundMatch) return false;

        int matchSizeCount = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                if (toRemove[r][c]) {
                    Tile tile = game.getBoard().getTile(r, c);
                    if (tile.getPlant() != null) {
                        game.removePlant(tile.getPlant());
                        tile.setPlant(null);
                        matchSizeCount++;
                    }
                }
            }
        }

        addMatch();
        int baseSunReward = 50;
        if (matchSizeCount == 4) baseSunReward = 100;
        else if (matchSizeCount >= 5) baseSunReward = 150;

        if (isCascade) {
            baseSunReward += 50;
        }

        game.addSun(baseSunReward);
        game.getGameLogMessages().add("Beghoul: Match of " + matchSizeCount + "! +" + baseSunReward + " suns. (" + matchesFormed + "/" + targetMatches + ")");

        applyGravityAndRefill(game);
        return true;
    }

    private void applyGravityAndRefill(Game game) {
    String[] types = {"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Repeater"};
    Random rand = new Random();

    for (int c = 0; c < 9; c++) {
        for (int r = 4; r >= 0; r--) {
            if (!hasCrater(r, c) && game.getBoard().getTile(r, c).getPlant() == null) {
                int nextRow = r - 1;
                while (nextRow >= 0 && (hasCrater(nextRow, c) || game.getBoard().getTile(nextRow, c).getPlant() == null)) {
                    nextRow--;
                }
                if (nextRow >= 0) {
                    Plant p = game.getBoard().getTile(nextRow, c).getPlant();
                    game.getBoard().getTile(nextRow, c).setPlant(null);
                    p.setY(r);
                    game.getBoard().getTile(r, c).setPlant(p);
                    r++;
                }
            }
        }
    }

    for (int r = 0; r < 5; r++) {
        for (int c = 0; c < 9; c++) {
            if (!hasCrater(r, c) && game.getBoard().getTile(r, c).getPlant() == null) {
                String type = types[rand.nextInt(types.length)];
                Plant p = PlantFactory.createPlant(type);
                if (p == null) {
                    p = new Plant(rand.nextInt(1000) + 200, type, "BEGHOULD", null, 0, 300, 20, 2.0, 0, null, 0, null, 0);
                    p.initHealth();
                }
                if (p != null) {
                    p.setX(c);
                    p.setY(r);
                    game.addPlant(p);
                    game.getBoard().getTile(r, c).setPlant(p);
                }
            }
        }
    }
}

    public boolean hasAnyPossibleMoves(Game game) {
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                if (c < 8 && !hasCrater(r, c) && !hasCrater(r, c + 1)) {
                    if (testSwapCheck(game, r, c, r, c + 1)) return true;
                }
                if (r < 4 && !hasCrater(r, c) && !hasCrater(r + 1, c)) {
                    if (testSwapCheck(game, r, c, r + 1, c)) return true;
                }
            }
        }
        return false;
    }

    private boolean testSwapCheck(Game game, int r1, int c1, int r2, int c2) {
        Tile t1 = game.getBoard().getTile(r1, c1);
        Tile t2 = game.getBoard().getTile(r2, c2);
        Plant p1 = t1.getPlant();
        Plant p2 = t2.getPlant();
        if (p1 == null || p2 == null) return false;

        t1.setPlant(p2);
        t2.setPlant(p1);

        boolean hasMatch = false;
        outerLoop:
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                if (c < 7) {
                    Plant pA = game.getBoard().getTile(r, c).getPlant();
                    Plant pB = game.getBoard().getTile(r, c + 1).getPlant();
                    Plant pC = game.getBoard().getTile(r, c + 2).getPlant();
                    if (pA != null && pB != null && pC != null && pA.getName().equalsIgnoreCase(pB.getName()) && pB.getName().equalsIgnoreCase(pC.getName())) {
                        hasMatch = true;
                        break outerLoop;
                    }
                }
                if (r < 3) {
                    Plant pA = game.getBoard().getTile(r, c).getPlant();
                    Plant pB = game.getBoard().getTile(r + 1, c).getPlant();
                    Plant pC = game.getBoard().getTile(r + 2, c).getPlant();
                    if (pA != null && pB != null && pC != null && pA.getName().equalsIgnoreCase(pB.getName()) && pB.getName().equalsIgnoreCase(pC.getName())) {
                        hasMatch = true;
                        break outerLoop;
                    }
                }
            }
        }

        t1.setPlant(p1);
        t2.setPlant(p2);
        return hasMatch;
    }
}
