package model.season;

import model.Game;
import model.board.Board;
import java.util.Random;

public class AncientEgypt extends Season {
    public AncientEgypt() {
        super("AncientEgypt", 4);
    }

    @Override
    public void setupEnvironment(Game game) {
        Board board = game.getBoard();
        board.setupGrave(1, 4, 700, 0, false);
        board.setupGrave(2, 5, 700, 0, false);
        board.setupGrave(3, 4, 700, 0, false);
    }

    @Override
    public int modifySpawnColumn(int currentWave, int totalWaves, int defaultColumn, int zombiesSpawned, Board board, int lane) {
        if (currentWave == totalWaves) {
            int forwardOffset = new Random().nextInt(4) + 1;
            return Math.max(0, defaultColumn - forwardOffset);
        }
        return defaultColumn;
    }
}