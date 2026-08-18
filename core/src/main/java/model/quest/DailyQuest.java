package model.quest;

public class DailyQuest extends Quest {
    public DailyQuest(String title, String description) {
        super(title, description, QuestType.DAILY, Priority.MEDIUM);
    }
}