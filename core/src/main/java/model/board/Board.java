package model.board;

import model.Game;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;
import model.enums.TileType;
import model.handler.GameStateManager;
import model.handler.ZombieAbilityHandler;
import view.game.renderers.ProjectileRenderer;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private final int rows;
    private final int columns;
    private final Tile[][] tiles;

    public Board(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.tiles = new Tile[rows][columns];
        initializeTiles();
    }

    private void initializeTiles() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                tiles[row][column] = new Tile(row, column);
            }
        }
    }

    public Tile getTile(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            return null;
        }
        return tiles[row][column];
    }

    public void updateTiles() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                tiles[row][column].updateTile();
            }
        }
    }

    public void setTileType(int row, int column, TileType type) {
        Tile tile = getTile(row, column);
        if (tile != null) {
            tile.setType(type);
        }
    }

    public boolean isTileWater(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.WATER;
    }

    public boolean isTileIce(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.ICE;
    }

    public boolean isTileGrave(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.isGrave();
    }

    public boolean isTileGrass(int row, int column) {
        Tile tile = getTile(row, column);
        return tile != null && tile.getType() == TileType.GRASS;
    }

    public int getRows() { return rows; }
    public int getColumns() { return columns; }

    public boolean isInBounds(int row, int column) {
        return row >= 0 && row < rows && column >= 0 && column < columns;
    }

    public void setupGrave(int row, int col, int health, int sun, boolean pf) {
        Tile tile = getTile(row, col);
        if (tile != null) {
            tile.setType(TileType.GRAVE);
            tile.setGraveHealth(health);
            tile.setSunReward(sun);
            tile.setHasPlantFoodReward(pf);
        }
    }

    public void removeGrave(int row, int col) {
        Tile tile = getTile(row, col);
        if (tile != null) {
            tile.setType(TileType.GRASS);
            tile.setGraveHealth(0);
            tile.setSunReward(0);
            tile.setHasPlantFoodReward(false);
        }
    }

    public void setupSlideway(int row, int col, int offset) {
        Tile tile = getTile(row, col);
        if (tile != null) {
            tile.setSlideway(true);
            tile.setSlideRowOffset(offset);
        }
    }

    public void setupLowBeach(int row, int col) {
        Tile tile = getTile(row, col);
        if (tile != null) {
            tile.setLowBeach(true);
        }
    }

    public void setupNecromancy(int row, int col) {
        Tile tile = getTile(row, col);
        if (tile != null) {
            tile.setNecromancyTile(true);
        }
    }

    public boolean hasGraveInRow(int row) {
        for (int c = 0; c < columns; c++) {
            if (isTileGrave(row, c)) {
                return true;
            }
        }
        return false;
    }

    public void updateProjectilesAndCollisions(Game game) {
        updateTiles();
        List<Bullet> bulletsToRemove = new ArrayList<>();
        List<Bullet> bulletsToAdd = new ArrayList<>();
        List<Zombie> zombiesKilled = new ArrayList<>();

        for (Bullet bullet : new ArrayList<>(game.getBullets())) {
            double oldBulletX = bullet.getColumn();
            bullet.move();
            double newBulletX = bullet.getColumn();
            int bRow = bullet.getRow();

            int checkTileCol = (int) Math.floor(newBulletX);
            Tile tile = getTile(bRow, checkTileCol);

            if (tile != null && tile.getPlant() != null && tile.getPlant().getName().replace(" ", "").replace("-", "").equalsIgnoreCase("Torchwood")) {
                if (bullet.getType() == Bullet.BulletType.NORMAL || bullet.getType() == Bullet.BulletType.ICE) {
                    bulletsToRemove.add(bullet);
                    int mult = tile.getPlant().isBlueFlame() ? 3 : 2;
                    Bullet fireBullet = new Bullet(
                        bullet.getDamage() * mult,
                        bRow,
                        (int) Math.floor(newBulletX) + 1,
                        Bullet.BulletType.FIRE,
                        bullet.isPierce(),
                        bullet.isExplosive(),
                        bullet.getSplashRadius()
                    );
                    fireBullet.setPlantName("Torchwood");
                    bulletsToAdd.add(fireBullet);
                    game.getGameLogMessages().add("Torchwood ignited passing pea! Damage multiplied by " + mult);
                    continue;
                }
            }

            if (tile != null && tile.isGrave() && bullet.getType() != Bullet.BulletType.LOB) {
                if (oldBulletX <= checkTileCol && newBulletX >= checkTileCol) {
                    tile.setGraveHealth(tile.getGraveHealth() - bullet.getDamage());
                    if (!bullet.isPierce()) {
                        bulletsToRemove.add(bullet);
                    }
                    if (tile.getGraveHealth() <= 0) {
                        game.getGameLogMessages().add("Grave destroyed at column " + checkTileCol + ", row " + bRow);
                        if (tile.getSunReward() > 0) game.addSun(tile.getSunReward());
                        if (tile.hasPlantFoodReward()) game.addPlantFood();
                        removeGrave(bRow, checkTileCol);
                    }
                    continue;
                }
            }

            Zombie targetZombie = null;
            for (Zombie z : game.getActiveZombies()) {
                if (!z.isHypnotized()) {
                    boolean inRow = (z instanceof Zomboss) ? ((Zomboss) z).occupiesRow(bRow) : (z.getY() == bRow);
                    if (inRow) {
                        double zombieX = z.getX();
                        if (newBulletX >= zombieX && oldBulletX <= zombieX + 0.8) {
                            targetZombie = z;
                            break;
                        }
                    }
                }
            }

            if (targetZombie != null) {
                boolean bypassArmor = (bullet.getType() == Bullet.BulletType.POISON);
                if (targetZombie instanceof Zomboss) {
                    Zomboss b = (Zomboss) targetZombie;
                    b.takeBossDamage(bullet.getDamage());
                    if (!b.isAlive() && !zombiesKilled.contains(b)) {
                        zombiesKilled.add(b);
                    }
                } else {
                    targetZombie.takeDamage(bullet.getDamage(), bypassArmor);
                }
                bullet.incrementHitZombieCount();

                if (bullet.getType() == Bullet.BulletType.ICE) {
                    targetZombie.applyChilled(3.0);
                }

                if (bullet.isExplosive() && bullet.getSplashRadius() > 0) {
                    int radius = bullet.getSplashRadius();
                    for (Zombie splashZ : new ArrayList<>(game.getActiveZombies())) {
                        if (splashZ != targetZombie) {
                            boolean inSplashRow = (splashZ instanceof Zomboss) ? ((Zomboss) splashZ).occupiesRow(bRow) : (Math.abs(splashZ.getY() - bRow) <= radius);
                            if (inSplashRow && Math.abs(splashZ.getX() - targetZombie.getX()) <= 1.2) {
                                if (splashZ instanceof Zomboss) {
                                    Zomboss sb = (Zomboss) splashZ;
                                    sb.takeBossDamage(bullet.getDamage() / 2);
                                    if (!sb.isAlive() && !zombiesKilled.contains(sb)) {
                                        zombiesKilled.add(sb);
                                    }
                                } else {
                                    splashZ.takeDamage(bullet.getDamage() / 2, false);
                                    if (!splashZ.isAlive() && !zombiesKilled.contains(splashZ)) {
                                        zombiesKilled.add(splashZ);
                                    }
                                }
                                if (bullet.getType() == Bullet.BulletType.ICE) {
                                    splashZ.applyChilled(3.0);
                                }
                            }
                        }
                    }
                }

                String explosionPam = ProjectileRenderer.getExplosionPamForBullet(bullet);
                if (explosionPam != null) {
                    float hitPx = view.game.GameGrid.getGridStartX() + ((float) targetZombie.getX() * view.game.GameGrid.TILE_WIDTH) + (view.game.GameGrid.TILE_WIDTH / 2f);
                    float hitPy = view.game.GameGrid.getGridStartY() + ((4 - targetZombie.getY()) * view.game.GameGrid.TILE_HEIGHT) + (view.game.GameGrid.TILE_HEIGHT / 2f);
                    ProjectileRenderer.triggerStaticImpact(explosionPam, hitPx, hitPy);
                }

                if (!(targetZombie instanceof Zomboss) && !targetZombie.isAlive() && !zombiesKilled.contains(targetZombie)) {
                    String deathMessage = "Zombie of type " + targetZombie.getName() + " is dead at (" + (int) Math.round(targetZombie.getX()) + ", " + targetZombie.getY() + ")";
                    game.getGameLogMessages().add(deathMessage);
                    zombiesKilled.add(targetZombie);
                }

                if (!bullet.isPierce() || (bullet.getMaxPierceTargets() > 0 && bullet.getHitZombieCount() >= bullet.getMaxPierceTargets())) {
                    bulletsToRemove.add(bullet);
                }
            } else if (bullet.isOutOfBounds(getColumns())) {
                bulletsToRemove.add(bullet);
            }
        }

        ZombieAbilityHandler abilityHandler = new ZombieAbilityHandler();
        boolean bossDefeated = false;
        for (Zombie z : zombiesKilled) {
            game.getActiveZombies().remove(z);
            for (int r = 0; r < getRows(); r++) {
                for (int c = 0; c < getColumns(); c++) {
                    if (getTile(r, c).getZombie() == z) {
                        getTile(r, c).setZombie(null);
                    }
                }
            }
            abilityHandler.processZombieDeathDrops(z, game);
            game.getScoreGame().onZombieKilled(z, game);
            game.incrementZombiesKilled();
            if (z instanceof Zomboss) {
                bossDefeated = true;
            }
        }

        game.getBullets().removeAll(bulletsToRemove);
        game.getBullets().addAll(bulletsToAdd);

        if (bossDefeated) {
            game.setWon(true);
            game.stop();
            game.getGameLogMessages().add("ZOMBOSS DEFEATED! VICTORY!");
            GameStateManager.handleVictoryStats(game);
        }
    }
}
