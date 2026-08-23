package model.handler;

import model.Game;
import model.board.LawnMower;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.entities.zombie.ZombieEffect;
import model.entities.zombie.boss.Zomboss;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import model.minigame.WallnutBowling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ZombieInteractionHandler {

    public void processZombiesTick(Game game) {
        List<Zombie> zombiesToRemove = new ArrayList<>();
        List<Zombie> zombiesToAdd = new ArrayList<>();
        ZombieAbilityHandler abilityHandler = new ZombieAbilityHandler();

        for (Zombie zombie : new ArrayList<>(game.getActiveZombies())) {
            if (zombie instanceof Zomboss) {
                continue;
            }

            zombie.updateEffects();
            zombie.updateCooldown();

            if (!zombie.isAlive()) {
                zombiesToRemove.add(zombie);
                if (zombie.getName().equalsIgnoreCase("ZombieCrystalSkull")) {
                    if (zombie.getStolenSunCount() > 0) {
                        game.addSun(zombie.getStolenSunCount());
                        game.addGameLogMessage("Turquoise Zombie died! Returned " + zombie.getStolenSunCount() + " stolen suns.");
                    }
                } else if (zombie.getStolenSuns() > 0) {
                    int returnSun = zombie.getStolenSuns();
                    game.addSun(returnSun);
                    game.addGameLogMessage("Ra Zombie died! Returned " + returnSun + " stolen suns.");
                }
                if (zombie.getName().equalsIgnoreCase("ZombieWizard")) {
                    for (Plant p : game.getActivePlants()) {
                        if (p.isTransformedToSheep()) {
                            p.setTransformedToSheep(false);
                        }
                    }
                }
                game.addGameLogMessage("Zombie of type " + zombie.getName() + " is dead at (" + (int) Math.round(zombie.getX()) + ", " + zombie.getY() + ")");
                abilityHandler.processZombieDeathDrops(zombie, game);
                game.getScoreGame().onZombieKilled(zombie, game);
                game.incrementZombiesKilled();
                continue;
            }

            double zombieXCoord = zombie.getX();
            int zombieYCoord = zombie.getY();

            for (Plant plant : new ArrayList<>(game.getActivePlants())) {
                if (plant.getY() == zombieYCoord && Math.abs(plant.getX() - zombieXCoord) <= 0.8) {
                    String pName = plant.getName();
                    if (pName.equalsIgnoreCase("Iceberg Lettuce")) {
                        zombie.applyFrozen(5.0);
                        game.removePlant(plant);
                        game.getBoard().getTile(plant.getY(), plant.getX()).setPlant(null);
                        game.addGameLogMessage("Iceberg Lettuce froze zombie " + zombie.getName() + "!");
                        break;
                    } else if ((pName.equalsIgnoreCase("Potato Mine") || pName.equalsIgnoreCase("Primal Potato Mine")) && plant.isArmed()) {
                        if (Math.abs(zombieXCoord - plant.getX()) <= 0.25) {
                            int rad = pName.contains("Primal") ? 1 : 0;
                            for (Zombie az : new ArrayList<>(game.getActiveZombies())) {
                                if (!(az instanceof Zomboss) && Math.abs(az.getY() - plant.getY()) <= rad && Math.abs(az.getX() - plant.getX()) <= (rad + 0.5)) {
                                    az.takeDamage(plant.getDamage() > 0 ? plant.getDamage() : 1800, true);
                                }
                            }
                            game.removePlant(plant);
                            game.getBoard().getTile(plant.getY(), plant.getX()).setPlant(null);
                            game.addGameLogMessage(pName + " detonated on zombie contact!");
                            break;
                        }
                    } else if (pName.equalsIgnoreCase("Tangle Kelp") || pName.equalsIgnoreCase("TangleKelp")) {
                        if (plant.getHitCount() == 0) {
                            plant.triggerAttack(1.2f);
                            zombie.takeDamage(99999, true);
                            plant.incrementHitCount();
                            plant.setLifespanTicks(12);
                            game.addGameLogMessage("Tangle Kelp dragged zombie " + zombie.getName() + " down underwater!");
                            break;
                        }
                    }
                }
            }

            abilityHandler.processSpecialZombieAbilities(zombie, zombiesToAdd, game);

            if (zombie.isHypnotized()) {
                for (Zombie enemy : game.getActiveZombies()) {
                    if (!enemy.isHypnotized() && enemy.getY() == zombie.getY() && Math.abs(enemy.getX() - zombie.getX()) <= 0.8) {
                        enemy.takeDamage(zombie.getDamage(), false);
                        game.addGameLogMessage("Hypnotized zombie attacked " + enemy.getName() + "!");
                        break;
                    }
                }
            } else if (!zombie.hasEffect(ZombieEffect.FROZEN)) {
                double zombieX = zombie.getX();
                int zombieY = zombie.getY();

                if (zombie.isDodoRider() && !zombie.isJumping() && zombie.getJumpCooldown() <= 0) {
                    int nextTileX = (int) Math.floor(zombieX) - 1;
                    if (nextTileX >= 0 && nextTileX < game.getBoard().getColumns()) {
                        Tile nextTile = game.getBoard().getTile(zombieY, nextTileX);
                        Plant plantAtNext = game.getPlantAt(nextTileX, zombieY);

                        boolean shouldJump = false;
                        if (nextTile != null && nextTile.isSlideway()) shouldJump = true;
                        if (plantAtNext != null && plantAtNext.getName().equalsIgnoreCase("WallNut") && !plantAtNext.getName().equalsIgnoreCase("TallNut")) shouldJump = true;
                        if (plantAtNext != null && (plantAtNext.getName().equalsIgnoreCase("Chomper") || plantAtNext.getName().equalsIgnoreCase("Squash") || plantAtNext.getName().equalsIgnoreCase("PotatoMine") || plantAtNext.getName().equalsIgnoreCase("PrimalPotatoMine") || plantAtNext.getName().equalsIgnoreCase("TangleKelp") || plantAtNext.getName().equalsIgnoreCase("Tangle Kelp"))) shouldJump = true;

                        if (shouldJump) {
                            int jumpDistance = 2;
                            double targetX = nextTileX - jumpDistance;
                            if (targetX < 0) targetX = 0;
                            zombie.startJump(targetX, 10);
                            game.addGameLogMessage("Dodo Rider jumped over obstacle at (" + nextTileX + ", " + zombieY + ")!");
                        }
                    }
                }

                int targetTileX = (int) Math.floor(zombieX);
                if (zombieX - targetTileX == 0.0) {
                    targetTileX = targetTileX - 1;
                }

                Tile targetTile = (targetTileX >= 0 && targetTileX < game.getBoard().getColumns()) ? game.getBoard().getTile(zombieY, targetTileX) : null;
                Plant targetPlant = null;
                if (targetTile != null) {
                    if (targetTile.getPumpkinPlant() != null && targetTile.getPumpkinPlant().isAlive()) {
                        targetPlant = targetTile.getPumpkinPlant();
                    } else if (targetTile.getPlant() != null && !targetTile.getPlant().isBowlingBall()) {
                        String plantName = targetTile.getPlant().getName();
                        if (!plantName.equalsIgnoreCase("Potato Mine") && !plantName.equalsIgnoreCase("Primal Potato Mine") && !plantName.equalsIgnoreCase("PotatoMine") && !plantName.equalsIgnoreCase("PrimalPotatoMine") && !plantName.equalsIgnoreCase("Tangle Kelp") && !plantName.equalsIgnoreCase("TangleKelp")) {
                            targetPlant = targetTile.getPlant();
                        }
                    }
                }

                if (targetPlant != null && zombieX - targetPlant.getX() <= 1.05) {
                    zombie.setEating(true);
                    if (zombie.isBarrelRoller() && !zombie.isBarrelDestroyed()) {
                        handlePlantEaten(game, targetPlant, targetTile);
                        game.incrementPlantsLost();
                        game.addGameLogMessage("Barrel Roller crushed " + targetPlant.getName() + " at (" + targetPlant.getX() + ", " + targetPlant.getY() + ")!");
                        continue;
                    }

                    if (zombie.isTroglobite() && !zombie.isIceBlockDestroyed()) {
                        handlePlantEaten(game, targetPlant, targetTile);
                        game.incrementPlantsLost();
                        game.addGameLogMessage("Troglobite crushed " + targetPlant.getName() + " with ice block at (" + targetPlant.getX() + ", " + targetPlant.getY() + ")!");
                        continue;
                    }

                    if (zombie.getName().equalsIgnoreCase("ZombieBeachSnorkel") && zombie.isUnderwater()) {
                        zombie.setUnderwater(false);
                        zombie.setHasSurfaced(true);
                        game.addGameLogMessage("Snorkel Zombie surfaced at (" + targetPlant.getX() + ", " + targetPlant.getY() + ")!");
                    }

                    if (zombie.getName().equalsIgnoreCase("ZombieExplorer") && zombie.isTorchLit()) {
                        handlePlantEaten(game, targetPlant, targetTile);
                        game.incrementPlantsLost();
                        game.addGameLogMessage("Explorer Zombie burned plant " + targetPlant.getName() + " at (" + targetPlant.getX() + ", " + targetPlant.getY() + ")!");
                    } else if (zombie.getName().equalsIgnoreCase("ZombieModernAllStar") && zombie.isCharging()) {
                        targetPlant.takeDamage(1500);
                        zombie.setCharging(false);
                        game.addGameLogMessage("All-Star Zombie tackled plant " + targetPlant.getName() + "!");
                        checkAndHandlePlantDeath(game, targetPlant, targetTile);
                    } else if (!zombie.getName().equalsIgnoreCase("ZombieWizard")) {
                        if (game.getTickCount() % 10 == 0) {
                            int damageToDeal = zombie.getName().toLowerCase().contains("gargantuar") ? 200 : zombie.getDamage();
                            targetPlant.takeDamage(damageToDeal);
                            game.getScoreGame().onDamageTaken(damageToDeal);

                            if (targetPlant.getName().equalsIgnoreCase("Sun Bean")) {
                                game.addSun(5);
                                game.addGameLogMessage("Sun Bean produced 5 suns from zombie bite!");
                            } else if (targetPlant.getName().equalsIgnoreCase("Hypno-shroom")) {
                                zombie.setHypnotized(true);
                                targetPlant.takeDamage(99999);
                                game.addGameLogMessage("Zombie ate Hypno-shroom and turned to fight for player!");
                            } else if (targetPlant.getName().equalsIgnoreCase("Endurian")) {
                                targetPlant.triggerAttack(0.8f);
                                zombie.takeDamage(20, false);
                                game.addGameLogMessage("Endurian reflected 20 damage back to zombie!");
                            } else if (targetPlant.getName().equalsIgnoreCase("Garlic")) {
                                int newY = zombie.getY() + (new Random().nextBoolean() ? 1 : -1);
                                if (newY >= 0 && newY < game.getBoard().getRows()) {
                                    zombie.setY(newY);
                                    game.addGameLogMessage("Garlic redirected zombie to lane " + newY + "!");
                                }
                            }
                            checkAndHandlePlantDeath(game, targetPlant, targetTile);
                        }
                    }
                } else {
                    zombie.setEating(false);
                    if (zombie.getName().equalsIgnoreCase("ZombieBeachSnorkel") && zombie.isUnderwater()) {
                        zombie.move();
                        continue;
                    }

                    int nextTileX = (int) Math.floor(zombieX);
                    if (nextTileX >= 0 && nextTileX < game.getBoard().getColumns() && zombieY >= 0 && zombieY < game.getBoard().getRows()) {
                        Tile currentTile = game.getBoard().getTile(zombieY, nextTileX);
                        if (game.getCurrentSeason() != null && "FrostbiteCaves".equalsIgnoreCase(game.getCurrentSeason().getName()) && currentTile != null && currentTile.isSlideway() && !zombie.isDodoRider()) {
                            int targetRow = zombie.getY() + currentTile.getSlideRowOffset();
                            if (targetRow >= 0 && targetRow < game.getBoard().getRows()) {
                                zombie.setY(targetRow);
                                zombie.move();
                            } else {
                                zombie.move();
                            }
                        } else {
                            zombie.move();
                        }
                    } else {
                        zombie.move();
                    }
                }
            }

            if (!zombie.isHypnotized() && zombie.getX() <= 0) {
                int row = zombie.getY();
                if (game.getActiveMiniGame() instanceof IZombie) {
                    IZombie iz = (IZombie) game.getActiveMiniGame();
                    iz.eatBrain(row);
                    zombiesToRemove.add(zombie);
                    if (iz.isVictoryConditionMet()) {
                        game.setWon(true);
                        game.stop();
                        game.addGameLogMessage("Dear humanz, zis is not done yet; we will come back to eat your brainz, humanz.");
                        return;
                    }
                    continue;
                }

                if (!(game.getActiveMiniGame() instanceof WallnutBowling)) {
                    LawnMower mower = game.getLawnMowers()[row];
                    if (!mower.isUsed()) {
                        List<Zombie> toKill = new ArrayList<>();
                        for (Zombie z : game.getActiveZombies()) {
                            if (!(z instanceof Zomboss) && z.getY() == row) toKill.add(z);
                        }
                        if (!toKill.isEmpty()) {
                            mower.activate();
                            game.addGameLogMessage("The lawn mower in the row " + row + " is triggered and killed these zombies:");
                            zombiesToRemove.addAll(toKill);
                            for (Zombie killed : toKill) {
                                game.getScoreGame().onZombieKilled(killed, game);
                                game.incrementZombiesKilled();
                                game.addGameLogMessage("Zombie of type " + killed.getName() + " is dead at (" + (int)Math.round(killed.getX()) + ", " + killed.getY() + ")");
                                abilityHandler.processZombieDeathDrops(killed, game);
                            }
                        }
                    } else {
                        if (!zombiesToRemove.contains(zombie)) {
                            game.setLost(true);
                            game.stop();
                            game.getScoreGame().onComboBreak();
                            game.addGameLogMessage("The zombie ate your brain; LOSER!!!");
                            return;
                        }
                    }
                }
            }
        }
        game.getActiveZombies().removeAll(zombiesToRemove);
        game.getActiveZombies().addAll(zombiesToAdd);

        for (Zombie z : game.getActiveZombies()) {
            if (z.getName().equalsIgnoreCase("ZombieBeachSnorkel") && z.isUnderwater()) {
                if (z.getHealth() < z.getMaxHealth()) z.setHealth(z.getHealth() + 1);
            }
        }
    }

    private void handlePlantEaten(Game game, Plant plant, Tile tile) {
        game.removePlant(plant);
        if (tile.getPumpkinPlant() == plant) {
            tile.setPumpkinPlant(null);
        } else {
            tile.setPlant(null);
        }

        if (game.getActiveMiniGame() instanceof Beghoul) {
            Beghoul bg = (Beghoul) game.getActiveMiniGame();
            bg.createCrater(plant.getY(), plant.getX());
            tile.setCrater(true);
            game.addGameLogMessage("Beghouled: Plant was eaten! Crater created at (" + plant.getX() + ", " + plant.getY() + ")!");
        }
    }

    private void checkAndHandlePlantDeath(Game game, Plant plant, Tile tile) {
        if (!plant.isAlive()) {
            handlePlantEaten(game, plant, tile);
        }
    }
}
