package model.season;

import model.board.Board;

import java.util.ArrayList;
import java.util.List;

public class Season {
    private String name;
    private int levelCount;
    private boolean unlocked;
    private int currentLevel;
    private List<String> unlockedLevels;
    private List<String> specialFeatures;
    private String description;

    public Season(String name, int levelCount) {
        this.name = name;
        this.levelCount = levelCount;
        this.unlocked = false;
        this.currentLevel = 1;
        this.unlockedLevels = new ArrayList<>();
        this.specialFeatures = new ArrayList<>();
        this.description = "";
        unlockedLevels.add("level_1");
    }

    public Season(String name, int levelCount, String description) {
        this(name, levelCount);
        this.description = description;
    }

    public void unlock() {
        this.unlocked = true;
    }

    public void unlockLevel(int level) {
        if (level <= levelCount && !unlockedLevels.contains("level_" + level)) {
            unlockedLevels.add("level_" + level);
        }
    }

    public boolean isLevelUnlocked(int level) {
        return unlockedLevels.contains("level_" + level) || (unlocked && level <= 1);
    }

    public void completeLevel(int level) {
        if (level < levelCount) {
            unlockLevel(level + 1);
        }
        currentLevel = Math.max(currentLevel, level + 1);
    }

    public void addSpecialFeature(String feature) {
        specialFeatures.add(feature);
    }

    public boolean hasSpecialFeature(String feature) {
        return specialFeatures.contains(feature);
    }

    public String getName() { return name; }
    public int getLevelCount() { return levelCount; }
    public boolean isUnlocked() { return unlocked; }
    public void setUnlocked(boolean unlocked) { this.unlocked = unlocked; }
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    public List<String> getUnlockedLevels() { return new ArrayList<>(unlockedLevels); }
    public List<String> getSpecialFeatures() { return new ArrayList<>(specialFeatures); }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public void setupEnvironment(model.Game game) {}
    public void handleWaveStart(model.Game game) {}
    public void handleTick(model.Game game) {}
    public int modifySpawnColumn(int currentWave, int totalWaves, int defaultColumn, int zombiesSpawned, Board board, int lane) { return defaultColumn; }
    public boolean allowsNaturalSunDrop() { return true; }
}