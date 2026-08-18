package model.quest;

public class EpicQuest extends Quest {
    public EpicQuest(String title, String description) {
        super(title, description, QuestType.EPIC, Priority.HIGH);
    }
}