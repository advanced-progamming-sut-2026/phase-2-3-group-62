package model;

import model.board.Board;
import model.board.Bullet;
import model.board.LawnMower;
import model.board.Sun;
import model.enums.Difficulty;
import model.entities.zombie.Spawner;
import model.entities.zombie.Zombie;
import model.entities.plant.Plant;
import model.handler.EnvironmentManager;
import model.handler.GameStateManager;
import model.handler.PlantAbilityHandler;
import model.handler.ZombieInteractionHandler;
import model.level.Level;
import model.score.ScoreGame;
import model.greenhouse.Greenhouse;
import model.enums.SpecialLevelType;
import model.minigame.MiniGame;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.minigame.IZombie;
import model.minigame.Zombotany;
import model.minigame.Beghoul;
import model.season.Season;
import model.user.UserSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Game {
    private Board board;
    private Level level;
    private Difficulty difficulty;
    private int difficultyLevel;
    private int sunCount;
    private boolean running;
    private int coins;
    private int diamonds;
    private int plantFoodCount;
    private Spawner spawner;
    private ScoreGame scoreGame;
    private Greenhouse greenhouse;
    private List<Bullet> bullets;
    private List<Sun> suns;
    private List<Zombie> activeZombies;
    private List<Plant> activePlants;
    private int tickCount;
    private LawnMower[] lawnMowers;
    private boolean won;
    private boolean lost;
    private int lastSunDropTick;
    private Season currentSeason;
    private List<String> conveyorBeltPlants;
    private List<Plant> seedsToProtect;
    private int zombiesKilledInLevel;
    private int sunsProducedInLevel;
    private int plantsLostCount;
    private boolean zombieWavesStarted;
    private MiniGame activeMiniGame;
    private List<String> gameLogMessages = new ArrayList<>();

    private final PlantAbilityHandler plantAbilityHandler = new PlantAbilityHandler();
    private final ZombieInteractionHandler zombieInteractionHandler = new ZombieInteractionHandler();
    private final EnvironmentManager environmentManager = new EnvironmentManager();
    private final GameStateManager gameStateManager = new GameStateManager();

    public Game() {
        this.board = new Board(5, 9);
        this.level = new Level(1);
        this.difficulty = Difficulty.NORMAL;
        this.difficultyLevel = 3;
        this.sunCount = 50;
        this.coins = 0;
        this.diamonds = 0;
        this.plantFoodCount = 0;
        this.bullets = new ArrayList<>();
        this.suns = new ArrayList<>();
        this.activeZombies = new ArrayList<>();
        this.activePlants = new ArrayList<>();
        this.scoreGame = new ScoreGame();
        this.tickCount = 0;
        this.lawnMowers = new LawnMower[5];
        for (int i = 0; i < 5; i++) {
            lawnMowers[i] = new LawnMower(i);
        }
        this.won = false;
        this.lost = false;
        this.lastSunDropTick = 0;
        this.currentSeason = new Season("Normal", 10);
        this.conveyorBeltPlants = new ArrayList<>();
        this.seedsToProtect = new ArrayList<>();
        this.zombiesKilledInLevel = 0;
        this.sunsProducedInLevel = 0;
        this.plantsLostCount = 0;
        this.zombieWavesStarted = true;
        this.activeMiniGame = null;
    }

    public Game(int rows, int columns, int levelNumber, int difficultyLevel) {
        this();
        this.board = new Board(rows, columns);
        this.level = new Level(levelNumber);
        this.difficultyLevel = difficultyLevel;
        if (difficultyLevel <= 2) {
            this.difficulty = Difficulty.EASY;
        } else if (difficultyLevel >= 4) {
            this.difficulty = Difficulty.HARD;
        } else {
            this.difficulty = Difficulty.NORMAL;
        }
        this.spawner = new Spawner(board, levelNumber * 2, this.difficulty);
        this.lawnMowers = new LawnMower[rows];
        for (int i = 0; i < rows; i++) {
            lawnMowers[i] = new LawnMower(i);
        }
    }

    public Game(int rows, int columns, int levelNumber, Difficulty difficulty) {
        this(rows, columns, levelNumber, difficulty == Difficulty.EASY ? 1 : (difficulty == Difficulty.HARD ? 5 : 3));
    }

    public void start() {
        running = true;
        if (spawner != null) {
            spawner.startWave(1);
            gameLogMessages.add("Wave " + spawner.getCurrentWave() + " started.");
        }
    }

    public void stop() {
        running = false;
    }

    public void setupSpecialLevelFeatures() {
        if (level == null) return;
        SpecialLevelType type = level.getSpecialLevelType();
        if (type == SpecialLevelType.SAVE_OUR_SEEDS) {
            for (int[] pos : level.getSeedProtectionPositions()) {
                Plant p = model.entities.plant.factory.PlantFactory.createPlant("PeaShooter");
                if (p != null) {
                    p.setX(pos[1]);
                    p.setY(pos[0]);
                    addPlant(p);
                    board.getTile(pos[0], pos[1]).setPlant(p);
                    seedsToProtect.add(p);
                }
            }
        } else if (type == SpecialLevelType.PLANT_WHAT_YOU_GET) {
            this.sunCount = level.getInitialSunAmount();
            this.zombieWavesStarted = false;
        }
    }

    private String getRandomUnlockedPlant() {
        List<String> unlocked = new ArrayList<>();
        if (UserSession.isLoggedIn() && UserSession.getCurrentUser() != null) {
            unlocked = UserSession.getCurrentUser().getUnlockedPlants();
        }
        if (unlocked == null || unlocked.isEmpty()) {
            unlocked = new ArrayList<>();
            unlocked.add("PeaShooter");
        }
        return unlocked.get(new Random().nextInt(unlocked.size()));
    }

    public void tick() {
        if (!running || won || lost) return;
        tickCount++;

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                board.getTile(r, c).setZombie(null);
            }
        }
        for (Zombie zombie : activeZombies) {
            int zX = (int) Math.round(zombie.getX());
            int zY = zombie.getY();
            if (zX >= 0 && zX < board.getColumns() && zY >= 0 && zY < board.getRows()) {
                board.getTile(zY, zX).setZombie(zombie);
            }
        }

        if (activeMiniGame instanceof Beghoul) {
            ((Beghoul) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof IZombie) {
            ((IZombie) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof Vasebreaker) {
            ((Vasebreaker) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof WallnutBowling) {
            ((WallnutBowling) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        } else if (activeMiniGame instanceof Zombotany) {
            ((Zombotany) activeMiniGame).updateMiniGame(this);
            if (won || lost) return;
        }

        SpecialLevelType specialType = level.getSpecialLevelType();
        if (activeMiniGame == null && specialType == SpecialLevelType.CONVEYOR_BELT) {
            if (tickCount == 1 || tickCount % 120 == 0) {
                conveyorBeltPlants.add(getRandomUnlockedPlant());
            }
        }


        if (gameStateManager.checkSpecialLevelRules(this)) return;


        plantAbilityHandler.updatePlantsAndAbilities(this);
        zombieInteractionHandler.processZombiesTick(this);

        if (lost || won || !running) return;


        if (spawner != null) {
            if (!((specialType == SpecialLevelType.PLANT_WHAT_YOU_GET && !zombieWavesStarted) || activeMiniGame instanceof Vasebreaker || activeMiniGame instanceof IZombie || activeMiniGame instanceof Beghoul)) {
                Zombie newlySpawned = spawner.update();
                if (newlySpawned != null) {
                    if (currentSeason != null && "AncientEgypt".equalsIgnoreCase(currentSeason.getName()) && spawner.isFinalWave()) {
                        int currentWave = spawner.getCurrentWave();
                        int totalWaves = spawner.getTotalWaves();
                        int defaultColumn = board.getColumns() - 1;
                        int modifiedCol = currentSeason.modifySpawnColumn(currentWave, totalWaves, defaultColumn, spawner.getZombiesSpawnedInWave(), board, newlySpawned.getY());
                        newlySpawned.setX(modifiedCol);
                    }
                    activeZombies.add(newlySpawned);
                    int cost = newlySpawned.getWaveCost();
                    gameLogMessages.add("Zombie " + newlySpawned.getName() + " spawned at wave " + spawner.getCurrentWave() + " in lane " + newlySpawned.getY() + " which costed " + cost + ".");
                }
            }
        }

        if (lost || won || !running) return;


        environmentManager.handleSunDrop(this);
        environmentManager.handleSeasonEffects(this);

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getColumns(); c++) {
                board.getTile(r, c).setZombie(null);
            }
        }
        for (Zombie zombie : activeZombies) {
            int zX = (int) Math.round(zombie.getX());
            int zY = zombie.getY();
            if (zX >= 0 && zX < board.getColumns() && zY >= 0 && zY < board.getRows()) {
                board.getTile(zY, zX).setZombie(zombie);
            }
        }

        board.updateProjectilesAndCollisions(this);

        gameStateManager.checkWaveProgress(this);
    }

    public void checkPlantDeath(Plant plant) {
        if (plant != null && !plant.isAlive()) {
            activePlants.remove(plant);
            board.getTile(plant.getY(), plant.getX()).setPlant(null);
            plantsLostCount++;
            gameLogMessages.add("Plant " + plant.getName() + " at (" + plant.getX() + ", " + plant.getY() + ") is destroyed.");

            if (plant.getName().equalsIgnoreCase("Explode-o-nut")) {
                for (Zombie z : new ArrayList<>(activeZombies)) {
                    if (Math.abs(z.getY() - plant.getY()) <= 1 && Math.abs(z.getX() - plant.getX()) <= 1.5) {
                        z.takeDamage(1800, true);
                    }
                }
                gameLogMessages.add("Explode-o-nut exploded on death in a 3x3 area!");
            }

            if (activeMiniGame instanceof Beghoul) {
                ((Beghoul) activeMiniGame).createCrater(plant.getY(), plant.getX());
            }
        }
    }

    public String applyPlantFood(Plant plant) {
        return plantAbilityHandler.applyPlantFood(plant, this);
    }

    public boolean hasZombieInRow(int row) {
        for (Zombie z : activeZombies) {
            if (!z.isHypnotized() && z.getY() == row) return true;
        }
        return false;
    }

    public Zombie getFirstZombieInRowAhead(int row, double x) {
        Zombie closest = null;
        for (Zombie z : activeZombies) {
            if (!z.isHypnotized() && z.getY() == row && z.getX() >= x) {
                if (closest == null || z.getX() < closest.getX()) {
                    closest = z;
                }
            }
        }
        return closest;
    }
    public void addGameLogMessage(String message) {
        if (message != null && !message.isEmpty()) {
            this.gameLogMessages.add(message);
        }
    }
    public Plant getPlantAt(int x, int y) {
        for (Plant p : activePlants) {
            if (p.getX() == x && p.getY() == y) return p;
        }
        return null;
    }

    public List<String> getRawLogMessagesDirectly() {
        List<String> currentMessages = new ArrayList<>(gameLogMessages);
        gameLogMessages.clear();
        return currentMessages;
    }


    public Season getCurrentSeason() { return currentSeason; }
    public void setCurrentSeason(Season currentSeason) {
        this.currentSeason = currentSeason;
        if (this.spawner != null) this.spawner.setCurrentSeason(currentSeason);
        if (currentSeason != null) currentSeason.setupEnvironment(this);
    }
    public boolean isZombieWavesStarted() { return zombieWavesStarted; }
    public void setZombieWavesStarted(boolean zombieWavesStarted) { this.zombieWavesStarted = zombieWavesStarted; }
    public void startZombieWaves() { this.zombieWavesStarted = true; }
    public List<String> getConveyorBeltPlants() { return conveyorBeltPlants; }
    public List<Plant> getSeedsToProtect() { return seedsToProtect; }
    public int getZombiesKilledInLevel() { return zombiesKilledInLevel; }
    public int getPlantsLostCount() { return plantsLostCount; }
    public void setLost(boolean lost) { this.lost = lost; }
    public void setWon(boolean won) { this.won = won; }
    public void setSunCount(int sunCount) { this.sunCount = sunCount; }
    public MiniGame getActiveMiniGame() { return activeMiniGame; }
    public void setActiveMiniGame(MiniGame activeMiniGame) { this.activeMiniGame = activeMiniGame; }
    public List<String> getGameLogMessages() {
        List<String> logCopy = new ArrayList<>(gameLogMessages);
        gameLogMessages.clear();
        return logCopy;
    }
    public void incrementZombiesKilled() { this.zombiesKilledInLevel++; }
    public void incrementPlantsLost() { this.plantsLostCount++; }
    public boolean isWon() { return won; }
    public boolean isLost() { return lost; }
    public LawnMower[] getLawnMowers() { return lawnMowers; }
    public boolean spendSun(int amount) {
        if (sunCount < amount) return false;
        sunCount -= amount;
        return true;
    }
    public void addSun(int amount) {
        sunCount += amount;
        sunsProducedInLevel += amount;
        scoreGame.onSunCollected(amount);
    }
    public boolean spendCoins(int amount) {
        if (coins < amount) return false;
        coins -= amount;
        return true;
    }
    public void addCoins(int amount) {
        coins += amount;
        scoreGame.onCoinEarned(amount);
    }
    public boolean spendDiamonds(int amount) {
        if (diamonds < amount) return false;
        diamonds -= amount;
        return true;
    }
    public void addDiamonds(int amount) {
        diamonds += amount;
        scoreGame.onDiamondEarned(amount);
    }
    public void addPlantFood() { plantFoodCount++; }
    public boolean usePlantFood() {
        if (plantFoodCount <= 0) return false;
        plantFoodCount--;
        return true;
    }
    public void addBullet(Bullet bullet) { bullets.add(bullet); }
    public void addSun(Sun sun) { suns.add(sun); }
    public void addZombie(Zombie zombie) { activeZombies.add(zombie); }
    public void addPlant(Plant plant) {
        activePlants.add(plant);
        scoreGame.onPlantPlaced(plant);
    }
    public void removePlant(Plant plant) { activePlants.remove(plant); }
    public void removeZombie(Zombie zombie) {
        activeZombies.remove(zombie);
        scoreGame.onZombieKilled(zombie, this);
    }
    public boolean isRunning() { return running; }
    public Board getBoard() { return board; }
    public Level getLevel() { return level; }
    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }
    public int getDifficultyLevel() { return difficultyLevel; }
    public void setDifficultyLevel(int difficultyLevel) { this.difficultyLevel = difficultyLevel; }
    public int getSunCount() { return sunCount; }
    public int getCoins() { return coins; }
    public int getDiamonds() { return diamonds; }
    public int getPlantFoodCount() { return plantFoodCount; }
    public Spawner getSpawner() { return spawner; }
    public void setSpawner(Spawner spawner) { this.spawner = spawner; }
    public model.score.ScoreGame getScoreGame() { return scoreGame; }
    public Greenhouse getGreenhouse() { return greenhouse; }
    public void setGreenhouse(Greenhouse greenhouse) { this.greenhouse = greenhouse; }
    public List<Bullet> getBullets() { return bullets; }
    public List<Sun> getSuns() { return suns; }
    public List<Zombie> getActiveZombies() { return activeZombies; }
    public List<Plant> getActivePlants() { return activePlants; }
    public int getTickCount() { return tickCount; }
    public int getLastSunDropTick() { return lastSunDropTick; }
    public void setLastSunDropTick(int lastSunDropTick) { this.lastSunDropTick = lastSunDropTick; }
}