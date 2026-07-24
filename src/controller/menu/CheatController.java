package controller.menu;

import controller.game.GameController;
import model.user.UserSession;
import util.ParsedCommand;

public class CheatController {
    private final GameController gameController;

    public CheatController(GameController gameController) {
        this.gameController = gameController;
    }

    public String handleCheatCommand(ParsedCommand cmd) {
        String action = cmd.getAction();

        if (action.equalsIgnoreCase("cheat remove-cooldown")) {
            return gameController.executeRemoveCooldownCheat();
        }
        if (action.equalsIgnoreCase("cheat add-plant-food")) {
            return gameController.executeAddPlantFoodCheat();
        }
        if (action.equalsIgnoreCase("cheat spawn-zombie")) {
            String type = cmd.getArg("-t");
            String loc = cmd.getArg("-l");
            if (type == null || loc == null) {
                return "Usage: cheat spawn-zombie -t <type> -l (<x>, <y>)";
            }
            try {
                loc = loc.replace("(", "").replace(")", "");
                String[] coords = loc.split(",");
                int x = Integer.parseInt(coords[0].trim());
                int y = Integer.parseInt(coords[1].trim());

                if (gameController.getGame() == null) {
                    return "Error: No active game session.";
                }

                if (y < 0 || y >= gameController.getGame().getBoard().getRows() || x < 0 || x >= gameController.getGame().getBoard().getColumns()) {
                    return "Error: Coordinates out of board bounds! Maximum row allowed is " + (gameController.getGame().getBoard().getRows() - 1);
                }

                String formattedType = type.equalsIgnoreCase("normalzombie") ? "NormalZombie" : type;
                model.entities.zombie.Zombie z = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn(formattedType, y, x, gameController.getGame().getDifficultyLevel());
                if (z != null) {
                    if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
                        java.util.List<String> observed = UserSession.getCurrentUser().getObservedZombies();
                        if (!observed.contains(z.getName())) {
                            observed.add(z.getName());
                            util.FileManager.updateUser(UserSession.getCurrentUser());
                        }
                    }
                    gameController.getGame().addZombie(z);
                    return "Zombie spawned via cheat.";
                }
                return "Invalid zombie type.";
            } catch (Exception e) {
                return "Invalid format! Use: cheat spawn-zombie -t <type> -l (<x>, <y>)";
            }
        }
        if (action.equalsIgnoreCase("cheat add")) {
            if (cmd.hasFlag("-n")) {
                try {
                    String countStr = cmd.getArg("-n");
                    int amount = Integer.parseInt(countStr.split(" ")[0]);
                    return gameController.addCheatSuns(amount);
                } catch (Exception e) {
                    return "Invalid cheat format.";
                }
            } else {
                String valueStr = cmd.getArg("VALUE");
                if (valueStr != null && valueStr.toLowerCase().contains("sun")) {
                    try {
                        int amount = Integer.parseInt(valueStr.toLowerCase().replace("suns", "").trim());
                        return gameController.addCheatSuns(amount);
                    } catch (Exception e) {
                        return "Invalid cheat format.";
                    }
                }
            }
        }
        return "Unknown cheat command.";
    }
}