package model.leaderboard;

import model.user.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Leaderboard {
    private final List<User> users;
    private SortType currentSortType;
    private boolean isAscending;

    public enum SortType {
        BY_TOTAL_SCORE,
        BY_LEVEL,
        BY_MINI_GAMES,
        BY_DAILY_QUESTS,
        BY_NON_DAILY_QUESTS,
        BY_SCORING_GAME
    }

    public Leaderboard() {
        this.users = new ArrayList<>();
        this.currentSortType = SortType.BY_TOTAL_SCORE;
        this.isAscending = false;
    }

    public void addUser(User user) {
        if (user != null) {
            users.add(user);
        }
    }

    public void removeUser(User user) {
        users.remove(user);
    }

    public List<User> getSortedUsers() {
        List<User> sorted = new ArrayList<>(users);
        Comparator<User> comparator;

        switch (currentSortType) {
            case BY_LEVEL:
                comparator = Comparator.comparingInt(u -> (u.getLastSeasonCompleted() * 100 + u.getLastLevelCompleted()));
                break;
            case BY_MINI_GAMES:
                comparator = Comparator.comparingInt(User::getCompletedMiniGames);
                break;
            case BY_DAILY_QUESTS:
                comparator = Comparator.comparingInt(User::getCompletedDailyQuests);
                break;
            case BY_NON_DAILY_QUESTS:
                comparator = Comparator.comparingInt(User::getCompletedNonDailyQuests);
                break;
            case BY_SCORING_GAME:
                comparator = Comparator.comparingInt(User::getHighestScoreInScoringGame);
                break;
            case BY_TOTAL_SCORE:
            default:
                comparator = Comparator.comparingInt(User::getScore);
                break;
        }

        if (!isAscending) {
            comparator = comparator.reversed();
        }

        sorted.sort(comparator);
        return sorted;
    }

    public List<User> getTopUsers(int limit) {
        List<User> sorted = getSortedUsers();
        if (limit > sorted.size()) {
            limit = sorted.size();
        }
        return sorted.subList(0, limit);
    }

    public void setSortType(SortType sortType, boolean isAscending) {
        this.currentSortType = sortType;
        this.isAscending = isAscending;
    }

    public List<User> getUsers() {
        return new ArrayList<>(users);
    }

    public SortType getCurrentSortType() {
        return currentSortType;
    }

    public boolean isAscending() {
        return isAscending;
    }

    public void clear() {
        users.clear();
    }

    public int size() {
        return users.size();
    }
}
