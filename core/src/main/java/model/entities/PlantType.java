package model.entities;

public enum PlantType {
    SUNFLOWER("Sunflower", "768/FULL/PLANT/SUNFLOWER/SUNFLOWER.PAM", "Sun Producer", "Day", 50, 300, 0, 24.0f, 5.0f, 0.28f),
    TWIN_SUNFLOWER("Twin Sunflower", "768/FULL/PLANT/TWINSUNFLOWER/TWINSUNFLOWER.PAM", "Sun Producer", "Day", 125, 300, 0, 24.0f, 15.0f, 0.28f),
    SUN_SHROOM("Sun-shroom", "768/FULL/PLANT/SUNSHROOM/SUNSHROOM.PAM", "Sun Producer", "Shroom, wramp-up, night", 25, 300, 0, 24.0f, 5.0f, 0.28f),
    PRIMAL_SUNFLOWER("Primal Sunflower", "768/FULL/PLANT/PRIMAL_SUNFLOWER/PRIMAL_SUNFLOWER.PAM", "Sun Producer", "-", 75, 300, 0, 24.0f, 5.0f, 0.28f),
    GOLD_BLOOM("Gold Bloom", "768/FULL/PLANT/GOLDLEAF/GOLDLEAF.PAM", "Sun Producer", "-", 0, 0, 0, 0.0f, 75.0f, 0.28f),
    PEASHOOTER("Peashooter", "768/FULL/PLANT/PEAPOD/PEAPOD.PAM", "Shooter", "Pea", 100, 300, 20, 1.5f, 5.0f, 0.28f),
    REPEATER("Repeater", "768/FULL/PLANT/PEAPOD/PEAPOD.PAM", "Shooter", "Pea", 200, 300, 40, 1.5f, 5.0f, 0.28f),
    THREEPEATER("Threepeater", "768/FULL/PLANT/PEAPOD/PEAPOD.PAM", "Shooter", "Pea", 300, 300, 20, 1.5f, 5.0f, 0.28f),
    SNOW_PEA("Snow Pea", "768/FULL/PLANT/PEAPOD/PEAPOD.PAM", "Shooter", "Ice, Pea", 150, 300, 20, 1.5f, 5.0f, 0.28f),
    ROTOBAGA("Rotobaga", "768/FULL/PLANT/ROTORUTABAGA/ROTORUTABAGA.PAM", "Shooter", "-", 150, 300, 30, 1.5f, 5.0f, 0.28f),
    PEA_POD("Pea Pod", "768/FULL/PLANT/PEAPOD/PEAPOD.PAM", "Shooter", "Pea, stack", 125, 300, 20, 1.5f, 5.0f, 0.28f),
    SPLIT_PEA("Split Pea", "768/FULL/PLANT/SPLITPEA/SPLITPEA.PAM", "Shooter", "Pea", 125, 300, 20, 1.5f, 5.0f, 0.28f),
    CITRON("Citron", "768/FULL/PLANT/CITRON/CITRON.PAM", "Shooter", "charge", 350, 300, 800, 9.0f, 5.0f, 0.28f),
    CAULIPOWER("Caulipower", "768/FULL/PLANT/DEVOURBLOOM/DEVOURBLOOM.PAM", "Homing", "Magic, charge", 250, 300, 9999, 12.0f, 15.0f, 0.28f),
    ELECTRIC_BLUEBERRY("Electric Blueberry", "768/FULL/PLANT/ELECTRICPEEL/ELECTRICPEEL.PAM", "Homing", "charge", 150, 300, 5000, 12.0f, 15.0f, 0.28f),
    BOWLING_BULB("Bowling Bulb", "768/FULL/PLANT/BOWLINGBULB/BOWLINGBULB.PAM", "Shooter", "charge", 200, 300, 40, 2.0f, 5.0f, 0.28f),
    CACTUS("Cactus", "768/FULL/PLANT/CHARDGUARD/CHARDGUARD.PAM", "Strike-through", "-", 175, 300, 30, 1.5f, 5.0f, 0.28f),
    FIRE_PEASHOOTER("Fire Peashooter", "768/FULL/PLANT/BLAZELEAF/BLAZELEAF.PAM", "Shooter", "Fire, Pea", 175, 300, 40, 1.5f, 5.0f, 0.28f),
    STARFRUIT("Starfruit", "768/FULL/PLANT/SOURSHOT/SOURSHOT.PAM", "Shooter", "-", 150, 300, 20, 1.5f, 5.0f, 0.28f),
    GOO_PEASHOOTER("Goo Peashooter", "768/FULL/PLANT/SHADOWSHROOM/SHADOWSHROOM.PAM", "Shooter", "Poison", 125, 300, 20, 1.5f, 5.0f, 0.28f),
    MEGA_GATLING_PEA("Mega Gatling Pea", "768/FULL/PLANT/PRIMAL_PEASHOOTER/PRIMAL_PEASHOOTER.PAM", "Shooter", "Pea", 400, 300, 80, 1.5f, 5.0f, 0.28f),
    SEA_SHROOM("Sea-shroom", "768/FULL/PLANT/SEASHROOM/SEASHROOM.PAM", "Shooter", "Shroom, Water", 0, 300, 20, 1.5f, 15.0f, 0.28f),
    PUFF_SHROOM("Puff-shroom", "768/FULL/PLANT/MINISHROOM/MINISHROOM.PAM", "Shooter", "Shroom", 0, 300, 20, 1.5f, 5.0f, 0.28f),
    FUME_SHROOM("Fume-shroom", "768/FULL/PLANT/GUARDSHROOM/GUARDSHROOM.PAM", "Strike-through", "Shroom", 125, 300, 20, 1.5f, 5.0f, 0.28f),
    CABBAGE_PULT("Cabbage-pult", "768/FULL/PLANT/PEPPERPULT/PEPPERPULT.PAM", "Lobber", "-", 100, 300, 40, 2.9f, 5.0f, 0.28f),
    KERNEL_PULT("Kernel-pult", "768/FULL/PLANT/CORNFETTIPOPPER/CORNFETTIPOPPER.PAM", "Lobber", "-", 100, 300, 30, 2.9f, 5.0f, 0.28f),
    MELON_PULT("Melon-pult", "768/FULL/PLANT/WINTERMELON/WINTERMELON.PAM", "Lobber", "AoE", 325, 300, 80, 2.9f, 5.0f, 0.28f),
    WINTER_MELON("Winter Melon", "768/FULL/PLANT/WINTERMELON/WINTERMELON.PAM", "Lobber", "Ice, AoE", 500, 300, 80, 2.9f, 5.0f, 0.28f),
    PEPPER_PULT("Pepper-pult", "768/FULL/PLANT/PEPPERPULT/PEPPERPULT.PAM", "Lobber", "Fire, AoE", 200, 300, 50, 2.9f, 5.0f, 0.28f),
    POTATO_MINE("Potato Mine", "768/FULL/PLANT/PRIMAL_POTATOMINE/PRIMAL_POTATOMINE.PAM", "Explosive", "Trap, charge", 25, 300, 1800, 0.0f, 25.0f, 0.28f),
    PRIMAL_POTATO_MINE("Primal Potato Mine", "768/FULL/PLANT/PRIMAL_POTATOMINE/PRIMAL_POTATOMINE.PAM", "Explosive", "Trap, charge", 50, 300, 2400, 0.0f, 5.0f, 0.28f),
    CHERRY_BOMB("Cherry Bomb", "768/FULL/PLANT/CHERRYBOMB/CHERRYBOMB.PAM", "Explosive", "-", 150, 0, 1800, 0.0f, 35.0f, 0.28f),
    SQUASH("Squash", "768/FULL/PLANT/HAMMERUIT/HAMMERUIT.PAM", "Explosive", "Trap", 50, 300, 1800, 0.0f, 20.0f, 0.28f),
    GRAPESHOT("Grapeshot", "768/FULL/PLANT/SWEETHEARTSNARE/SWEETHEARTSNARE.PAM", "Explosive", "-", 150, 0, 1800, 0.0f, 35.0f, 0.28f),
    JALAPENO("Jalapeno", "768/FULL/PLANT/BLAZELEAF/BLAZELEAF.PAM", "Explosive", "Fire", 125, 0, 1800, 0.0f, 35.0f, 0.28f),
    DOOM_SHROOM("Doom-shroom", "768/FULL/PLANT/DOOMSHROOM/DOOMSHROOM.PAM", "Explosive", "Shroom", 125, 0, 1800, 0.0f, 15.0f, 0.28f),
    TANGLE_KELP("Tangle Kelp", "768/FULL/PLANT/TANGLEKELP/TANGLEKELP.PAM", "Explosive", "Trap, Water", 25, 300, 9999, 0.0f, 15.0f, 0.28f),
    ICEBERG_LETTUCE("Iceberg Lettuce", "768/FULL/PLANT/FROSTBONNET/FROSTBONNET.PAM", "Explosive", "Trap, Ice", 0, 300, 0, 0.0f, 20.0f, 0.28f),
    BONK_CHOY("Bonk Choy", "768/FULL/PLANT/CELERYSTALKER/CELERYSTALKER.PAM", "Melee", "-", 150, 300, 15, 0.25f, 5.0f, 0.28f),
    PHAT_BEET("Phat Beet", "768/FULL/PLANT/PHATBEETS/PHATBEETS.PAM", "Melee", "AoE", 150, 300, 15, 2.0f, 5.0f, 0.28f),
    CHOMPER("Chomper", "768/FULL/PLANT/SNAPDRAGON/SNAPDRAGON.PAM", "Melee", "-", 150, 300, 9999, 40.0f, 5.0f, 0.28f),
    WASABI_WHIP("Wasabi Whip", "768/FULL/PLANT/BLAZINGKNIGHT/BLAZINGKNIGHT.PAM", "Melee", "Fire", 150, 300, 40, 2.0f, 5.0f, 0.28f),
    KIWIBEAST("Kiwibeast", "768/FULL/PLANT/MANGOFIER/MANGOFIER.PAM", "Melee", "AoE, wramp-up", 175, 300, 30, 2.0f, 5.0f, 0.28f),
    WALL_NUT("Wall-nut", "768/FULL/PLANT/PRIMAL_WALLNUT/PRIMAL_WALLNUT.PAM", "Wall-nut", "-", 50, 4000, 0, 0.0f, 20.0f, 0.28f),
    TALL_NUT("Tall-nut", "768/FULL/PLANT/TALLNUT/TALLNUT.PAM", "Wall-nut", "-", 125, 8000, 0, 0.0f, 20.0f, 0.28f),
    ENDURIAN("Endurian", "768/FULL/PLANT/ENDURIAN/ENDURIAN.PAM", "Wall-nut", "-", 100, 3000, 20, 0.0f, 15.0f, 0.28f),
    GARLIC("Garlic", "768/FULL/PLANT/GARLIC/GARLIC.PAM", "Wall-nut", "moveZombies", 50, 300, 0, 0.0f, 20.0f, 0.28f),
    SWEET_POTATO("Sweet Potato", "768/FULL/PLANT/STUNION/STUNION.PAM", "Wall-nut", "moveZombies", 150, 3000, 0, 0.0f, 20.0f, 0.28f),
    EXPLODE_O_NUT("Explode-o-nut", "768/FULL/PLANT/PRIMAL_WALLNUT/PRIMAL_WALLNUT.PAM", "Wall-nut", "Explosive", 50, 4000, 1800, 0.0f, 20.0f, 0.28f),
    PUMPKIN("Pumpkin", "768/FULL/PLANT/INFINUT/INFINUT.PAM", "Wall-nut", "stack", 150, 4000, 0, 0.0f, 20.0f, 0.28f),
    SUN_BEAN("Sun Bean", "768/FULL/PLANT/SUNBEAN/SUNBEAN.PAM", "Wall-nut", "Sun", 50, 1000, 0, 0.0f, 20.0f, 0.28f),
    TORCHWOOD("Torchwood", "768/FULL/PLANT/PEPPERPULT/PEPPERPULT.PAM", "Modifier", "Fire", 175, 300, 0, 0.0f, 5.0f, 0.28f),
    MAGNET_SHROOM("Magnet-shroom", "768/FULL/PLANT/MAGNETSHROOM/MAGNETSHROOM.PAM", "Homing", "Shroom, Magic", 100, 300, 0, 10.0f, 15.0f, 0.28f),
    HYPNO_SHROOM("Hypno-shroom", "768/FULL/PLANT/PERFSHROOM/PERFSHROOM.PAM", "Modifier", "Shroom, Magic", 125, 300, 0, 0.0f, 20.0f, 0.28f),
    CAT_TAIL("Cat-tail", "768/FULL/PLANT/SEASHOOTER/SEASHOOTER.PAM", "Homing", "-", 175, 300, 15, 1.5f, 20.0f, 0.28f),
    IMITATER("Imitater", "768/FULL/PLANT/THYMEWARP/THYMEWARP.PAM", "Modifier", "-", 0, 0, 0, 0.0f, 0.0f, 0.28f),
    ICE_SHROOM("Ice-shroom", "768/FULL/PLANT/ICESHROOM/ICESHROOM.PAM", "Explosive", "Shroom, Ice", 75, 0, 0, 0.0f, 50.0f, 0.28f),
    LILY_PAD("Lily Pad", "768/FULL/PLANT/LILYPAD/LILYPAD.PAM", "Modifier", "Water, stack", 25, 300, 0, 0.0f, 5.0f, 0.28f),
    HOT_POTATO("Hot Potato", "768/FULL/PLANT/HOTPOTATO/HOTPOTATO.PAM", "Explosive", "Fire", 0, 0, 0, 0.0f, 5.0f, 0.28f),
    GRAVE_BUSTER("Grave Buster", "768/FULL/PLANT/ZNAKELILY/ZNAKELILY.PAM", "Explosive", "-", 0, 0, 9999, 0.0f, 10.0f, 0.28f),
    ENLIGHTEN_MINT("Enlighten-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Sun Producer", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    APPEASE_MINT("Appease-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Shooter", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    ARMA_MINT("Arma-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Lobber", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    BOMBARD_MINT("Bombard-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Explosive", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    ENFORCE_MINT("Enforce-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Melee", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    REINFORCE_MINT("Reinforce-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Wall-nut", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    ENCHANT_MINT("Enchant-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Modifier", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    PIERCE_MINT("Pierce-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Strike-through", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f),
    CATTAIL_MINT("catTail-mint", "768/FULL/PLANT/POWERPLANT_PROTO/POWERPLANT_PROTO.PAM", "Homing", "-", 0, 0, 0, 0.0f, 85.0f, 0.28f);

    private final String displayName;
    private final String pamPath;
    private final String category;
    private final String tags;
    private final int cost;
    private final int baseHp;
    private final int damage;
    private final float actionInterval;
    private final float recharge;
    private final float scale;

    PlantType(String displayName, String pamPath, String category, String tags, int cost, int baseHp, int damage, float actionInterval, float recharge, float scale) {
        this.displayName = displayName;
        this.pamPath = pamPath;
        this.category = category;
        this.tags = tags;
        this.cost = cost;
        this.baseHp = baseHp;
        this.damage = damage;
        this.actionInterval = actionInterval;
        this.recharge = recharge;
        this.scale = scale;
    }

    public static PlantType fromName(String name) {
        if (name == null) return PEASHOOTER;
        for (PlantType type : values()) {
            if (type.displayName.equalsIgnoreCase(name) || type.name().equalsIgnoreCase(name.replace(" ", "_").replace("-", "_"))) {
                return type;
            }
        }
        return PEASHOOTER;
    }

    public String getDisplayName() { return displayName; }
    public String getPamPath() { return pamPath; }
    public String getCategory() { return category; }
    public String getTags() { return tags; }
    public int getCost() { return cost; }
    public int getBaseHp() { return baseHp; }
    public int getDamage() { return damage; }
    public float getActionInterval() { return actionInterval; }
    public float getRecharge() { return recharge; }
    public float getScale() { return scale; }
}
