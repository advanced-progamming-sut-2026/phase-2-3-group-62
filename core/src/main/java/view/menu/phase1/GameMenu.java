package view.menu.phase1;

import controller.menu.GameMenuController;
import controller.menu.MenuController;
import view.game.GameView;

public class GameMenu extends Menu {
    private final GameView gameView = new GameView();
    private final GameMenuController gameMenuController;

    public GameMenu(MenuController controller) {
        super(controller);
        this.gameMenuController = new GameMenuController(controller);
    }

    @Override
    public void runMenu() {
        gameView.showBoardState(gameMenuController.getGame());

        if (gameMenuController.getGame().isWon()) {
            view.showMessage("Congratulations! You won the game!");
            manager.setCurrentMenu(new MainMenu(controller));
            return;
        }
        if (gameMenuController.getGame().isLost()) {
            view.showMessage("Game Over! The zombies ate your brains!");
            manager.setCurrentMenu(new MainMenu(controller));
            return;
        }

        String input = view.getInput("game play");

        if (input.isEmpty()) {
            input = "advance time -t 15";
        }

        String result = gameMenuController.handleGameMenuInput(input);

        if (!result.equals("EXIT_GAME") && !result.equals("SHOW_MAP_TRIGGER")) {
            view.showMessage(result);
        }
    }
}
