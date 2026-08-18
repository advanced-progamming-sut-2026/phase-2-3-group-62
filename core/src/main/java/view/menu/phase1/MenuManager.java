package view.menu.phase1;

public class MenuManager {
    private static MenuManager instance;
    private Menu currentMenu;

    private MenuManager() {}

    public static MenuManager getInstance() {
        if (instance == null) {
            instance = new MenuManager();
        }
        return instance;
    }

    public void setCurrentMenu(Menu menu) {
        this.currentMenu = menu;
    }

    public void run() {
        while (currentMenu != null) {
            currentMenu.run();
        }
    }
}
