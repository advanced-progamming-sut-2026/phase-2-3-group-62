package model.season;

import model.Game;
import model.board.Board;
import model.board.Tile;
import model.enums.TileType;
import model.entities.zombie.Zombie;
import model.entities.zombie.factory.ZombieFactory;
import java.util.Random;

public class DarkAges extends Season {
    public DarkAges() {
        super("DarkAges", 4);
    }

    @Override
    public void setupEnvironment(Game game) {
        Board board = game.getBoard();
        board.setupNecromancy(1, 3);
        board.setupNecromancy(3, 5);
        board.setupGrave(0, 4, 700, 50, false);
        board.setupGrave(2, 4, 700, 0, true);
        board.setupGrave(4, 5, 700, 0, false);
    }

    @Override
    public void handleWaveStart(Game game) {
        Board board = board = game.getBoard();
        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                Tile tile = board.getTile(r, c);
                if (tile != null && tile.isNecromancyTile() && tile.getType() == TileType.GRAVE) {
                    Zombie zombie = ZombieFactory.createZombieAtColumn("NormalZombie", r, c);
                    if (zombie != null) {
                        tile.setZombie(zombie);
                        game.addZombie(zombie);
                        game.getGameLogMessages().add("Necromancy! A zombie raised from the grave at (" + c + ", " + r + ")!");
                    }
                }
            }
        }

        Random rand = new Random();
        int graveCount = rand.nextInt(2) + 1;
        for (int i = 0; i < graveCount; i++) {
            int r = rand.nextInt(board.getRows());
            int c = rand.nextInt(board.getColumns());
            Tile tile = board.getTile(r, c);
            if (tile != null && tile.isEmpty() && tile.getType() == TileType.GRASS) {
                int sunReward = (rand.nextInt(100) < 30) ? 50 : 0;
                boolean pfReward = (sunReward == 0 && rand.nextInt(100) < 15);

                board.setupGrave(r, c, 700, sunReward, pfReward);

                if (sunReward > 0) {
                    game.getGameLogMessages().add("A new special grave containing 50 suns formed at (" + c + ", " + r + ")");
                } else if (pfReward) {
                    game.getGameLogMessages().add("A new special grave containing a Plant Food formed at (" + c + ", " + r + ")");
                } else {
                    game.getGameLogMessages().add("A new grave formed at (" + c + ", " + r + ")");
                }
            }
        }
    }

    @Override
    public boolean allowsNaturalSunDrop() {
        return false;
    }
}