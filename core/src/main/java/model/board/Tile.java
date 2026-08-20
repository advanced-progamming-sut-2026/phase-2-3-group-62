package model.board;

import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.enums.TileType;

public class Tile {
    private final int row;
    private final int column;
    private TileType type;
    private Plant plant;
    private Plant pumpkinPlant;
    private Zombie zombie;
    private int graveHealth;
    private int sunReward;
    private boolean hasPlantFoodReward;
    private boolean isSlideway;
    private int slideRowOffset;
    private boolean isLowBeach;
    private boolean isNecromancyTile;
    private Plant supportPlant;
    private boolean isPlantedWithLilyPad;

    private String temporarySeedPacket;
    private int seedPacketTimer;
    private boolean isCrater;
    private int fireTimerTicks;

    public Tile(int row, int column) {
        this.row = row;
        this.column = column;
        this.type = TileType.GRASS;
        this.pumpkinPlant = null;
        this.temporarySeedPacket = null;
        this.seedPacketTimer = 0;
        this.isCrater = false;
        this.graveHealth = 0;
        this.sunReward = 0;
        this.hasPlantFoodReward = false;
        this.isSlideway = false;
        this.slideRowOffset = 0;
        this.isLowBeach = false;
        this.isNecromancyTile = false;
        this.isPlantedWithLilyPad = false;
        this.fireTimerTicks = 0;
    }

    public void updateTile() {
        if (fireTimerTicks > 0) {
            fireTimerTicks--;
        }
        if (seedPacketTimer > 0) {
            seedPacketTimer--;
            if (seedPacketTimer <= 0) {
                temporarySeedPacket = null;
            }
        }
    }

    public boolean isOnFire() {
        return fireTimerTicks > 0;
    }

    public void ignite(int ticks) {
        this.fireTimerTicks = ticks;
        if (this.plant != null) {
            this.plant.takeDamage(99999);
            this.plant = null;
        }
        if (this.pumpkinPlant != null) {
            this.pumpkinPlant.takeDamage(99999);
            this.pumpkinPlant = null;
        }
    }

    public boolean isGrave() {
        return type == TileType.GRAVE || graveHealth > 0;
    }

    public boolean isEmpty() {
        return plant == null && pumpkinPlant == null && zombie == null && !isCrater && !isGrave() && !isOnFire();
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public TileType getType() {
        return type;
    }

    public void setType(TileType type) {
        this.type = type;
    }

    public Plant getPlant() {
        return plant;
    }

    public void setPlant(Plant plant) {
        this.plant = plant;
    }

    public Plant getPumpkinPlant() {
        return pumpkinPlant;
    }

    public void setPumpkinPlant(Plant pumpkinPlant) {
        this.pumpkinPlant = pumpkinPlant;
    }

    public Zombie getZombie() {
        return zombie;
    }

    public void setZombie(Zombie zombie) {
        this.zombie = zombie;
    }

    public int getGraveHealth() {
        return graveHealth;
    }

    public void setGraveHealth(int graveHealth) {
        this.graveHealth = graveHealth;
    }

    public int getSunReward() {
        return sunReward;
    }

    public void setSunReward(int sunReward) {
        this.sunReward = sunReward;
    }

    public boolean hasPlantFoodReward() {
        return hasPlantFoodReward;
    }

    public void setHasPlantFoodReward(boolean hasPlantFoodReward) {
        this.hasPlantFoodReward = hasPlantFoodReward;
    }

    public boolean isSlideway() {
        return isSlideway;
    }

    public void setSlideway(boolean slideway) {
        isSlideway = slideway;
    }

    public int getSlideRowOffset() {
        return slideRowOffset;
    }

    public void setSlideRowOffset(int slideRowOffset) {
        this.slideRowOffset = slideRowOffset;
    }

    public boolean isLowBeach() {
        return isLowBeach;
    }

    public void setLowBeach(boolean lowBeach) {
        isLowBeach = lowBeach;
    }

    public boolean isNecromancyTile() {
        return isNecromancyTile;
    }

    public void setNecromancyTile(boolean necromancyTile) {
        isNecromancyTile = necromancyTile;
    }

    public Plant getSupportPlant() {
        return supportPlant;
    }

    public void setSupportPlant(Plant supportPlant) {
        this.supportPlant = supportPlant;
    }

    public boolean isPlantedWithLilyPad() {
        return isPlantedWithLilyPad;
    }

    public void setPlantedWithLilyPad(boolean plantedWithLilyPad) {
        this.isPlantedWithLilyPad = plantedWithLilyPad;
    }

    public String getTemporarySeedPacket() {
        return temporarySeedPacket;
    }

    public void setTemporarySeedPacket(String temporarySeedPacket) {
        this.temporarySeedPacket = temporarySeedPacket;
    }

    public int getSeedPacketTimer() {
        return seedPacketTimer;
    }

    public void setSeedPacketTimer(int seedPacketTimer) {
        this.seedPacketTimer = seedPacketTimer;
    }

    public boolean isCrater() {
        return isCrater;
    }

    public void setCrater(boolean crater) {
        isCrater = crater;
    }

    public int getFireTimerTicks() {
        return fireTimerTicks;
    }

    public void setFireTimerTicks(int fireTimerTicks) {
        this.fireTimerTicks = fireTimerTicks;
    }
}
