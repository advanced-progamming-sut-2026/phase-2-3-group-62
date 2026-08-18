package controller.menu;

import model.greenhouse.Greenhouse;
import model.greenhouse.Pot;
import model.shop.Item;
import model.shop.Shop;
import model.user.User;
import model.user.UserSession;
import util.FileManager;
import util.ParsedCommand;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShopController {
    private final Shop shop;
    private final Greenhouse greenhouse;

    public ShopController(Shop shop, Greenhouse greenhouse) {
        this.shop = shop;
        this.greenhouse = greenhouse;
    }

    public String showShopList() {
        StringBuilder sb = new StringBuilder("=== Permanent Items ===\n");
        for (Item item : shop.getPermanentItems()) {
            sb.append("- ").append(item.getName())
                    .append(" (ID: ").append(item.getId()).append(") | Price: ")
                    .append(item.getPrice()).append(" ").append(item.getCurrencyType())
                    .append(" | Bundle Quantity: ").append(item.getQuantity()).append("\n");
        }
        return sb.toString().trim();
    }

    public String showDailyOffer() {
        shop.checkAndRefreshDailyOffers();
        StringBuilder sb = new StringBuilder("=== Daily Offer ===\n");
        for (Map.Entry<String, Shop.DailyOffer> entry : shop.getDailyOffers().entrySet()) {
            Shop.DailyOffer offer = entry.getValue();
            String status = offer.isPurchased() ? "[PURCHASED]" : "[AVAILABLE]";
            sb.append("- Special 10x Seed Pack for ").append(offer.getPlantType())
                    .append(" | Price: ").append(offer.getDiscountedPrice()).append(" coins (20% Off, Base: ")
                    .append(offer.getPrice()).append(") | Status: ").append(status).append("\n");
        }
        return sb.toString().trim();
    }

    public String buyItem(ParsedCommand cmd) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user logged in.";
        }

        String itemId = cmd.getArg("-i");
        String countStr = cmd.getArg("-n");
        String plantType = cmd.getArg("-t");

        if (itemId == null || countStr == null) {
            return "Error: Missing required parameters. Usage: shop buy -i <item_id> -n <count> [-t <plant_type>]";
        }

        int count;
        try {
            count = Integer.parseInt(countStr);
            if (count <= 0) return "Error: Count must be greater than zero.";
        } catch (NumberFormatException e) {
            return "Error: Count must be an integer.";
        }

        shop.checkAndRefreshDailyOffers();
        itemId = itemId.toLowerCase().replace(" ", "_");

        if (shop.getDailyOffers().containsKey(itemId)) {
            Shop.DailyOffer offer = shop.getDailyOffers().get(itemId);
            if (offer.isPurchased()) {
                return "Error: This daily offer has already been purchased today.";
            }
            if (count != 1) {
                return "Error: Daily offer can only be purchased as 1 unit (contains 10 packets).";
            }
            if (currentUser.getCoins() < offer.getDiscountedPrice()) {
                return "Error: Not enough coins! Required: " + offer.getDiscountedPrice() + " coins.";
            }

            currentUser.setCoins(currentUser.getCoins() - offer.getDiscountedPrice());
            int currentPackets = currentUser.getSeedPackets().getOrDefault(offer.getPlantType(), 0);
            currentUser.getSeedPackets().put(offer.getPlantType(), currentPackets + offer.getQuantity());
            offer.setPurchased(true);
            FileManager.updateUser(currentUser);
            return "Successfully purchased Daily Offer: 10 seed packets for " + offer.getPlantType() + ".";
        }

        Item item = shop.getItemById(itemId);
        if (item == null) {
            return "Error: Item not found in shop.";
        }

        int totalCost = item.getPrice() * count;
        if (item.getCurrencyType().equalsIgnoreCase("coin")) {
            if (currentUser.getCoins() < totalCost) {
                return "Error: Not enough coins! Required: " + totalCost + " coins.";
            }
        } else {
            if (currentUser.getGems() < totalCost) {
                return "Error: Not enough gems! Required: " + totalCost + " gems.";
            }
        }

        if (itemId.equals("pot")) {
            if (greenhouse.getUnlockedPotCount() + count > 20) {
                return "Error: Cannot purchase. Unlocking these pots would exceed the maximum greenhouse capacity of 20 pots.";
            }
            currentUser.setCoins(currentUser.getCoins() - totalCost);
            int unlocked = 0;
            for (Pot p : greenhouse.getPots()) {
                if (p.isLocked() && unlocked < count) {
                    p.setLocked(false);
                    unlocked++;
                }
            }
            FileManager.updateUser(currentUser);
            return "Successfully purchased " + count + " Pot slots in the greenhouse.";
        }

        if (itemId.equals("plant_food")) {
            if (currentUser.getPlantFoodInventory() + count > 3) {
                return "Error: Cannot purchase. Maximum stored plant food capacity is 3 units.";
            }
            currentUser.setGems(currentUser.getGems() - totalCost);
            currentUser.setPlantFoodInventory(currentUser.getPlantFoodInventory() + count);
            FileManager.updateUser(currentUser);
            return "Successfully purchased " + count + " Plant Food units.";
        }

        if (itemId.equals("random_seed_pack")) {
            if (item.getCurrencyType().equalsIgnoreCase("coin")) {
                currentUser.setCoins(currentUser.getCoins() - totalCost);
            } else {
                currentUser.setGems(currentUser.getGems() - totalCost);
            }
            Random rand = new Random();
            List<String> unlocked = currentUser.getUnlockedPlants();
            String randomPlant = "PeaShooter";
            if (unlocked != null && !unlocked.isEmpty()) {
                randomPlant = unlocked.get(rand.nextInt(unlocked.size()));
            }
            int totalPackets = item.getQuantity() * count;
            int currentPackets = currentUser.getSeedPackets().getOrDefault(randomPlant, 0);
            currentUser.getSeedPackets().put(randomPlant, currentPackets + totalPackets);
            FileManager.updateUser(currentUser);
            return "Successfully purchased " + count + " Random Seed Packs. Added " + totalPackets + " seed packets to " + randomPlant + ".";
        }

        if (itemId.equals("choice_seed_pack")) {
            if (plantType == null) {
                return "Error: The -t <plant_type> parameter is mandatory for choosing a specific seed pack.";
            }
            boolean plantUnlocked = false;
            for (String p : currentUser.getUnlockedPlants()) {
                if (p.equalsIgnoreCase(plantType)) {
                    plantType = p;
                    plantUnlocked = true;
                    break;
                }
            }
            if (!plantUnlocked) {
                return "Error: You can only choose seed packs for plants you have already unlocked.";
            }
            currentUser.setGems(currentUser.getGems() - totalCost);
            int totalPackets = item.getQuantity() * count;
            int currentPackets = currentUser.getSeedPackets().getOrDefault(plantType, 0);
            currentUser.getSeedPackets().put(plantType, currentPackets + totalPackets);
            FileManager.updateUser(currentUser);
            return "Successfully purchased " + count + " Choice Seed Packs. Added " + totalPackets + " seed packets to " + plantType + ".";
        }

        if (itemId.equals("currency_exchange")) {
            currentUser.setGems(currentUser.getGems() - totalCost);
            int coinsGained = item.getQuantity() * count;
            currentUser.setCoins(currentUser.getCoins() + coinsGained);
            FileManager.updateUser(currentUser);
            return "Successfully exchanged " + totalCost + " gems for " + coinsGained + " coins.";
        }

        return "Error: Purchase processing failed.";
    }
}
