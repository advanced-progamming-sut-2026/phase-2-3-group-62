package controller.menu;

import controller.CommandParser;
import util.ParsedCommand;
import view.menu.CollectionMenu;
import view.menu.GameMenu;
import view.menu.GreenhouseMenu;
import view.menu.LeaderboardMenu;
import view.menu.MainMenu;
import view.menu.MenuManager;
import view.menu.TravelLogMenu;

public class PlayMenuController extends Controller {
    private final PreGameController preGameController;
    private final MenuController menuController;
    private final CommandParser parser;

    public PlayMenuController(MenuController menuController) {
        super(menuController);
        this.menuController = menuController;
        this.preGameController = new PreGameController();
        this.parser = new CommandParser();
    }

    public String handlePlayMenuInput(String input) {
        if (input.equalsIgnoreCase("back")) {
            MenuManager.getInstance().setCurrentMenu(new MainMenu(menuController));
            return "RETURNING_TO_MAIN";
        }

        ParsedCommand cmd = parser.parse(input);
        String action = cmd.getAction();

        if (action.equalsIgnoreCase("menu enter chapter")) {
            return preGameController.processCommand(cmd, "menu enter chapter");
        }
        if (action.equalsIgnoreCase("menu coin-wallet")) {
            return menuController.processPlay(cmd, "coin-wallet");
        }
        if (action.equalsIgnoreCase("menu gem-wallet")) {
            return menuController.processPlay(cmd, "gem-wallet");
        }
        if (action.equalsIgnoreCase("cheat add")) {
            return menuController.processPlay(cmd, "cheat add");
        }
        if (action.equalsIgnoreCase("show all plants")) {
            return preGameController.processCommand(cmd, "show all plants");
        }
        if (action.equalsIgnoreCase("show available plants")) {
            return preGameController.processCommand(cmd, "show available plants");
        }
        if (action.equalsIgnoreCase("add plant")) {
            return preGameController.processCommand(cmd, "add plant");
        }
        if (action.equalsIgnoreCase("remove plant")) {
            return preGameController.processCommand(cmd, "remove plant");
        }
        if (action.equalsIgnoreCase("boost plant")) {
            return preGameController.processCommand(cmd, "boost plant");
        }
        if (action.equalsIgnoreCase("start game")) {
            String result = preGameController.processCommand(cmd, "start game");
            if (result.equals("START_GAME_CONFIRMED")) {
                MenuManager.getInstance().setCurrentMenu(new GameMenu(menuController));
                return "ENTERING_BATTLEFIELD";
            }
            return result;
        }
        if (action.equalsIgnoreCase("menu Collection")) {
            MenuManager.getInstance().setCurrentMenu(new CollectionMenu(menuController));
            return "ENTERING_COLLECTION";
        }
        if (action.equalsIgnoreCase("menu greenhouse")) {
            MenuManager.getInstance().setCurrentMenu(new GreenhouseMenu(menuController));
            return "ENTERING_GREENHOUSE";
        }
        if (action.equalsIgnoreCase("menu travel-log")) {
            MenuManager.getInstance().setCurrentMenu(new TravelLogMenu(menuController));
            return "ENTERING_TRAVEL_LOG";
        }
        if (action.equalsIgnoreCase("menu leaderboard")) {
            MenuManager.getInstance().setCurrentMenu(new LeaderboardMenu(menuController));
            return "ENTERING_LEADERBOARD";
        }
        if (action.equalsIgnoreCase("menu enter minigame")) {
            return preGameController.processCommand(cmd, "menu enter minigame");
        }

        return "Unknown command inside Play Menu.";
    }
}