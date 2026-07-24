package model.handler;

import model.Game;
import model.board.Sun;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieAbilityHandler {

    public void processSpecialZombieAbilities(Zombie zombie, List<Zombie> zombiesToAdd, Game game) {
        String name = zombie.getName();

        if (name.equalsIgnoreCase("ZombieGargantuar") && !zombie.isHasThrownImp()) {
            if (zombie.getHealth() <= zombie.getMaxHealth() / 2) {
                zombie.setHasThrownImp(true);
                Zombie imp = model.entities.zombie.factory.ZombieFactory.createZombie("ZombieImp", game.getDifficultyLevel());
                if (imp != null) {
                    imp.setY(zombie.getY());
                    imp.setX(2.0);
                    zombiesToAdd.add(imp);
                    game.getGameLogMessages().add("Gargantuar threw an Imp to column 2!");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieRa")) {
            zombie.incrementRaStealTimer();
            if (zombie.getRaStealTimer() >= 20) {
                zombie.resetRaStealTimer();
                if (!game.getSuns().isEmpty()) {
                    Sun targetSun = game.getSuns().remove(0);
                    zombie.setStolenSuns(zombie.getStolenSuns() + targetSun.getValue());
                    game.getGameLogMessages().add("Ra Zombie absorbed a sun from position (" + targetSun.getColumn() + ", " + targetSun.getRow() + ")!");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieCrystalSkull")) {
            zombie.incrementTurquoiseLaserTimer();
            if (zombie.getTurquoiseLaserTimer() >= 30) {
                zombie.resetTurquoiseLaserTimer();
                if (!game.getSuns().isEmpty()) {
                    Sun targetSun = game.getSuns().remove(0);
                    int sunValue = targetSun.getValue();
                    zombie.setStolenSuns(zombie.getStolenSuns() + sunValue);
                    game.getGameLogMessages().add("Turquoise Zombie stole " + sunValue + " suns from position (" + targetSun.getColumn() + ", " + targetSun.getRow() + ")!");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieTombRaiser")) {
            zombie.incrementTombraiserTimer();
            if (zombie.getTombraiserTimer() >= 100) {
                zombie.resetTombraiserTimer();
                Random r = new Random();
                int rx = r.nextInt(game.getBoard().getColumns());
                int ry = r.nextInt(game.getBoard().getRows());
                Tile tile = game.getBoard().getTile(ry, rx);
                if (tile != null && tile.isEmpty() && tile.getType() == model.enums.TileType.GRASS) {
                    game.getBoard().setupGrave(ry, rx, 700, 0, false);
                    game.getGameLogMessages().add("Tombraiser Zombie created a grave at (" + rx + ", " + ry + ")");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieIceAgeHunter")) {
            if (game.getTickCount() % 30 == 0) {
                Plant p = getFirstPlantInRow(zombie.getY(), game);
                if (p != null) {
                    p.setFreezeLevel(p.getFreezeLevel() + 1);
                    game.getGameLogMessages().add("Hunter Zombie threw a snowball at plant " + p.getName() + "!");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieBeachFisherman")) {
            zombie.incrementFishermanTimer();
            if (zombie.getFishermanTimer() >= 25) {
                zombie.resetFishermanTimer();
                Plant target = getFirstPlantInRow(zombie.getY(), game);
                if (target != null) {
                    if (target.getX() + 1 == (int) Math.round(zombie.getX())) {
                        game.removePlant(target);
                        game.getBoard().getTile(target.getY(), target.getX()).setPlant(null);
                        game.getGameLogMessages().add("Fisherman Zombie hooked and destroyed plant " + target.getName() + "!");
                    } else if (target.getX() + 1 < game.getBoard().getColumns()) {
                        game.getBoard().getTile(target.getY(), target.getX()).setPlant(null);
                        target.setX(target.getX() + 1);
                        game.getBoard().getTile(target.getY(), target.getX()).setPlant(target);
                        game.getGameLogMessages().add("Fisherman Zombie pulled plant " + target.getName() + " to column " + target.getX());
                    }
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieBeachOctopus")) {
            zombie.incrementOctopusTimer();
            if (zombie.getOctopusTimer() >= 40) {
                zombie.resetOctopusTimer();
                Plant p = getFirstPlantInRow(zombie.getY(), game);
                if (p != null && !p.isFrozen()) {
                    p.setFreezeLevel(3);
                    game.getGameLogMessages().add("Octopus Zombie threw an octopus on plant " + p.getName() + "!");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieDarkKing")) {
            zombie.incrementKingTimer();
            if (zombie.getKingTimer() >= 25) {
                zombie.resetKingTimer();
                for (Zombie neighbor : game.getActiveZombies()) {
                    if (Math.abs(neighbor.getY() - zombie.getY()) <= 1 && Math.abs((int) neighbor.getX() - (int) zombie.getX()) <= 2) {
                        if (neighbor.getName().equalsIgnoreCase("ZombieDefault") || neighbor.getName().equalsIgnoreCase("NormalZombie")) {
                            neighbor.setArmorHealth(1600);
                            neighbor.setArmorType("KNIGHT");
                            game.getGameLogMessages().add("King Zombie knighted a zombie at lane " + neighbor.getY() + "!");
                            break;
                        }
                    }
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieWizard")) {
            zombie.incrementWizardTimer();
            if (zombie.getWizardTimer() >= 60) {
                zombie.resetWizardTimer();
                List<Plant> candidates = new ArrayList<>();
                for (Plant p : game.getActivePlants()) {
                    if (p.getY() == zombie.getY() && p.getX() < zombie.getX()) {
                        candidates.add(p);
                    }
                }
                candidates.sort((a, b) -> Double.compare(b.getX(), a.getX()));

                Plant target = null;
                if (!candidates.isEmpty()) {
                    target = candidates.get(0);
                } else if (!game.getActivePlants().isEmpty()) {
                    target = game.getActivePlants().get(new Random().nextInt(game.getActivePlants().size()));
                }

                if (target != null && !target.isTransformedToSheep()) {
                    target.setTransformedToSheep(true);
                    game.getGameLogMessages().add("Wizard Zombie transformed " + target.getName() + " at (" + target.getX() + ", " + target.getY() + ") into a sheep!");
                }
            }
        }

        if (name.equalsIgnoreCase("ZombieProspector") && zombie.getDynamiteTimer() > 0) {
            if (zombie.getDynamiteTimer() <= 1.0) {
                zombie.setDynamiteTimer(0.0);
                zombie.setX(1.0);
                zombie.setAngry(true);
                game.getGameLogMessages().add("Prospector Zombie landed at column 1 after dynamite explosion!");
            }
        }

        if (name.equalsIgnoreCase("ZombiePiano")) {
            zombie.incrementPianoPlayTimer();
            if (zombie.getPianoPlayTimer() >= 30) {
                zombie.resetPianoPlayTimer();
                Random r = new Random();
                for (Zombie z : game.getActiveZombies()) {
                    if (!z.isBoss() && r.nextBoolean()) {
                        int newY = z.getY() + (r.nextBoolean() ? 1 : -1);
                        if (newY >= 0 && newY < game.getBoard().getRows()) {
                            z.setY(newY);
                        }
                    }
                }
                game.getGameLogMessages().add("Piano Zombie played music! Zombies swapped lanes!");
            }
        }
    }

    public void processZombieDeathDrops(Zombie zombie, Game game) {
        Random r = new Random();
        if (zombie.isGlowing()) {
            if (game.getPlantFoodCount() < 3) {
                game.addPlantFood();
                game.getGameLogMessages().add("The glowing zombie dropped a plant food; you have " + game.getPlantFoodCount() + " plant foods now.");
            }
        }
        if (r.nextInt(100) < 10) {
            int dropType = r.nextInt(3);
            if (dropType == 0) {
                game.addCoins(50);
                game.getGameLogMessages().add("A zombie dropped a coin; you have " + game.getCoins() + " coins now.");
            } else if (dropType == 1) {
                game.addDiamonds(1);
                game.getGameLogMessages().add("A zombie dropped a diamond; you have " + game.getDiamonds() + " diamonds now.");
            } else {
                if (game.getGreenhouse() != null) {
                    game.getGreenhouse().addPot(new model.greenhouse.Pot(0, 0));
                }
                int potCount = game.getGreenhouse() != null ? game.getGreenhouse().getUnlockedPotCount() : 1;
                game.getGameLogMessages().add("A zombie dropped a pot; you have " + potCount + " pots now.");
            }
        }
    }

    private Plant getFirstPlantInRow(int row, Game game) {
        Plant closest = null;
        for (Plant p : game.getActivePlants()) {
            if (p.getY() == row) {
                if (closest == null || p.getX() > closest.getX()) {
                    closest = p;
                }
            }
        }
        return closest;
    }
}