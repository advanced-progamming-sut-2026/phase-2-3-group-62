package model.level;

import model.enums.SpecialLevelType;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private final int number;
    private Wave wave;
    private SpecialLevelType specialLevelType;
    private int timeLimitTicks;
    private int targetZombiesToKill;
    private int targetSunsToProduce;
    private int maxPlantsLostAllowed;
    private int deadlineColumn;
    private int initialSunAmount;
    private List<int[]> seedProtectionPositions;

    public Level(int number) {
        this.number = number;
        this.wave = new Wave(number);
        this.specialLevelType = SpecialLevelType.NONE;
        this.timeLimitTicks = 0;
        this.targetZombiesToKill = 0;
        this.targetSunsToProduce = 0;
        this.maxPlantsLostAllowed = 0;
        this.deadlineColumn = 0;
        this.initialSunAmount = 50;
        this.seedProtectionPositions = new ArrayList<>();
    }

    public Level(int number, SpecialLevelType type) {
        this(number);
        this.specialLevelType = type;
        setupSpecialLevelConfig();
    }

    private void setupSpecialLevelConfig() {
        switch (specialLevelType) {
            case CONVEYOR_BELT:
                break;
            case LOCKED_PLANTS:
                break;
            case SAVE_OUR_SEEDS:
                seedProtectionPositions.add(new int[]{2, 2});
                seedProtectionPositions.add(new int[]{2, 3});
                break;
            case TIMED_WAR:
                timeLimitTicks = 600;
                targetZombiesToKill = 5;
                targetSunsToProduce = 500;
                break;
            case NIGHT_OPS:
                initialSunAmount = 50;
                break;
            case DEAD_LINE:
                deadlineColumn = 3;
                initialSunAmount = 150;
                break;
            case LOVE_YOUR_PLANTS:
                maxPlantsLostAllowed = 3;
                break;
            case PLANT_WHAT_YOU_GET:
                initialSunAmount = 800;
                break;
            default:
                break;
        }
    }

    public int getNumber() {
        return number;
    }

    public Wave getWave() {
        return wave;
    }

    public SpecialLevelType getSpecialLevelType() {
        return specialLevelType;
    }

    public void setSpecialLevelType(SpecialLevelType specialLevelType) {
        this.specialLevelType = specialLevelType;
        setupSpecialLevelConfig();
    }

    public int getTimeLimitTicks() {
        return timeLimitTicks;
    }

    public int getTargetZombiesToKill() {
        return targetZombiesToKill;
    }

    public int getTargetSunsToProduce() {
        return targetSunsToProduce;
    }

    public int getMaxPlantsLostAllowed() {
        return maxPlantsLostAllowed;
    }

    public int getDeadlineColumn() {
        return deadlineColumn;
    }

    public int getInitialSunAmount() {
        return initialSunAmount;
    }

    public List<int[]> getSeedProtectionPositions() {
        return seedProtectionPositions;
    }
}
