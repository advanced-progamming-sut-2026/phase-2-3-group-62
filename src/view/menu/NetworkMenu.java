package view.menu;

import controller.menu.MenuController;

public class NetworkMenu extends Menu {

    public NetworkMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        view.showMessage("Network Menu is under construction.");
    }
}