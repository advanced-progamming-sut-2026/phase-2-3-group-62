package view.game.renderers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import model.Game;
import model.board.DroppedItem;
import model.board.LawnMower;
import model.board.Sun;
import model.board.Tile;
import model.enums.TileType;
import model.season.Season;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import view.game.GameGrid;

import java.lang.reflect.Method;

public class LawnRenderer {
    private final TextureRegion bgRegion;
    private final TextureRegion slidewayIceRegion;
    private final TextureRegion beachWaterSquareRegion;
    private final TextureRegion necromancyTileRegion;
    private final TextureRegion plantFoodDropRegion;
    private final TextureRegion gemDropRegion;
    private final TextureRegion coinDropRegion;
    private final TextureRegion potDropRegion;
    private final ShapeRenderer shapeRenderer;
    private final PamPlayer pamPlayer;

    public LawnRenderer(TextureBank textureBank, TextureRegion bgRegion, ShapeRenderer shapeRenderer, PamPlayer pamPlayer) {
        this.bgRegion = bgRegion;
        this.shapeRenderer = shapeRenderer;
        this.slidewayIceRegion = textureBank.region("IMAGE_ZEN_GARDEN_ZEN_POT_WATER_ZEN_POT_WATER_160X97");
        this.beachWaterSquareRegion = textureBank.region("IMAGE_UI_CARDS_BACKGROUNDS_CARD_PLANT_BG_BEACH_WATER");
        this.necromancyTileRegion = textureBank.region("IMAGE_UI_UNIVERSE_UNIVERSE_PORTAL_UNIVERSE_PORTAL_763X763");
        this.plantFoodDropRegion = textureBank.region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");
        this.gemDropRegion = textureBank.region("IMAGE_UI_QUESTS_GEM_ICON");
        this.coinDropRegion = textureBank.region("IMAGE_UI_DANGERROOM_COIN_MIDSIZE");
        this.potDropRegion = textureBank.region("IMAGE_FIREBREAKER_VASE_GREEN_FIREWORKS_VASE_GREEN_FIREWORKS_115X150");
        this.pamPlayer = pamPlayer;
    }

    public void renderBackground(SpriteBatch batch, float width, float height) {
        if (bgRegion != null) {
            batch.draw(bgRegion, 0, 0, width, height);
        }
    }

    private String getSeasonMowerPamPath(Season season) {
        if (season == null || season.getName() == null) {
            return "768/INITIAL/MOWERS/MOWER_TUTORIAL/MOWER_TUTORIAL.PAM";
        }
        String name = season.getName().toLowerCase();
        if (name.contains("egypt")) {
            return "768/INITIAL/MOWERS/MOWER_EGYPT/MOWER_EGYPT.PAM";
        } else if (name.contains("beach")) {
            return "768/FULL/MOWERS/MOWER_BEACH/MOWER_BEACH.PAM";
        } else if (name.contains("dark")) {
            return "768/FULL/MOWERS/MOWER_DARK/MOWER_DARK.PAM";
        } else if (name.contains("caves") || name.contains("ice") || name.contains("frost")) {
            return "768/FULL/MOWERS/MOWER_ICEAGE/MOWER_ICEAGE.PAM";
        }
        return "768/INITIAL/MOWERS/MOWER_TUTORIAL/MOWER_TUTORIAL.PAM";
    }

    private String getMowerClipName(LawnMower mower) {
        if (mower == null) return "idle";
        try {
            Method isMovingMethod = mower.getClass().getMethod("isMoving");
            Object res = isMovingMethod.invoke(mower);
            if (res instanceof Boolean && (Boolean) res) {
                return "attack";
            }
        } catch (Exception ignored) {}

        try {
            Method isTriggeredMethod = mower.getClass().getMethod("isTriggered");
            Object res = isTriggeredMethod.invoke(mower);
            if (res instanceof Boolean && (Boolean) res) {
                return "attack";
            }
        } catch (Exception ignored) {}

        return "idle";
    }

    private float getMowerX(LawnMower mower, int row) {
        float defaultX = GameGrid.getGridStartX() - 60f;
        if (mower == null) return defaultX;
        try {
            Method getXMethod = mower.getClass().getMethod("getX");
            Object res = getXMethod.invoke(mower);
            if (res instanceof Number) {
                float mx = ((Number) res).floatValue();
                if (mx > 0) return mx;
            }
        } catch (Exception ignored) {}
        return defaultX;
    }

    private boolean isRadioactiveSun(Sun sun) {
        if (sun == null) return false;
        String className = sun.getClass().getSimpleName().toLowerCase();
        if (className.contains("radioactive") || className.contains("bomb") || className.contains("purple")) {
            return true;
        }
        try {
            Method m = sun.getClass().getMethod("isRadioactive");
            Object res = m.invoke(sun);
            if (res instanceof Boolean) return (Boolean) res;
        } catch (Exception ignored) {}
        return sun.getValue() == 50 && sun.getValue() != 25 && sun.getValue() != 100;
    }

    private boolean isSpecialSun(Sun sun) {
        if (sun == null) return false;
        return sun.getValue() == 100;
    }

    public void renderLawnElements(SpriteBatch batch, Game game, float stateTime) {
        if (game == null) return;

        Season season = game.getCurrentSeason();
        String mowerPamPath = getSeasonMowerPamPath(season);
        LawnMower[] lawnMowers = game.getLawnMowers();

        float startX = GameGrid.getGridStartX();
        float startY = GameGrid.getGridStartY();

        for (int r = 0; r < GameGrid.ROWS; r++) {
            for (int c = 0; c < GameGrid.COLS; c++) {
                Tile t = game.getBoard().getTile(r, c);
                if (t == null) continue;

                float tx = startX + (c * GameGrid.TILE_WIDTH);
                float ty = startY + ((4 - r) * GameGrid.TILE_HEIGHT);

                if (t.isNecromancyTile() && necromancyTileRegion != null) {
                    batch.draw(necromancyTileRegion, tx, ty, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                }

                if (t.getType() == TileType.WATER && beachWaterSquareRegion != null) {
                    batch.draw(beachWaterSquareRegion, tx, ty, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                }

                if (t.isSlideway() && slidewayIceRegion != null) {
                    float renderVisualX = tx + GameGrid.TILE_WIDTH;
                    batch.draw(slidewayIceRegion, renderVisualX, ty, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                }
            }
        }

        if (lawnMowers != null) {
            for (int r = 0; r < GameGrid.ROWS; r++) {
                if (r < lawnMowers.length && !lawnMowers[r].isUsed()) {
                    LawnMower mower = lawnMowers[r];
                    float lx = getMowerX(mower, r);
                    float ly = startY + ((4 - r) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);

                    String clipName = getMowerClipName(mower);
                    try {
                        pamPlayer.draw(batch, mowerPamPath, clipName, stateTime, lx, ly, true);
                    } catch (Exception e1) {
                        try {
                            pamPlayer.draw(batch, mowerPamPath, "transition", stateTime, lx, ly, true);
                        } catch (Exception e2) {
                            try {
                                pamPlayer.draw(batch, mowerPamPath, "idle", stateTime, lx, ly, true);
                            } catch (Exception e3) {
                                try {
                                    pamPlayer.draw(batch, mowerPamPath, "", stateTime, lx, ly, true);
                                } catch (Exception ignored) {}
                            }
                        }
                    }
                }
            }
        }

        boolean isEgypt = season != null && season.getName() != null && season.getName().toLowerCase().contains("egypt");

        for (int r = 0; r < GameGrid.ROWS; r++) {
            for (int c = 0; c < GameGrid.COLS; c++) {
                Tile t = game.getBoard().getTile(r, c);
                if (t != null && t.getType() == TileType.GRAVE) {
                    Vector2 center = GameGrid.getTileCenterPosition(r, c);
                    String pamPath;
                    if (isEgypt) {
                        pamPath = "768/INITIAL/GRAVESTONES/EGYPT_HIEROGLYPH/EGYPT_HIEROGLYPH.PAM";
                    } else if (t.hasPlantFoodReward()) {
                        pamPath = "768/FULL/GRAVESTONES/DARK_PLANTFOOD/DARK_PLANTFOOD.PAM";
                    } else if (t.getSunReward() > 0) {
                        pamPath = "768/FULL/GRAVESTONES/DARK_SUN/DARK_SUN.PAM";
                    } else {
                        pamPath = "768/FULL/GRAVESTONES/DARK_NOOP/DARK_NOOP.PAM";
                    }

                    String clipName = getGraveClip(t.getGraveHealth());
                    try {
                        pamPlayer.draw(batch, pamPath, clipName, stateTime, center.x, center.y, true);
                    } catch (Exception e) {
                        try {
                            pamPlayer.draw(batch, pamPath, "undamaged", stateTime, center.x, center.y, true);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }

        for (Sun sun : game.getSuns()) {
            Vector2 sPos = GameGrid.getTileCenterPosition(sun.getRow(), sun.getColumn());
            boolean radioactive = isRadioactiveSun(sun);
            boolean special = isSpecialSun(sun);

            Color originalColor = batch.getColor().cpy();
            if (radioactive) {
                batch.setColor(1.0f, 0.25f, 0.25f, 1.0f);
            } else if (special) {
                batch.setColor(0.35f, 0.65f, 1.0f, 1.0f);
            }

            try {
                pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "animation", stateTime, sPos.x, sPos.y, true);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "", stateTime, sPos.x, sPos.y, true);
                } catch (Exception ignored) {}
            }

            batch.setColor(originalColor);
        }

        if (game.getDroppedItems() != null) {
            for (DroppedItem item : game.getDroppedItems()) {
                Vector2 center = GameGrid.getTileCenterPosition(item.getRow(), item.getColumn());
                TextureRegion region = null;
                float w = 60f;
                float h = 60f;

                if (item.getType() == DroppedItem.ItemType.PLANT_FOOD) {
                    region = plantFoodDropRegion;
                    w = 56f; h = 56f;
                } else if (item.getType() == DroppedItem.ItemType.DIAMOND) {
                    region = gemDropRegion;
                    w = 48f; h = 48f;
                } else if (item.getType() == DroppedItem.ItemType.COIN) {
                    region = coinDropRegion;
                    w = 46f; h = 46f;
                } else if (item.getType() == DroppedItem.ItemType.POT) {
                    region = potDropRegion;
                    w = 52f; h = 68f;
                }

                if (region != null) {
                    batch.draw(region, center.x - w / 2f, center.y - h / 2f, w, h);
                }
            }
        }
    }

    private String getGraveClip(int health) {
        if (health >= 560) {
            return "undamaged";
        } else if (health >= 420) {
            return "damage1";
        } else if (health >= 280) {
            return "damage2";
        } else if (health >= 140) {
            return "damage3";
        } else {
            return "damage4";
        }
    }

    public void renderBeachAndSpecialOverlays(Game game, Matrix4 projectionMatrix) {
        if (game == null) return;

        Season season = game.getCurrentSeason();
        boolean isBeach = season != null && season.getName() != null && season.getName().toLowerCase().contains("beach");
        if (!isBeach) return;

        float startX = GameGrid.getGridStartX();
        float startY = GameGrid.getGridStartY();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.setProjectionMatrix(projectionMatrix);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.1f, 0.5f, 0.9f, 0.22f);
        for (int r = 0; r < GameGrid.ROWS; r++) {
            for (int c = 0; c < GameGrid.COLS; c++) {
                Tile t = game.getBoard().getTile(r, c);
                if (t != null && t.isLowBeach()) {
                    float tx = startX + (c * GameGrid.TILE_WIDTH);
                    float ty = startY + ((4 - r) * GameGrid.TILE_HEIGHT);
                    shapeRenderer.rect(tx, ty, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                }
            }
        }
        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(0.2f, 0.8f, 1f, 0.75f);
        Gdx.gl.glLineWidth(3);
        for (int r = 0; r < GameGrid.ROWS; r++) {
            for (int c = 0; c < GameGrid.COLS; c++) {
                Tile t = game.getBoard().getTile(r, c);
                if (t != null && t.isLowBeach()) {
                    float tx = startX + (c * GameGrid.TILE_WIDTH);
                    float ty = startY + ((4 - r) * GameGrid.TILE_HEIGHT);
                    shapeRenderer.rect(tx + 2, ty + 2, GameGrid.TILE_WIDTH - 4, GameGrid.TILE_HEIGHT - 4);
                }
            }
        }

        int maxWaterCol = 4;
        float tideLineX = startX + maxWaterCol * GameGrid.TILE_WIDTH;
        shapeRenderer.setColor(0.0f, 0.9f, 1.0f, 0.9f);
        Gdx.gl.glLineWidth(4);
        shapeRenderer.line(tideLineX, startY, tideLineX, startY + GameGrid.GRID_TOTAL_HEIGHT);
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);
    }

    public void renderDebugGrid(boolean showGrid, int hoveredCol, int hoveredRow) {
        if (!showGrid) return;

        float startX = GameGrid.getGridStartX();
        float startY = GameGrid.getGridStartY();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);

        for (int r = 0; r <= GameGrid.ROWS; r++) {
            float y = startY + (r * GameGrid.TILE_HEIGHT);
            shapeRenderer.line(startX, y, startX + GameGrid.GRID_TOTAL_WIDTH, y);
        }

        for (int c = 0; c <= GameGrid.COLS; c++) {
            float x = startX + (c * GameGrid.TILE_WIDTH);
            shapeRenderer.line(x, startY, x, startY + GameGrid.GRID_TOTAL_HEIGHT);
        }

        if (hoveredCol != -1 && hoveredRow != -1) {
            shapeRenderer.setColor(Color.YELLOW);
            float hx = startX + (hoveredCol * GameGrid.TILE_WIDTH);
            float hy = startY + ((4 - hoveredRow) * GameGrid.TILE_HEIGHT);
            shapeRenderer.rect(hx, hy, GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
        }

        shapeRenderer.end();
    }
}
