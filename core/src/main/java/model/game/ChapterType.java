package model.game;

public enum ChapterType {
    ANCIENT_EGYPT(
        "Ancient Egypt",
        "IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_COWBOY",
        "768/FULL/WORLDMAP/COWBOY/ANIM11/ANIM11.PAM",
        0.28f, 0f, 0f, 1
    ),
    FROSTBITE_CAVES(
        "Frostbite Caves",
        "IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_ICEAGE",
        "768/FULL/WORLDMAP/ICEAGE/ANIM26/ANIM26.PAM",
        0.28f, 0f, 0f, 2
    ),
    DARK_AGES(
        "Dark Ages",
        "IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_DARK",
        "768/FULL/WORLDMAP/ZOMBOSS_NODE_DINO/ZOMBOSS_NODE_DINO.PAM",
        0.28f, 0f, 0f, 3
    ),
    BIG_WAVE_BEACH(
        "Big Wave Beach",
        "IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_BEACH_WATER",
        "768/FULL/WORLDMAP/BEACH/ANIM17/ANIM17.PAM",
        0.28f, 0f, 0f, 4
    );

    private final String title;
    private final String bgRegionName;
    private final String nodePamPath;
    private final float nodePamScale;
    private final float nodeOffsetX;
    private final float nodeOffsetY;
    private final int seasonIndex;

    ChapterType(String title, String bgRegionName, String nodePamPath, float nodePamScale, float nodeOffsetX, float nodeOffsetY, int seasonIndex) {
        this.title = title;
        this.bgRegionName = bgRegionName;
        this.nodePamPath = nodePamPath;
        this.nodePamScale = nodePamScale;
        this.nodeOffsetX = nodeOffsetX;
        this.nodeOffsetY = nodeOffsetY;
        this.seasonIndex = seasonIndex;
    }

    public String getTitle() { return title; }
    public String getBgRegionName() { return bgRegionName; }
    public String getNodePamPath() { return nodePamPath; }
    public float getNodePamScale() { return nodePamScale; }
    public float getNodeOffsetX() { return nodeOffsetX; }
    public float getNodeOffsetY() { return nodeOffsetY; }
    public int getSeasonIndex() { return seasonIndex; }
}
