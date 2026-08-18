package model.quest;

public class StoryQuest extends Quest {
    public StoryQuest(String title, String description) {
        super(title, description, QuestType.STORY, Priority.CRITICAL);
    }
}