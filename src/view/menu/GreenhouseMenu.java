package view.menu;

import controller.CommandParser;
import controller.menu.MenuController;
import controller.menu.GreenhouseController;
import controller.menu.ShopController;
import model.greenhouse.Greenhouse;
import model.shop.Shop;
import view.greenhouse.GreenhouseView;
import util.ParsedCommand;

public class GreenhouseMenu extends Menu {
    private Greenhouse greenhouse;
    private final GreenhouseView greenhouseView;
    private final GreenhouseController greenhouseController;
    private final Shop shop;
    private final ShopController shopController;
    private boolean inShopMode;

    public GreenhouseMenu(MenuController controller) {
        super(controller);
        this.greenhouse = new Greenhouse();
        this.greenhouseView = new GreenhouseView();
        this.greenhouseController = new GreenhouseController(this.greenhouse, this.greenhouseView);
        this.shop = new Shop();
        this.shopController = new ShopController(this.shop, this.greenhouse);
        this.inShopMode = false;
    }

    @Override
    public void runMenu() {
        while (true) {
            String prompt = inShopMode ? "ShopMenu" : "GreenhouseMenu";
            String input = view.getInput(prompt);

            if (input.equalsIgnoreCase("back")) {
                if (inShopMode) {
                    inShopMode = false;
                    view.showMessage("Returned to the greenhouse.");
                } else {
                    manager.setCurrentMenu(new PlayMenu(controller));
                    break;
                }
                continue;
            }

            String lowerInput = input.trim().toLowerCase();

            if (lowerInput.startsWith("cheat add") || lowerInput.startsWith("menu cheat add")) {
                CommandParser parser = new CommandParser();
                ParsedCommand cmd = parser.parse(input);
                view.showMessage(controller.processPlay(cmd, "cheat add"));
                continue;
            }

            if (!inShopMode) {
                if (lowerInput.equals("enter shop")) {
                    inShopMode = true;
                    view.showMessage("Welcome to the shop! Permanent items and daily offers are available.");
                } else if (lowerInput.equals("show greenhouse")) {
                    greenhouseController.showGreenhouse();
                } else if (lowerInput.startsWith("plant pot at")) {
                    try {
                        String loc = input.substring(input.indexOf("(")).replace("(", "").replace(")", "").trim();
                        String[] parts = loc.split(",");
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        view.showMessage(greenhouseController.plantPot(x, y));
                    } catch (Exception e) {
                        view.showMessage("Invalid format! Use: plant pot at (<x>, <y>)");
                    }
                } else if (lowerInput.startsWith("collect")) {
                    try {
                        String loc = input.substring(input.indexOf("(")).replace("(", "").replace(")", "").trim();
                        String[] parts = loc.split(",");
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        view.showMessage(greenhouseController.collectPot(x, y));
                    } catch (Exception e) {
                        view.showMessage("Invalid format! Use: collect (<x>, <y>)");
                    }
                } else if (lowerInput.startsWith("grow")) {
                    try {
                        String loc = input.substring(input.indexOf("(")).replace("(", "").replace(")", "").trim();
                        String[] parts = loc.split(",");
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        view.showMessage(greenhouseController.acceleratePot(x, y));
                    } catch (Exception e) {
                        view.showMessage("Invalid format! Use: grow (<x>, <y>)");
                    }
                } else if (lowerInput.startsWith("unlock")) {
                    try {
                        String loc = input.substring(input.indexOf("(")).replace("(", "").replace(")", "").trim();
                        String[] parts = loc.split(",");
                        int x = Integer.parseInt(parts[0].trim());
                        int y = Integer.parseInt(parts[1].trim());
                        view.showMessage(greenhouseController.unlockPot(x, y));
                    } catch (Exception e) {
                        view.showMessage("Invalid format! Use: unlock (<x>, <y>)");
                    }
                } else {
                    view.showMessage("Unknown or incomplete command in Greenhouse Menu.");
                }
            } else {
                CommandParser parser = new CommandParser();
                ParsedCommand cmd = parser.parse(input);
                String action = cmd.getAction();

                if (action.equalsIgnoreCase("shop list")) {
                    view.showMessage(shopController.showShopList());
                } else if (action.equalsIgnoreCase("shop daily")) {
                    view.showMessage(shopController.showDailyOffer());
                } else if (action.equalsIgnoreCase("shop buy")) {
                    view.showMessage(shopController.buyItem(cmd));
                } else {
                    view.showMessage("Unknown command in Shop Mode.");
                }
            }
        }
    }
}