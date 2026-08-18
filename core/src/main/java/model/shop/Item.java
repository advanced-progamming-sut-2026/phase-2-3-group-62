package model.shop;

public class Item {
    private String id;
    private String name;
    private int price;
    private String currencyType;
    private int quantity;
    private String bonusType;
    private int bonusAmount;
    private boolean isPermanent;

    public Item(String name, int price) {
        this(name, price, "coin");
    }

    public Item(String name, int price, String currencyType) {
        this.id = name.toLowerCase().replace(" ", "_");
        this.name = name;
        this.price = price;
        this.currencyType = currencyType;
        this.quantity = 1;
        this.isPermanent = true;
    }

    public Item(String name, int price, String currencyType, int quantity) {
        this(name, price, currencyType);
        this.quantity = quantity;
    }

    public Item(String name, int price, String currencyType, int quantity, String bonusType) {
        this(name, price, currencyType);
        this.quantity = quantity;
        this.bonusType = bonusType;
        this.bonusAmount = 500;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public int getPrice() { return price; }
    public String getCurrencyType() { return currencyType; }
    public int getQuantity() { return quantity; }
    public String getBonusType() { return bonusType; }
    public int getBonusAmount() { return bonusAmount; }
    public boolean isPermanent() { return isPermanent; }
    public void setPermanent(boolean permanent) { isPermanent = permanent; }
}