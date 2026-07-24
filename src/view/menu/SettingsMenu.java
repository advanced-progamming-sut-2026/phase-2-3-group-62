package view.menu;

import controller.CommandParser;
import controller.menu.MenuController;
import util.ParsedCommand;

public class SettingsMenu extends Menu {

    public SettingsMenu(MenuController controller) {
        super(controller);
    }

    @Override
    public void runMenu() {
        CommandParser parser = new CommandParser();
        MenuController ctrl = (MenuController) this.controller;
        while (true) {
            String input = view.getInput("settings");
            ParsedCommand cmd = parser.parse(input);

            if (input.equalsIgnoreCase("back")) {
                manager.setCurrentMenu(new MainMenu(controller));
                break;
            }
            else if(cmd.getAction().equalsIgnoreCase("menu settings change-difficulty")){
                view.showMessage(controller.processSetting(cmd));
            }

        }
    }
}