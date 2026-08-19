package model.season;

import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class FrostbiteCaves extends Season {
    private final List<Integer> lastChilledRows = new ArrayList<>();

    public FrostbiteCaves() {
        super("FrostbiteCaves", 4);
    }

    @Override
    public void setupEnvironment(Game game) {
        if (game.getBoard() != null) {
            game.getBoard().setupSlideway(1, 4, 1);
            game.getBoard().setupSlideway(3, 4, -1);
        }
    }

    @Override
    public void handleWaveStart(Game game) {
        lastChilledRows.clear();
        Random rand = new Random();
        int affectedRowsCount = 1 + rand.nextInt(2);

        for (int i = 0; i < affectedRowsCount; i++) {
            int row = rand.nextInt(game.getBoard().getRows());
            if (!lastChilledRows.contains(row)) {
                lastChilledRows.add(row);
                applyChillingWindToRow(game, row);
            }
        }
    }

    private void applyChillingWindToRow(Game game, int row) {
        for (Plant plant : new ArrayList<>(game.getActivePlants())) {
            if (plant.getY() == row) {
                boolean isFirePlant = isFirePlant(plant);
                if (!isFirePlant) {
                    int currentLvl = plant.getFreezeLevel();
                    plant.setFreezeLevel(currentLvl + 1);
                    game.addGameLogMessage("Freezing wind hit " + plant.getName() + " in row " + row + "! Freeze level: " + plant.getFreezeLevel());
                }
            }
        }
    }

    private boolean isFirePlant(Plant plant) {
        if (plant.getName() != null) {
            String n = plant.getName().toLowerCase();
            if (n.contains("fire") || n.contains("jalapeno") || n.contains("pepper") || n.contains("torchwood") || n.contains("wasabi") || n.contains("hot potato")) {
                return true;
            }
        }
        if (plant.getTags() != null) {
            for (String tag : plant.getTags()) {
                if ("fire".equalsIgnoreCase(tag)) return true;
            }
        }
        return false;
    }

    @Override
    public void handleTick(Game game) {
        for (Plant plant : game.getActivePlants()) {
            if (plant.isFrozen() && plant.getIceHealth() > 0) {
                boolean hasAdjacentFirePlant = false;
                int px = plant.getX();
                int py = plant.getY();

                for (Plant other : game.getActivePlants()) {
                    if (other != plant && isFirePlant(other)) {
                        if (Math.abs(other.getX() - px) <= 1 && Math.abs(other.getY() - py) <= 1) {
                            hasAdjacentFirePlant = true;
                            break;
                        }
                    }
                }

                if (hasAdjacentFirePlant) {
                    plant.damageIce(6);
                    if (!plant.isFrozen()) {
                        game.addGameLogMessage("Ice around " + plant.getName() + " melted from nearby warmth!");
                    }
                }
            }
        }
    }

    public List<Integer> getLastChilledRows() {
        return new ArrayList<>(lastChilledRows);
    }
}
