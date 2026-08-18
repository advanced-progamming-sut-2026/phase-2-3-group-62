package model.handler;

import model.Game;
import model.board.Bullet;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.entities.zombie.ZombieEffect;
import model.enums.TileType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlantAbilityHandler {

    public void updatePlantsAndAbilities(Game game) {
        List<Plant> plantsToRemove = new ArrayList<>();
        List<Plant> activePlants = new ArrayList<>(game.getActivePlants());

        for (Plant plant : activePlants) {
            if (plant.isFrozen() || plant.isBowlingBall() || plant.isTransformedToSheep()) {
                continue;
            }
            plant.update();
            String name = plant.getName();

            if (name.equalsIgnoreCase("Iceberg Lettuce")) {
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    if (!z.isHypnotized() && z.getY() == plant.getY() && Math.abs(z.getX() - plant.getX()) <= 0.6) {
                        z.applyFrozen(5.0);
                        plantsToRemove.add(plant);
                        game.getGameLogMessages().add("Iceberg Lettuce froze zombie " + z.getName() + " at column " + plant.getX() + "!");
                        break;
                    }
                }
            }

            if (name.equalsIgnoreCase("Sweet Potato")) {
                for (Zombie z : game.getActiveZombies()) {
                    if (Math.abs(z.getY() - plant.getY()) == 1 && Math.abs(z.getX() - plant.getX()) <= 3) {
                        z.setY(plant.getY());
                        game.getGameLogMessages().add("Sweet Potato attracted zombie from adjacent lane to lane " + plant.getY() + "!");
                    }
                }
            } else if (name.equalsIgnoreCase("Imitater")) {
                Plant copyPlant = model.entities.plant.factory.PlantFactory.createPlant("Peashooter");
                if (copyPlant != null) {
                    copyPlant.setX(plant.getX());
                    copyPlant.setY(plant.getY());
                    game.getBoard().getTile(plant.getY(), plant.getX()).setPlant(copyPlant);
                    game.addPlant(copyPlant);
                    plantsToRemove.add(plant);
                    game.getGameLogMessages().add("Imitater copied plant at (" + plant.getX() + ", " + plant.getY() + ")!");
                }
            }

            if ("SUN_PRODUCER".equalsIgnoreCase(plant.getCategory()) || name.equalsIgnoreCase("Enlighten-mint")) {
                if (name.equalsIgnoreCase("Primal Sunflower")) {
                    if (!plant.isHasSunToCollect()) {
                        plant.setHasSunToCollect(true);
                        plant.triggerAttack(0.8f);
                    }
                } else if (name.equalsIgnoreCase("Gold Bloom")) {
                    game.addSun((int) plant.getAbilityValue());
                    plantsToRemove.add(plant);
                    game.getGameLogMessages().add("Gold Bloom burst and produced " + (int) plant.getAbilityValue() + " suns!");
                } else if (name.equalsIgnoreCase("Enlighten-mint")) {
                    executeMintLogic(plant, plantsToRemove, game);
                } else {
                    int intervalTicks = (int) (plant.getActionInterval() * 10);
                    if (intervalTicks > 0 && plant.shouldShoot()) {
                        if (!plant.isHasSunToCollect()) {
                            plant.setHasSunToCollect(true);
                            plant.triggerAttack(0.8f);
                            game.getGameLogMessages().add("Plant " + name + " produced sun at (" + plant.getX() + ", " + plant.getY() + ")");
                        }
                    }
                }
            }

            boolean isAttacker = "SHOOTER".equalsIgnoreCase(plant.getCategory()) ||
                "STRIKE_THROUGH".equalsIgnoreCase(plant.getCategory()) ||
                "HOMING".equalsIgnoreCase(plant.getCategory()) ||
                "LOBBER".equalsIgnoreCase(plant.getCategory());

            if (isAttacker && plant.shouldShoot()) {
                boolean targetInRow = game.hasZombieInRow(plant.getY()) || game.getBoard().hasGraveInRow(plant.getY());
                if (name.equalsIgnoreCase("Threepeater")) {
                    int py = plant.getY();
                    targetInRow = game.hasZombieInRow(py) || game.getBoard().hasGraveInRow(py)
                        || (py > 0 && (game.hasZombieInRow(py - 1) || game.getBoard().hasGraveInRow(py - 1)))
                        || (py < game.getBoard().getRows() - 1 && (game.hasZombieInRow(py + 1) || game.getBoard().hasGraveInRow(py + 1)));
                }

                if (targetInRow || "HOMING".equalsIgnoreCase(plant.getCategory()) || name.equalsIgnoreCase("Starfruit") || name.equalsIgnoreCase("Laser Bean")) {
                    spawnBulletsForPlant(plant, game);
                }
            }

            if ("MELEE".equalsIgnoreCase(plant.getCategory()) && plant.shouldShoot()) {
                executeMeleeAttack(plant, game);
            }

            if ("EXPLOSIVE".equalsIgnoreCase(plant.getCategory())) {
                executeExplosiveLogic(plant, plantsToRemove, game);
            }

            if (("MODIFIER".equalsIgnoreCase(plant.getCategory()) || "HOMING".equalsIgnoreCase(plant.getCategory())) && plant.shouldShoot()) {
                executeUtilityLogic(plant, plantsToRemove, game);
            }

            if (name.toLowerCase().contains("mint")) {
                executeMintLogic(plant, plantsToRemove, game);
            }
        }

        for (Plant p : plantsToRemove) {
            game.removePlant(p);
            Tile t = game.getBoard().getTile(p.getY(), p.getX());
            if (t != null && t.getPlant() == p) {
                t.setPlant(null);
            }
        }
    }

    public void spawnBulletsForPlant(Plant plant, Game game) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        int dmg = plant.getDamage() > 0 ? plant.getDamage() : 20;

        plant.triggerAttack(0.6f);

        Zombie targetZombie = game.getFirstZombieInRowAhead(py, px);
        double targetCol = targetZombie != null ? targetZombie.getX() : 8.0;

        if (name.equalsIgnoreCase("Threepeater")) {
            createBulletWithMeta(game, dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            if (py > 0) createBulletWithMeta(game, dmg, py - 1, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            if (py < game.getBoard().getRows() - 1) createBulletWithMeta(game, dmg, py + 1, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Split Pea")) {
            createBulletWithMeta(game, 20, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            createBulletWithMeta(game, 20, py, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
            createBulletWithMeta(game, 20, py, Math.max(0, px - 1), Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
        } else if (name.equalsIgnoreCase("Snow Pea")) {
            createBulletWithMeta(game, 20, py, px + 1, Bullet.BulletType.ICE, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Fire Peashooter")) {
            createBulletWithMeta(game, 40, py, px + 1, Bullet.BulletType.FIRE, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Cactus")) {
            Bullet cactusBullet = createBulletWithMeta(game, 30, py, px + 1, Bullet.BulletType.STRIKE_THROUGH, true, false, 0, name, targetCol);
            cactusBullet.setMaxPierceTargets(3);
        } else if (name.equalsIgnoreCase("Goo Peashooter")) {
            createBulletWithMeta(game, 20, py, px + 1, Bullet.BulletType.POISON, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Cabbage-pult")) {
            createBulletWithMeta(game, 40, py, px + 1, Bullet.BulletType.LOB, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Kernel-pult")) {
            createBulletWithMeta(game, 30, py, px + 1, Bullet.BulletType.LOB, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Melon-pult")) {
            createBulletWithMeta(game, 80, py, px + 1, Bullet.BulletType.LOB, false, true, 1, name, targetCol);
        } else if (name.equalsIgnoreCase("Winter Melon")) {
            createBulletWithMeta(game, 80, py, px + 1, Bullet.BulletType.ICE, false, true, 1, name, targetCol);
        } else if (name.equalsIgnoreCase("Pepper-pult")) {
            createBulletWithMeta(game, 50, py, px + 1, Bullet.BulletType.FIRE, false, true, 1, name, targetCol);
        } else if (name.equalsIgnoreCase("Bowling Bulb")) {
            int bulbDmg = 40;
            if (plant.getHitCount() % 3 == 1) bulbDmg = 120;
            else if (plant.getHitCount() % 3 == 2) bulbDmg = 180;
            plant.incrementHitCount();
            createBulletWithMeta(game, bulbDmg, py, px + 1, Bullet.BulletType.NORMAL, true, true, 1, name, targetCol);
        } else if (name.equalsIgnoreCase("Peashooter") || name.equalsIgnoreCase("Pea Pod")) {
            int heads = plant.getPeaPodHeads();
            for (int i = 0; i < heads; i++) {
                createBulletWithMeta(game, dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            }
        } else if (name.equalsIgnoreCase("Repeater")) {
            createBulletWithMeta(game, dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            createBulletWithMeta(game, dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
        } else {
            createBulletWithMeta(game, dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
        }
    }

    private Bullet createBulletWithMeta(Game game, int dmg, int row, double col, Bullet.BulletType type, boolean pierce, boolean explosive, int splashRadius, String plantName, double targetCol) {
        Bullet b = new Bullet(dmg, row, col, type, pierce, explosive, splashRadius);
        b.setPlantName(plantName);
        b.setTargetColumn(targetCol);
        game.addBullet(b);
        return b;
    }

    public String applyPlantFood(Plant plant, Game game) {
        if (plant == null) return "";

        plant.heal(plant.getMaxHealth());
        plant.triggerPlantFood(2.0f);
        String name = plant.getName();

        if (name.equalsIgnoreCase("Sunflower")) {
            game.addSun(150);
            game.getGameLogMessages().add("Sunflower used Plant Food and instantly generated 150 suns!");
            return "Plant Food Effect: Produced 150 suns!";
        } else if (name.equalsIgnoreCase("Twin Sunflower")) {
            game.addSun(250);
            game.getGameLogMessages().add("Twin Sunflower used Plant Food and instantly generated 250 suns!");
            return "Plant Food Effect: Produced 250 suns!";
        } else if (name.equalsIgnoreCase("Sun-shroom")) {
            plant.setPlantStage(3);
            game.addSun(225);
            game.getGameLogMessages().add("Sun-shroom instantly grew to max stage and produced 225 suns!");
            return "Plant Food Effect: Grew to max size and produced 225 suns!";
        } else if (name.equalsIgnoreCase("Primal Sunflower")) {
            game.addSun(225);
            game.getGameLogMessages().add("Primal Sunflower used Plant Food and produced 225 suns!");
            return "Plant Food Effect: Produced 225 suns!";
        } else if (name.equalsIgnoreCase("Peashooter")) {
            for (int i = 0; i < 60; i++) {
                game.addBullet(new Bullet(20, plant.getY(), plant.getX() + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            game.getGameLogMessages().add("Peashooter unleashed a massive pea barrage!");
            return "Plant Food Effect: Unleashed Gatling barrage!";
        } else if (name.equalsIgnoreCase("Repeater")) {
            for (int i = 0; i < 60; i++) {
                game.addBullet(new Bullet(20, plant.getY(), plant.getX() + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            game.addBullet(new Bullet(400, plant.getY(), plant.getX() + 1, Bullet.BulletType.NORMAL, false, false, 0));
            game.getGameLogMessages().add("Repeater fired a giant pea with 20x damage!");
            return "Plant Food Effect: Unleashed heavy barrage & giant pea!";
        } else if (name.equalsIgnoreCase("Threepeater")) {
            for (int r = 0; r < game.getBoard().getRows(); r++) {
                for (int i = 0; i < 30; i++) {
                    game.addBullet(new Bullet(20, r, plant.getX() + 1, Bullet.BulletType.NORMAL, false, false, 0));
                }
            }
            game.getGameLogMessages().add("Threepeater spread pea barrage across all lanes!");
            return "Plant Food Effect: Unleashed fan barrage across all lanes!";
        } else if (name.equalsIgnoreCase("Snow Pea")) {
            for (Zombie z : game.getActiveZombies()) {
                if (z.getY() == plant.getY()) {
                    z.applyFrozen(5.0);
                }
            }
            for (int i = 0; i < 60; i++) {
                game.addBullet(new Bullet(20, plant.getY(), plant.getX() + 1, Bullet.BulletType.ICE, false, false, 0));
            }
            game.getGameLogMessages().add("Snow Pea froze its lane and shot ice barrage!");
            return "Plant Food Effect: Froze lane and shot ice barrage!";
        } else if (name.equalsIgnoreCase("Fire Peashooter")) {
            for (Zombie z : game.getActiveZombies()) {
                if (z.getY() == plant.getY()) {
                    z.takeDamage(1800, true);
                }
            }
            game.getGameLogMessages().add("Fire Peashooter incinerated the entire lane with fire!");
            return "Plant Food Effect: Incinerated the entire lane!";
        } else if (name.equalsIgnoreCase("Citron")) {
            for (Zombie z : game.getActiveZombies()) {
                if (z.getY() == plant.getY()) {
                    z.takeDamage(5000, true);
                }
            }
            game.getGameLogMessages().add("Citron fired a massive plasma ball wiping the lane!");
            return "Plant Food Effect: Fired massive plasma ball!";
        } else if (name.equalsIgnoreCase("Cabbage-pult") || name.equalsIgnoreCase("Kernel-pult") || name.equalsIgnoreCase("Melon-pult") || name.equalsIgnoreCase("Winter Melon")) {
            for (Zombie z : game.getActiveZombies()) {
                int dmg = name.toLowerCase().contains("melon") ? 200 : 100;
                z.takeDamage(dmg, false);
                if (name.equalsIgnoreCase("Kernel-pult")) z.applyFrozen(3.0);
            }
            game.getGameLogMessages().add(name + " launched heavy artillery at all zombies on field!");
            return "Plant Food Effect: Launched heavy artillery!";
        } else if (name.equalsIgnoreCase("Potato Mine") || name.equalsIgnoreCase("Primal Potato Mine")) {
            plant.setArmed(true);
            Random r = new Random();
            for (int k = 0; r.nextInt(2) == 0 && k < 2; k++) {
                int rx = r.nextInt(game.getBoard().getColumns());
                int ry = r.nextInt(game.getBoard().getRows());
                Plant clone = model.entities.plant.factory.PlantFactory.createPlant(name);
                if (clone != null && game.getBoard().getTile(ry, rx).isEmpty()) {
                    clone.setX(rx);
                    clone.setY(ry);
                    clone.setArmed(true);
                    game.addPlant(clone);
                    game.getBoard().getTile(ry, rx).setPlant(clone);
                }
            }
            game.getGameLogMessages().add(name + " armed instantly and spawned clone mines!");
            return "Plant Food Effect: Armed instantly and spawned clones!";
        } else if (name.equalsIgnoreCase("Iceberg Lettuce")) {
            for (Zombie z : game.getActiveZombies()) {
                z.applyFrozen(5.0);
            }
            game.getGameLogMessages().add("Iceberg Lettuce froze all zombies on screen!");
            return "Plant Food Effect: Froze all zombies on screen!";
        } else if (name.equalsIgnoreCase("Wall-nut")) {
            plant.applyPlantFoodArmor(4000);
            game.getGameLogMessages().add("Wall-nut gained 4000 armor!");
            return "Plant Food Effect: Gained 4000 armor!";
        } else if (name.equalsIgnoreCase("Tall-nut")) {
            plant.applyPlantFoodArmor(8000);
            game.getGameLogMessages().add("Tall-nut gained 8000 heavy armor!");
            return "Plant Food Effect: Gained 8000 heavy armor!";
        } else if (name.equalsIgnoreCase("Endurian") || name.equalsIgnoreCase("Explode-o-nut") || name.equalsIgnoreCase("Pumpkin") || name.equalsIgnoreCase("Sun Bean")) {
            plant.applyPlantFoodArmor(4000);
            game.getGameLogMessages().add(name + " gained metal Plant Food armor!");
            return "Plant Food Effect: Gained metal armor!";
        } else if (name.equalsIgnoreCase("Torchwood")) {
            plant.setBlueFlame(true);
            game.getGameLogMessages().add("Torchwood ignited blue flame (3x damage to passing peas)!");
            return "Plant Food Effect: Ignited blue flame!";
        } else if (name.equalsIgnoreCase("Hypno-shroom")) {
            for (Zombie z : game.getActiveZombies()) {
                if (z.getY() == plant.getY() && Math.abs(z.getX() - plant.getX()) <= 1.0) {
                    z.setHypnotized(true);
                    game.getGameLogMessages().add("Hypno-shroom hypnotized adjacent zombie into Gargantuar ally!");
                    break;
                }
            }
            return "Plant Food Effect: Hypnotized zombie!";
        } else if (name.equalsIgnoreCase("Bonk Choy") || name.equalsIgnoreCase("Wasabi Whip")) {
            for (Zombie z : game.getActiveZombies()) {
                if (Math.abs(z.getY() - plant.getY()) <= 1 && Math.abs(z.getX() - plant.getX()) <= 1.5) {
                    z.takeDamage(1500, false);
                }
            }
            game.getGameLogMessages().add(name + " unleashed 3x3 rapid flurry attacks!");
            return "Plant Food Effect: Unleashed rapid flurry attacks!";
        } else if (name.equalsIgnoreCase("Chomper")) {
            int swallowed = 0;
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                if (swallowed < 3 && z.getY() == plant.getY() && z.getX() >= plant.getX()) {
                    z.takeDamage(99999, true);
                    swallowed++;
                }
            }
            game.getGameLogMessages().add("Chomper instantly swallowed " + swallowed + " zombies!");
            return "Plant Food Effect: Instantly swallowed zombies!";
        }
        return "Plant Food applied!";
    }

    private void executeMeleeAttack(Plant plant, Game game) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        if (name.equalsIgnoreCase("Chomper")) {
            if (!plant.isDigesting()) {
                Zombie target = game.getFirstZombieInRowAhead(py, px);
                if (target != null && target.getX() - px <= 1.2) {
                    plant.triggerAttack(0.8f);
                    target.takeDamage(99999, true);
                    plant.startDigestion(400);
                    game.getGameLogMessages().add("Chomper swallowed a zombie at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Bonk Choy") || name.equalsIgnoreCase("Wasabi Whip")) {
            for (Zombie z : game.getActiveZombies()) {
                if (z.getY() == py && Math.abs(z.getX() - px) <= 1.1) {
                    plant.triggerAttack(0.4f);
                    z.takeDamage(plant.getDamage(), false);
                }
            }
        } else if (name.equalsIgnoreCase("Phat Beet") || name.equalsIgnoreCase("Kiwibeast")) {
            int radius = plant.getPlantStage();
            for (Zombie z : game.getActiveZombies()) {
                if (Math.abs(z.getY() - py) <= radius && Math.abs(z.getX() - px) <= radius) {
                    plant.triggerAttack(0.5f);
                    z.takeDamage(plant.getDamage(), false);
                }
            }
        }
    }

    private void executeExplosiveLogic(Plant plant, List<Plant> toRemove, Game game) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();

        if (name.equalsIgnoreCase("Doom-shroom")) {
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                z.takeDamage(1800, true);
            }
            toRemove.add(plant);
            Tile t = game.getBoard().getTile(py, px);
            if (t != null) {
                t.setCrater(true);
            }
            game.getGameLogMessages().add("Doom-shroom exploded and created an unplantable crater at (" + px + ", " + py + ")!");
        } else if (name.equalsIgnoreCase("Ice-shroom")) {
            for (Zombie z : game.getActiveZombies()) {
                z.applyFrozen(5.0);
            }
            toRemove.add(plant);
            game.getGameLogMessages().add("Ice-shroom froze all zombies on map!");
        } else if (name.equalsIgnoreCase("Hot Potato")) {
            Tile tile = game.getBoard().getTile(py, px);
            if (tile != null && tile.getPlant() != null && tile.getPlant().isFrozen()) {
                tile.getPlant().melt();
                game.getGameLogMessages().add("Hot Potato instantly melted ice on plant at (" + px + ", " + py + ")!");
            }
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                if (z.getY() == py && (int) Math.round(z.getX()) == px) {
                    z.removeEffect(ZombieEffect.FROZEN);
                    z.removeEffect(ZombieEffect.CHILLED);
                    z.setFrozenIceHealth(0);
                    game.getGameLogMessages().add("Hot Potato instantly melted ice on zombie " + z.getName() + " at (" + px + ", " + py + ")!");
                }
            }
            toRemove.add(plant);
        } else if (name.equalsIgnoreCase("Grave Buster")) {
            Tile tile = game.getBoard().getTile(py, px);
            if (tile != null && tile.getType() == TileType.GRAVE) {
                if (tile.getSunReward() > 0) game.addSun(tile.getSunReward());
                if (tile.hasPlantFoodReward()) game.addPlantFood();
                game.getBoard().removeGrave(py, px);
                toRemove.add(plant);
                game.getGameLogMessages().add("Grave Buster completely removed the grave at (" + px + ", " + py + ")!");
            }
        } else if (name.equalsIgnoreCase("Cherry Bomb") || name.equalsIgnoreCase("Grapeshot")) {
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                if (Math.abs(z.getY() - py) <= 1 && Math.abs(z.getX() - px) <= 1.5) {
                    z.takeDamage(1800, true);
                }
            }
            toRemove.add(plant);
            game.getGameLogMessages().add(name + " exploded in a 3x3 area!");
        } else if (name.equalsIgnoreCase("Jalapeno")) {
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                if (z.getY() == py) {
                    z.takeDamage(1800, true);
                }
            }
            toRemove.add(plant);
            game.getGameLogMessages().add("Jalapeno incinerated row " + py + "!");
        } else if (name.equalsIgnoreCase("Tangle Kelp")) {
            Tile t = game.getBoard().getTile(py, px);
            if (t != null && t.getType() == TileType.WATER) {
                Zombie target = game.getFirstZombieInRowAhead(py, px - 0.5);
                if (target != null && Math.abs(target.getX() - px) <= 0.8) {
                    target.takeDamage(99999, true);
                    toRemove.add(plant);
                    game.getGameLogMessages().add("Tangle Kelp pulled zombie underwater at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Potato Mine") || name.equalsIgnoreCase("Primal Potato Mine")) {
            if (plant.isArmed()) {
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    if (!z.isHypnotized() && z.getY() == py && Math.abs((int) Math.round(z.getX()) - px) <= 1) {
                        int radius = name.contains("Primal") ? 1 : 0;
                        for (Zombie az : new ArrayList<>(game.getActiveZombies())) {
                            if (Math.abs(az.getY() - py) <= radius && Math.abs((int) Math.round(az.getX()) - px) <= (radius + 1)) {
                                az.takeDamage(1800, true);
                            }
                        }
                        toRemove.add(plant);
                        game.getGameLogMessages().add(name + " detonated!");
                        break;
                    }
                }
            }
        } else if (name.equalsIgnoreCase("Iceberg Lettuce")) {
            for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                if (!z.isHypnotized() && z.getY() == py && Math.abs((int) Math.round(z.getX()) - px) <= 1) {
                    z.applyFrozen(5.0);
                    toRemove.add(plant);
                    game.getGameLogMessages().add("Iceberg Lettuce froze zombie " + z.getName() + "!");
                    break;
                }
            }
        }
    }

    private void executeUtilityLogic(Plant plant, List<Plant> toRemove, Game game) {
        String name = plant.getName();
        if (name.equalsIgnoreCase("Magnet-shroom") && plant.getMagnetCooldownTicks() <= 0) {
            for (Zombie z : game.getActiveZombies()) {
                if (z.getArmorHealth() > 0 && ("BUCKET".equalsIgnoreCase(z.getArmorType()) || "CONE".equalsIgnoreCase(z.getArmorType()) || "KNIGHT".equalsIgnoreCase(z.getArmorType()))) {
                    plant.triggerAttack(1.0f);
                    z.setArmorHealth(0);
                    z.setArmorType("none");
                    plant.startMagnetCooldown(150);
                    game.getGameLogMessages().add("Magnet-shroom removed armor from zombie at lane " + z.getY() + "!");
                    break;
                }
            }
        } else if (name.equalsIgnoreCase("Caulipower") || name.equalsIgnoreCase("Electric Blueberry")) {
            if (!game.getActiveZombies().isEmpty()) {
                Zombie target = game.getActiveZombies().get(new Random().nextInt(game.getActiveZombies().size()));
                if (target != null) {
                    plant.triggerAttack(1.0f);
                    if (name.equalsIgnoreCase("Caulipower")) {
                        target.setHypnotized(true);
                        game.getGameLogMessages().add("Caulipower hypnotized zombie " + target.getName() + "!");
                    } else {
                        target.takeDamage(5000, true);
                        game.getGameLogMessages().add("Electric Blueberry zapped zombie " + target.getName() + "!");
                    }
                }
            }
        }
    }

    private void executeMintLogic(Plant plant, List<Plant> toRemove, Game game) {
        String name = plant.getName().trim();
        String familyCategory = "";

        if (name.equalsIgnoreCase("Appease-mint")) familyCategory = "SHOOTER";
        else if (name.equalsIgnoreCase("Arma-mint")) familyCategory = "LOBBER";
        else if (name.equalsIgnoreCase("Bombard-mint")) familyCategory = "EXPLOSIVE";
        else if (name.equalsIgnoreCase("Enforce-mint")) familyCategory = "MELEE";
        else if (name.equalsIgnoreCase("Reinforce-mint")) familyCategory = "WALL_NUT";
        else if (name.equalsIgnoreCase("Enchant-mint")) familyCategory = "MODIFIER";
        else if (name.equalsIgnoreCase("Pierce-mint")) familyCategory = "STRIKE_THROUGH";
        else if (name.equalsIgnoreCase("catTail-mint")) familyCategory = "HOMING";
        else if (name.equalsIgnoreCase("Enlighten-mint")) familyCategory = "SUN_PRODUCER";

        if (!familyCategory.isEmpty()) {
            triggerMintBoost(familyCategory, game);
            toRemove.add(plant);
            game.getGameLogMessages().add(name + " activated Plant Food boost for all " + familyCategory + " family plants!");
        }
    }

    private void triggerMintBoost(String category, Game game) {
        String normalizedTarget = category.replace("-", "").replace("_", "").toLowerCase();
        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            String pCat = p.getCategory();
            if (pCat != null) {
                String normalizedCat = pCat.replace("-", "").replace("_", "").toLowerCase();
                if (normalizedCat.equals(normalizedTarget)) {
                    if (normalizedTarget.equals("wallnut")) {
                        p.applyPlantFoodArmor(4000);
                        game.getGameLogMessages().add("Reinforce-mint boosted " + p.getName() + " with 4000 armor!");
                    } else {
                        p.heal(p.getMaxHealth());
                        applyPlantFood(p, game);
                        game.getGameLogMessages().add("Boosted family plant: " + p.getName());
                    }
                }
            }
        }
    }
}
