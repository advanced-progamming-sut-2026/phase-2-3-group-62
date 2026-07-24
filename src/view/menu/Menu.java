package view.menu;

import controller.CommandParser;
import controller.menu.MenuController;
import view.game.TerminalView;

public abstract class Menu {
    protected MenuController controller;
    protected TerminalView view;
    protected MenuManager manager;
    CommandParser parser = new CommandParser();
    public Menu(MenuController controller) {
        this.controller = controller;
        this.view = new TerminalView();
        this.manager = MenuManager.getInstance();
    }
    public final void run() {
        runMenu();
    }
    private void showHeader() {
        view.showUnknownCommandError();
    }
    public abstract void runMenu();

}