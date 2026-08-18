package model.quest;

import model.user.User;
import util.FileManager;

import java.util.Map;

public class Quest {
    private String id;
    private String title;
    private String description;
    private boolean completed;
    private QuestType type;
    private QuestStatus status;
    private int progress;
    private int target;
    private int rewardCoins;
    private int rewardDiamonds;
    private String rewardUnlockable;
    private int rewardSeedPackets;
    private String rewardSeedPlantType;
    private Priority priority;
    private String additionalCondition;
    private int variableN;

    public enum QuestType {
        STORY,
        EPIC,
        DAILY
    }

    public enum QuestStatus {
        LOCKED,
        AVAILABLE,
        IN_PROGRESS,
        COMPLETED,
        CLAIMED
    }

    public enum Priority {
        CRITICAL,
        HIGH,
        MEDIUM,
        LOW
    }

    public Quest(String title, String description, QuestType type, Priority priority) {
        this.id = title.toLowerCase().replace(" ", "_").replace("'", "").replace("-", "_");
        this.title = title;
        this.description = description;
        this.completed = false;
        this.type = type;
        this.status = QuestStatus.AVAILABLE;
        this.progress = 0;
        this.target = 1;
        this.rewardCoins = 0;
        this.rewardDiamonds = 0;
        this.rewardSeedPackets = 0;
        this.rewardSeedPlantType = null;
        this.priority = priority;
        this.additionalCondition = "";
        this.variableN = 0;
    }

    public void resetProgress() {
        this.progress = 0;
        if (this.status == QuestStatus.IN_PROGRESS) {
            this.status = QuestStatus.AVAILABLE;
        }
    }

    public void applyReward(User user) {
        if (status != QuestStatus.COMPLETED) return;

        if (rewardCoins > 0) {
            user.setCoins(user.getCoins() + rewardCoins);
        }

        if (rewardDiamonds > 0) {
            user.setGems(user.getGems() + rewardDiamonds);
        }

        if (rewardUnlockable != null && !rewardUnlockable.isEmpty()) {
            if (isPlantName(rewardUnlockable)) {
                if (!user.getUnlockedPlants().contains(rewardUnlockable)) {
                    user.getUnlockedPlants().add(rewardUnlockable);
                    user.getPlantLevels().put(rewardUnlockable, 1);
                    user.addNews("New plant unlocked: " + rewardUnlockable + "!");
                }
            } else if (isStageName(rewardUnlockable)) {
                user.addNews("New stage unlocked: " + rewardUnlockable + "!");
            }
        }

        if (rewardSeedPackets > 0 && rewardSeedPlantType != null) {
            Map<String, Integer> packets = user.getSeedPackets();
            int current = packets.getOrDefault(rewardSeedPlantType, 0);
            packets.put(rewardSeedPlantType, current + rewardSeedPackets);
            user.addNews("Received " + rewardSeedPackets + " seed packets for " + rewardSeedPlantType + "!");
        }

        if (type == QuestType.DAILY) {
            user.setCompletedDailyQuests(user.getCompletedDailyQuests() + 1);
        } else {
            user.setCompletedNonDailyQuests(user.getCompletedNonDailyQuests() + 1);
        }

        status = QuestStatus.CLAIMED;
        FileManager.updateUser(user);
    }

    private boolean isPlantName(String name) {
        String[] plants = {"PeaShooter", "Sunflower", "WallNut", "SnowPea", "Repeater",
                "Threepeater", "Cabbagepult", "Kernelpult", "Melonpult",
                "WinterMelon", "TallNut", "Pumpkin", "CherryBomb",
                "PotatoMine", "Squash", "BonkChoy", "LaserBean",
                "WitchHazel", "LilyPad", "TangleKelp", "HomingThistle",
                "LightningReed", "PuffShroom", "FumeShroom", "SunShroom"};
        for (String p : plants) {
            if (p.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private boolean isStageName(String name) {
        String[] stages = {"AncientEgypt", "FrostbiteCaves", "BigWaveBeach", "DarkAges"};
        for (String s : stages) {
            if (s.equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    public void complete() {
        completed = true;
        status = QuestStatus.COMPLETED;
    }

    public void updateProgress(int amount, User user) {
        if (status == QuestStatus.LOCKED || status == QuestStatus.COMPLETED || status == QuestStatus.CLAIMED) return;
        if (status == QuestStatus.AVAILABLE) {
            status = QuestStatus.IN_PROGRESS;
        }
        progress = Math.min(progress + amount, target);
        if (progress >= target) {
            complete();
        }
        if (user != null) {
            FileManager.updateUser(user);
        }
    }

    public void updateProgress(int amount) {
        updateProgress(amount, null);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }
    public QuestType getType() { return type; }
    public QuestStatus getStatus() { return status; }
    public void setStatus(QuestStatus status) { this.status = status; }
    public int getProgress() { return progress; }
    public int getTarget() { return target; }
    public void setTarget(int target) { this.target = target; }
    public int getRewardCoins() { return rewardCoins; }
    public void setRewardCoins(int rewardCoins) { this.rewardCoins = rewardCoins; }
    public int getRewardDiamonds() { return rewardDiamonds; }
    public void setRewardDiamonds(int rewardDiamonds) { this.rewardDiamonds = rewardDiamonds; }
    public String getRewardUnlockable() { return rewardUnlockable; }
    public void setRewardUnlockable(String rewardUnlockable) { this.rewardUnlockable = rewardUnlockable; }
    public int getRewardSeedPackets() { return rewardSeedPackets; }
    public void setRewardSeedPackets(int rewardSeedPackets) { this.rewardSeedPackets = rewardSeedPackets; }
    public String getRewardSeedPlantType() { return rewardSeedPlantType; }
    public void setRewardSeedPlantType(String rewardSeedPlantType) { this.rewardSeedPlantType = rewardSeedPlantType; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public String getAdditionalCondition() { return additionalCondition; }
    public void setAdditionalCondition(String cond) { this.additionalCondition = cond; }
    public int getVariableN() { return variableN; }
    public void setVariableN(int n) { this.variableN = n; }

    public Quest withRewards(int coins, int diamonds, String unlockable, int seedPackets, String seedPlant) {
        this.rewardCoins = coins;
        this.rewardDiamonds = diamonds;
        this.rewardUnlockable = unlockable;
        this.rewardSeedPackets = seedPackets;
        this.rewardSeedPlantType = seedPlant;
        return this;
    }
}