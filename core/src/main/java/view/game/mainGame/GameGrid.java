package view.game.mainGame;

import com.badlogic.gdx.math.Vector2;
import controller.menu.PreGameController;
import model.season.Season;

public class GameGrid {
    public static final float GRID_START_X = 481.0f;
    public static final float GRID_START_Y = 116.0f;
    public static final float TILE_WIDTH = 152.77777f;
    public static final float TILE_HEIGHT = 141.0f;
    public static final float GRID_TOTAL_WIDTH = 1375.0f;
    public static final float GRID_TOTAL_HEIGHT = 705.0f;

    public static float ICE_OFFSET_X = 0.0f;
    public static float ICE_OFFSET_Y = 14.0f;

    public static final int COLS = 9;
    public static final int ROWS = 5;

    public static Season activeSeasonContext = null;

    public static boolean isIceMap() {
        if (activeSeasonContext != null && activeSeasonContext.getName() != null) {
            String sLower = activeSeasonContext.getName().toLowerCase();
            if (sLower.contains("cave") || sLower.contains("ice") || sLower.contains("frost")) {
                return true;
            }
        }
        String seasonName = PreGameController.activeChapterName != null ? PreGameController.activeChapterName : "";
        String sLower = seasonName.toLowerCase();
        return sLower.contains("cave") || sLower.contains("ice") || sLower.contains("frost");
    }

    public static float getGridStartX() {
        return GRID_START_X + (isIceMap() ? ICE_OFFSET_X : 0.0f);
    }

    public static float getGridStartY() {
        return GRID_START_Y + (isIceMap() ? ICE_OFFSET_Y : 0.0f);
    }

    public static Vector2 getTileCenterPosition(int row, int col) {
        float x = getGridStartX() + (col * TILE_WIDTH) + (TILE_WIDTH / 2f);
        float y = getGridStartY() + ((4 - row) * TILE_HEIGHT) + (TILE_HEIGHT / 2f);
        return new Vector2(x, y);
    }

    public static int getColumnAt(float worldX) {
        float startX = getGridStartX();
        if (worldX < startX || worldX >= startX + GRID_TOTAL_WIDTH) return -1;
        return (int) ((worldX - startX) / TILE_WIDTH);
    }

    public static int getRowAt(float worldY) {
        float startY = getGridStartY();
        if (worldY < startY || worldY >= startY + GRID_TOTAL_HEIGHT) return -1;
        int rowFromBottom = (int) ((worldY - startY) / TILE_HEIGHT);
        return 4 - rowFromBottom;
    }
}
