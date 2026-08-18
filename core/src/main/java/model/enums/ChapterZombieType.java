package model.enums;

import java.util.ArrayList;
import java.util.List;

public enum ChapterZombieType {
    DEFAULT("NormalZombie", "ALL"),
    CONE_HEAD("ConeZombie", "ALL"),
    BUCKET_HEAD("BucketZombie", "ALL"),
    BRICK_HEAD("BrickZombie", "ALL"),
    KNIGHT("KnightZombie", "ALL"),
    GARGANTUAR("ZombieGargantuar", "ALL"),
    IMP("ZombieImp", "ALL"),
    ALL_STAR("ZombieModernAllStar", "ALL"),
    ARCADE("ZombieArcade", "ALL"),
    NEWSPAPER("ZombieNewspaper", "ALL"),
    RA("ZombieRa", "AncientEgypt"),
    EXPLORER("ZombieExplorer", "AncientEgypt"),
    TOMB_RAISER("ZombieTombRaiser", "AncientEgypt"),
    DODO("ZombieIceAgeDodo", "FrostbiteCaves"),
    HUNTER("ZombieIceAgeHunter", "FrostbiteCaves"),
    TROGLOBITE("ZombieIceAgeTroglobite", "FrostbiteCaves"),
    FISHERMAN("ZombieBeachFisherman", "BigWaveBeach"),
    OCTOPUS("ZombieBeachOctopus", "BigWaveBeach"),
    SNORKEL("ZombieBeachSnorkel", "BigWaveBeach"),
    JUGGLER("ZombieDarkJuggler", "DarkAges"),
    WIZARD("ZombieWizard", "DarkAges"),
    KING("ZombieDarkKing", "DarkAges"),
    IMP_DRAGON("ZombieDarkImpDragon", "DarkAges"),
    UMBRELLA("ZombieLostCityJane", "LostCity"),
    TURQUOISE("ZombieCrystalSkull", "LostCity"),
    PROSPECTOR("ZombieProspector", "LostCity"),
    PIANO("ZombiePiano", "WildWest");

    private final String jsonId;
    private final String chapterRestriction;

    ChapterZombieType(String jsonId, String chapterRestriction) {
        this.jsonId = jsonId;
        this.chapterRestriction = chapterRestriction;
    }

    public String getJsonId() {
        return jsonId;
    }

    public String getChapterRestriction() {
        return chapterRestriction;
    }

    public static List<String> getAvailableZombiesForChapter(String activeChapter) {
        List<String> validZombies = new ArrayList<>();
        String baseChapter = activeChapter != null ? activeChapter.replaceAll("\\d+", "") : "ALL";
        for (ChapterZombieType type : values()) {
            if (type.chapterRestriction.equals("ALL") || type.chapterRestriction.equalsIgnoreCase(baseChapter)) {
                validZombies.add(type.jsonId);
            }
        }
        return validZombies;
    }
}