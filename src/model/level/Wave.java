package model.level;

import java.util.ArrayList;
import java.util.List;

public class Wave {
    private final int number;
    private int remainingZombies;
    private int totalZombies;
    private boolean isFinalWave;
    private boolean isActive;
    private List<String> zombieQueue;
    private int zombiesSpawned;
    private int spawnTimer;
    private int spawnInterval;

    public Wave(int number) {
        this.number = number;
        this.remainingZombies = number * 5;
        this.totalZombies = number * 5;
        this.isFinalWave = false;
        this.isActive = false;
        this.zombieQueue = new ArrayList<>();
        this.zombiesSpawned = 0;
        this.spawnTimer = 0;
        this.spawnInterval = 60;
    }

    public Wave(int number, boolean isFinalWave) {
        this(number);
        this.isFinalWave = isFinalWave;
        if (isFinalWave) {
            this.remainingZombies = number * 10;
            this.totalZombies = number * 10;
        }
    }

    public void start() {
        this.isActive = true;
        this.spawnTimer = 0;
    }

    public void update() {
        if (!isActive || remainingZombies <= 0) return;

        spawnTimer++;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0;
        }
    }

    public boolean isComplete() {
        return remainingZombies <= 0 && zombiesSpawned >= totalZombies;
    }

    public void zombieDefeated() {
        if (remainingZombies > 0) {
            remainingZombies--;
        }
    }

    public void zombieSpawned() {
        zombiesSpawned++;
    }

    public int getNumber() { return number; }
    public int getRemainingZombies() { return remainingZombies; }
    public int getTotalZombies() { return totalZombies; }
    public boolean isFinalWave() { return isFinalWave; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public int getZombiesSpawned() { return zombiesSpawned; }
    public int getSpawnInterval() { return spawnInterval; }
    public void setSpawnInterval(int spawnInterval) { this.spawnInterval = spawnInterval; }
    public void addZombieToQueue(String zombieType) { zombieQueue.add(zombieType); }
    public List<String> getZombieQueue() { return zombieQueue; }
    public String getNextZombie() {
        if (zombieQueue.isEmpty()) return null;
        return zombieQueue.remove(0);
    }
}