package model.board;

public class DroppedItem {
    public enum ItemType { PLANT_FOOD, COIN, DIAMOND, POT }

    private final ItemType type;
    private final int row;
    private final int column;
    private final float pixelX;
    private final float pixelY;

    public DroppedItem(ItemType type, int row, int column, float pixelX, float pixelY) {
        this.type = type;
        this.row = row;
        this.column = column;
        this.pixelX = pixelX;
        this.pixelY = pixelY;
    }

    public ItemType getType() { return type; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public float getPixelX() { return pixelX; }
    public float getPixelY() { return pixelY; }
}
