package view.menu.phase1;

import controller.menu.MenuController;

public class LeaderboardMenu extends Menu {

    public LeaderboardMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        MenuController ctrl = (MenuController) this.controller;
        String input = view.getInput("LeaderboardMenu");
        ctrl.handleLeaderboardMenuInput(input);
    }
}
