package model.board;

public class Sun {
    private int value;
    private int row;
    private int column;

    public Sun(int value, int row, int column) {
        this.value = value;
        this.row = row;
        this.column = column;
    }

    public int getValue() {
        return value;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}

