package controller.menu;

import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.greenhouse.Greenhouse;
import model.greenhouse.Pot;
import model.user.User;
import model.user.UserSession;
import util.FileManager;
import view.greenhouse.GreenhouseView;

import java.util.List;
import java.util.Random;

public class GreenhouseController {
    private final Greenhouse greenhouse;
    private final GreenhouseView view;

    public GreenhouseController(Greenhouse greenhouse, GreenhouseView view) {
        this.greenhouse = greenhouse;
        this.view = view;
    }

    public void showGreenhouse() {
        greenhouse.updateAllPots();
        view.showGreenhouseState(greenhouse);
    }

    public String plantPot(int x, int y) {
        int row = y - 1;
        int col = x - 1;
        Pot pot = greenhouse.getPot(row, col);
        if (pot == null) {
            return "Error: Invalid pot coordinates! x must be 1-5 and y must be 1-4.";
        }
        if (pot.isLocked()) {
            return "Error: This pot is locked!";
        }
        if (!pot.isEmpty()) {
            return "Error: This pot is already occupied.";
        }

        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user logged in.";
        }

        Random rand = new Random();
        Plant plant;
        if (rand.nextInt(100) < 50) {
            plant = PlantFactory.createPlant("Marigold");
            if (plant == null) {
                plant = new Plant(999, "Marigold", "Marigold", null, 0, 100, 0, 0, 0, null, 0, null, 0);
            }
        } else {
            List<String> unlocked = currentUser.getUnlockedPlants();
            String plantName = "PeaShooter";
            if (unlocked != null && !unlocked.isEmpty()) {
                plantName = unlocked.get(rand.nextInt(unlocked.size()));
            }
            plant = PlantFactory.createPlant(plantName);
        }

        if (plant == null) {
            return "Error: Failed to create plant.";
        }

        pot.plant(plant);
        FileManager.updateUser(currentUser);
        return "Successfully planted " + plant.getName() + " at Pot (" + x + ", " + y + ").";
    }

    public String collectPot(int x, int y) {
        greenhouse.updateAllPots();
        int row = y - 1;
        int col = x - 1;
        Pot pot = greenhouse.getPot(row, col);
        if (pot == null) {
            return "Error: Invalid pot coordinates!";
        }
        if (pot.isLocked()) {
            return "Error: This pot is locked.";
        }
        if (pot.isEmpty()) {
            return "Error: This pot is empty.";
        }
        if (!pot.isReadyToHarvest()) {
            return "Error: Plant is not fully grown yet.";
        }

        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user logged in.";
        }

        Plant plant = pot.getPlant();
        currentUser.setCoins(currentUser.getCoins() + 500);

        if (!plant.getName().equalsIgnoreCase("Marigold")) {
            currentUser.getGreenhouseBoosts().put(plant.getName(), true);
        }

        pot.harvest();
        FileManager.updateUser(currentUser);
        return "Harvested " + plant.getName() + "! Received 500 coins.";
    }

    public String acceleratePot(int x, int y) {
        greenhouse.updateAllPots();
        int row = y - 1;
        int col = x - 1;
        Pot pot = greenhouse.getPot(row, col);
        if (pot == null) {
            return "Error: Invalid pot coordinates!";
        }
        if (pot.isLocked()) {
            return "Error: This pot is locked.";
        }
        if (pot.isEmpty()) {
            return "Error: This pot is empty.";
        }
        if (pot.isReadyToHarvest()) {
            return "Error: This plant is already fully grown.";
        }

        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user logged in.";
        }

        int cost = pot.getDiamondCostToAccelerate();
        if (currentUser.getGems() < cost) {
            return "Error: Not enough gems! Required: " + cost + " gems.";
        }

        currentUser.setGems(currentUser.getGems() - cost);
        pot.accelerateGrowth();
        FileManager.updateUser(currentUser);
        return "Accelerated growth! Spent " + cost + " gems.";
    }

    public String unlockPot(int x, int y) {
        int row = y - 1;
        int col = x - 1;
        Pot pot = greenhouse.getPot(row, col);
        if (pot == null) {
            return "Error: Invalid pot coordinates!";
        }
        if (!pot.isLocked()) {
            return "Error: This pot is already unlocked.";
        }

        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user logged in.";
        }

        int unlockCost = 200;
        if (currentUser.getCoins() < unlockCost) {
            return "Error: Not enough coins! Required: " + unlockCost + " coins.";
        }

        currentUser.setCoins(currentUser.getCoins() - unlockCost);
        pot.setLocked(false);
        FileManager.updateUser(currentUser);
        return "Successfully unlocked pot at (" + x + ", " + y + ") for " + unlockCost + " coins.";
    }
}
