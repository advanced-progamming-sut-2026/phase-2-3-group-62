package model.handler;

import model.Game;
import model.board.Sun;
import model.board.Tile;
import model.entities.plant.Plant;

import java.util.ArrayList;
import java.util.Random;

public class EnvironmentManager {

    public void handleSunDrop(Game game) {
        if (game.getCurrentSeason() != null && !game.getCurrentSeason().allowsNaturalSunDrop()) {
            return;
        }
        model.enums.SpecialLevelType specialType = game.getLevel().getSpecialLevelType();
        if (specialType == model.enums.SpecialLevelType.NIGHT_OPS || specialType == model.enums.SpecialLevelType.PLANT_WHAT_YOU_GET || game.getActiveMiniGame() != null) {
            return;
        }
        double t = game.getTickCount() / 10.0;
        double formulaInterval = Math.max(6 + 0.05 * t, 12);
        double scaleIncrease = game.getDifficultyLevel() / 3.0;
        int sunDropInterval = (int) (formulaInterval * 10 * scaleIncrease);

        if (game.getTickCount() - game.getLastSunDropTick() >= sunDropInterval) {
            game.setLastSunDropTick(game.getTickCount());
            Random r = new Random();
            int x = r.nextInt(game.getBoard().getColumns());
            int y = r.nextInt(game.getBoard().getRows());
            int chance = r.nextInt(100);
            String sunType = "Normal";
            if (chance < 5) {
                sunType = "Radioactive";
                game.addSun(new Sun(50, y, x));
            } else if (chance < 20) {
                sunType = "Special";
                game.addSun(new Sun(100, y, x));
            } else {
                game.addSun(new Sun(25, y, x));
            }
            game.addGameLogMessage("New " + sunType + " sun is dropping at position (" + x + ", " + y + ")");
            game.addGameLogMessage("Sun reached the ground at position (" + x + ", " + y + ")");
        }
    }

    public void handleSeasonEffects(Game game) {
        if (game.getCurrentSeason() != null) {
            game.getCurrentSeason().handleTick(game);
            if (game.getCurrentSeason().getName().equalsIgnoreCase("BigWaveBeach")) {
                for (Plant p : new ArrayList<>(game.getActivePlants())) {
                    Tile t = game.getBoard().getTile(p.getY(), p.getX());
                    if (t != null && t.getType() == model.enums.TileType.WATER) {
                        boolean isAquatic = p.isAquatic();
                        boolean hasLilyPad = (t.getSupportPlant() != null && t.getSupportPlant().getName().equalsIgnoreCase("Lily Pad"));
                        if (!isAquatic && !hasLilyPad) {
                            game.removePlant(p);
                            t.setPlant(null);
                            game.addGameLogMessage("Plant " + p.getName() + " drowned in the rising tide!");
                        }
                    }
                }
            }
        }
    }
}