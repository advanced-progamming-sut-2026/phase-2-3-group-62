package model.entities.plant;

import java.util.List;
import java.util.Random;

public class Plant {
    private int id;
    private String name;
    private String category;
    private List<String> tags;
    private int cost;
    private int baseHp;
    private int damage;
    private double actionInterval;
    private double recharge;
    private String abilityType;
    private double abilityValue;
    private String plantFoodType;
    private double plantFoodValue;
    private String pamPath;
    private int health;
    private int maxHealth;
    private boolean isBoosted;
    private boolean hasSunToCollect;
    private int x;
    private int y;
    private int freezeLevel;
    private int iceHealth;
    private boolean isTransformedToSheep;
    private String animState = "idle";
    private float animStateTimer = 0f;

    public void triggerAttack(float duration) {
        if (!"plantfood".equals(this.animState)) {
            this.animState = "attack";
            this.animStateTimer = duration;
        }
    }

    public void triggerPlantFood(float duration) {
        this.animState = "plantfood";
        this.animStateTimer = duration;
    }

    public String getAnimState() {
        return animState;
    }

    public void updateAnimState(float delta) {
        if (animStateTimer > 0) {
            animStateTimer -= delta;
            if (animStateTimer <= 0) {
                animState = "idle";
                animStateTimer = 0;
            }
        }
    }
    private int dx;
    private int dy;
    private int hitCount;
    private boolean isBowlingBall;
    private int tickCounter;

    private int plantStage;
    private int stageTickCounter;
    private int lifespanTicks;
    private boolean isArmed;
    private int armTimerTicks;
    private int digestTimerTicks;
    private int peaPodHeads;
    private boolean hasPlantFoodArmor;
    private int armorHp;
    private boolean isBlueFlame;
    private int magnetCooldownTicks;

    private int level = 1;

    public Plant(int id, String name, String category, List<String> tags, int cost, int baseHp, int damage, double actionInterval, double recharge, String abilityType, double abilityValue, String plantFoodType, double plantFoodValue) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.tags = tags;
        this.cost = cost;
        this.baseHp = baseHp;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
        this.abilityType = abilityType;
        this.abilityValue = abilityValue;
        this.plantFoodType = plantFoodType;
        this.plantFoodValue = plantFoodValue;
        this.pamPath = "";
        this.health = baseHp;
        this.maxHealth = baseHp;
        this.isBoosted = false;
        this.hasSunToCollect = false;
        this.freezeLevel = 0;
        this.iceHealth = 0;
        this.isTransformedToSheep = false;
        this.dx = 0;
        this.dy = 0;
        this.hitCount = 0;
        this.isBowlingBall = false;
        this.tickCounter = 0;
        this.plantStage = 1;
        this.stageTickCounter = 0;
        this.lifespanTicks = 600;
        this.isArmed = false;
        this.armTimerTicks = 0;
        this.digestTimerTicks = 0;
        this.peaPodHeads = 1;
        this.hasPlantFoodArmor = false;
        this.armorHp = 0;
        this.isBlueFlame = false;
        this.magnetCooldownTicks = 0;
        if (name != null) {
            if (name.equalsIgnoreCase("Potato Mine")) {
                this.armTimerTicks = 150;
            } else if (name.equalsIgnoreCase("Primal Potato Mine")) {
                this.armTimerTicks = 50;
            }
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
        applyLevelUpgrades();
    }

    private void applyLevelUpgrades() {
        if (name == null) return;
        String cleanName = name.replace(" ", "").replace("-", "");

        if (cleanName.equalsIgnoreCase("Sunflower")) {
            if (level >= 2) this.actionInterval = Math.max(1.0, this.actionInterval - 2.0);
            if (level >= 3) { this.baseHp += 150; initHealth(); }
        } else if (cleanName.equalsIgnoreCase("TwinSunflower")) {
            if (level >= 2) this.actionInterval = Math.max(1.0, this.actionInterval - 2.0);
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("SunShroom")) {
            if (level >= 2) this.actionInterval = Math.max(1.0, this.actionInterval - 2.0);
            if (level >= 3) { this.baseHp += 150; initHealth(); }
        } else if (cleanName.equalsIgnoreCase("PrimalSunflower")) {
            if (level >= 2) this.actionInterval = Math.max(1.0, this.actionInterval - 2.0);
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("GoldBloom")) {
            if (level >= 2) this.recharge = Math.max(0.0, this.recharge - 5.0);
            if (level >= 3) this.abilityValue += 50.0;
            if (level >= 4) this.recharge = Math.max(0.0, this.recharge - 10.0);
        } else if (cleanName.equalsIgnoreCase("Peashooter")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("Repeater")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) this.recharge = Math.max(0.0, this.recharge - 1.0);
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("Threepeater")) {
            if (level >= 2) this.cost = Math.max(0, this.cost - 25);
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.recharge = Math.max(0.0, this.recharge - 1.0);
        } else if (cleanName.equalsIgnoreCase("SnowPea")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) this.abilityValue += 1.5;
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("Rotobaga")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("PeaPod")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) { this.baseHp += 200; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("SplitPea")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) { this.baseHp += 200; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("Citron")) {
            if (level >= 2) this.actionInterval = Math.max(1.0, this.actionInterval - 1.0);
            if (level >= 3) this.damage += 150;
            if (level >= 4) this.cost = Math.max(0, this.cost - 50);
        } else if (cleanName.equalsIgnoreCase("Caulipower")) {
            if (level >= 2) this.recharge = Math.max(0.0, this.recharge - 2.0);
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 50);
        } else if (cleanName.equalsIgnoreCase("ElectricBlueberry")) {
            if (level >= 2) this.recharge = Math.max(0.0, this.recharge - 2.0);
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("BowlingBulb")) {
            if (level >= 2) this.actionInterval = Math.max(1.0, this.actionInterval - 1.0);
            if (level >= 3) this.damage += 15;
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("Cactus")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) this.damage += 10;
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("Starfruit")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("FirePeashooter")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) { this.baseHp += 150; initHealth(); }
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        } else if (cleanName.equalsIgnoreCase("LaserBean")) {
            if (level >= 2) this.damage += 10;
            if (level >= 3) this.actionInterval = Math.max(1.0, this.actionInterval - 0.5);
            if (level >= 4) this.cost = Math.max(0, this.cost - 25);
        }
    }

    public void initHealth() {
        this.health = this.baseHp;
        this.maxHealth = this.baseHp;
    }

    public void takeDamage(int amount) {
        if (this.armorHp > 0) {
            this.armorHp -= amount;
            if (this.armorHp < 0) {
                this.health += this.armorHp;
                this.armorHp = 0;
            }
        } else {
            this.health -= amount;
        }
        if (this.health < 0) {
            this.health = 0;
        }
    }

    public void heal(int amount) {
        this.health += amount;
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }

    public boolean isAlive() {
        return this.health > 0 || this.armorHp > 0;
    }

    public void update() {
        this.tickCounter++;
        this.stageTickCounter++;
        if (this.name != null) {
            if (this.name.equalsIgnoreCase("Sun-shroom")) {
                int stg2Ticks = (level >= 2) ? 190 : 240;
                int stg3Ticks = (level >= 2) ? 720 : 960;
                if (this.plantStage == 1 && this.stageTickCounter >= stg2Ticks) {
                    this.plantStage = 2;
                } else if (this.plantStage == 2 && this.stageTickCounter >= stg3Ticks) {
                    this.plantStage = 3;
                }
            } else if (this.name.equalsIgnoreCase("Kiwibeast")) {
                if (this.plantStage == 1 && this.stageTickCounter >= 240) {
                    this.plantStage = 2;
                } else if (this.plantStage == 2 && this.stageTickCounter >= 960) {
                    this.plantStage = 3;
                }
            } else if (this.name.equalsIgnoreCase("Puff-shroom") || this.name.equalsIgnoreCase("Sea-shroom")) {
                if (this.lifespanTicks > 0) {
                    this.lifespanTicks--;
                    if (this.lifespanTicks <= 0) {
                        this.health = 0;
                    }
                }
            } else if (this.name.equalsIgnoreCase("Potato Mine") || this.name.equalsIgnoreCase("Primal Potato Mine")) {
                if (!this.isArmed) {
                    if (this.armTimerTicks > 0) {
                        this.armTimerTicks--;
                        if (this.armTimerTicks <= 0) {
                            this.isArmed = true;
                        }
                    }
                }
            } else if (this.name.equalsIgnoreCase("Chomper")) {
                if (this.digestTimerTicks > 0) {
                    this.digestTimerTicks--;
                }
            } else if (this.name.equalsIgnoreCase("Magnet-shroom")) {
                if (this.magnetCooldownTicks > 0) {
                    this.magnetCooldownTicks--;
                }
            }
        }
    }

    public boolean shouldShoot() {
        int intervalTicks = (int) (this.actionInterval * 10);
        if (intervalTicks <= 0) {
            intervalTicks = 15;
        }
        if (this.tickCounter >= intervalTicks) {
            this.tickCounter = 0;
            return true;
        }
        return false;
    }

    public String getShootBehavior() {
        if (this.category != null && (this.category.equalsIgnoreCase("SHOOTER") || this.category.equalsIgnoreCase("STRIKE_THROUGH") || this.category.equalsIgnoreCase("HOMING") || this.category.equalsIgnoreCase("LOBBER"))) {
            return this.category;
        }
        return "None";
    }

    public double getCooldown() {
        return this.recharge;
    }

    public double getSunProduce() {
        if (this.category != null && this.category.equalsIgnoreCase("SUN_PRODUCER")) {
            if (this.name != null && this.name.equalsIgnoreCase("Sun-shroom")) {
                double baseProd = this.plantStage == 1 ? 25.0 : (this.plantStage == 2 ? 50.0 : 75.0);
                if (this.level >= 4 && new Random().nextBoolean()) {
                    baseProd *= 2;
                }
                return baseProd;
            }
            double amount = this.abilityValue;
            if (this.name != null && this.name.equalsIgnoreCase("Sunflower") && this.level >= 4) {
                if (new Random().nextBoolean()) {
                    amount *= 2;
                }
            }
            return amount;
        }
        return 0.0;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public List<String> getTags() { return tags; }
    public int getCost() { return cost; }
    public int getBaseHp() { return baseHp; }
    public int getDamage() {
        if (this.name != null && this.name.equalsIgnoreCase("Pea Pod")) {
            return (20 + (level >= 2 ? 10 : 0)) * this.peaPodHeads;
        }
        if (this.name != null && this.name.equalsIgnoreCase("Kiwibeast")) {
            return 15 * this.plantStage;
        }
        return damage;
    }
    public double getActionInterval() { return actionInterval; }
    public double getRecharge() { return recharge; }
    public String getAbilityType() { return abilityType; }
    public double getAbilityValue() { return abilityValue; }
    public String getPlantFoodType() { return plantFoodType; }
    public double getPlantFoodValue() { return plantFoodValue; }
    public String getPamPath() { return (pamPath != null && !pamPath.isEmpty()) ? pamPath : "768/INITIAL/PLANT/PEASHOOTER/PEASHOOTER.PAM"; }
    public void setPamPath(String pamPath) { this.pamPath = pamPath; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public boolean isBoosted() { return isBoosted; }
    public void setBoosted(boolean boosted) { isBoosted = boosted; }
    public boolean isHasSunToCollect() { return hasSunToCollect; }
    public void setHasSunToCollect(boolean hasSunToCollect) { this.hasSunToCollect = hasSunToCollect; }
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public boolean isTransformedToSheep() { return isTransformedToSheep; }
    public void setTransformedToSheep(boolean transformedToSheep) { isTransformedToSheep = transformedToSheep; }

    public int getFreezeLevel() { return freezeLevel; }
    public void setFreezeLevel(int freezeLevel) {
        this.freezeLevel = freezeLevel;
        if (this.freezeLevel >= 3) {
            this.freezeLevel = 3;
            this.iceHealth = 600;
        } else if (this.freezeLevel < 0) {
            this.freezeLevel = 0;
            this.iceHealth = 0;
        }
    }

    public int getIceHealth() { return iceHealth; }
    public void setIceHealth(int iceHealth) {
        this.iceHealth = iceHealth;
        if (this.iceHealth <= 0) {
            this.iceHealth = 0;
            this.freezeLevel = 0;
        }
    }

    public boolean isFrozen() { return freezeLevel == 3; }
    public void damageIce(int amount) {
        this.iceHealth -= amount;
        if (this.iceHealth <= 0) {
            this.iceHealth = 0;
            this.freezeLevel = 0;
        }
    }
    public void melt() {
        this.iceHealth = 0;
        this.freezeLevel = 0;
    }

    public boolean isAquatic() {
        if (name != null && (name.equalsIgnoreCase("Lily Pad") || name.equalsIgnoreCase("Tangle Kelp") || name.equalsIgnoreCase("Sea-shroom"))) {
            return true;
        }
        if (tags != null) {
            for (String tag : tags) {
                if (tag.equalsIgnoreCase("aquatic") || tag.equalsIgnoreCase("water")) {
                    return true;
                }
            }
        }
        return false;
    }

    public int getDx() { return dx; }
    public void setDx(int dx) { this.dx = dx; }
    public int getDy() { return dy; }
    public void setDy(int dy) { this.dy = dy; }
    public int getHitCount() { return hitCount; }
    public void incrementHitCount() { this.hitCount++; }
    public boolean isBowlingBall() { return isBowlingBall; }
    public void setBowlingBall(boolean bowlingBall) { this.isBowlingBall = bowlingBall; }

    public int getPlantStage() { return plantStage; }
    public void setPlantStage(int plantStage) { this.plantStage = plantStage; }
    public int getLifespanTicks() { return lifespanTicks; }
    public void resetLifespan() { this.lifespanTicks = 600; }
    public boolean isArmed() { return isArmed; }
    public void setArmed(boolean armed) { this.isArmed = armed; }
    public int getDigestTimerTicks() { return digestTimerTicks; }
    public void startDigestion(int ticks) { this.digestTimerTicks = ticks; }
    public boolean isDigesting() { return digestTimerTicks > 0; }
    public int getPeaPodHeads() { return peaPodHeads; }
    public void incrementPeaPodHead() { if (this.peaPodHeads < 5) this.peaPodHeads++; }
    public boolean hasPlantFoodArmor() { return hasPlantFoodArmor; }
    public void applyPlantFoodArmor(int armorAmount) {
        this.hasPlantFoodArmor = true;
        this.armorHp = armorAmount;
    }
    public int getArmorHp() { return armorHp; }
    public boolean isBlueFlame() { return isBlueFlame; }
    public void setBlueFlame(boolean blueFlame) { isBlueFlame = blueFlame; }
    public int getMagnetCooldownTicks() { return magnetCooldownTicks; }
    public void startMagnetCooldown(int ticks) { this.magnetCooldownTicks = ticks; }
}
