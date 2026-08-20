package model.board;

public class Bullet {
    private int damage;
    private int row;
    private double column;
    private double exactRow;
    private double dx;
    private double dy;
    private BulletType type;
    private boolean pierce;
    private boolean explosive;
    private int splashRadius;
    private int targetRow;
    private boolean active;
    private int hitZombieCount;
    private int maxPierceTargets;
    private String plantName;
    private double startColumn;
    private double targetColumn;
    private boolean bouncing;
    private int lifespanTicks;

    public enum BulletType {
        NORMAL,
        ICE,
        FIRE,
        POISON,
        LASER,
        LOB,
        HOMING,
        STRIKE_THROUGH,
        ELECTRIC,
        MAGIC
    }

    public Bullet(int damage, int row, double column) {
        this(damage, row, column, BulletType.NORMAL, false, false, 0);
    }

    public Bullet(int damage, int row, double column, BulletType type, boolean pierce, boolean explosive, int splashRadius) {
        this.damage = damage;
        this.row = row;
        this.exactRow = row;
        this.column = column;
        this.startColumn = column;
        this.targetColumn = 8.0;
        this.dx = 0.35;
        this.dy = 0.0;
        this.type = type;
        this.pierce = pierce;
        this.explosive = explosive;
        this.splashRadius = splashRadius;
        this.targetRow = row;
        this.active = true;
        this.hitZombieCount = 0;
        this.maxPierceTargets = 0;
        this.plantName = "";
        this.bouncing = false;
        this.lifespanTicks = 0;
    }

    public void move() {
        this.column += this.dx;
        this.exactRow += this.dy;

        if (this.bouncing) {
            if (this.lifespanTicks > 0) {
                this.lifespanTicks--;
            }
            if (this.column <= 0.0) {
                this.column = 0.0;
                this.dx = Math.abs(this.dx);
            } else if (this.column >= 8.5) {
                this.column = 8.5;
                this.dx = -Math.abs(this.dx);
            }
            if (this.exactRow <= 0.0) {
                this.exactRow = 0.0;
                this.dy = Math.abs(this.dy);
            } else if (this.exactRow >= 4.0) {
                this.exactRow = 4.0;
                this.dy = -Math.abs(this.dy);
            }
        }

        this.row = (int) Math.round(this.exactRow);
    }

    public int getDamage() { return damage; }
    public void setDamage(int damage) { this.damage = damage; }
    public int getRow() { return row; }
    public void setRow(int row) { this.row = row; this.exactRow = row; }
    public double getExactRow() { return exactRow; }
    public void setExactRow(double exactRow) { this.exactRow = exactRow; this.row = (int) Math.round(exactRow); }
    public double getColumn() { return column; }
    public void setColumn(double column) { this.column = column; }
    public double getStartColumn() { return startColumn; }
    public void setStartColumn(double startColumn) { this.startColumn = startColumn; }
    public double getTargetColumn() { return targetColumn; }
    public void setTargetColumn(double targetColumn) { this.targetColumn = targetColumn; }
    public double getDx() { return dx; }
    public void setDx(double dx) { this.dx = dx; }
    public double getDy() { return dy; }
    public void setDy(double dy) { this.dy = dy; }
    public BulletType getType() { return type; }
    public void setType(BulletType type) { this.type = type; }
    public boolean isPierce() { return pierce; }
    public void setPierce(boolean pierce) { this.pierce = pierce; }
    public boolean isExplosive() { return explosive; }
    public void setExplosive(boolean explosive) { this.explosive = explosive; }
    public int getSplashRadius() { return splashRadius; }
    public void setSplashRadius(int splashRadius) { this.splashRadius = splashRadius; }
    public int getTargetRow() { return targetRow; }
    public void setTargetRow(int targetRow) { this.targetRow = targetRow; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) { this.plantName = plantName; }
    public boolean isBouncing() { return bouncing; }
    public void setBouncing(boolean bouncing) { this.bouncing = bouncing; }
    public int getLifespanTicks() { return lifespanTicks; }
    public void setLifespanTicks(int lifespanTicks) { this.lifespanTicks = lifespanTicks; }

    public int getHitZombieCount() { return hitZombieCount; }
    public void incrementHitZombieCount() { this.hitZombieCount++; }
    public int getMaxPierceTargets() { return maxPierceTargets; }
    public void setMaxPierceTargets(int maxPierceTargets) { this.maxPierceTargets = maxPierceTargets; }

    public boolean isOutOfBounds(int maxColumns) {
        if (this.bouncing) {
            return this.lifespanTicks <= 0;
        }
        return column > maxColumns || column < -1.0 || exactRow < -1.0 || exactRow > 5.0;
    }
}
