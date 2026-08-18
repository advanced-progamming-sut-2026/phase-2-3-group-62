package model.entities.zombie;

public class Zombie {
    private String name;
    private int health;
    private int maxHealth;
    private double speed;
    private int damage;
    private int waveCost;
    private double x;
    private int y;
    private int armorHealth;
    private int maxArmorHealth;
    private String armorType;
    private boolean isGlowing;
    private boolean isHypnotized;
    private double chilledDuration;
    private double frozenDuration;
    private int stolenSuns;
    private boolean isAngry;
    private boolean isTorchLit;
    private double dynamiteTimer;
    private int frozenIceHealth;
    private boolean immuneToFreeze;

    private boolean isTransformedToSheep;
    private boolean hasThrownImp;
    private boolean isCharging;
    private boolean isSubmerged;
    private boolean isSpinning;
    private int iceHitsTaken;
    private int wizardTimer;
    private int kingTimer;
    private int raStealTimer;
    private int tombraiserTimer;
    private int fishermanTimer;
    private int octopusTimer;
    private int turquoiseLaserTimer;
    private int pianoPlayTimer;
    private boolean isEating = false;

    private String customAnim = null;
    private float customAnimTimer = 0f;

    public void triggerCustomAnim(String animName, float duration) {
        this.customAnim = animName;
        this.customAnimTimer = duration;
    }

    public String getCustomAnim() {
        return customAnim;
    }

    public void updateCustomAnim(float delta) {
        if (customAnimTimer > 0) {
            customAnimTimer -= delta;
            if (customAnimTimer <= 0) {
                customAnim = null;
                customAnimTimer = 0;
            }
        }
    }

    private int zombotanyJalapenoTimer;
    private int izombieSunProductionTicks;

    private int sunStealCooldown;
    private int sunStealTimer;
    private int stolenSunCount;

    private boolean hasLandedAfterExplosion;
    private double landingX;

    private int barrelHealth;
    private int maxBarrelHealth;
    private boolean barrelDestroyed;
    private boolean isBarrelRoller;

    private int iceBlockHealth;
    private int maxIceBlockHealth;
    private boolean iceBlockDestroyed;
    private boolean isTroglobite;

    private boolean isUnderwater;
    private boolean hasSurfaced;
    private int underwaterTimer;

    private boolean isInsane;
    private int insanityThreshold;
    private int damageTakenSinceLastReset;
    private int reflectCooldown;
    private boolean isReflecting;

    private int transformationCooldown;
    private int transformationTimer;
    private boolean isWizard;

    private boolean isKing;
    private boolean isIdle;
    private double spawnX;
    private int kingTimerTicks;

    private boolean isDodoRider;
    private int jumpCooldown;
    private boolean isJumping;
    private double jumpTargetX;
    private int jumpTimer;

    public Zombie(String name, int health, double speed, int damage, int waveCost) {
        this.name = name;
        this.health = health;
        this.maxHealth = health;
        this.speed = speed;
        this.damage = damage;
        this.waveCost = waveCost;
        this.x = 8.0;
        this.y = 0;
        this.armorHealth = 0;
        this.maxArmorHealth = 0;
        this.armorType = "none";
        this.isGlowing = false;
        this.isHypnotized = false;
        this.chilledDuration = 0.0;
        this.frozenDuration = 0.0;
        this.stolenSuns = 0;
        this.isAngry = false;
        this.isTorchLit = true;
        this.dynamiteTimer = 10.0;
        this.frozenIceHealth = 0;
        this.immuneToFreeze = false;

        this.isTransformedToSheep = false;
        this.hasThrownImp = false;
        this.isCharging = false;
        this.isSubmerged = false;
        this.isSpinning = false;
        this.iceHitsTaken = 0;
        this.wizardTimer = 0;
        this.kingTimer = 0;
        this.raStealTimer = 0;
        this.tombraiserTimer = 0;
        this.fishermanTimer = 0;
        this.octopusTimer = 0;
        this.turquoiseLaserTimer = 0;
        this.pianoPlayTimer = 0;

        this.zombotanyJalapenoTimer = 0;
        this.izombieSunProductionTicks = 0;

        this.sunStealCooldown = 30;
        this.sunStealTimer = 0;
        this.stolenSunCount = 0;

        this.hasLandedAfterExplosion = false;
        this.landingX = 1.0;

        this.barrelHealth = 0;
        this.maxBarrelHealth = 0;
        this.barrelDestroyed = false;
        this.isBarrelRoller = false;

        this.iceBlockHealth = 0;
        this.maxIceBlockHealth = 0;
        this.iceBlockDestroyed = false;
        this.isTroglobite = false;

        this.isUnderwater = true;
        this.hasSurfaced = false;
        this.underwaterTimer = 0;

        this.isInsane = false;
        this.insanityThreshold = 100;
        this.damageTakenSinceLastReset = 0;
        this.reflectCooldown = 0;
        this.isReflecting = false;

        this.transformationCooldown = 60;
        this.transformationTimer = 0;
        this.isWizard = false;

        this.isKing = false;
        this.isIdle = false;
        this.spawnX = 8.0;
        this.kingTimerTicks = 0;

        this.isDodoRider = false;
        this.jumpCooldown = 0;
        this.isJumping = false;
        this.jumpTargetX = 0;
        this.jumpTimer = 0;
    }

    public boolean isEating() {
        return isEating;
    }

    public void setEating(boolean eating) {
        this.isEating = eating;
    }

    public Zombie(String name, int health, double speed, int damage) {
        this(name, health, speed, damage, 100);
    }

    public int getWaveCost() {
        return waveCost;
    }

    public void takeDamage(int amount, boolean bypassArmor) {
        if (frozenIceHealth > 0) {
            frozenIceHealth -= amount;
            if (frozenIceHealth < 0) {
                frozenIceHealth = 0;
            }
            return;
        }

        if (isTroglobite && !iceBlockDestroyed && iceBlockHealth > 0) {
            iceBlockHealth -= amount;
            if (iceBlockHealth <= 0) {
                iceBlockHealth = 0;
                iceBlockDestroyed = true;
            }
            return;
        }

        if (isBarrelRoller && !barrelDestroyed && barrelHealth > 0) {
            barrelHealth -= amount;
            if (barrelHealth <= 0) {
                barrelHealth = 0;
                barrelDestroyed = true;
            }
            return;
        }

        if (bypassArmor || armorHealth <= 0) {
            health -= amount;
        } else {
            armorHealth -= amount;
            if (armorHealth < 0) {
                health += armorHealth;
                armorHealth = 0;
            }
        }
        if (health < 0) {
            health = 0;
        }

        if (isJuggler() && !isInsane) {
            damageTakenSinceLastReset += amount;
            if (damageTakenSinceLastReset >= insanityThreshold) {
                isInsane = true;
                isReflecting = true;
                reflectCooldown = 0;
            }
        }
    }

    public boolean isAlive() {
        if (isTroglobite && !iceBlockDestroyed && iceBlockHealth > 0) {
            return true;
        }
        if (isBarrelRoller && !barrelDestroyed && barrelHealth > 0) {
            return true;
        }
        return health > 0;
    }

    public void applyChilled(double duration) {
        if (!immuneToFreeze) {
            this.chilledDuration = Math.max(this.chilledDuration, duration);
        }
    }

    public void applyFrozen(double duration) {
        if (!immuneToFreeze) {
            this.frozenDuration = Math.max(this.frozenDuration, duration);
        }
    }

    public void removeEffect(ZombieEffect effect) {
        if (effect == ZombieEffect.FROZEN) {
            this.frozenDuration = 0.0;
            this.frozenIceHealth = 0;
        } else if (effect == ZombieEffect.CHILLED) {
            this.chilledDuration = 0.0;
        }
    }

    public void updateEffects(double deltaSeconds) {
        if (chilledDuration > 0) {
            chilledDuration -= deltaSeconds;
            if (chilledDuration < 0) chilledDuration = 0;
        }
        if (frozenDuration > 0) {
            frozenDuration -= deltaSeconds;
            if (frozenDuration < 0) frozenDuration = 0;
        }
        if (dynamiteTimer > 0) {
            dynamiteTimer -= deltaSeconds;
            if (dynamiteTimer < 0) dynamiteTimer = 0;
        }

        if (reflectCooldown > 0) {
            reflectCooldown--;
        }
        if (isReflecting && reflectCooldown <= 0) {
            isReflecting = false;
        }

        if (isUnderwater) {
            underwaterTimer++;
        }
    }

    public void updateEffects() {
        updateEffects(1.0);
    }

    public void updateCooldown() {
        if (sunStealTimer > 0) {
            sunStealTimer--;
        }

        if (transformationTimer > 0) {
            transformationTimer--;
        }

        if (kingTimerTicks > 0) {
            kingTimerTicks--;
        }

        if (jumpCooldown > 0) {
            jumpCooldown--;
        }
        if (isJumping) {
            jumpTimer--;
            if (jumpTimer <= 0) {
                isJumping = false;
                x = jumpTargetX;
            }
        }

        if (isInsane && !isReflecting && reflectCooldown <= 0) {
            damageTakenSinceLastReset = Math.max(0, damageTakenSinceLastReset - 1);
            if (damageTakenSinceLastReset < insanityThreshold / 2) {
                isInsane = false;
            }
        }
    }

    public boolean hasEffect(ZombieEffect effect) {
        if (effect == ZombieEffect.FROZEN && (frozenDuration > 0 || frozenIceHealth > 0)) {
            return true;
        }
        if (effect == ZombieEffect.CHILLED && chilledDuration > 0) {
            return true;
        }
        return false;
    }

    public void move() {
        if (isKing) {
            this.x = spawnX;
            return;
        }

        if (isDodoRider && isJumping) {
            this.x -= getEffectiveSpeed() * 2.0;
            return;
        }

        double currentSpeed = getEffectiveSpeed();
        if (isHypnotized) {
            this.x += currentSpeed;
            if (this.x > 8.0) {
                this.x = 8.0;
            }
            return;
        }
        if (name.equalsIgnoreCase("SquashZombie")) {
            this.x -= (currentSpeed * 2.5);
        } else if (isCharging) {
            this.x -= (currentSpeed * 3.0);
        } else {
            this.x -= currentSpeed;
        }
        if (this.x < 0) {
            this.x = 0;
        }
    }

    public void setEnraged(boolean enraged) {
        this.isAngry = enraged;
    }

    public boolean isBoss() {
        return this.name.equalsIgnoreCase("gargantuar") || this.name.equalsIgnoreCase("ZombieGargantuar");
    }

    public String getType() {
        return this.name;
    }

    public double getEffectiveSpeed() {
        if (frozenDuration > 0 || frozenIceHealth > 0) {
            return 0.0;
        }

        double currentSpeed = speed;
        if (currentSpeed <= 0) {
            currentSpeed = 1.0;
        }

        double speedPerTick = currentSpeed / 10.0;
        if (chilledDuration > 0) {
            speedPerTick /= 2.0;
        }
        if (isAngry) {
            speedPerTick *= 2.0;
        }
        return speedPerTick;
    }

    public boolean isDodoRider() {
        return isDodoRider || (name != null && (name.equalsIgnoreCase("dodo rider") || name.equalsIgnoreCase("ZombieIceAgeDodo")));
    }

    public boolean isJuggler() {
        return name != null && (name.equalsIgnoreCase("ZombieDarkJuggler") || name.equalsIgnoreCase("JesterZombie"));
    }

    public void startJump(double targetX, int duration) {
        this.isJumping = true;
        this.jumpTargetX = targetX;
        this.jumpTimer = duration;
    }

    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public double getSpeed() { return speed; }
    public int getDamage() { return damage; }
    public double getX() { return x; }
    public void setX(double x) { this.x = x; }
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    public int getArmorHealth() { return armorHealth; }
    public void setArmorHealth(int armorHealth) { this.armorHealth = armorHealth; this.maxArmorHealth = armorHealth; }
    public int getMaxArmorHealth() { return maxArmorHealth; }
    public void setMaxArmorHealth(int maxArmorHealth) { this.maxArmorHealth = maxArmorHealth; }
    public String getArmorType() { return armorType; }
    public void setArmorType(String armorType) { this.armorType = armorType; }
    public boolean isGlowing() { return isGlowing; }
    public void setGlowing(boolean glowing) { isGlowing = glowing; }
    public boolean isHypnotized() { return isHypnotized; }
    public void setHypnotized(boolean hypnotized) { isHypnotized = hypnotized; }
    public double getChilledDuration() { return chilledDuration; }
    public double getFrozenDuration() { return frozenDuration; }
    public int getStolenSuns() { return stolenSuns; }
    public void setStolenSuns(int stolenSuns) { this.stolenSuns = stolenSuns; }
    public boolean isAngry() { return isAngry; }
    public void setAngry(boolean angry) { this.isAngry = angry; }
    public boolean isTorchLit() { return isTorchLit; }
    public void setTorchLit(boolean torchLit) { this.isTorchLit = torchLit; }
    public double getDynamiteTimer() { return dynamiteTimer; }
    public void setDynamiteTimer(double dynamiteTimer) { this.dynamiteTimer = dynamiteTimer; }
    public int getFrozenIceHealth() { return frozenIceHealth; }
    public void setFrozenIceHealth(int frozenIceHealth) { this.frozenIceHealth = frozenIceHealth; }
    public boolean isImmuneToFreeze() { return immuneToFreeze; }
    public void setImmuneToFreeze(boolean immuneToFreeze) { this.immuneToFreeze = immuneToFreeze; }

    public boolean isTransformedToSheep() { return isTransformedToSheep; }
    public void setTransformedToSheep(boolean transformedToSheep) { isTransformedToSheep = transformedToSheep; }
    public boolean isHasThrownImp() { return hasThrownImp; }
    public void setHasThrownImp(boolean hasThrownImp) { this.hasThrownImp = hasThrownImp; }
    public boolean isCharging() { return isCharging; }
    public void setCharging(boolean charging) { this.isCharging = charging; }
    public boolean isSubmerged() { return isSubmerged; }
    public void setSubmerged(boolean submerged) { isSubmerged = submerged; }
    public boolean isSpinning() { return isSpinning; }
    public void setSpinning(boolean spinning) { this.isSpinning = spinning; }
    public int getIceHitsTaken() { return iceHitsTaken; }
    public void incrementIceHitsTaken() { this.iceHitsTaken++; }
    public void resetIceHitsTaken() { this.iceHitsTaken = 0; }

    public int getWizardTimer() { return wizardTimer; }
    public void incrementWizardTimer() { this.wizardTimer++; }
    public void resetWizardTimer() { this.wizardTimer = 0; }
    public int getKingTimer() { return kingTimer; }
    public void incrementKingTimer() { this.kingTimer++; }
    public void resetKingTimer() { this.kingTimer = 0; }
    public int getRaStealTimer() { return raStealTimer; }
    public void incrementRaStealTimer() { this.raStealTimer++; }
    public void resetRaStealTimer() { this.raStealTimer = 0; }
    public int getTombraiserTimer() { return tombraiserTimer; }
    public void incrementTombraiserTimer() { this.tombraiserTimer++; }
    public void resetTombraiserTimer() { this.tombraiserTimer = 0; }
    public int getFishermanTimer() { return fishermanTimer; }
    public void incrementFishermanTimer() { this.fishermanTimer++; }
    public void resetFishermanTimer() { this.fishermanTimer = 0; }
    public int getOctopusTimer() { return octopusTimer; }
    public void incrementOctopusTimer() { this.octopusTimer++; }
    public void resetOctopusTimer() { this.octopusTimer = 0; }
    public int getTurquoiseLaserTimer() { return turquoiseLaserTimer; }
    public void incrementTurquoiseLaserTimer() { this.turquoiseLaserTimer++; }
    public void resetTurquoiseLaserTimer() { this.turquoiseLaserTimer = 0; }
    public int getPianoPlayTimer() { return pianoPlayTimer; }
    public void incrementPianoPlayTimer() { this.pianoPlayTimer++; }
    public void resetPianoPlayTimer() { this.pianoPlayTimer = 0; }

    public int getZombotanyJalapenoTimer() { return zombotanyJalapenoTimer; }
    public void incrementJalapenoTimer() { this.zombotanyJalapenoTimer++; }
    public int getIzombieSunProductionTicks() { return izombieSunProductionTicks; }
    public void incrementIzombieSunTicks() { this.izombieSunProductionTicks++; }

    public int getSunStealCooldown() { return sunStealCooldown; }
    public void setSunStealCooldown(int sunStealCooldown) { this.sunStealCooldown = sunStealCooldown; }
    public int getSunStealTimer() { return sunStealTimer; }
    public void setSunStealTimer(int sunStealTimer) { this.sunStealTimer = sunStealTimer; }
    public void incrementSunStealTimer() { this.sunStealTimer++; }
    public void resetSunStealTimer() { this.sunStealTimer = 0; }
    public int getStolenSunCount() { return stolenSunCount; }
    public void setStolenSunCount(int stolenSunCount) { this.stolenSunCount = stolenSunCount; }
    public void addStolenSunCount(int amount) { this.stolenSunCount += amount; }

    public boolean isHasLandedAfterExplosion() { return hasLandedAfterExplosion; }
    public void setHasLandedAfterExplosion(boolean hasLanded) { this.hasLandedAfterExplosion = hasLanded; }
    public double getLandingX() { return landingX; }
    public void setLandingX(double landingX) { this.landingX = landingX; }

    public int getBarrelHealth() { return barrelHealth; }
    public void setBarrelHealth(int barrelHealth) { this.barrelHealth = barrelHealth; this.maxBarrelHealth = barrelHealth; }
    public int getMaxBarrelHealth() { return maxBarrelHealth; }
    public boolean isBarrelDestroyed() { return barrelDestroyed; }
    public void setBarrelDestroyed(boolean barrelDestroyed) { this.barrelDestroyed = barrelDestroyed; }
    public boolean isBarrelRoller() { return isBarrelRoller; }
    public void setBarrelRoller(boolean isBarrelRoller) { this.isBarrelRoller = isBarrelRoller; }

    public int getIceBlockHealth() { return iceBlockHealth; }
    public void setIceBlockHealth(int iceBlockHealth) { this.iceBlockHealth = iceBlockHealth; this.maxIceBlockHealth = iceBlockHealth; }
    public int getMaxIceBlockHealth() { return maxIceBlockHealth; }
    public boolean isIceBlockDestroyed() { return iceBlockDestroyed; }
    public void setIceBlockDestroyed(boolean iceBlockDestroyed) { this.iceBlockDestroyed = iceBlockDestroyed; }
    public boolean isTroglobite() { return isTroglobite; }
    public void setTroglobite(boolean isTroglobite) { this.isTroglobite = isTroglobite; }

    public boolean isUnderwater() { return isUnderwater; }
    public void setUnderwater(boolean isUnderwater) { this.isUnderwater = isUnderwater; }
    public boolean isHasSurfaced() { return hasSurfaced; }
    public void setHasSurfaced(boolean hasSurfaced) { this.hasSurfaced = hasSurfaced; }
    public int getUnderwaterTimer() { return underwaterTimer; }
    public void setUnderwaterTimer(int underwaterTimer) { this.underwaterTimer = underwaterTimer; }

    public boolean isInsane() { return isInsane; }
    public void setInsane(boolean isInsane) { this.isInsane = isInsane; }
    public int getInsanityThreshold() { return insanityThreshold; }
    public void setInsanityThreshold(int insanityThreshold) { this.insanityThreshold = insanityThreshold; }
    public int getDamageTakenSinceLastReset() { return damageTakenSinceLastReset; }
    public void setDamageTakenSinceLastReset(int damageTaken) { this.damageTakenSinceLastReset = damageTaken; }
    public int getReflectCooldown() { return reflectCooldown; }
    public void setReflectCooldown(int reflectCooldown) { this.reflectCooldown = reflectCooldown; }
    public boolean isReflecting() { return isReflecting; }
    public void setReflecting(boolean isReflecting) { this.isReflecting = isReflecting; }
    public void incrementReflectCooldown() { this.reflectCooldown++; }

    public int getTransformationCooldown() { return transformationCooldown; }
    public void setTransformationCooldown(int transformationCooldown) { this.transformationCooldown = transformationCooldown; }
    public int getTransformationTimer() { return transformationTimer; }
    public void setTransformationTimer(int transformationTimer) { this.transformationTimer = transformationTimer; }
    public void incrementTransformationTimer() { this.transformationTimer++; }
    public void resetTransformationTimer() { this.transformationTimer = 0; }
    public boolean isWizard() { return isWizard; }
    public void setWizard(boolean isWizard) { this.isWizard = isWizard; }

    public boolean isKing() { return isKing; }
    public void setKing(boolean isKing) { this.isKing = isKing; }
    public boolean isIdle() { return isIdle; }
    public void setIdle(boolean isIdle) { this.isIdle = isIdle; }
    public double getSpawnX() { return spawnX; }
    public void setSpawnX(double spawnX) { this.spawnX = spawnX; }
    public int getKingTimerTicks() { return kingTimerTicks; }
    public void setKingTimerTicks(int kingTimerTicks) { this.kingTimerTicks = kingTimerTicks; }
    public void incrementKingTimerTicks() { this.kingTimerTicks++; }
    public void resetKingTimerTicks() { this.kingTimerTicks = 0; }

    public boolean isDodoRiderField() { return isDodoRider; }
    public void setDodoRider(boolean isDodoRider) { this.isDodoRider = isDodoRider; }
    public int getJumpCooldown() { return jumpCooldown; }
    public void setJumpCooldown(int jumpCooldown) { this.jumpCooldown = jumpCooldown; }
    public boolean isJumping() { return isJumping; }
    public void setJumping(boolean isJumping) { this.isJumping = isJumping; }
    public double getJumpTargetX() { return jumpTargetX; }
    public void setJumpTargetX(double jumpTargetX) { this.jumpTargetX = jumpTargetX; }
    public int getJumpTimer() { return jumpTimer; }
    public void setJumpTimer(int jumpTimer) { this.jumpTimer = jumpTimer; }

    public void setHealth(int health) {
        this.health = Math.max(0, health);
        if (this.health > this.maxHealth) {
            this.health = this.maxHealth;
        }
    }
}
