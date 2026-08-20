package controller.game;

import model.Game;
import model.board.Sun;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.plant.loader.PlantLoader;
import model.enums.TileType;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.user.UserSession;

import java.util.List;

public class GameActionController {

    public String plantPlant(Game game, String type, int x, int y) {
        if (game == null) return "Error: No active game session.";
        if (x < 0 || x >= game.getBoard().getColumns() || y < 0 || y >= game.getBoard().getRows()) {
            return "Error: Coordinates out of bounds!";
        }

        if (game.getActiveMiniGame() instanceof Beghoul) {
            return "Error: Cannot plant normally in Beghoul mode! You must swap existing plants.";
        }

        if (game.getActiveMiniGame() instanceof IZombie) {
            return "Error: Cannot plant in I, Zombie mode! You must deploy zombies using placeZombie.";
        }

        if (game.getActiveMiniGame() instanceof Vasebreaker) {
            return "Error: Cannot plant normally in Vasebreaker! Plants only come from smashing vases.";
        }

        if (game.getActiveMiniGame() instanceof WallnutBowling) {
            WallnutBowling wb = (WallnutBowling) game.getActiveMiniGame();
            if (x > wb.getRedLineX()) {
                return "Error: Cannot plant past the red line! Max column allowed: " + wb.getRedLineX();
            }

            boolean onBelt = false;
            String beltType = null;
            for (String pName : game.getConveyorBeltPlants()) {
                if (pName.equalsIgnoreCase(type)) {
                    onBelt = true;
                    beltType = pName;
                    break;
                }
            }

            if (!onBelt) {
                for (String pName : game.getConveyorBeltPlants()) {
                    String lowerName = pName.toLowerCase();
                    String lowerType = type.toLowerCase();
                    if (lowerName.contains(lowerType) || lowerType.contains(lowerName)) {
                        onBelt = true;
                        beltType = pName;
                        break;
                    }
                }
            }

            if (!onBelt) {
                return "Error: This walnut is not available on the conveyor belt! Available: " + String.join(", ", game.getConveyorBeltPlants());
            }

            Plant ball = PlantFactory.createPlant("WallNut");
            if (ball == null) ball = new Plant(88, beltType, "BOWLING", null, 0, 300, 50, 0, 0, null, 0, null, 0);
            game.getConveyorBeltPlants().remove(beltType);
            ball.setX(x);
            ball.setY(y);
            ball.setDx(1);
            ball.setDy(0);
            ball.setBowlingBall(true);
            game.addPlant(ball);
            game.getBoard().getTile(y, x).setPlant(ball);
            return "Successfully launched " + beltType + " bowling ball down row " + y;
        }

        Tile tile = game.getBoard().getTile(y, x);
        String cleanType = type.replace(" ", "").replace("-", "").toLowerCase();
        boolean isGraveBuster = cleanType.equalsIgnoreCase("gravebuster");
        boolean isPumpkin = cleanType.equalsIgnoreCase("pumpkin");
        boolean isPeaPod = cleanType.equalsIgnoreCase("peapod");

        if (isGraveBuster) {
            if (tile == null || tile.getType() != TileType.GRAVE) {
                return "Error: Grave Buster can only be planted on graves!";
            }
            if (tile.getPlant() != null) {
                return "Error: A Grave Buster is already devouring this grave!";
            }
        } else {
            if (tile != null && tile.getType() == TileType.GRAVE) {
                return "Error: Cannot plant on this tile! It is blocked by a grave. Use Grave Buster.";
            }
        }

        if (tile != null && tile.isSlideway()) {
            return "Error: Cannot plant on this tile! It is blocked by environment.";
        }

        Plant currentPlant = tile != null ? tile.getPlant() : null;
        Plant currentPumpkin = tile != null ? tile.getPumpkinPlant() : null;

        // منطق Stack شدن Pea Pod
        if (isPeaPod && currentPlant != null && currentPlant.getName().replace(" ", "").replace("-", "").equalsIgnoreCase("peapod")) {
            if (currentPlant.getPeaPodHeads() >= 5) {
                return "Error: Pea Pod already has maximum heads (5)!";
            }

            int cost = 125;
            Plant tempPlant = PlantFactory.createPlant("Pea Pod");
            if (tempPlant != null) cost = tempPlant.getCost();

            if (game.getSunCount() < cost) {
                return "Error: Not enough suns to stack Pea Pod! Required: " + cost;
            }

            game.spendSun(cost);
            currentPlant.incrementPeaPodHead();
            currentPlant.triggerGrowth(0.6f);
            return "Successfully added head to Pea Pod at (" + x + ", " + y + "). Total heads: " + currentPlant.getPeaPodHeads();
        }

        if (isPumpkin) {
            if (currentPumpkin != null) {
                return "Error: There is already a Pumpkin protecting this tile!";
            }
        } else if (!isGraveBuster) {
            if (currentPlant != null && !currentPlant.getName().equalsIgnoreCase("Lily Pad")) {
                return "Error: There is already a plant here!";
            }
        }

        if (game.getActiveMiniGame() == null && !game.getLevel().getSpecialLevelType().name().contains("CONVEYOR")) {
            boolean isSelected = false;
            if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
                for (String p : UserSession.getCurrentUser().getUnlockedPlants()) {
                    if (p.replace(" ", "").replace("-", "").equalsIgnoreCase(cleanType)) {
                        isSelected = true;
                        type = p;
                        break;
                    }
                }
            }
            if (!isSelected) {
                return "Error: You cannot plant a plant that you didn't select or haven't unlocked!";
            }
        }

        Plant newPlant = PlantFactory.createPlant(type);
        if (newPlant == null) {
            List<Plant> allPlants = PlantLoader.loadPlants();
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(type)) {
                    newPlant = PlantFactory.createPlant(p.getName());
                    break;
                }
            }

            if (newPlant == null) {
                String[] variations = {type, type.toLowerCase(), type.toUpperCase(), type.replace(" ", ""), type.replace(" ", "_")};
                for (String var : variations) {
                    newPlant = PlantFactory.createPlant(var);
                    if (newPlant != null) break;
                }
            }
            if (newPlant == null) {
                return "Error: Plant type not found! Try: PeaShooter, Sunflower, WallNut, Pea Pod, etc.";
            }
        }

        if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
            int userLvl = UserSession.getCurrentUser().getPlantLevels().getOrDefault(newPlant.getName(), 1);
            newPlant.setPlantStage(userLvl);
        }

        if (game.getSunCount() < newPlant.getCost()) {
            return "Error: Not enough suns! Required: " + newPlant.getCost();
        }

        if (tile != null && tile.getType() == TileType.WATER) {
            boolean isNewAquatic = newPlant.isAquatic();
            boolean hasLilyPad = (currentPlant != null && currentPlant.getName().equalsIgnoreCase("Lily Pad")) ||
                (tile.getSupportPlant() != null && tile.getSupportPlant().getName().equalsIgnoreCase("Lily Pad"));
            if (!isNewAquatic && !hasLilyPad) {
                return "Error: Cannot plant non-aquatic plant on water without a Lily Pad!";
            }
            if (currentPlant != null && currentPlant.getName().equalsIgnoreCase("Lily Pad") && !isNewAquatic) {
                tile.setSupportPlant(currentPlant);
            }
        }

        if (tile != null && (tile.isSlideway() || tile.isCrater())) {
            return "Error: Cannot plant on this tile! It is blocked by environment or a crater.";
        }

        game.spendSun(newPlant.getCost());
        newPlant.setX(x);
        newPlant.setY(y);
        game.addPlant(newPlant);

        if (isPumpkin) {
            tile.setPumpkinPlant(newPlant);
        } else {
            tile.setPlant(newPlant);
        }

        return "Successfully planted " + type + " at (" + x + ", " + y + ")";
    }

    public String pluckPlant(Game game, int x, int y) {
        if (game == null) return "Error: No active game session.";
        Tile tile = game.getBoard().getTile(y, x);
        if (tile == null) return "Error: Tile out of bounds.";

        Plant target = null;
        if (tile.getPlant() != null) {
            target = tile.getPlant();
            tile.setPlant(null);
            if (tile.getSupportPlant() != null) {
                tile.setPlant(tile.getSupportPlant());
                game.addPlant(tile.getSupportPlant());
                tile.setSupportPlant(null);
            }
        } else if (tile.getPumpkinPlant() != null) {
            target = tile.getPumpkinPlant();
            tile.setPumpkinPlant(null);
        } else if (tile.getSupportPlant() != null) {
            target = tile.getSupportPlant();
            tile.setSupportPlant(null);
        }

        if (target == null) return "Error: There is no plant at this location to pluck.";
        game.removePlant(target);
        return "Successfully plucked " + target.getName() + " at (" + x + ", " + y + ")";
    }

    public String feedPlant(Game game, int x, int y) {
        if (game == null) return "Error: No active game session.";
        Tile tile = game.getBoard().getTile(y, x);
        Plant target = tile != null ? (tile.getPlant() != null ? tile.getPlant() : tile.getPumpkinPlant()) : null;
        if (target == null) return "Error: There is no plant here to feed.";
        if (game.getPlantFoodCount() <= 0) return "Error: You do not have any plant food left.";
        if (game.usePlantFood()) {
            target.heal(target.getMaxHealth());
            String pfReport = game.applyPlantFood(target);
            return "Successfully fed plant at (" + x + ", " + y + "). HP fully restored! " + pfReport;
        }
        return "Error: Could not use plant food.";
    }

    public String collectSun(Game game, int x, int y) {
        if (game == null) return "Error: No active game session.";
        Sun targetSun = null;
        for (Sun s : game.getSuns()) {
            if (s.getColumn() == x && s.getRow() == y) {
                targetSun = s;
                break;
            }
        }
        Plant targetPlant = game.getPlantAt(x, y);
        if (targetSun != null) {
            game.addSun(targetSun.getValue());
            game.getSuns().remove(targetSun);
            return "Collected sun at (" + x + ", " + y + "). Total: " + game.getSunCount();
        } else if (targetPlant != null && targetPlant.isHasSunToCollect()) {
            int sunAmount = (int) targetPlant.getSunProduce();
            if (sunAmount <= 0) sunAmount = 25;
            game.addSun(sunAmount);
            targetPlant.setHasSunToCollect(false);
            return "Collected " + sunAmount + " sun from " + targetPlant.getName() + " at (" + x + ", " + y + "). Total: " + game.getSunCount();
        }
        return "Error: No sun available to collect at this location.";
    }

    public int advanceTime(Game game, int ticks, List<String> accumulatedTurnLogs) {
        if (game == null) return 0;
        int actualTicksExecuted = 0;
        int activeZombiesAtStart = game.getActiveZombies().size();
        for (int i = 0; i < ticks; i++) {
            if (game.isLost() || game.isWon() || !game.isRunning()) {
                break;
            }
            if (game.getSpawner() != null && game.getSpawner().ticksSinceLastSpawn == 0 && game.getActiveZombies().size() > activeZombiesAtStart) {
                game.getSpawner().ticksSinceLastSpawn = 1;
            }
            game.tick();

            accumulatedTurnLogs.addAll(game.getRawLogMessagesDirectly());

            actualTicksExecuted++;
        }
        return actualTicksExecuted;
    }
}
