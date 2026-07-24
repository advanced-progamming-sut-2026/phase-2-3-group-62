package model.handler;

import model.quest.Quest;
import model.user.User;
import model.user.UserSession;

public class QuestManager {

    public static void notifyZombieKilled() {
        if (!UserSession.isLoggedIn()) return;
        User user = UserSession.getCurrentUser();
        if (user == null || user.getQuests() == null) return;

        for (Quest quest : user.getQuests()) {
            String title = quest.getTitle().toLowerCase();
            String desc = quest.getDescription().toLowerCase();
            if (title.contains("zombie") || desc.contains("kill") || desc.contains("zombie")) {
                quest.updateProgress(1, user);
            }
        }
    }

    public static void notifySunCollected(int amount) {
        if (!UserSession.isLoggedIn()) return;
        User user = UserSession.getCurrentUser();
        if (user == null || user.getQuests() == null) return;

        for (Quest quest : user.getQuests()) {
            String title = quest.getTitle().toLowerCase();
            String desc = quest.getDescription().toLowerCase();
            if (title.contains("sun") || desc.contains("sun") || desc.contains("collect")) {
                quest.updateProgress(amount, user);
            }
        }
    }

    public static void notifyPlantPlaced() {
        if (!UserSession.isLoggedIn()) return;
        User user = UserSession.getCurrentUser();
        if (user == null || user.getQuests() == null) return;

        for (Quest quest : user.getQuests()) {
            String title = quest.getTitle().toLowerCase();
            String desc = quest.getDescription().toLowerCase();
            if (title.contains("plant") || desc.contains("plant") || desc.contains("place")) {
                quest.updateProgress(1, user);
            }
        }
    }

    public static void notifyLevelCompleted() {
        if (!UserSession.isLoggedIn()) return;
        User user = UserSession.getCurrentUser();
        if (user == null || user.getQuests() == null) return;

        for (Quest quest : user.getQuests()) {
            String title = quest.getTitle().toLowerCase();
            String desc = quest.getDescription().toLowerCase();
            if (title.contains("level") || desc.contains("level") || title.contains("win") || desc.contains("win")) {
                quest.updateProgress(1, user);
            }
        }
    }

    public static void notifyMinigameCompleted() {
        if (!UserSession.isLoggedIn()) return;
        User user = UserSession.getCurrentUser();
        if (user == null || user.getQuests() == null) return;

        for (Quest quest : user.getQuests()) {
            String title = quest.getTitle().toLowerCase();
            String desc = quest.getDescription().toLowerCase();
            if (title.contains("minigame") || desc.contains("minigame") || title.contains("mini")) {
                quest.updateProgress(1, user);
            }
        }
    }
}