package model.shop;

import model.user.UserSession;

import java.util.*;

public class Shop {
    private final List<Item> items;
    private final List<Item> permanentItems;
    private final Map<String, DailyOffer> dailyOffers;
    private long lastRefreshTime;
    private String lastRefreshDateStr;

    public static class DailyOffer {
        private String id;
        private String plantType;
        private int price;
        private int discountPercentage;
        private int quantity;
        private boolean purchased;

        public DailyOffer(String id, String plantType, int price, int discountPercentage, int quantity) {
            this.id = id;
            this.plantType = plantType;
            this.price = price;
            this.discountPercentage = discountPercentage;
            this.quantity = quantity;
            this.purchased = false;
        }

        public String getId() { return id; }
        public String getPlantType() { return plantType; }
        public int getPrice() { return price; }
        public int getDiscountPercentage() { return discountPercentage; }
        public int getQuantity() { return quantity; }
        public boolean isPurchased() { return purchased; }
        public void setPurchased(boolean purchased) { this.purchased = purchased; }
        public int getDiscountedPrice() {
            return (int) (price * (1 - discountPercentage / 100.0));
        }
    }

    public Shop() {
        this.items = new ArrayList<>();
        this.permanentItems = new ArrayList<>();
        this.dailyOffers = new HashMap<>();
        this.lastRefreshTime = System.currentTimeMillis();
        this.lastRefreshDateStr = java.time.LocalDate.now().toString();
        initializeShop();
    }

    private void initializeShop() {
        Item pot = new Item("Pot", 2000, "coin", 1);
        Item pf = new Item("Plant Food", 3, "diamond", 1);
        Item randPack = new Item("Random Seed Pack", 1000, "coin", 5);
        Item choicePack = new Item("Choice Seed Pack", 5, "diamond", 10);
        Item exchange = new Item("Currency Exchange", 5, "diamond", 500, "coin");

        permanentItems.add(pot);
        permanentItems.add(pf);
        permanentItems.add(randPack);
        permanentItems.add(choicePack);
        permanentItems.add(exchange);

        items.addAll(permanentItems);
        refreshDailyOffers();
    }

    public void checkAndRefreshDailyOffers() {
        String today = java.time.LocalDate.now().toString();
        if (!today.equals(lastRefreshDateStr)) {
            refreshDailyOffers();
            lastRefreshDateStr = today;
        }
    }

    public void refreshDailyOffers() {
        dailyOffers.clear();
        List<String> unlocked = new ArrayList<>();
        if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
            unlocked = UserSession.getCurrentUser().getUnlockedPlants();
        }
        if (unlocked == null || unlocked.isEmpty()) {
            unlocked = new ArrayList<>();
            unlocked.add("PeaShooter");
        }
        String plant = unlocked.get(new Random().nextInt(unlocked.size()));
        String today = java.time.LocalDate.now().toString();
        dailyOffers.put(plant.toLowerCase(), new DailyOffer(
                "daily_" + today.replace("-", ""),
                plant,
                2000,
                20,
                10
        ));
        lastRefreshTime = System.currentTimeMillis();
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public List<Item> getItems() {
        return items;
    }

    public Item getItemById(String id) {
        for (Item item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public DailyOffer getDailyOffer(String plantType) {
        checkAndRefreshDailyOffers();
        return dailyOffers.get(plantType.toLowerCase());
    }

    public Map<String, DailyOffer> getDailyOffers() {
        checkAndRefreshDailyOffers();
        return new HashMap<>(dailyOffers);
    }

    public boolean purchaseItem(String itemId, int quantity) {
        Item item = getItemById(itemId);
        if (item == null) return false;
        return true;
    }

    public boolean purchaseDailyOffer(String plantType) {
        checkAndRefreshDailyOffers();
        DailyOffer offer = dailyOffers.get(plantType.toLowerCase());
        if (offer == null || offer.isPurchased()) return false;
        offer.setPurchased(true);
        return true;
    }

    public List<Item> getPermanentItems() {
        return new ArrayList<>(permanentItems);
    }

    public boolean needsRefresh() {
        String today = java.time.LocalDate.now().toString();
        return !today.equals(lastRefreshDateStr);
    }
}
