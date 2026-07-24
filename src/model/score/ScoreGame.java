package model.score;

import model.Game;
import model.entities.zombie.Zombie;
import model.entities.plant.Plant;
import model.handler.QuestManager;

import java.util.*;

public class ScoreGame {
    private int totalScore;
    private int zombiesKilled;
    private int plantsPlaced;
    private int sunsCollected;
    private int wavesSurvived;
    private int maxCombo;
    private int currentCombo;
    private int totalDamageDealt;
    private int totalDamageTaken;
    private int coinsEarned;
    private int diamondsEarned;
    private Map<String, Integer> plantUsage;
    private Map<String, Integer> zombieKills;

    public enum ScoringPattern {
        AGGRESSIVE,
        DEFENSIVE,
        EFFICIENT,
        COMBO_MASTER,
        PERFECTIONIST
    }

    private Set<ScoringPattern> activePatterns;

    public ScoreGame() {
        this.totalScore = 0;
        this.zombiesKilled = 0;
        this.plantsPlaced = 0;
        this.sunsCollected = 0;
        this.wavesSurvived = 0;
        this.maxCombo = 0;
        this.currentCombo = 0;
        this.totalDamageDealt = 0;
        this.totalDamageTaken = 0;
        this.coinsEarned = 0;
        this.diamondsEarned = 0;
        this.plantUsage = new HashMap<>();
        this.zombieKills = new HashMap<>();
        this.activePatterns = new HashSet<>();
    }

    public void onZombieKilled(Zombie zombie, String plantType, Game game) {
        int basePoints = 10;
        int healthBonus = zombie.getMaxHealth() / 20;
        int typeBonus = zombie.isBoss() ? 100 : 0;
        int comboBonus = currentCombo * 2;

        int points = basePoints + healthBonus + typeBonus + comboBonus;
        totalScore += points;
        zombiesKilled++;

        zombieKills.put(zombie.getType(), zombieKills.getOrDefault(zombie.getType(), 0) + 1);
        plantUsage.put(plantType, plantUsage.getOrDefault(plantType, 0) + 1);

        currentCombo++;
        if (currentCombo > maxCombo) {
            maxCombo = currentCombo;
        }

        QuestManager.notifyZombieKilled();
        checkPatterns();
    }

    public void onZombieKilled(Zombie zombie, Game game) {
        onZombieKilled(zombie, "unknown", game);
    }

    public void onPlantPlaced(Plant plant) {
        plantsPlaced++;
        plantUsage.put(plant.getName(), plantUsage.getOrDefault(plant.getName(), 0) + 1);
        QuestManager.notifyPlantPlaced();
    }

    public void onSunCollected(int amount) {
        sunsCollected++;
        totalScore += amount / 5;
        QuestManager.notifySunCollected(amount);
    }

    public void onWaveCompleted(int waveNumber) {
        wavesSurvived = waveNumber;
        int bonus = waveNumber * 10;
        totalScore += bonus;
    }

    public void onDamageDealt(int amount) {
        totalDamageDealt += amount;
    }

    public void onDamageTaken(int amount) {
        totalDamageTaken += amount;
        checkPatterns();
    }

    public void onComboBreak() {
        currentCombo = 0;
    }

    public void onCoinEarned(int amount) {
        coinsEarned += amount;
    }

    public void onDiamondEarned(int amount) {
        diamondsEarned += amount;
    }

    private void checkPatterns() {
        if (zombiesKilled >= 10 && totalScore > 200) {
            activePatterns.add(ScoringPattern.AGGRESSIVE);
        }
        if (totalDamageTaken < 100 && wavesSurvived >= 3) {
            activePatterns.add(ScoringPattern.DEFENSIVE);
        }
        if (plantsPlaced >= 5 && zombiesKilled >= 5) {
            activePatterns.add(ScoringPattern.EFFICIENT);
        }
        if (maxCombo >= 10) {
            activePatterns.add(ScoringPattern.COMBO_MASTER);
        }
        if (plantsPlaced > 0 && totalDamageTaken == 0) {
            activePatterns.add(ScoringPattern.PERFECTIONIST);
        }
    }

    public int getFinalScore() {
        int finalScore = totalScore;

        if (activePatterns.contains(ScoringPattern.AGGRESSIVE)) finalScore += 50;
        if (activePatterns.contains(ScoringPattern.DEFENSIVE)) finalScore += 40;
        if (activePatterns.contains(ScoringPattern.EFFICIENT)) finalScore += 30;
        if (activePatterns.contains(ScoringPattern.COMBO_MASTER)) finalScore += 60;
        if (activePatterns.contains(ScoringPattern.PERFECTIONIST)) finalScore += 80;

        int efficiency = (sunsCollected > 0 && plantsPlaced > 0)
                ? (sunsCollected / plantsPlaced) * 2 : 0;
        finalScore += Math.min(efficiency, 50);

        return finalScore;
    }

    public int getTotalScore() { return totalScore; }
    public int getZombiesKilled() { return zombiesKilled; }
    public int getPlantsPlaced() { return plantsPlaced; }
    public int getSunsCollected() { return sunsCollected; }
    public int getWavesSurvived() { return wavesSurvived; }
    public int getMaxCombo() { return maxCombo; }
    public int getCurrentCombo() { return currentCombo; }
    public int getTotalDamageDealt() { return totalDamageDealt; }
    public int getTotalDamageTaken() { return totalDamageTaken; }
    public int getCoinsEarned() { return coinsEarned; }
    public int getDiamondsEarned() { return diamondsEarned; }
    public Map<String, Integer> getPlantUsage() { return new HashMap<>(plantUsage); }
    public Map<String, Integer> getZombieKills() { return new HashMap<>(zombieKills); }
    public Set<ScoringPattern> getActivePatterns() { return new HashSet<>(activePatterns); }

    public void reset() {
        totalScore = 0;
        zombiesKilled = 0;
        plantsPlaced = 0;
        sunsCollected = 0;
        wavesSurvived = 0;
        maxCombo = 0;
        currentCombo = 0;
        totalDamageDealt = 0;
        totalDamageTaken = 0;
        coinsEarned = 0;
        diamondsEarned = 0;
        plantUsage.clear();
        zombieKills.clear();
        activePatterns.clear();
    }
}