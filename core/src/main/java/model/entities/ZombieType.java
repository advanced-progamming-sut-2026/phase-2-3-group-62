package model.entities;

import model.entities.zombie.Zombie;

public enum ZombieType {
    DEFAULT("ZombieDefault", "Basic Zombie", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM", 100, 190, 0.185, 100, "none", 0, false, 0.28f, 0f, 0f),
    CONE_HEAD("ZombieArmor1", "Conehead Zombie", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM", 100, 190, 0.185, 200, "Cone", 370, false, 0.28f, 0f, 0f),
    BUCKET_HEAD("ZombieArmor2", "Buckethead Zombie", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM", 100, 190, 0.185, 400, "Bucket", 1100, true, 0.28f, 0f, 0f),
    BRICK_HEAD("ZombieArmor4", "Brickhead Zombie", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_BASIC/ZOMBIE_EGYPT_BASIC.PAM", 100, 190, 0.185, 700, "Brick", 2200, false, 0.28f, 0f, 0f),
    KNIGHT("ZombieDarkArmor3", "Knight Zombie", "768/FULL/ZOMBIE/ZOMBIE_DARK_BASIC/ZOMBIE_DARK_BASIC.PAM", 100, 190, 0.185, 550, "Crown", 1600, true, 0.28f, 0f, 0f),
    GARGANTUAR("ZombieGargantuar", "Gargantuar", "768/FULL/ZOMBIE/DARK_GARGANTUAR/DARK_GARGANTUAR.PAM", 1500, 3600, 0.240, 1500, "none", 0, false, 0.24f, 0f, 0f),
    IMP("ZombieImp", "Imp", "768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_MONK/ZOMBIE_DARK_IMP_MONK.PAM", 100, 190, 0.220, 100, "none", 0, false, 0.28f, 0f, 0f),
    IMP_DRAGON("ZombieDarkImpDragon", "Dragon Imp", "768/FULL/ZOMBIE/ZOMBIE_DARK_IMP_DRAGON/ZOMBIE_DARK_IMP_DRAGON.PAM", 100, 190, 0.185, 150, "none", 0, false, 0.28f, 0f, 0f),
    UMBRELLA("ZombieLostCityJane", "Parasol Zombie", "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_JANE/ZOMBIE_LOSTCITY_JANE.PAM", 100, 350, 0.250, 200, "none", 0, false, 0.28f, 0f, 0f),
    TURQUOISE("ZombieCrystalSkull", "Turquoise Skull Zombie", "768/FULL/ZOMBIE/ZOMBIE_LOSTCITY_CRYSTALSKULL/ZOMBIE_LOSTCITY_CRYSTALSKULL.PAM", 100, 250, 0.185, 500, "none", 0, false, 0.28f, 0f, 0f),
    PIANO("ZombiePiano", "Pianist Zombie", "768/FULL/ZOMBIE/ZOMBIE_PIANO/ZOMBIE_PIANO.PAM", 4000, 840, 0.120, 450, "none", 0, false, 0.28f, 0f, 0f),
    RA("ZombieRa", "Ra Zombie", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_RA/ZOMBIE_EGYPT_RA.PAM", 100, 190, 0.200, 100, "none", 0, false, 0.28f, 0f, 0f),
    EXPLORER("ZombieExplorer", "Explorer Zombie", "768/INITIAL/ZOMBIE/ZOMBIE_EXPLORER/ZOMBIE_EXPLORER.PAM", 100, 250, 0.250, 250, "none", 0, false, 0.28f, 0f, 0f),
    TOMB_RAISER("ZombieTombRaiser", "Tomb Raiser Zombie", "768/INITIAL/ZOMBIE/ZOMBIE_EGYPT_TOMBRAISER/ZOMBIE_EGYPT_TOMBRAISER.PAM", 100, 380, 0.185, 300, "none", 0, false, 0.28f, 0f, 0f),
    DODO("ZombieIceAgeDodo", "Dodo Rider Zombie", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_DODORIDER/ZOMBIE_ICEAGE_DODORIDER.PAM", 100, 490, 0.300, 600, "none", 0, false, 0.28f, 0f, 0f),
    HUNTER("ZombieIceAgeHunter", "Hunter Zombie", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_HUNTER/ZOMBIE_ICEAGE_HUNTER.PAM", 100, 700, 0.120, 500, "none", 0, false, 0.28f, 0f, 0f),
    TROGLOBITE("ZombieIceAgeTroglobite", "Troglobite", "768/FULL/ZOMBIE/ZOMBIE_ICEAGE_TROGLOBITE/ZOMBIE_ICEAGE_TROGLOBITE.PAM", 100, 470, 0.185, 600, "none", 0, false, 0.28f, 0f, 0f),
    FISHERMAN("ZombieBeachFisherman", "Fisherman Zombie", "768/FULL/ZOMBIE/ZOMBIE_BEACH_FISHERMAN/ZOMBIE_BEACH_FISHERMAN.PAM", 100, 1000, 0.185, 700, "none", 0, false, 0.28f, 0f, 0f),
    OCTOPUS("ZombieBeachOctopus", "Octopus Zombie", "768/FULL/ZOMBIE/ZOMBIE_BEACH_OCTOPUS/ZOMBIE_BEACH_OCTOPUS.PAM", 100, 910, 0.120, 900, "none", 0, false, 0.28f, 0f, 0f),
    SNORKEL("ZombieBeachSnorkel", "Snorkel Zombie", "768/FULL/ZOMBIE/ZOMBIE_BEACH_SNORKELER/ZOMBIE_BEACH_SNORKELER.PAM", 100, 350, 0.185, 200, "none", 0, false, 0.28f, 0f, 0f),
    JUGGLER("ZombieDarkJuggler", "Jester Zombie", "768/FULL/ZOMBIE/ZOMBIE_DARK_JESTER/ZOMBIE_DARK_JESTER.PAM", 100, 420, 0.200, 450, "none", 0, false, 0.28f, 0f, 0f),
    WIZARD("ZombieWizard", "Wizard Zombie", "768/FULL/ZOMBIE/ZOMBIE_DARK_WIZARD/ZOMBIE_DARK_WIZARD.PAM", 100, 490, 0.120, 800, "none", 0, false, 0.28f, 0f, 0f),
    KING("ZombieDarkKing", "Zombie King", "768/FULL/ZOMBIE/ZOMBIE_DARK_KING/ZOMBIE_DARK_KING.PAM", 100, 1000, 0.000, 750, "none", 0, false, 0.28f, 0f, 0f),
    ALL_STAR("ZombieModernAllStar", "All-Star Zombie", "768/FULL/ZOMBIE/ZOMBIE_MODERN_ALLSTAR/ZOMBIE_MODERN_ALLSTAR.PAM", 100, 1100, 0.160, 1000, "none", 0, false, 0.28f, 0f, 0f),
    ARCADE("ZombieArcade", "Arcade Zombie", "768/FULL/ZOMBIE/ZOMBIE_80S_ARCADE/ZOMBIE_80S_ARCADE.PAM", 100, 490, 0.190, 600, "none", 0, false, 0.28f, 0f, 0f),
    PROSPECTOR("ZombieProspector", "Prospector Zombie", "768/FULL/ZOMBIE/ZOMBIE_PROSPECTOR/ZOMBIE_PROSPECTOR.PAM", 100, 190, 0.160, 200, "none", 0, false, 0.28f, 0f, 0f),
    NEWSPAPER("ZombieNewspaper", "Newspaper Zombie", "768/FULL/ZOMBIE/ZOMBIE_MODERN_NEWSPAPER/ZOMBIE_MODERN_NEWSPAPER.PAM", 200, 460, 0.220, 700, "Newspaper", 800, false, 0.28f, 0f, 0f);

    private final String id;
    private final String displayName;
    private final String pamPath;
    private final int eatDps;
    private final int hitpoints;
    private final double speed;
    private final int waveCost;
    private final String armorType;
    private final int armorHp;
    private final boolean isMetallic;
    private final float scale;
    private final float offsetX;
    private final float offsetY;

    ZombieType(String id, String displayName, String pamPath, int eatDps, int hitpoints, double speed, int waveCost, String armorType, int armorHp, boolean isMetallic, float scale, float offsetX, float offsetY) {
        this.id = id;
        this.displayName = displayName;
        this.pamPath = pamPath;
        this.eatDps = eatDps;
        this.hitpoints = hitpoints;
        this.speed = speed;
        this.waveCost = waveCost;
        this.armorType = armorType;
        this.armorHp = armorHp;
        this.isMetallic = isMetallic;
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public static ZombieType fromZombie(Zombie zombie) {
        if (zombie == null) return DEFAULT;
        return fromIdAndArmor(zombie.getName(), zombie.getArmorType());
    }

    public static ZombieType fromIdAndArmor(String id, String armorType) {
        String aClean = armorType != null ? armorType.toLowerCase() : "";
        if (aClean.contains("cone")) return CONE_HEAD;
        if (aClean.contains("bucket")) return BUCKET_HEAD;
        if (aClean.contains("brick")) return BRICK_HEAD;
        if (aClean.contains("crown") || aClean.contains("knight")) return KNIGHT;
        if (aClean.contains("newspaper")) return NEWSPAPER;

        return fromId(id);
    }

    public static ZombieType fromId(String id) {
        if (id == null) return DEFAULT;
        String clean = id.replaceAll("[\\s_-]", "").toLowerCase();

        if (clean.contains("armor1") || clean.contains("cone")) return CONE_HEAD;
        if (clean.contains("armor2") || clean.contains("bucket")) return BUCKET_HEAD;
        if (clean.contains("armor4") || clean.contains("brick")) return BRICK_HEAD;
        if (clean.contains("armor3") || clean.contains("knight") || clean.contains("crown")) return KNIGHT;

        for (ZombieType type : values()) {
            if (type.id.replaceAll("[\\s_-]", "").equalsIgnoreCase(clean) ||
                type.name().replaceAll("[\\s_-]", "").equalsIgnoreCase(clean) ||
                type.displayName.replaceAll("[\\s_-]", "").equalsIgnoreCase(clean)) {
                return type;
            }
        }

        return DEFAULT;
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public String getPamPath() { return pamPath; }
    public int getEatDps() { return eatDps; }
    public int getHitpoints() { return hitpoints; }
    public double getSpeed() { return speed; }
    public int getWaveCost() { return waveCost; }
    public String getArmorType() { return armorType; }
    public int getArmorHp() { return armorHp; }
    public boolean isMetallic() { return isMetallic; }
    public float getScale() { return scale; }
    public float getOffsetX() { return offsetX; }
    public float getOffsetY() { return offsetY; }
}
