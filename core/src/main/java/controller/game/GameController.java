package controller.game;

import controller.menu.Controller;
import controller.menu.MenuController;
import model.Game;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;

import java.util.ArrayList;
import java.util.List;

public class GameController extends Controller {
    private Game game;
    private boolean cooldownCheatActive = false;
    private final List<String> accumulatedTurnLogs = new ArrayList<>();

    private final GameActionController actionController = new GameActionController();
    private final MiniGameController miniGameController = new MiniGameController();

    public GameController(MenuController controller) {
        super(controller);
    }

    public GameController(Game game) {
        super(null);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public boolean isCooldownCheatActive() {
        return cooldownCheatActive;
    }

    public void setCooldownCheatActive(boolean active) {
        this.cooldownCheatActive = active;
    }

    public String plantPlant(String type, int x, int y) {
        return actionController.plantPlant(game, type, x, y);
    }

    public String pluckPlant(int x, int y) {
        return actionController.pluckPlant(game, x, y);
    }

    public String feedPlant(int x, int y) {
        return actionController.feedPlant(game, x, y);
    }

    public String collectSun(int x, int y) {
        return actionController.collectSun(game, x, y);
    }

    public int advanceTime(int ticks) {
        return actionController.advanceTime(game, ticks, accumulatedTurnLogs);
    }

    public String swapPlants(int x1, int y1, int x2, int y2) {
        return miniGameController.swapPlants(game, x1, y1, x2, y2);
    }

    public String upgradePlants(String fromType, String toType) {
        return miniGameController.upgradePlants(game, fromType, toType);
    }

    public String placeZombie(String type, int lane) {
        return placeZombie(type, 8, lane);
    }

    public String placeZombie(String type, int x, int y) {
        return miniGameController.placeZombie(game, type, x, y);
    }

    public String smashVase(int x, int y) {
        return miniGameController.smashVase(game, x, y);
    }

    public String pickupPacket(int x, int y) {
        return miniGameController.pickupPacket(game, x, y);
    }

    public String executeNuke() {
        if (game == null) return "Error: No active game session.";
        List<Zombie> toRemove = new ArrayList<>();
        for (Zombie z : game.getActiveZombies()) {
            if (!(z instanceof Zomboss)) {
                game.getBoard().getTile(z.getY(), (int) Math.round(z.getX())).setZombie(null);
                toRemove.add(z);
            }
        }
        game.getActiveZombies().removeAll(toRemove);
        return "Nuke released! " + toRemove.size() + " regular zombies wiped off the map. Bosses are immune to nukes!";
    }

    public String executeRemoveCooldownCheat() {
        cooldownCheatActive = !cooldownCheatActive;
        if (cooldownCheatActive) {
            return "Cheat activated: Cooldown limits removed for all plants.";
        } else {
            return "Cheat deactivated: Cooldown limits restored.";
        }
    }

    public String executeAddPlantFoodCheat() {
        if (game == null) return "Error: No active game session.";
        game.addPlantFood();
        return "Cheat activated: Added 1 plant food. Total: " + game.getPlantFoodCount();
    }

    public String addCheatSuns(int amount) {
        if (game == null) return "Error: No active game session.";
        game.addSun(amount);
        return "Cheat activated: Added " + amount + " suns.";
    }

    public List<String> extractAccumulatedTurnLogs() {
        List<String> copy = new ArrayList<>(accumulatedTurnLogs);
        accumulatedTurnLogs.clear();
        return copy;
    }
}
