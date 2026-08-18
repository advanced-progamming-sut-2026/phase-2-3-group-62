package view.menu.phase1;

import controller.CommandParser;
import controller.menu.MenuController;
import model.quest.Quest;
import model.user.User;
import model.user.UserSession;
import util.ParsedCommand;

import java.util.ArrayList;
import java.util.List;

public class TravelLogMenu extends Menu {

    public TravelLogMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        while (true) {
            String input = view.getInput("TravelLogMenu");
            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new PlayMenu(controller));
                break;
            }

            ParsedCommand cmd = parser.parse(input);
            String action = cmd.getAction();

            if (action.equalsIgnoreCase("travel log page")) {
                String pageName = input.substring("travel log page".length()).trim().toLowerCase();
                if (pageName.isEmpty()) {
                    view.showMessage("Error: Please specify a page name (story, epic, daily, minigame).");
                    continue;
                }

                User currentUser = UserSession.getCurrentUser();
                if (currentUser == null) {
                    view.showMessage("Error: No user logged in.");
                    continue;
                }

                List<Quest> currentQuests = currentUser.getUserQuests();

                if (pageName.equals("story")) {
                    view.showMessage("=== STORY QUESTS (CRITICAL) ===");
                    displayQuestsSorted(currentQuests, Quest.QuestType.STORY);
                } else if (pageName.equals("epic")) {
                    view.showMessage("=== EPIC QUESTS (HIGH PRIORITY) ===");
                    displayQuestsSorted(currentQuests, Quest.QuestType.EPIC);
                } else if (pageName.equals("daily")) {
                    view.showMessage("=== DAILY QUESTS ===");
                    displayQuestsSorted(currentQuests, Quest.QuestType.DAILY);
                } else if (pageName.equals("minigame")) {
                    view.showMessage("=== MINI-GAMES PAGE ===");
                    displayMinigamePage(currentUser);
                } else {
                    view.showMessage("Error: Unknown travel log page.");
                }
            } else if (action.equalsIgnoreCase("claim quests") || input.equalsIgnoreCase("claim")) {
                User currentUser = UserSession.getCurrentUser();
                if (currentUser == null) {
                    view.showMessage("Error: No user logged in.");
                    continue;
                }
                view.showMessage(currentUser.claimAllCompletedQuests());
            } else {
                view.showMessage("Unknown command inside Travel Log Menu.");
                view.showMessage("Available commands: 'travel log page <story/epic/daily/minigame>', 'claim quests', 'back'");
            }
        }
    }

    private void displayQuestsSorted(List<Quest> list, Quest.QuestType filterType) {
        List<Quest> filtered = new ArrayList<>();
        for (Quest q : list) {
            if (q.getType() == filterType) {
                filtered.add(q);
            }
        }

        if (filtered.isEmpty()) {
            view.showMessage("No quests available in this category.");
            return;
        }


        filtered.sort((q1, q2) -> {
            if (q1.getPriority() == Quest.Priority.CRITICAL) return -1;
            if (q2.getPriority() == Quest.Priority.CRITICAL) return 1;
            if (q1.getPriority() == Quest.Priority.HIGH) return -1;
            if (q2.getPriority() == Quest.Priority.HIGH) return 1;
            if (q1.getPriority() == Quest.Priority.MEDIUM) return -1;
            if (q2.getPriority() == Quest.Priority.MEDIUM) return 1;
            return 0;
        });

        for (Quest q : filtered) {
            String priorityBadge = switch (q.getPriority()) {
                case CRITICAL -> "⭐ ";
                case HIGH -> "🔥 ";
                case MEDIUM -> "📌 ";
                case LOW -> "📎 ";
            };

            String statusStr = switch (q.getStatus()) {
                case LOCKED -> "[🔒 LOCKED]";
                case AVAILABLE -> "[📋 AVAILABLE]";
                case IN_PROGRESS -> "[⏳ IN PROGRESS]";
                case COMPLETED -> "[✅ COMPLETED]";
                case CLAIMED -> "[🏆 CLAIMED]";
            };

            int progress = q.getProgress();
            int target = q.getTarget();
            String progressBar = getProgressBar(progress, target);

            view.showMessage(String.format("%s %s", priorityBadge, q.getTitle()));
            view.showMessage("    " + q.getDescription());
            view.showMessage(String.format("    %s | Progress: %d/%d %s", statusStr, progress, target, progressBar));

            if (q.getStatus() != Quest.QuestStatus.COMPLETED && q.getStatus() != Quest.QuestStatus.CLAIMED) {
                String rewards = getRewardPreview(q);
                if (!rewards.isEmpty()) {
                    view.showMessage("    Reward: " + rewards);
                }
            }
            view.showMessage("");
        }
    }

    private String getProgressBar(int progress, int target) {
        if (target <= 0) return "";
        int percent = (int) ((double) progress / target * 100);
        int barLength = 20;
        int filled = (int) ((double) progress / target * barLength);
        StringBuilder bar = new StringBuilder("[");
        for (int i = 0; i < barLength; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("] ").append(percent).append("%");
        return bar.toString();
    }

    private String getRewardPreview(Quest q) {
        List<String> rewards = new ArrayList<>();
        if (q.getRewardCoins() > 0) rewards.add(q.getRewardCoins() + " coins");
        if (q.getRewardDiamonds() > 0) rewards.add(q.getRewardDiamonds() + " gems");
        if (q.getRewardUnlockable() != null && !q.getRewardUnlockable().isEmpty()) {
            rewards.add("Unlock: " + q.getRewardUnlockable());
        }
        if (q.getRewardSeedPackets() > 0 && q.getRewardSeedPlantType() != null) {
            rewards.add(q.getRewardSeedPackets() + "x " + q.getRewardSeedPlantType() + " seeds");
        }
        return String.join(", ", rewards);
    }

    private void displayMinigamePage(User currentUser) {
        view.showMessage("┌─────────────────────────────────────────────────────┐");
        view.showMessage("│                    MINI-GAMES                     │");
        view.showMessage("├─────────────────────────────────────────────────────┤");

        String[] minigames = {"Vasebreaker", "WallnutBowling", "IZombie", "Beghoul", "Zombotany"};
        String[] internalNames = {"Vasebreaker", "WallnutBowling", "IZombie", "Beghoul", "Zombotany"};

        for (int i = 0; i < minigames.length; i++) {
            String name = minigames[i];
            String internal = internalNames[i];

            boolean unlocked = true;
            boolean completed = isMinigameCompleted(currentUser, internal);

            String status;
            if (completed) {
                status = "✅ COMPLETED";
            } else if (unlocked) {
                status = "🔓 AVAILABLE";
            } else {
                status = "🔒 LOCKED";
            }

            String stageInfo = getMinigameStageInfo(currentUser, internal);
            view.showMessage(String.format("│ %-2d. %-17s │ %-12s │ %s",
                    i + 1, name, status, stageInfo));
        }

        view.showMessage("├─────────────────────────────────────────────────────┤");
        view.showMessage("│ Use: menu enter minigame -m <name>                │");
        view.showMessage("│      start game                                   │");
        view.showMessage("└─────────────────────────────────────────────────────┘");
    }

    private boolean isMinigameCompleted(User user, String minigameName) {
        return false;
    }

    private String getMinigameStageInfo(User user, String minigameName) {
        return "Level 1/3";
    }
}
