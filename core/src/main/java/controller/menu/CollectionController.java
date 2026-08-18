package controller.menu;

import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import model.entities.zombie.Zombie;
import model.entities.zombie.loader.ZombieLoader;
import model.user.User;
import model.user.UserSession;
import util.FileManager;
import util.ParsedCommand;

import java.util.List;

public class CollectionController {
    private final MenuController menuController;

    public CollectionController(MenuController menuController) {
        this.menuController = menuController;
    }

    public String processCollection(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }
        List<Plant> allPlants = PlantLoader.loadPlants();
        List<Zombie> allZombies = ZombieLoader.loadZombies();

        if (action.equalsIgnoreCase("show-plants")) {
            List<String> unlocked = currentUser.getUnlockedPlants();
            if (unlocked.isEmpty()) {
                return "You have no unlocked plants.";
            }
            StringBuilder sb = new StringBuilder("Your unlocked plants:\n");
            for (String plant : unlocked) {
                int level = currentUser.getPlantLevels().getOrDefault(plant, 1);
                sb.append("- ").append(plant).append(" (Level ").append(level).append(")\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-all-plants")) {
            StringBuilder sb = new StringBuilder("All game plants:\n");
            for (Plant plant : allPlants) {
                sb.append("- ").append(plant.getName());

                String category = plant.getCategory() != null ? plant.getCategory().toString() : "N/A";
                String tags = (plant.getTags() != null && !plant.getTags().isEmpty())
                        ? String.join(", ", plant.getTags())
                        : "None";

                sb.append(" [Category: ").append(category)
                        .append(" | Tags: ").append(tags).append("]\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-zombies")) {
            List<String> observed = currentUser.getObservedZombies();
            if (observed.isEmpty()) {
                return "You have not observed any zombies yet.";
            }
            StringBuilder sb = new StringBuilder("Observed zombies:\n");
            for (String zombie : observed) {
                sb.append("- ").append(zombie).append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-all-zombies")) {
            StringBuilder sb = new StringBuilder("All game zombies:\n");
            for (Zombie zombie : allZombies) {
                sb.append("- ").append(zombie.getName()).append("\n");
            }
            return sb.toString().trim();
        }
        if (action.equalsIgnoreCase("show-plant")) {
            String plantName = cmd.getArg("-p");
            Plant targetPlant = null;
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(plantName)) {
                    targetPlant = p;
                    break;
                }
            }
            if (targetPlant == null) {
                return "Error: Plant not found in game data.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Plant: ").append(targetPlant.getName()).append("\n");
            sb.append("Category: ").append(targetPlant.getCategory()).append("\n");
            sb.append("Tags: ").append(targetPlant.getTags() != null ? String.join(", ", targetPlant.getTags()) : "None").append("\n");
            sb.append("Sun Cost: ").append(targetPlant.getCost()).append("\n");
            sb.append("HP: ").append(targetPlant.getHealth()).append("\n");
            sb.append("Shoot Behavior: ").append(targetPlant.getShootBehavior());
            if (targetPlant.getCooldown() > 0) {
                sb.append("\nCooldown: ").append(targetPlant.getCooldown()).append("s");
            }
            if (targetPlant.getSunProduce() > 0) {
                sb.append("\nSun Produce: ").append(targetPlant.getSunProduce());
            }
            return sb.toString();
        }
        if (action.equalsIgnoreCase("show-zombie")) {
            String zombieName = cmd.getArg("-z");
            Zombie targetZombie = null;
            for (Zombie z : allZombies) {
                if (z.getName().equalsIgnoreCase(zombieName)) {
                    targetZombie = z;
                    break;
                }
            }
            if (targetZombie == null) {
                return "Error: Zombie not found in game data.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Zombie: ").append(targetZombie.getName()).append("\n");
            sb.append("HP: ").append(targetZombie.getHealth()).append("\n");
            sb.append("Speed: ").append(targetZombie.getSpeed()).append("\n");
            sb.append("Damage: ").append(targetZombie.getDamage());
            return sb.toString();
        }
        if (action.equalsIgnoreCase("upgrade-plant")) {
            String target = cmd.getArg("-p");
            int currentLevel = currentUser.getPlantLevels().getOrDefault(target, 1);
            int upgradeCost = currentLevel * 1000;
            if (currentUser.getCoins() < upgradeCost) {
                return "Error: Insufficient coins. Required: " + upgradeCost + ", You have: " + currentUser.getCoins();
            }
            currentUser.setCoins(currentUser.getCoins() - upgradeCost);
            currentUser.getPlantLevels().put(target, currentLevel + 1);
            FileManager.updateUser(currentUser);
            return "Plant " + target + " upgraded to Level " + (currentLevel + 1) + " successfully!";
        }
        if (action.equalsIgnoreCase("purchase-plant")) {
            String target = cmd.getArg("-p");
            if (target == null) return "Error: Please specify plant with -p <plant_name>";

            target = target.replaceAll("^\"|\"$", "").trim();
            final String cleanTarget = target;

            Plant dbPlant = allPlants.stream()
                    .filter(p -> p.getName().replace(" ", "").replace("-", "")
                            .equalsIgnoreCase(cleanTarget.replace(" ", "").replace("-", "")))
                    .findFirst().orElse(null);

            if (dbPlant == null) {
                return "Error: Plant not found in game data.";
            }

            String realName = dbPlant.getName();

            boolean alreadyUnlocked = currentUser.getUnlockedPlants().stream()
                    .anyMatch(p -> p.replace(" ", "").replace("-", "").replace("\"", "")
                            .equalsIgnoreCase(realName.replace(" ", "").replace("-", "")));

            if (alreadyUnlocked) {
                return "Error: You already own this plant.";
            }
            if (currentUser.getCoins() < 2000) {
                return "Error: Not enough coins. Cost is 2000. You have: " + currentUser.getCoins();
            }

            currentUser.setCoins(currentUser.getCoins() - 2000);
            currentUser.getUnlockedPlants().add(realName);
            currentUser.getPlantLevels().put(realName, 1);
            menuController.addNews("New plant unlocked: " + realName + "! Check your collection.");
            FileManager.updateUser(currentUser);
            return "Plant " + realName + " purchased successfully for 2000 coins!";
        }
        return "error";
    }
}