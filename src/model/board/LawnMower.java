package model.board;

public class LawnMower {
    private final int row;
    private boolean used;

    public LawnMower(int row) {
        this.row = row;
    }

    public void activate() {
        used = true;
    }

    public int getRow() {
        return row;
    }

    public boolean isUsed() {
        return used;
    }
}

