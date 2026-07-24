package model.season;

import model.Game;
import model.board.Board;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import java.util.ArrayList;
import java.util.Random;

public class FrostbiteCaves extends Season {
    public FrostbiteCaves() {
        super("FrostbiteCaves", 4);
    }

    @Override
    public void setupEnvironment(Game game) {
        Board board = game.getBoard();
        board.setupSlideway(1, 3, 1);
        board.setupSlideway(3, 4, -1);

        Zombie fz1 = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("NormalZombie", 0, 4);
        if (fz1 != null) { fz1.setFrozenIceHealth(600); board.getTile(0, 4).setZombie(fz1); game.addZombie(fz1); }
        Zombie fz2 = model.entities.zombie.factory.ZombieFactory.createZombieAtColumn("NormalZombie", 4, 5);
        if (fz2 != null) { fz2.setFrozenIceHealth(600); board.getTile(4, 5).setZombie(fz2); game.addZombie(fz2); }
    }

    @Override
    public void handleWaveStart(Game game) {
        Board board = game.getBoard();
        Random rand = new Random();
        int windLane = rand.nextInt(board.getRows());
        game.getGameLogMessages().add("Ice wind hit row " + windLane + "!");

        for (int col = 0; col < board.getColumns(); col++) {
            Plant p = game.getPlantAt(col, windLane);
            if (p != null) {
                boolean isFire = false;
                if (p.getTags() != null) {
                    for (String tag : p.getTags()) {
                        if (tag.equalsIgnoreCase("fire") || p.getName().toLowerCase().contains("fire")) {
                            isFire = true;
                            break;
                        }
                    }
                }
                if (!isFire) {
                    p.setFreezeLevel(p.getFreezeLevel() + 1);
                }
            }
        }
    }

    @Override
    public void handleTick(Game game) {
        if (game.getTickCount() % 10 == 0) {
            for (Plant p : new ArrayList<>(game.getActivePlants())) {
                if (p.isFrozen()) {
                    boolean fireAdjacent = false;
                    for (int dr = -1; dr <= 1; dr++) {
                        for (int dc = -1; dc <= 1; dc++) {
                            if (dr == 0 && dc == 0) continue;
                            Plant adj = game.getPlantAt(p.getX() + dc, p.getY() + dr);
                            if (adj != null && adj.getTags() != null) {
                                for (String tag : adj.getTags()) {
                                    if (tag.equalsIgnoreCase("fire") || adj.getName().toLowerCase().contains("fire")) {
                                        fireAdjacent = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    if (fireAdjacent) {
                        p.damageIce(60);
                    }
                }
            }
        }
    }
}