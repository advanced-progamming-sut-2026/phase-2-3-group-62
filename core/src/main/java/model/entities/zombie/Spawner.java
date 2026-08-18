package model.entities.zombie;

import controller.menu.PreGameController;
import model.board.Board;
import model.board.Tile;
import model.entities.zombie.factory.ZombieFactory;
import model.enums.ChapterZombieType;
import model.enums.Difficulty;
import model.season.Season;
import model.user.UserSession;

import java.util.*;

public class Spawner {
    private Board board;
    private int currentWave;
    private int totalWaves;
    private int remainingZombies;
    private Difficulty difficulty;
    private Map<Integer, List<String>> waveSchedule;
    private boolean finalWaveStarted;
    private int zombiesSpawnedInWave;
    public int ticksSinceLastSpawn;
    private int spawnInterval;
    private Season currentSeason;

    public Spawner(Board board, int levelNumber, Difficulty difficulty) {
        this.board = board;
        this.totalWaves = Math.max(3, levelNumber + 2);
        this.difficulty = difficulty;
        this.currentWave = 0;
        this.remainingZombies = 0;
        this.zombiesSpawnedInWave = 0;
        this.ticksSinceLastSpawn = getSpawnInterval();
        this.finalWaveStarted = false;
        this.waveSchedule = new HashMap<>();
        this.spawnInterval = getSpawnInterval();
        initializeWaves();
    }

    private void initializeWaves() {
        for (int wave = 1; wave <= totalWaves; wave++) {
            int waveCost = calculateWaveCost(wave);
            List<String> zombieTypes = generateZombieTypes(waveCost, wave);
            waveSchedule.put(wave, zombieTypes);
        }
    }

    private int calculateWaveCost(int wave) {
        int baseCost = 1000 + (wave - 1) * 500;

        switch (difficulty) {
            case EASY:
                baseCost = (int) (baseCost * 0.9);
                break;
            case HARD:
                baseCost = (int) (baseCost * 1.3);
                break;
            default:
                break;
        }

        if (wave == totalWaves) {
            baseCost = (int) (baseCost * 1.5);
        }

        return Math.max(baseCost, 1000 + (wave - 1) * 500);
    }

    private List<String> generateZombieTypes(int waveCost, int wave) {
        List<String> types = new ArrayList<>();
        int remainingCost = waveCost;
        List<String> availableZombies = getAvailableZombiesForWave(wave);
        Random rand = new Random();

        while (remainingCost >= 100 && !availableZombies.isEmpty()) {
            String chosenType = availableZombies.get(rand.nextInt(availableZombies.size()));
            int cost = ZombieFactory.getWaveCost(chosenType);

            if (cost <= remainingCost) {
                types.add(chosenType);
                remainingCost -= cost;
            } else {
                List<String> cheaperZombies = new ArrayList<>();
                for (String z : availableZombies) {
                    if (ZombieFactory.getWaveCost(z) <= remainingCost) {
                        cheaperZombies.add(z);
                    }
                }
                if (!cheaperZombies.isEmpty()) {
                    String cheapType = cheaperZombies.get(rand.nextInt(cheaperZombies.size()));
                    types.add(cheapType);
                    remainingCost -= ZombieFactory.getWaveCost(cheapType);
                } else {
                    break;
                }
            }
        }

        if (types.isEmpty()) {
            types.add("ZombieDefault");
        }

        return types;
    }

    private List<String> getAvailableZombiesForWave(int wave) {
        List<String> available = new ArrayList<>();
        String chapterName = (currentSeason != null) ? currentSeason.getName() : PreGameController.activeChapterName;
        List<String> chapterAllowed = ChapterZombieType.getAvailableZombiesForChapter(chapterName);

        for (String zId : chapterAllowed) {
            int cost = ZombieFactory.getWaveCost(zId);
            if (wave == 1) {
                if (cost <= 350) {
                    available.add(zId);
                }
            } else if (wave == 2) {
                if (cost <= 800) {
                    available.add(zId);
                }
            } else {
                available.add(zId);
            }
        }

        if (available.isEmpty()) {
            available.add("ZombieDefault");
        }

        return available;
    }

    public int getSpawnInterval() {
        switch (difficulty) {
            case EASY: return 120;
            case HARD: return 40;
            default: return 80;
        }
    }

    public void startWave(int waveNumber) {
        currentWave = waveNumber;
        List<String> zombieTypes = waveSchedule.get(waveNumber);
        if (zombieTypes == null) return;

        remainingZombies = zombieTypes.size();
        zombiesSpawnedInWave = 0;
        finalWaveStarted = (waveNumber == totalWaves);
        ticksSinceLastSpawn = spawnInterval;
    }

    public Zombie spawnNextZombie() {
        List<String> zombieTypes = waveSchedule.get(currentWave);
        if (zombieTypes == null || zombiesSpawnedInWave >= zombieTypes.size()) {
            return null;
        }

        String type = zombieTypes.get(zombiesSpawnedInWave);
        int lane = new Random().nextInt(board.getRows());
        if (currentWave == totalWaves) {
            lane = zombiesSpawnedInWave % board.getRows();
        }

        int column = board.getColumns() - 1;
        if (currentSeason != null) {
            column = currentSeason.modifySpawnColumn(currentWave, totalWaves, column, zombiesSpawnedInWave, board, lane);
        }

        int minAllowedColumn = 4;
        if (currentSeason != null && "DarkAges".equalsIgnoreCase(currentSeason.getName())) {
            minAllowedColumn = 1;
        }

        if (column < minAllowedColumn) {
            column = minAllowedColumn;
        }
        if (column >= board.getColumns()) {
            column = board.getColumns() - 1;
        }

        Zombie zombie = ZombieFactory.createZombieAtColumn(type, lane, column);
        if (zombie != null) {
            if (new Random().nextInt(100) < 5) {
                zombie.setGlowing(true);
            }

            if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
                List<String> observed = UserSession.getCurrentUser().getObservedZombies();
                if (!observed.contains(zombie.getName())) {
                    observed.add(zombie.getName());
                    UserSession.getCurrentUser().addNews("New zombie encountered: " + zombie.getName() + "!");
                    util.FileManager.updateUser(UserSession.getCurrentUser());
                }
            }

            Tile tile = board.getTile(lane, column);
            if (tile != null) {
                tile.setZombie(zombie);
            }

            zombiesSpawnedInWave++;
            remainingZombies--;
        }

        return zombie;
    }

    public Zombie update() {
        ticksSinceLastSpawn++;
        if (ticksSinceLastSpawn >= spawnInterval && remainingZombies > 0) {
            ticksSinceLastSpawn = 0;
            return spawnNextZombie();
        }
        return null;
    }

    public boolean isWaveComplete() {
        return remainingZombies <= 0 && zombiesSpawnedInWave >= waveSchedule.getOrDefault(currentWave, Collections.emptyList()).size();
    }

    public boolean isFinalWave() { return currentWave == totalWaves; }
    public boolean isFinalWaveStarted() { return finalWaveStarted; }
    public int getCurrentWave() { return currentWave; }
    public int getTotalWaves() { return totalWaves; }
    public int getRemainingZombies() { return remainingZombies; }
    public int getZombiesSpawnedInWave() { return zombiesSpawnedInWave; }
    public List<String> getCurrentWaveZombies() { return waveSchedule.getOrDefault(currentWave, Collections.emptyList()); }
    public int getZombiesInWave() { return waveSchedule.getOrDefault(currentWave, Collections.emptyList()).size(); }
    public void setSpawnInterval(int spawnInterval) { this.spawnInterval = spawnInterval; }
    public Season getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(Season currentSeason) { this.currentSeason = currentSeason; }
}
