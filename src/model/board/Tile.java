package model.board;

import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.enums.TileType;

public class Tile {
    private final int row;
    private final int column;
    private TileType type;
    private Plant plant;
    private Zombie zombie;
    private int graveHealth;
    private int sunReward;
    private boolean hasPlantFoodReward;
    private boolean isSlideway;
    private int slideRowOffset;
    private boolean isLowBeach;
    private boolean isNecromancyTile;
    private Plant supportPlant;

    private String temporarySeedPacket;
    private int seedPacketTimer;
    private boolean isCrater;

    public Tile(int row, int column) {
        this.row = row;
        this.column = column;
        this.type = TileType.GRASS;
        this.temporarySeedPacket = null;
        this.seedPacketTimer = 0;
        this.isCrater = false;
    }

    public boolean isEmpty() {
        return plant == null && zombie == null && !isCrater;
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
}