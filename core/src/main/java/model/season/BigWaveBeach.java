package model.season;

import model.Game;
import model.board.Board;
import model.board.Tile;
import model.entities.plant.Plant;
import model.enums.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BigWaveBeach extends Season {
    private int waterLevel = 2;

    public BigWaveBeach() {
        super("BigWaveBeach", 4);
    }

    @Override
    public void setupEnvironment(Game game) {
        Board board = game.getBoard();
        board.setupLowBeach(2, 7);
        board.setupLowBeach(3, 8);
        updateWaterTiles(board);
    }

    private void updateWaterTiles(Board board) {
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                if (c >= board.getColumns() - waterLevel) {
                    board.getTile(r, c).setType(TileType.WATER);
                } else {
                    board.getTile(r, c).setType(TileType.GRASS);
                }
            }
        }
    }

    @Override
    public void handleWaveStart(Game game) {
        Board board = game.getBoard();
        Random rand = new Random();

        waterLevel = rand.nextInt(4) + 2;
        game.getGameLogMessages().add("The tide changed! Water level is now " + waterLevel + " columns wide.");
        updateWaterTiles(board);

        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            Tile t = board.getTile(p.getY(), p.getX());
            if (t != null && t.getType() == TileType.WATER) {
                boolean isAquatic = p.isAquatic();
                boolean hasLilyPad = (t.getSupportPlant() != null && t.getSupportPlant().getName().equalsIgnoreCase("Lily Pad"));

                if (!isAquatic && !hasLilyPad) {
                    game.getActivePlants().remove(p);
                    t.setPlant(null);
                    game.getGameLogMessages().add("Plant " + p.getName() + " drowned in the rising tide!");
                }
            }
        }
    }

    @Override
    public void handleTick(Game game) {
        Board board = game.getBoard();
        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            Tile t = board.getTile(p.getY(), p.getX());
            if (t != null && t.getType() == TileType.WATER) {
                boolean isAquatic = p.isAquatic();
                boolean hasLilyPad = (t.getSupportPlant() != null && t.getSupportPlant().getName().equalsIgnoreCase("Lily Pad"));

                if (!isAquatic && !hasLilyPad) {
                    game.getActivePlants().remove(p);
                    t.setPlant(null);
                    game.getGameLogMessages().add("Plant " + p.getName() + " drowned in the ocean tide!");
                }
            }
        }
    }

    @Override
    public int modifySpawnColumn(int currentWave, int totalWaves, int defaultColumn, int zombiesSpawned, Board board, int lane) {
        List<Tile> lowBeaches = new ArrayList<>();
        for (int col = 0; col < board.getColumns(); col++) {
            Tile t = board.getTile(lane, col);
            if (t != null && t.isLowBeach() && t.getType() == TileType.WATER) {
                lowBeaches.add(t);
            }
        }
        if (!lowBeaches.isEmpty()) {
            return lowBeaches.get(new Random().nextInt(lowBeaches.size())).getColumn();
        }
        return defaultColumn;
    }
}
