package view.menu.phase1;

import controller.menu.MenuController;

public class CollectionMenu extends Menu {

    public CollectionMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        MenuController ctrl = (MenuController) this.controller;
        String input = view.getInput("CollectionMenu");
        ctrl.handleCollectionMenuInput(input);
    }
}
