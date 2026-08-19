package model.season;

import model.Game;
import model.board.Board;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;

public class Season {
    protected String name;
    protected int currentLevel = 1;
    protected int maxLevel = 4;
    protected List<Plant> unlockedPlants = new ArrayList<>();
    protected List<Zombie> seasonZombies = new ArrayList<>();

    public Season(String name, int maxLevel) {
        this.name = name;
        this.maxLevel = maxLevel;
    }

    public Season(String name) {
        this(name, 4);
    }

    public String getName() {
        return name;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int level) {
        if (level >= 1 && level <= maxLevel) {
            this.currentLevel = level;
        }
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setupEnvironment(Game game) {}
    public void handleWaveStart(Game game) {}
    public void handleTick(Game game) {}
    public boolean allowsNaturalSunDrop() { return true; }

    public int modifySpawnColumn(int currentWave, int totalWaves, int defaultColumn, int zombiesSpawned, Board board, int lane) {
        return defaultColumn;
    }

    public String getBackgroundPath() {
        return "IMAGE_BACKGROUNDS_ZEN_GARDEN";
    }
}
