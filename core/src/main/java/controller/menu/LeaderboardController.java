package controller.menu;


import model.leaderboard.Leaderboard;
import model.user.User;
import util.FileManager;
import util.ParsedCommand;
import view.game.phase1.TerminalView;

import java.util.List;

public class LeaderboardController {
    private final TerminalView view;

    public LeaderboardController(TerminalView view) {
        this.view = view;
    }

    public void handleLeaderboardMenuInput(ParsedCommand cmd) {
        List<User> allUsers = FileManager.loadUsers();
        Leaderboard leaderboard = new Leaderboard();
        for (User u : allUsers) {
            leaderboard.addUser(u);
        }

        String sortBy = cmd.getArg("-s");
        String order = cmd.getArg("-o");

        Leaderboard.SortType sortType = Leaderboard.SortType.BY_TOTAL_SCORE;
        boolean isAscending = false;

        if (sortBy != null) {
            switch (sortBy.toLowerCase()) {
                case "level":
                    sortType = Leaderboard.SortType.BY_LEVEL;
                    break;
                case "minigame":
                    sortType = Leaderboard.SortType.BY_MINI_GAMES;
                    break;
                case "dailyquest":
                    sortType = Leaderboard.SortType.BY_DAILY_QUESTS;
                    break;
                case "nondailyquest":
                    sortType = Leaderboard.SortType.BY_NON_DAILY_QUESTS;
                    break;
                case "scoring":
                    sortType = Leaderboard.SortType.BY_SCORING_GAME;
                    break;
                case "score":
                default:
                    sortType = Leaderboard.SortType.BY_TOTAL_SCORE;
                    break;
            }
        }

        if (order != null && (order.equalsIgnoreCase("asc") || order.equalsIgnoreCase("ascending"))) {
            isAscending = true;
        }

        leaderboard.setSortType(sortType, isAscending);
        List<User> sortedList = leaderboard.getSortedUsers();

        view.showMessage("\n======================================== GLOBAL LEADERBOARD ========================================");
        String header = String.format("%-4s | %-12s | %-11s | %-10s | %-9s | %-12s | %-16s | %-12s",
                "Rank", "Username", "Total Score", "Last Level", "Minigames", "Daily Quests", "Non-Daily Quests", "Scoring High");
        view.showMessage(header);
        view.showMessage("----------------------------------------------------------------------------------------------------");

        int rank = 1;
        for (User u : sortedList) {
            String levelStr = "S" + u.getLastSeasonCompleted() + "-L" + u.getLastLevelCompleted();
            String row = String.format("%-4d | %-12s | %-11d | %-10s | %-9d | %-12d | %-16d | %-12d",
                    rank,
                    u.getUsername(),
                    u.getScore(),
                    levelStr,
                    u.getCompletedMiniGames(),
                    u.getCompletedDailyQuests(),
                    u.getCompletedNonDailyQuests(),
                    u.getHighestScoreInScoringGame());
            view.showMessage(row);
            rank++;
        }
        view.showMessage("====================================================================================================\n");
    }
}
