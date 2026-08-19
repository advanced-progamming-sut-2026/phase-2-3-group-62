package model.entities.zombie.boss;

import model.entities.zombie.Zombie;

public class Zomboss extends Zombie {
    private final ZombossType zombossType;
    private final int totalPhases = 3;
    private int currentPhase = 1;
    private int phaseMaxHealth;
    private int phaseCurrentHealth;

    private int primaryRow;
    private int secondaryRow;

    private boolean isStunned = false;
    private int stunTimerTicks = 0;

    private int actionCooldownTicks = 160;
    private int actionTimer = -60;

    private boolean isCharging = false;
    private double originalX = 7.5;
    private boolean returningFromCharge = false;

    private boolean isTurbineActive = false;
    private int turbineDurationTicks = 0;

    private String activeAnimation = "idle";
    private float animationTimer = 0f;

    public Zomboss(ZombossType type, int totalHp) {
        super(type.getDisplayName(), totalHp, 0.0, 500, 0);
        this.zombossType = type;
        this.phaseMaxHealth = totalHp / 3;
        this.phaseCurrentHealth = this.phaseMaxHealth;
        this.primaryRow = 1;
        this.secondaryRow = 2;
        this.setY(primaryRow);
        this.setX(7.5);
    }

    public void takeBossDamage(int amount) {
        phaseCurrentHealth -= amount;

        if (phaseCurrentHealth <= 0) {
            if (currentPhase < totalPhases) {
                currentPhase++;
                phaseCurrentHealth = phaseMaxHealth;
                triggerStun(100);
            } else {
                phaseCurrentHealth = 0;
                setHealth(0);
            }
        }
        setHealth(phaseCurrentHealth + ((totalPhases - currentPhase) * phaseMaxHealth));
    }

    public void triggerStun(int durationTicks) {
        this.isStunned = true;
        this.stunTimerTicks = durationTicks;
        this.isCharging = false;
        this.returningFromCharge = false;
        this.isTurbineActive = false;
        this.activeAnimation = "stun";
        this.animationTimer = durationTicks / 10f;
    }

    public void playBossAnimation(String clipName, float durationSeconds) {
        this.activeAnimation = clipName;
        this.animationTimer = durationSeconds;
    }

    public void updateBossState() {
        if (isStunned) {
            stunTimerTicks--;
            if (stunTimerTicks <= 0) {
                isStunned = false;
                activeAnimation = "idle";
            }
            return;
        }

        actionTimer++;
        if (isTurbineActive) {
            turbineDurationTicks--;
            if (turbineDurationTicks <= 0) {
                isTurbineActive = false;
            }
        }
    }

    public void updateAnimationTimer(float delta) {
        if (animationTimer > 0) {
            animationTimer -= delta;
            if (animationTimer <= 0 && !isStunned) {
                activeAnimation = "idle";
            }
        }
    }

    public boolean occupiesRow(int row) {
        return row == primaryRow || row == secondaryRow;
    }

    public void setOccupiedRows(int topRow) {
        if (topRow < 0) topRow = 0;
        if (topRow > 3) topRow = 3;
        this.primaryRow = topRow;
        this.secondaryRow = topRow + 1;
        this.setY(primaryRow);
    }

    public ZombossType getZombossType() { return zombossType; }
    public int getCurrentPhase() { return currentPhase; }
    public int getPhaseMaxHealth() { return phaseMaxHealth; }
    public int getPhaseCurrentHealth() { return phaseCurrentHealth; }
    public boolean isStunned() { return isStunned; }
    public int getPrimaryRow() { return primaryRow; }
    public int getSecondaryRow() { return secondaryRow; }
    public int getActionTimer() { return actionTimer; }
    public void resetActionTimer() { this.actionTimer = 0; }
    public int getActionCooldownTicks() { return actionCooldownTicks; }
    public void setActionCooldownTicks(int ticks) { this.actionCooldownTicks = ticks; }
    public boolean isBossCharging() { return isCharging; }
    public void setBossCharging(boolean charging) { this.isCharging = charging; }
    public boolean isReturningFromCharge() { return returningFromCharge; }
    public void setReturningFromCharge(boolean returning) { this.returningFromCharge = returning; }
    public double getOriginalX() { return originalX; }
    public boolean isTurbineActive() { return isTurbineActive; }
    public void setTurbineActive(boolean active, int duration) { this.isTurbineActive = active; this.turbineDurationTicks = duration; }
    public String getActiveAnimation() { return activeAnimation; }
}
