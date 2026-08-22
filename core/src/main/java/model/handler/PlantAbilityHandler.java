package model.handler;

import model.Game;
import model.board.Bullet;
import model.board.Tile;
import model.entities.plant.Plant;
import model.entities.zombie.Zombie;
import model.entities.zombie.ZombieEffect;
import model.enums.TileType;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import view.game.GameGrid;
import view.game.renderers.ProjectileRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlantAbilityHandler {

    public void updatePlantsAndAbilities(Game game) {
        List<Plant> plantsToRemove = new ArrayList<>();
        List<Plant> activePlants = new ArrayList<>(game.getActivePlants());
        boolean isSunDisabledMiniGame = game.getActiveMiniGame() instanceof IZombie || game.getActiveMiniGame() instanceof Beghoul;

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

            if ("SUN_PRODUCER".equalsIgnoreCase(plant.getCategory()) && !isSunDisabledMiniGame) {
                if (name.equalsIgnoreCase("Gold Bloom")) {
                    plant.triggerAttack(2.0f);
                    if (plant.getHitCount() == 0) {
                        plant.incrementHitCount();
                        plant.setLifespanTicks(20);
                        int sunVal = (int) plant.getAbilityValue();
                        if (sunVal <= 0) sunVal = 375;
                        game.addSun(sunVal);
                        game.getGameLogMessages().add("Gold Bloom produced " + sunVal + " suns!");
                    } else {
                        plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                        if (plant.getLifespanTicks() <= 0) {
                            plantsToRemove.add(plant);
                        }
                    }
                } else if (!name.toLowerCase().contains("mint")) {
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

            if (isAttacker && !name.toLowerCase().contains("mint") && plant.shouldShoot()) {
                boolean shouldShoot = false;
                int py = plant.getY();
                int px = plant.getX();

                if (name.equalsIgnoreCase("Rotobaga") || name.equalsIgnoreCase("Starfruit")) {
                    shouldShoot = !game.getActiveZombies().isEmpty();
                } else if (name.equalsIgnoreCase("Split Pea")) {
                    boolean targetAhead = game.hasZombieInRowAhead(py, px) || hasGraveAhead(game, py, px);
                    boolean targetBehind = game.hasZombieInRowBehind(py, px);
                    shouldShoot = targetAhead || targetBehind;
                } else if (name.equalsIgnoreCase("Threepeater")) {
                    shouldShoot = (game.hasZombieInRow(py) || hasGraveAhead(game, py, px))
                        || (py > 0 && (game.hasZombieInRow(py - 1) || hasGraveAhead(game, py - 1, px)))
                        || (py < game.getBoard().getRows() - 1 && (game.hasZombieInRow(py + 1) || hasGraveAhead(game, py + 1, px)));
                } else {
                    shouldShoot = game.hasZombieInRowAhead(py, px) || hasGraveAhead(game, py, px)
                        || "HOMING".equalsIgnoreCase(plant.getCategory()) || name.equalsIgnoreCase("Laser Bean");
                }

                if (shouldShoot) {
                    spawnBulletsForPlant(plant, game);
                }
            }

            if ("MELEE".equalsIgnoreCase(plant.getCategory()) && !name.toLowerCase().contains("mint")) {
                executeMeleeAttack(plant, game);
            }

            if ("EXPLOSIVE".equalsIgnoreCase(plant.getCategory())) {
                executeExplosiveLogic(plant, plantsToRemove, game);
            }

            if (("MODIFIER".equalsIgnoreCase(plant.getCategory()) || "HOMING".equalsIgnoreCase(plant.getCategory())) && !name.toLowerCase().contains("mint") && plant.shouldShoot()) {
                executeUtilityLogic(plant, plantsToRemove, game);
            }

            if (name.toLowerCase().contains("mint")) {
                executeMintLogic(plant, plantsToRemove, game);
            }
        }

        for (Plant p : plantsToRemove) {
            game.removePlant(p);
            Tile t = game.getBoard().getTile(p.getY(), p.getX());
            if (t != null) {
                if (t.getPlant() == p) {
                    t.setPlant(null);
                }
                if (t.getPumpkinPlant() == p) {
                    t.setPumpkinPlant(null);
                }
            }
        }
    }

    private boolean hasGraveAhead(Game game, int row, int col) {
        if (game == null || game.getBoard() == null) return false;
        for (int c = col + 1; c < game.getBoard().getColumns(); c++) {
            if (game.getBoard().isTileGrave(row, c)) {
                return true;
            }
        }
        return false;
    }

    private double getFirstGraveColumnAhead(Game game, int row, int col) {
        if (game == null || game.getBoard() == null) return 8.0;
        for (int c = col + 1; c < game.getBoard().getColumns(); c++) {
            if (game.getBoard().isTileGrave(row, c)) {
                return (double) c;
            }
        }
        return 8.0;
    }

    public void spawnBulletsForPlant(Plant plant, Game game) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        int dmg = plant.getDamage() > 0 ? plant.getDamage() : 20;

        plant.triggerAttack(0.6f);

        Zombie targetZombie = game.getFirstZombieInRowAhead(py, px);
        double targetCol;
        if (targetZombie != null) {
            targetCol = targetZombie.getX();
        } else {
            targetCol = getFirstGraveColumnAhead(game, py, px);
        }

        if (name.equalsIgnoreCase("Fume-shroom") || name.equalsIgnoreCase("Fumeshroom")) {
            Bullet fume = createBulletWithMeta(game, dmg, py, px + 0.5, Bullet.BulletType.STRIKE_THROUGH, true, false, 0, name, targetCol);
            fume.setMaxPierceTargets(999);
        } else if (name.equalsIgnoreCase("Mega Gatling Pea") || name.equalsIgnoreCase("MegaGatlingPea") || name.equalsIgnoreCase("Gatling Pea")) {
            createBulletWithMeta(game, dmg, py, px + 0.35, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            createBulletWithMeta(game, dmg, py, px + 0.65, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            createBulletWithMeta(game, dmg, py, px + 0.95, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            createBulletWithMeta(game, dmg, py, px + 1.25, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Repeater")) {
            createBulletWithMeta(game, dmg, py, px + 0.5, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            createBulletWithMeta(game, dmg, py, px + 0.9, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
        } else if (name.equalsIgnoreCase("Rotobaga")) {
            double diagSpeedX = 0.20;
            double diagSpeedY = 0.08;

            Bullet brUp = createBulletWithMeta(game, dmg, py, px + 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            brUp.setDx(diagSpeedX);
            brUp.setDy(-diagSpeedY);

            Bullet brDown = createBulletWithMeta(game, dmg, py, px + 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            brDown.setDx(diagSpeedX);
            brDown.setDy(diagSpeedY);

            Bullet blUp = createBulletWithMeta(game, dmg, py, px - 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
            blUp.setDx(-diagSpeedX);
            blUp.setDy(-diagSpeedY);

            Bullet blDown = createBulletWithMeta(game, dmg, py, px - 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
            blDown.setDx(-diagSpeedX);
            blDown.setDy(diagSpeedY);
        } else if (name.equalsIgnoreCase("Starfruit")) {
            double diagSpeedX = 0.22;
            double diagSpeedY = 0.10;

            Bullet front = createBulletWithMeta(game, dmg, py, px + 0.5, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            front.setDx(0.35);
            front.setDy(0.0);

            Bullet back = createBulletWithMeta(game, dmg, py, px - 0.5, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
            back.setDx(-0.35);
            back.setDy(0.0);

            Bullet brUp = createBulletWithMeta(game, dmg, py, px + 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            brUp.setDx(diagSpeedX);
            brUp.setDy(-diagSpeedY);

            Bullet brDown = createBulletWithMeta(game, dmg, py, px + 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            brDown.setDx(diagSpeedX);
            brDown.setDy(diagSpeedY);

            Bullet blUp = createBulletWithMeta(game, dmg, py, px - 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
            blUp.setDx(-diagSpeedX);
            blUp.setDy(-diagSpeedY);

            Bullet blDown = createBulletWithMeta(game, dmg, py, px - 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
            blDown.setDx(-diagSpeedX);
            blDown.setDy(diagSpeedY);
        } else if (name.equalsIgnoreCase("Split Pea")) {
            boolean hasAhead = game.hasZombieInRowAhead(py, px) || hasGraveAhead(game, py, px);
            boolean hasBehind = game.hasZombieInRowBehind(py, px);

            if (hasAhead) {
                createBulletWithMeta(game, dmg, py, px + 0.6, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            }
            if (hasBehind) {
                Bullet b1 = createBulletWithMeta(game, dmg, py, px - 0.4, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
                b1.setDx(-0.35);

                Bullet b2 = createBulletWithMeta(game, dmg, py, px - 0.8, Bullet.BulletType.NORMAL, false, false, 0, name, 0.0);
                b2.setDx(-0.35);
            }
        } else if (name.equalsIgnoreCase("Threepeater")) {
            createBulletWithMeta(game, dmg, py, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            if (py > 0) createBulletWithMeta(game, dmg, py - 1, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            if (py < game.getBoard().getRows() - 1) createBulletWithMeta(game, dmg, py + 1, px + 1, Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
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
        } else if (name.replace(" ", "").replace("-", "").equalsIgnoreCase("peapod")) {
            int heads = Math.max(1, plant.getPeaPodHeads());
            int baseDmg = 20 + (plant.getLevel() >= 2 ? 10 : 0);
            for (int i = 0; i < heads; i++) {
                createBulletWithMeta(game, baseDmg, py, px + 0.35 + (i * 0.15), Bullet.BulletType.NORMAL, false, false, 0, name, targetCol);
            }
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

        if (name.equalsIgnoreCase("Fume-shroom") || name.equalsIgnoreCase("Fumeshroom")) {
            for (int i = 0; i < 40; i++) {
                Bullet fume = new Bullet(20, plant.getY(), plant.getX() + 0.5, Bullet.BulletType.STRIKE_THROUGH, true, false, 0);
                fume.setPlantName("Fume-shroom");
                fume.setMaxPierceTargets(999);
                game.addBullet(fume);
            }
            game.getGameLogMessages().add("Fume-shroom unleashed massive bubble barrage across its lane!");
            return "Plant Food Effect: Unleashed bubble wave!";
        } else if (name.equalsIgnoreCase("Sunflower")) {
            game.addSun(150);
            game.getGameLogMessages().add("Sunflower used Plant Food and instantly generated 150 suns!");
            return "Plant Food Effect: Produced 150 suns!";
        } else if (name.equalsIgnoreCase("Twin Sunflower")) {
            game.addSun(250);
            game.getGameLogMessages().add("Twin Sunflower used Plant Food and instantly generated 250 suns!");
            return "Plant Food Effect: Produced 250 suns!";
        } else if (name.equalsIgnoreCase("Sun-shroom") || name.equalsIgnoreCase("Sunshroom")) {
            plant.setPlantStage(3);
            game.addSun(225);
            game.getGameLogMessages().add("Sun-shroom instantly grew to max stage and produced 225 suns!");
            return "Plant Food Effect: Grew to max size and produced 225 suns!";
        } else if (name.equalsIgnoreCase("Primal Sunflower")) {
            game.addSun(225);
            game.getGameLogMessages().add("Primal Sunflower used Plant Food and produced 225 suns!");
            return "Plant Food Effect: Produced 225 suns!";
        } else if (name.equalsIgnoreCase("Mega Gatling Pea") || name.equalsIgnoreCase("MegaGatlingPea") || name.equalsIgnoreCase("Gatling Pea")) {
            for (int i = 0; i < 120; i++) {
                game.addBullet(new Bullet(25, plant.getY(), plant.getX() + 1, Bullet.BulletType.NORMAL, false, false, 0));
            }
            game.getGameLogMessages().add("Mega Gatling Pea unleashed a devastating Gatling barrage!");
            return "Plant Food Effect: Unleashed Mega Gatling barrage!";
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
        } else if (name.equalsIgnoreCase("Starfruit")) {
            double speed = 0.30;
            for (int i = 0; i < 16; i++) {
                double angleStep = (2 * Math.PI) / 8;
                for (int d = 0; d < 8; d++) {
                    double angle = d * angleStep + (i * 0.1);
                    Bullet star = new Bullet(25, plant.getY(), plant.getX() + 0.5, Bullet.BulletType.NORMAL, false, false, 0);
                    star.setPlantName("Starfruit");
                    star.setDx(Math.cos(angle) * speed);
                    star.setDy(Math.sin(angle) * (speed * 0.45));
                    game.addBullet(star);
                }
            }
            game.getGameLogMessages().add("Starfruit unleashed a giant star barrage in all 360 degrees!");
            return "Plant Food Effect: Unleashed 360-degree star barrage!";
        } else if (name.equalsIgnoreCase("Rotobaga")) {
            for (int i = 0; i < 20; i++) {
                Bullet brUp = new Bullet(20, plant.getY(), plant.getX() + 0.4, Bullet.BulletType.NORMAL, false, false, 0);
                brUp.setPlantName("Rotobaga");
                brUp.setDx(0.24); brUp.setDy(-0.10);
                game.addBullet(brUp);

                Bullet brDown = new Bullet(20, plant.getY(), plant.getX() + 0.4, Bullet.BulletType.NORMAL, false, false, 0);
                brDown.setPlantName("Rotobaga");
                brDown.setDx(0.24); brDown.setDy(0.10);
                game.addBullet(brDown);

                Bullet blUp = new Bullet(20, plant.getY(), plant.getX() - 0.4, Bullet.BulletType.NORMAL, false, false, 0);
                blUp.setPlantName("Rotobaga");
                blUp.setDx(-0.24); blUp.setDy(-0.10);
                game.addBullet(blUp);

                Bullet blDown = new Bullet(20, plant.getY(), plant.getX() - 0.4, Bullet.BulletType.NORMAL, false, false, 0);
                blDown.setPlantName("Rotobaga");
                blDown.setDx(-0.24); blDown.setDy(0.10);
                game.addBullet(blDown);
            }
            game.getGameLogMessages().add("Rotobaga unleashed heavy diagonal firestorm!");
            return "Plant Food Effect: Unleashed 4-way diagonal barrage!";
        } else if (name.equalsIgnoreCase("Split Pea")) {
            for (int i = 0; i < 60; i++) {
                game.addBullet(new Bullet(20, plant.getY(), plant.getX() + 1, Bullet.BulletType.NORMAL, false, false, 0));
                Bullet back = new Bullet(20, plant.getY(), plant.getX() - 0.5, Bullet.BulletType.NORMAL, false, false, 0);
                back.setDx(-0.35);
                game.addBullet(back);
            }
            game.getGameLogMessages().add("Split Pea fired twin barrage forward and backward!");
            return "Plant Food Effect: Fired front and back barrage!";
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
        } else if (name.replace(" ", "").replace("-", "").equalsIgnoreCase("peapod")) {
            int heads = Math.max(1, plant.getPeaPodHeads());
            int baseDmg = 20 + (plant.getLevel() >= 2 ? 10 : 0);
            for (int i = 0; i < 20 * heads; i++) {
                game.addBullet(new Bullet(baseDmg, plant.getY(), plant.getX() + 0.5 + (i * 0.05), Bullet.BulletType.NORMAL, false, false, 0));
            }
            game.getGameLogMessages().add("Pea Pod unleashed a massive " + heads + "-head pea barrage!");
            return "Plant Food Effect: Unleashed " + heads + "-head pea barrage!";
        }
        return "Plant Food applied!";
    }

    private void executeMeleeAttack(Plant plant, Game game) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();
        if (name.equalsIgnoreCase("Chomper")) {
            if (!plant.isDigesting()) {
                Zombie target = null;
                for (Zombie z : game.getActiveZombies()) {
                    if (!z.isHypnotized() && z.getY() == py && z.getX() >= px && (z.getX() - px) <= 1.5) {
                        if (target == null || z.getX() < target.getX()) {
                            target = z;
                        }
                    }
                }
                if (target != null) {
                    plant.triggerCustomAnim("bite", 1.0f);
                    target.takeDamage(99999, true);
                    plant.startDigestion(400);
                    game.getGameLogMessages().add("Chomper swallowed a zombie at (" + px + ", " + py + ")!");
                }
            }
        } else if (plant.shouldShoot()) {
            if (name.equalsIgnoreCase("Bonk Choy") || name.equalsIgnoreCase("Wasabi Whip")) {
                for (Zombie z : game.getActiveZombies()) {
                    if (z.getY() == py && Math.abs(z.getX() - px) <= 1.1) {
                        plant.triggerAttack(0.4f);
                        z.takeDamage(plant.getDamage(), false);
                    }
                }
            } else if (name.equalsIgnoreCase("Phat Beet")) {
                boolean hasTarget = false;
                for (Zombie z : game.getActiveZombies()) {
                    if (Math.abs(z.getY() - py) <= 1 && Math.abs(z.getX() - px) <= 1.5) {
                        hasTarget = true;
                        z.takeDamage(plant.getDamage(), false);
                    }
                }
                if (hasTarget) {
                    plant.triggerAttack(0.5f);
                    float pulseX = GameGrid.getGridStartX() + (px * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                    float pulseY = GameGrid.getGridStartY() + ((4 - py) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                    ProjectileRenderer.triggerStaticImpact("768/FULL/EFFECTS/PHATBEETS_ATTACK_PULSE/PHATBEETS_ATTACK_PULSE.PAM", pulseX, pulseY);
                }
            } else if (name.equalsIgnoreCase("Kiwibeast")) {
                if (!game.getActiveZombies().isEmpty()) {
                    plant.triggerAttack(0.5f);
                    float pulseX = GameGrid.getGridStartX() + (px * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                    float pulseY = GameGrid.getGridStartY() + ((4 - py) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                    ProjectileRenderer.triggerStaticImpact("768/INITIAL/EFFECTS/KIWIBEAST_ATTACK_PULSE/KIWIBEAST_ATTACK_PULSE.PAM", pulseX, pulseY);
                    for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                        z.takeDamage(plant.getDamage(), false);
                    }
                }
            }
        }
    }

    private void executeExplosiveLogic(Plant plant, List<Plant> toRemove, Game game) {
        String name = plant.getName();
        int px = plant.getX();
        int py = plant.getY();

        if (name.equalsIgnoreCase("Squash")) {
            if (plant.getHitCount() == 0) {
                List<Zombie> adjacentZombies = new ArrayList<>();
                for (Zombie z : game.getActiveZombies()) {
                    if (!z.isHypnotized() && Math.abs(z.getY() - py) <= 1 && Math.abs(z.getX() - px) <= 1.2) {
                        adjacentZombies.add(z);
                    }
                }
                if (!adjacentZombies.isEmpty()) {
                    plant.triggerCustomAnim("jump_down_right", 1.2f);
                    plant.incrementHitCount();
                    plant.setLifespanTicks(12);
                    for (Zombie az : adjacentZombies) {
                        az.takeDamage(1800, true);
                    }
                    game.getGameLogMessages().add("Squash crushed adjacent zombies at (" + px + ", " + py + ")!");
                }
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    toRemove.add(plant);
                }
            }
        } else if (name.equalsIgnoreCase("Potato Mine") || name.equalsIgnoreCase("Primal Potato Mine")) {
            if (plant.isArmed()) {
                for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                    if (!z.isHypnotized() && z.getY() == py && Math.abs(z.getX() - px) <= 0.25) {
                        plant.triggerAttack(0.6f);
                        float expX = GameGrid.getGridStartX() + (px * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                        float expY = GameGrid.getGridStartY() + ((4 - py) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                        ProjectileRenderer.triggerStaticImpact("768/INITIAL/EFFECTS/ESCAPEROOT_EXPLOSION_POTATOMINE/ESCAPEROOT_EXPLOSION_POTATOMINE.PAM", expX, expY);

                        int radius = name.contains("Primal") ? 1 : 0;
                        for (Zombie az : new ArrayList<>(game.getActiveZombies())) {
                            if (Math.abs(az.getY() - py) <= radius && Math.abs(az.getX() - px) <= (radius + 0.5)) {
                                az.takeDamage(plant.getDamage() > 0 ? plant.getDamage() : 1800, true);
                            }
                        }
                        toRemove.add(plant);
                        game.getGameLogMessages().add(name + " detonated!");
                        break;
                    }
                }
            }
        } else if (name.equalsIgnoreCase("Grapeshot")) {
            plant.triggerAttack(1.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(12);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                        if (Math.abs(z.getY() - py) <= 1 && Math.abs(z.getX() - px) <= 1.5) {
                            z.takeDamage(1800, true);
                        }
                    }

                    Random r = new Random();
                    for (int i = 0; i < 8; i++) {
                        double angle = (i * (2 * Math.PI / 8)) + ((r.nextDouble() - 0.5) * 0.4);
                        double speed = 0.28;
                        Bullet grape = new Bullet(200, py, px + 0.5, Bullet.BulletType.NORMAL, false, false, 0);
                        grape.setPlantName("Grapeshot");
                        grape.setDx(Math.cos(angle) * speed);
                        grape.setDy(Math.sin(angle) * (speed * 0.45));
                        grape.setBouncing(true);
                        grape.setLifespanTicks(50);
                        game.addBullet(grape);
                    }

                    toRemove.add(plant);
                    game.getGameLogMessages().add("Grapeshot exploded and scattered bouncing grapes across the lawn!");
                }
            }
        } else if (name.equalsIgnoreCase("Cherry Bomb")) {
            plant.triggerAttack(1.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(12);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                        if (Math.abs(z.getY() - py) <= 1 && Math.abs(z.getX() - px) <= 1.5) {
                            z.takeDamage(1800, true);
                        }
                    }
                    toRemove.add(plant);
                    game.getGameLogMessages().add("Cherry Bomb exploded in a 3x3 area!");
                }
            }
        } else if (name.equalsIgnoreCase("Doom-shroom")) {
            plant.triggerAttack(1.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(12);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                        z.takeDamage(1800, true);
                    }
                    toRemove.add(plant);
                    Tile t = game.getBoard().getTile(py, px);
                    if (t != null) {
                        t.setCrater(true);
                    }
                    game.getGameLogMessages().add("Doom-shroom exploded and created an unplantable crater at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Ice-shroom")) {
            plant.triggerAttack(1.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(12);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    for (Zombie z : game.getActiveZombies()) {
                        z.applyFrozen(5.0);
                    }
                    toRemove.add(plant);
                    game.getGameLogMessages().add("Ice-shroom froze all zombies on map!");
                }
            }
        } else if (name.equalsIgnoreCase("Hot Potato")) {
            plant.triggerAttack(1.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(10);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
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
                }
            }
        } else if (name.equalsIgnoreCase("Jalapeno")) {
            plant.triggerAttack(1.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(12);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    for (Zombie z : new ArrayList<>(game.getActiveZombies())) {
                        if (z.getY() == py) {
                            int zCol = (int) Math.floor(z.getX());
                            if (zCol >= 0 && zCol < game.getBoard().getColumns()) {
                                float fx = GameGrid.getGridStartX() + (zCol * GameGrid.TILE_WIDTH) + (GameGrid.TILE_WIDTH / 2f);
                                float fy = GameGrid.getGridStartY() + ((4 - py) * GameGrid.TILE_HEIGHT) + (GameGrid.TILE_HEIGHT / 2f);
                                ProjectileRenderer.triggerStaticImpact("768/INITIAL/EFFECTS/JALAPENO_FIRE/JALAPENO_FIRE.PAM", fx, fy);
                            }
                            z.takeDamage(1800, true);
                        }
                    }
                    toRemove.add(plant);
                    game.getGameLogMessages().add("Jalapeno incinerated row " + py + "!");
                }
            }
        } else if (name.equalsIgnoreCase("Grave Buster")) {
            Tile tile = game.getBoard().getTile(py, px);
            plant.triggerAttack(4.0f);
            if (plant.getHitCount() == 0) {
                plant.incrementHitCount();
                plant.setLifespanTicks(40);
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    if (tile != null && tile.getType() == TileType.GRAVE) {
                        if (tile.getSunReward() > 0) game.addSun(tile.getSunReward());
                        if (tile.hasPlantFoodReward()) game.addPlantFood();
                        game.getBoard().removeGrave(py, px);
                    }
                    toRemove.add(plant);
                    game.getGameLogMessages().add("Grave Buster completely removed the grave at (" + px + ", " + py + ")!");
                }
            }
        } else if (name.equalsIgnoreCase("Tangle Kelp") || name.equalsIgnoreCase("TangleKelp")) {
            if (plant.getHitCount() == 0) {
                Zombie target = null;
                for (Zombie z : game.getActiveZombies()) {
                    if (!z.isHypnotized() && z.getY() == py && Math.abs(z.getX() - px) <= 0.8) {
                        target = z;
                        break;
                    }
                }
                if (target != null) {
                    plant.triggerAttack(1.2f);
                    target.takeDamage(99999, true);
                    plant.incrementHitCount();
                    plant.setLifespanTicks(12);
                    game.getGameLogMessages().add("Tangle Kelp pulled zombie underwater at (" + px + ", " + py + ")!");
                }
            } else {
                plant.setLifespanTicks(plant.getLifespanTicks() - 1);
                if (plant.getLifespanTicks() <= 0) {
                    toRemove.add(plant);
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

        if (familyCategory.isEmpty()) return;

        if (plant.getHitCount() == 0) {
            plant.incrementHitCount();
            plant.setLifespanTicks(60);
            triggerMintBoost(familyCategory, game);
            game.getGameLogMessages().add(name + " activated Plant Food boost for all " + familyCategory + " family plants!");
        } else {
            plant.setLifespanTicks(plant.getLifespanTicks() - 1);
            if (plant.getLifespanTicks() <= 0) {
                toRemove.add(plant);
            }
        }
    }

    private void triggerMintBoost(String category, Game game) {
        String normalizedTarget = category.replace("-", "").replace("_", "").toLowerCase();
        for (Plant p : new ArrayList<>(game.getActivePlants())) {
            String pCat = p.getCategory();
            if (pCat != null && !p.getName().toLowerCase().contains("mint")) {
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
