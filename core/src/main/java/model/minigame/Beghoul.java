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

public class Beghoul extends MiniGame {
    private int matchesFormed;
    private int targetMatches;
    private boolean[][] craters;
    private int stageLevel;
    private int maxStageLevel;
    private boolean isSetup;
    private String[] currentStagePlantTypes;

    private static final String[][] STAGE_PLANTS = {
        {"Peashooter", "Sunflower", "Wall-nut", "Cabbage-pult", "Puff-shroom"},
        {"Peashooter", "Sunflower", "Wall-nut", "Cabbage-pult", "Melon-pult"},
        {"Repeater", "Tall-nut", "Melon-pult", "Fume-shroom", "Snow Pea"}
    };

    private static final String[][] UPGRADE_PATHS = {
        {"Peashooter", "Repeater"},
        {"Repeater", "Mega Gatling Pea"},
        {"Wall-nut", "Tall-nut"},
        {"Puff-shroom", "Fume-shroom"},
        {"Cabbage-pult", "Melon-pult"},
        {"Melon-pult", "Winter Melon"}
    };

    private static final int[] UPGRADE_COSTS = {
        500,
        1500,
        500,
        250,
        1000,
        750
    };

    public Beghoul() {
        super("Beghoul");
        this.matchesFormed = 0;
        this.targetMatches = 10;
        this.craters = new boolean[5][9];
        this.stageLevel = 1;
        this.maxStageLevel = 3;
        this.isSetup = false;
        this.currentStagePlantTypes = STAGE_PLANTS[0];
    }

    public void setupStage(Game game, int level) {
        this.stageLevel = level;
        this.matchesFormed = 0;
        this.targetMatches = 10 + (level - 1) * 10;
        this.craters = new boolean[5][9];
        this.isSetup = true;
        this.currentStagePlantTypes = STAGE_PLANTS[Math.min(level - 1, STAGE_PLANTS.length - 1)].clone();

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

        fillGridWithNoInitialMatches(game);

        game.getGameLogMessages().add("Beghouled: Stage " + level + " started! Target matches: " + targetMatches);
    }

    private void fillGridWithNoInitialMatches(Game game) {
        Random rand = new Random();
        int rows = game.getBoard().getRows();
        int cols = game.getBoard().getColumns();

        do {
            for (Plant p : new ArrayList<>(game.getActivePlants())) {
                Tile t = game.getBoard().getTile(p.getY(), p.getX());
                if (t != null) t.setPlant(null);
                game.removePlant(p);
            }

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (hasCrater(r, c)) continue;

                    List<String> validTypes = new ArrayList<>();
                    for (String type : currentStagePlantTypes) {
                        validTypes.add(type);
                    }

                    if (c >= 2) {
                        Plant p1 = game.getBoard().getTile(r, c - 1).getPlant();
                        Plant p2 = game.getBoard().getTile(r, c - 2).getPlant();
                        if (p1 != null && p2 != null && normalize(p1.getName()).equals(normalize(p2.getName()))) {
                            validTypes.remove(p1.getName());
                        }
                    }
                    if (r >= 2) {
                        Plant p1 = game.getBoard().getTile(r - 1, c).getPlant();
                        Plant p2 = game.getBoard().getTile(r - 2, c).getPlant();
                        if (p1 != null && p2 != null && normalize(p1.getName()).equals(normalize(p2.getName()))) {
                            validTypes.remove(p1.getName());
                        }
                    }

                    if (validTypes.isEmpty()) {
                        for (String type : currentStagePlantTypes) validTypes.add(type);
                    }

                    String chosenType = validTypes.get(rand.nextInt(validTypes.size()));
                    Plant p = PlantFactory.createPlant(chosenType);
                    if (p != null) {
                        p.setX(c);
                        p.setY(r);
                        game.addPlant(p);
                        game.getBoard().getTile(r, c).setPlant(p);
                    }
                }
            }
        } while (!hasAnyPossibleMoves(game));
    }

    public boolean swapPlants(int r1, int c1, int r2, int c2, Game game) {
        if (Math.abs(r1 - r2) + Math.abs(c1 - c2) != 1) return false;
        if (hasCrater(r1, c1) || hasCrater(r2, c2)) return false;

        Tile t1 = game.getBoard().getTile(r1, c1);
        Tile t2 = game.getBoard().getTile(r2, c2);
        if (t1 == null || t2 == null) return false;

        Plant p1 = t1.getPlant();
        Plant p2 = t2.getPlant();
        if (p1 == null || p2 == null) return false;

        performTileSwap(t1, t2, p1, p2, r1, c1, r2, c2);

        List<List<Plant>> matches = findMatches(game);
        if (matches.isEmpty()) {
            performTileSwap(t1, t2, p2, p1, r1, c1, r2, c2);
            return false;
        }

        resolveMatchesCascade(game, matches, false);
        return true;
    }

    private void performTileSwap(Tile t1, Tile t2, Plant p1, Plant p2, int r1, int c1, int r2, int c2) {
        t1.setPlant(p2);
        t2.setPlant(p1);
        p1.setX(c2);
        p1.setY(r2);
        p2.setX(c1);
        p2.setY(r1);
    }

    public boolean checkAndProcessMatches(Game game, boolean isCascade) {
        List<List<Plant>> matches = findMatches(game);
        if (matches.isEmpty()) return false;
        resolveMatchesCascade(game, matches, isCascade);
        return true;
    }

    private List<List<Plant>> findMatches(Game game) {
        List<List<Plant>> allMatches = new ArrayList<>();
        int rows = game.getBoard().getRows();
        int cols = game.getBoard().getColumns();

        for (int r = 0; r < rows; r++) {
            int matchLen = 1;
            for (int c = 0; c < cols; c++) {
                Plant pCur = game.getBoard().getTile(r, c).getPlant();
                Plant pNext = (c + 1 < cols) ? game.getBoard().getTile(r, c + 1).getPlant() : null;

                if (pCur != null && pNext != null && normalize(pCur.getName()).equals(normalize(pNext.getName())) && !hasCrater(r, c) && !hasCrater(r, c + 1)) {
                    matchLen++;
                } else {
                    if (matchLen >= 3) {
                        List<Plant> match = new ArrayList<>();
                        for (int k = 0; k < matchLen; k++) {
                            match.add(game.getBoard().getTile(r, c - k).getPlant());
                        }
                        allMatches.add(match);
                    }
                    matchLen = 1;
                }
            }
        }

        for (int c = 0; c < cols; c++) {
            int matchLen = 1;
            for (int r = 0; r < rows; r++) {
                Plant pCur = game.getBoard().getTile(r, c).getPlant();
                Plant pNext = (r + 1 < rows) ? game.getBoard().getTile(r + 1, c).getPlant() : null;

                if (pCur != null && pNext != null && normalize(pCur.getName()).equals(normalize(pNext.getName())) && !hasCrater(r, c) && !hasCrater(r + 1, c)) {
                    matchLen++;
                } else {
                    if (matchLen >= 3) {
                        List<Plant> match = new ArrayList<>();
                        for (int k = 0; k < matchLen; k++) {
                            match.add(game.getBoard().getTile(r - k, c).getPlant());
                        }
                        allMatches.add(match);
                    }
                    matchLen = 1;
                }
            }
        }

        return allMatches;
    }

    private void resolveMatchesCascade(Game game, List<List<Plant>> matches, boolean isCascade) {
        for (List<Plant> match : matches) {
            int size = match.size();
            int sunGained = 50;
            if (size == 4) sunGained = 100;
            else if (size >= 5) sunGained = 150;

            if (isCascade) {
                sunGained += 50;
            }

            game.addSun(sunGained);
            this.matchesFormed++;

            for (Plant p : match) {
                if (p != null) {
                    p.triggerMatchRemoval();
                    Tile t = game.getBoard().getTile(p.getY(), p.getX());
                    if (t != null && t.getPlant() == p) {
                        t.setPlant(null);
                    }
                }
            }
            game.getGameLogMessages().add("Beghouled: Match of " + size + "! +" + sunGained + " suns. (" + matchesFormed + "/" + targetMatches + ")");
        }

        applyGravityAndRefill(game);

        List<List<Plant>> nextMatches = findMatches(game);
        if (!nextMatches.isEmpty()) {
            resolveMatchesCascade(game, nextMatches, true);
        } else {
            if (!hasAnyPossibleMoves(game)) {
                game.getGameLogMessages().add("Beghouled: No possible moves! Resetting garden plants...");
                fillGridWithNoInitialMatches(game);
            }
        }
    }

    private void applyGravityAndRefill(Game game) {
        int rows = game.getBoard().getRows();
        int cols = game.getBoard().getColumns();
        Random rand = new Random();

        for (int c = 0; c < cols; c++) {
            for (int r = rows - 1; r >= 0; r--) {
                if (hasCrater(r, c)) continue;
                Tile t = game.getBoard().getTile(r, c);
                if (t != null && t.getPlant() == null) {
                    for (int aboveR = r - 1; aboveR >= 0; aboveR--) {
                        if (hasCrater(aboveR, c)) continue;
                        Tile aboveT = game.getBoard().getTile(aboveR, c);
                        if (aboveT != null && aboveT.getPlant() != null) {
                            Plant fallingPlant = aboveT.getPlant();
                            aboveT.setPlant(null);
                            int droppedRows = r - aboveR;
                            fallingPlant.setVisualOffsetY(droppedRows * 80f);
                            fallingPlant.setY(r);
                            t.setPlant(fallingPlant);
                            break;
                        }
                    }
                }
            }

            for (int r = 0; r < rows; r++) {
                if (hasCrater(r, c)) continue;
                Tile t = game.getBoard().getTile(r, c);
                if (t != null && t.getPlant() == null) {
                    String chosenType = currentStagePlantTypes[rand.nextInt(currentStagePlantTypes.length)];
                    Plant p = PlantFactory.createPlant(chosenType);
                    if (p != null) {
                        p.setX(c);
                        p.setY(r);
                        p.setVisualOffsetY((r + 1) * 80f);
                        game.addPlant(p);
                        t.setPlant(p);
                    }
                }
            }
        }
    }

    public boolean hasAnyPossibleMoves(Game game) {
        int rows = game.getBoard().getRows();
        int cols = game.getBoard().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c + 1 < cols && !hasCrater(r, c) && !hasCrater(r, c + 1)) {
                    if (testSwapCheck(game, r, c, r, c + 1)) return true;
                }
                if (r + 1 < rows && !hasCrater(r, c) && !hasCrater(r + 1, c)) {
                    if (testSwapCheck(game, r, c, r + 1, c)) return true;
                }
            }
        }
        return false;
    }

    private boolean testSwapCheck(Game game, int r1, int c1, int r2, int c2) {
        Tile t1 = game.getBoard().getTile(r1, c1);
        Tile t2 = game.getBoard().getTile(r2, c2);
        if (t1 == null || t2 == null) return false;
        Plant p1 = t1.getPlant();
        Plant p2 = t2.getPlant();
        if (p1 == null || p2 == null) return false;

        performTileSwap(t1, t2, p1, p2, r1, c1, r2, c2);
        boolean hasMatch = !findMatches(game).isEmpty();
        performTileSwap(t1, t2, p2, p1, r1, c1, r2, c2);
        return hasMatch;
    }

    public boolean upgradePlants(String fromType, String toType, Game game) {
        int cost = getUpgradeCost(fromType, toType);
        if (cost < 0) return false;
        if (game.getSunCount() < cost) {
            game.getGameLogMessages().add("Beghoul: Not enough suns! Required: " + cost);
            return false;
        }

        int upgradedCount = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (!hasCrater(r, c) && tile != null && tile.getPlant() != null && normalize(tile.getPlant().getName()).equals(normalize(fromType))) {
                    game.removePlant(tile.getPlant());
                    Plant up = PlantFactory.createPlant(toType);
                    if (up != null) {
                        up.setX(c);
                        up.setY(r);
                        game.addPlant(up);
                        tile.setPlant(up);
                        upgradedCount++;
                    }
                }
            }
        }

        if (upgradedCount > 0) {
            game.spendSun(cost);
            for (int i = 0; i < currentStagePlantTypes.length; i++) {
                if (normalize(currentStagePlantTypes[i]).equals(normalize(fromType))) {
                    currentStagePlantTypes[i] = toType;
                    break;
                }
            }
            game.getGameLogMessages().add("Beghoul: Upgraded " + upgradedCount + " plants from " + fromType + " to " + toType + ".");
            return true;
        }
        return false;
    }

    public int getUpgradeCost(String fromType, String toType) {
        for (int i = 0; i < UPGRADE_PATHS.length; i++) {
            if (normalize(UPGRADE_PATHS[i][0]).equals(normalize(fromType)) && normalize(UPGRADE_PATHS[i][1]).equals(normalize(toType))) {
                return UPGRADE_COSTS[i];
            }
        }
        return -1;
    }

    public static String[][] getUpgradePaths() {
        return UPGRADE_PATHS;
    }

    public static int[] getUpgradeCosts() {
        return UPGRADE_COSTS;
    }

    private String normalize(String name) {
        return name == null ? "" : name.replaceAll("[\\s_-]", "").toLowerCase();
    }

    public int getMatchesFormed() { return matchesFormed; }
    public int getTargetMatches() { return targetMatches; }
    public int getStageLevel() { return stageLevel; }
    public String[] getCurrentStagePlantTypes() { return currentStagePlantTypes; }

    public boolean hasCrater(int r, int c) {
        if (r >= 0 && r < 5 && c >= 0 && c < 9) return craters[r][c];
        return false;
    }

    public void createCrater(int r, int c) {
        if (r >= 0 && r < 5 && c >= 0 && c < 9) {
            craters[r][c] = true;
        }
    }

    public boolean isVictoryConditionMet() {
        return matchesFormed >= targetMatches;
    }


    public void updateMiniGame(Game game) {
        if (game == null) return;

        if (!isSetup) {
            setupStage(game, stageLevel);
            return;
        }

        if (isVictoryConditionMet()) {
            completeLevel(stageLevel, matchesFormed);
            if (stageLevel < maxStageLevel) {
                stageLevel++;
                isSetup = false;
                game.getGameLogMessages().add("Beghoul: Stage " + (stageLevel - 1) + " complete! Moving to Stage " + stageLevel);
                setupStage(game, stageLevel);
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    Tile t = game.getBoard().getTile(z.getY(), (int) Math.round(z.getX()));
                    if (t != null) t.setZombie(null);
                    game.removeZombie(z);
                }
                return;
            } else {
                game.setWon(true);
                game.stop();
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    Tile t = game.getBoard().getTile(z.getY(), (int) Math.round(z.getX()));
                    if (t != null) t.setZombie(null);
                    game.removeZombie(z);
                }
                game.getGameLogMessages().add("Beghoul: Victory! All stages completed!");
                return;
            }
        }

        if (game.getTickCount() % 100 == 0) {
            Random rand = new Random();
            int lane = rand.nextInt(5);
            String[] pool = {"NormalZombie", "ConeZombie", "BucketZombie", "ZombieImp"};
            String zType = pool[rand.nextInt(pool.length)];
            Zombie z = ZombieFactory.createZombieAtColumn(zType, lane, 8, game.getDifficultyLevel());
            if (z != null) {
                game.addZombie(z);
                Tile t = game.getBoard().getTile(lane, 8);
                if (t != null) t.setZombie(z);
            }
        }
    }
}
