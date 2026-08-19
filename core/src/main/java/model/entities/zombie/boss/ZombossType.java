package model.entities.zombie.boss;

public enum ZombossType {
    SPHINX_INATOR("Zombot Sphinx-inator", "Ancient Egypt"),
    DARK_DRAGON("Zombot Dark Dragon", "Dark Ages"),
    TUSKMASTER("Zombot Tuskmaster 10,000 BC", "Frostbite Caves"),
    SHARKTRONIC("Zombot Sharktronic Sub", "Big Wave Beach");

    private final String displayName;
    private final String seasonName;

    ZombossType(String displayName, String seasonName) {
        this.displayName = displayName;
        this.seasonName = seasonName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSeasonName() {
        return seasonName;
    }
}
