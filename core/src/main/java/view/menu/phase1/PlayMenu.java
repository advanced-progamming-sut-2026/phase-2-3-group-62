package view.menu.phase1;

import controller.menu.MenuController;
import controller.menu.PlayMenuController;

public class PlayMenu extends Menu {
    private final PlayMenuController playMenuController;

    public PlayMenu(MenuController controller) {
        super(controller);
        this.playMenuController = new PlayMenuController(controller);
    }

    @Override
    public void runMenu() {
        String input = view.getInput("play menu");
        String result = playMenuController.handlePlayMenuInput(input);
        if (!result.equals("RETURNING_TO_MAIN") && !result.equals("ENTERING_BATTLEFIELD") &&
                !result.equals("ENTERING_COLLECTION") && !result.equals("ENTERING_GREENHOUSE") &&
                !result.equals("ENTERING_TRAVEL_LOG") && !result.equals("ENTERING_LEADERBOARD")) {
            view.showMessage(result);
        }
    }
}
