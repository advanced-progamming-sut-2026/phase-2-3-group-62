package model.handler;

import model.Game;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import model.minigame.Vasebreaker;
import model.user.User;
import model.user.UserSession;

public class GameStateManager {

    public boolean checkSpecialLevelRules(Game game) {
        model.enums.SpecialLevelType specialType = game.getLevel().getSpecialLevelType();

        if (specialType == model.enums.SpecialLevelType.SAVE_OUR_SEEDS) {
            for (Plant p : game.getSeedsToProtect()) {
                if (!p.isAlive() || !game.getActivePlants().contains(p)) {
                    game.setLost(true);
                    game.stop();
                    game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
                    return true;
                }
            }
        }

        if (specialType == model.enums.SpecialLevelType.TIMED_WAR) {
            if (game.getZombiesKilledInLevel() >= game.getLevel().getTargetZombiesToKill() || game.getSunCount() >= game.getLevel().getTargetSunsToProduce()) {
                game.setWon(true);
                game.stop();
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                handleVictoryStats(game);
                return true;
            }
            if (game.getTickCount() >= game.getLevel().getTimeLimitTicks()) {
                game.setLost(true);
                game.stop();
                game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
                return true;
            }
        }

        if (specialType == model.enums.SpecialLevelType.DEAD_LINE) {
            int lineCol = game.getLevel().getDeadlineColumn();
            for (Zombie z : game.getActiveZombies()) {
                if (z.getX() <= lineCol) {
                    game.setLost(true);
                    game.stop();
                    game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
                    return true;
                }
            }
        }

        if (specialType == model.enums.SpecialLevelType.LOVE_YOUR_PLANTS) {
            if (game.getPlantsLostCount() > game.getLevel().getMaxPlantsLostAllowed()) {
                game.setLost(true);
                game.stop();
                game.getGameLogMessages().add("The zombie ate your brain; LOSER!!!");
                return true;
            }
        }

        return false;
    }

    public void checkWaveProgress(Game game) {
        if (game.getSpawner() == null || game.getActiveMiniGame() instanceof Vasebreaker || game.getActiveMiniGame() instanceof IZombie || game.getActiveMiniGame() instanceof Beghoul) {
            return;
        }

        if (game.getSpawner().isWaveComplete() && game.getActiveZombies().isEmpty()) {
            if (game.getSpawner().getCurrentWave() < game.getSpawner().getTotalWaves()) {
                int nextWave = game.getSpawner().getCurrentWave() + 1;
                if (game.getCurrentSeason() != null) { game.getCurrentSeason().handleWaveStart(game); }
                game.getSpawner().startWave(nextWave);
                if (game.getSpawner().isFinalWave()) {
                    game.getGameLogMessages().add("The final wave has come.");
                } else {
                    game.getGameLogMessages().add("Wave " + nextWave + " started.");
                }
            } else {
                game.setWon(true);
                game.stop();
                game.getScoreGame().onWaveCompleted(game.getSpawner().getCurrentWave());
                game.getGameLogMessages().add("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                handleVictoryStats(game);
            }
        } else if (!game.getSpawner().isWaveComplete()) {
            double healthSum = 0;
            double maxHealthSum = 0;
            for (Zombie z : game.getActiveZombies()) {
                healthSum += z.getHealth();
                maxHealthSum += z.getMaxHealth();
            }
            int scheduledCount = game.getSpawner().getZombiesInWave();
            int remainingToSpawn = scheduledCount - game.getSpawner().getZombiesSpawnedInWave();
            double remainingSpawnHealth = remainingToSpawn * 200.0;
            double totalWaveHealth = (scheduledCount * 200.0) + maxHealthSum;
            double currentWaveHealth = healthSum + remainingSpawnHealth;
            if (totalWaveHealth > 0 && (currentWaveHealth / totalWaveHealth) <= 0.25) {
                if (game.getSpawner().getCurrentWave() < game.getSpawner().getTotalWaves()) {
                    int nextWave = game.getSpawner().getCurrentWave() + 1;
                    if (game.getCurrentSeason() != null) { game.getCurrentSeason().handleWaveStart(game); }
                    game.getSpawner().startWave(nextWave);
                    if (game.getSpawner().isFinalWave()) {
                        game.getGameLogMessages().add("The final wave has come.");
                    } else {
                        game.getGameLogMessages().add("Wave " + nextWave + " started.");
                    }
                }
            }
        }
    }

    public static void handleVictoryStats(Game game) {
        if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
            User user = UserSession.getCurrentUser();
            int earnedScore = game.getScoreGame().getFinalScore();
            user.setScore(user.getScore() + earnedScore);

            if (earnedScore > user.getHighestScoreInScoringGame()) {
                user.setHighestScoreInScoringGame(earnedScore);
            }

            if (game.getActiveMiniGame() != null) {
                QuestManager.notifyMinigameCompleted();
            } else {
                QuestManager.notifyLevelCompleted();
            }

            user.addNews("Congratulations! Level completed successfully.");
            util.FileManager.updateUser(user);
        }
    }
}