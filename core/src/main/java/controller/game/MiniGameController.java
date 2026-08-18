package controller.game;

import model.Game;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import model.minigame.Vasebreaker;

import java.util.Random;

public class MiniGameController {

    public String swapPlants(Game game, int x1, int y1, int x2, int y2) {
        if (game == null || !(game.getActiveMiniGame() instanceof Beghoul)) {
            return "Error: Not currently in a Beghoul mini-game.";
        }
        if (x1 < 0 || x1 >= 9 || y1 < 0 || y1 >= 5 || x2 < 0 || x2 >= 9 || y2 < 0 || y2 >= 5) {
            return "Error: Coordinates out of bounds.";
        }
        if (Math.abs(x1 - x2) + Math.abs(y1 - y2) != 1) {
            return "Error: You can only swap adjacent tiles (horizontally or vertically).";
        }

        Beghoul bg = (Beghoul) game.getActiveMiniGame();
        if (bg.hasCrater(y1, x1) || bg.hasCrater(y2, x2)) {
            return "Error: Cannot swap items in a crater grid tile!";
        }

        Tile t1 = game.getBoard().getTile(y1, x1);
        Tile t2 = game.getBoard().getTile(y2, x2);
        Plant p1 = t1.getPlant();
        Plant p2 = t2.getPlant();

        if (p1 == null || p2 == null) {
            return "Error: Both tiles must contain a plant to swap.";
        }

        t1.setPlant(p2);
        t2.setPlant(p1);
        p1.setX(x2); p1.setY(y2);
        p2.setX(x1); p2.setY(y1);

        boolean initialMatch = bg.checkAndProcessMatches(game, false);
        if (!initialMatch) {
            t1.setPlant(p1);
            t2.setPlant(p2);
            p1.setX(x1); p1.setY(y1);
            p2.setX(x2); p2.setY(y2);
            return "Error: Invalid move! Swap does not create a combination of 3 or more.";
        }

        while (bg.checkAndProcessMatches(game, true)) {
            System.out.println("Beghoul: Cascade reaction triggered additional combinations!");
        }

        return "Successfully swapped plants. Combination formed!";
    }

    public String upgradePlants(Game game, String fromType, String toType) {
        if (game == null || !(game.getActiveMiniGame() instanceof Beghoul)) {
            return "Error: Not currently in a Beghoul mini-game.";
        }

        int cost = 500;
        if (fromType.equalsIgnoreCase("peashooter") && toType.equalsIgnoreCase("repeater")) cost = 500;
        else if (fromType.equalsIgnoreCase("repeater") && toType.equalsIgnoreCase("threepeater")) cost = 1500;
        else if (fromType.equalsIgnoreCase("wall-nut") && toType.equalsIgnoreCase("tall-nut")) cost = 500;
        else if (fromType.equalsIgnoreCase("puff-shroom") && toType.equalsIgnoreCase("scaredy-shroom")) cost = 250;
        else if (fromType.equalsIgnoreCase("cabbage-pult") && toType.equalsIgnoreCase("melon-pult")) cost = 1000;
        else if (fromType.equalsIgnoreCase("melon-pult") && toType.equalsIgnoreCase("winter-melon")) cost = 750;
        else return "Error: Invalid upgrade combination specified.";
        if (game.getSunCount() < cost) {
            return "Error: Not enough suns! Required: " + cost + ", Available: " + game.getSunCount();
        }

        game.spendSun(cost);
        int upgradedCount = 0;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 9; c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile.getPlant() != null && tile.getPlant().getName().equalsIgnoreCase(fromType)) {
                    game.removePlant(tile.getPlant());
                    Plant up = PlantFactory.createPlant(toType);
                    if (up == null) {
                        up = new Plant(new Random().nextInt(1000) + 200, toType, "BEGHOULD", null, 0, 400, 40, 1.5, 0, null, 0, null, 0);
                    }
                    up.setX(c);
                    up.setY(r);
                    game.addPlant(up);
                    tile.setPlant(up);
                    upgradedCount++;
                }
            }
        }

        return "Successfully upgraded " + upgradedCount + " plants from " + fromType + " to " + toType + ".";
    }

    public String placeZombie(Game game, String type, int x, int y) {
        if (game == null || !(game.getActiveMiniGame() instanceof IZombie)) {
            return "Error: Not currently in an I, Zombie mini-game.";
        }
        if (y < 0 || y >= game.getBoard().getRows()) {
            return "Error: Invalid lane number.";
        }

        IZombie iz = (IZombie) game.getActiveMiniGame();
        boolean placed = iz.placeZombie(type, x, y, game);
        if (placed) {
            return "Successfully deployed " + type + " at (" + x + ", " + y + ").";
        } else {
            return "Error: Could not place zombie. Check sun cost, available zombies, or coordinates (must be x > 4).";
        }
    }

    public String smashVase(Game game, int x, int y) {
        if (game == null || !(game.getActiveMiniGame() instanceof Vasebreaker)) {
            return "Error: Not currently in a Vasebreaker mini-game.";
        }
        Vasebreaker vb = (Vasebreaker) game.getActiveMiniGame();
        if (!vb.hasVase(y, x)) {
            return "Error: No vase exists at tile (" + x + ", " + y + ").";
        }
        if (vb.isVaseBroken(y, x)) {
            return "Error: Vase at (" + x + ", " + y + ") is already smashed.";
        }

        String content = vb.getVaseContent(y, x);
        vb.breakVase(y, x, game);
        Tile tile = game.getBoard().getTile(y, x);
        if (tile != null && tile.getTemporarySeedPacket() != null) {
            return "Smashed vase at (" + x + ", " + y + "): Dropped a Seed Packet! Pick it up quickly.";
        }

        if (content == null || content.equals(Vasebreaker.VASE_EMPTY)) {
            return "Smashed vase at (" + x + ", " + y + "): Found nothing! The vase was empty.";
        } else if (content.equals(Vasebreaker.VASE_ZOMBIE)) {
            return "Smashed vase at (" + x + ", " + y + "): A Zombie appeared!";
        } else if (content.equals(Vasebreaker.VASE_GARGANTUAR)) {
            return "Smashed vase at (" + x + ", " + y + "): A Gargantuar appeared!";
        } else if (content.equals(Vasebreaker.VASE_PLANT) || content.equals(Vasebreaker.VASE_SPECIAL_PLANT)) {
            return "Smashed vase at (" + x + ", " + y + "): Dropped a Seed Packet! Pick it up quickly.";
        } else if (content.equals(Vasebreaker.VASE_SUN)) {
            return "Smashed vase at (" + x + ", " + y + "): Found 50 suns!";
        } else {
            return "Smashed vase at (" + x + ", " + y + ")";
        }
    }

    public String pickupPacket(Game game, int x, int y) {
        if (game == null || !(game.getActiveMiniGame() instanceof Vasebreaker)) {
            return "Error: Not currently in a Vasebreaker mini-game.";
        }
        Tile tile = game.getBoard().getTile(y, x);
        String packet = tile.getTemporarySeedPacket();
        if (packet == null) {
            return "Error: No dropped seed packet available at this tile.";
        }

        Plant droppedPlant = PlantFactory.createPlant(packet);
        if (droppedPlant == null) droppedPlant = PlantFactory.createPlant("PeaShooter");

        tile.setTemporarySeedPacket(null);
        tile.setSeedPacketTimer(0);

        droppedPlant.setX(x);
        droppedPlant.setY(y);
        game.addPlant(droppedPlant);
        tile.setPlant(droppedPlant);
        return "Picked up and successfully planted " + packet + " at tile (" + x + ", " + y + ").";
    }
}