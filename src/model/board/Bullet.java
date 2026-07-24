package model.board;

public class Bullet {
    private int damage;
    private int row;
    private int column;
    private BulletType type;
    private boolean pierce;
    private boolean explosive;
    private int splashRadius;
    private int targetRow;
    private boolean active;
    private int hitZombieCount;
    private int maxPierceTargets;

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

    public Bullet(int damage, int row, int column) {
        this(damage, row, column, BulletType.NORMAL, false, false, 0);
    }

    public Bullet(int damage, int row, int column, BulletType type, boolean pierce, boolean explosive, int splashRadius) {
        this.damage = damage;
        this.row = row;
        this.column = column;
        this.type = type;
        this.pierce = pierce;
        this.explosive = explosive;
        this.splashRadius = splashRadius;
        this.targetRow = row;
        this.active = true;
        this.hitZombieCount = 0;
        this.maxPierceTargets = 0;
    }

    public void move() {
        column++;
    }

    public int getDamage() { return damage; }
    public int getRow() { return row; }
    public int getColumn() { return column; }
    public void setColumn(int column) { this.column = column; }
    public BulletType getType() { return type; }
    public boolean isPierce() { return pierce; }
    public boolean isExplosive() { return explosive; }
    public int getSplashRadius() { return splashRadius; }
    public int getTargetRow() { return targetRow; }
    public void setTargetRow(int targetRow) { this.targetRow = targetRow; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public int getHitZombieCount() { return hitZombieCount; }
    public void incrementHitZombieCount() { this.hitZombieCount++; }
    public int getMaxPierceTargets() { return maxPierceTargets; }
    public void setMaxPierceTargets(int maxPierceTargets) { this.maxPierceTargets = maxPierceTargets; }

    public boolean isOutOfBounds(int maxColumns) {
        return column > maxColumns || column < 0;
    }
}