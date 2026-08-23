package view.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import controller.game.GameController;
import main.Maini;
import model.Game;
import model.entities.ZombieType;
import model.entities.plant.Plant;
import model.entities.plant.factory.PlantFactory;
import model.entities.plant.loader.PlantLoader;
import model.minigame.IZombie;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.game.mainGame.GameGrid;
import view.game.mainGame.GamePlayScreen;
import view.ui.PamActor;
import view.ui.PlantSeedCard;
import view.ui.ZombieSeedCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePlaySeedBank {
    private final GamePlayScreen screen;
    private final Stage stage;
    private final Maini game;
    private final PamPlayer pamPlayer;
    private final GameController gameController;

    private final Table seedBankTable = new Table();
    private final List<PlantSeedCard> seedCardWidgets = new ArrayList<>();
    private final List<ZombieSeedCard> zombieCardWidgets = new ArrayList<>();
    private final Map<String, Float> cooldownTimers = new HashMap<>();

    private Texture seedBankBgTexture;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion badgeRegion;
    private PamActor cursorGhostActor = null;

    public GamePlaySeedBank(GamePlayScreen screen, Stage stage, Maini game, PamPlayer pamPlayer, GameController gameController) {
        this.screen = screen;
        this.stage = stage;
        this.game = game;
        this.pamPlayer = pamPlayer;
        this.gameController = gameController;

        initTextures();
        rebuildSeedBank();
    }

    private void attachHoverEffect(Actor actor, float targetScale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(targetScale, targetScale, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }

    private void initTextures() {
        plantCardFaceRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_FACE");
        badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        seedBankBgTexture = createSolidTexture(new Color(0.12f, 0.08f, 0.05f, 0.88f));
    }

    public void rebuildSeedBank() {
        seedBankTable.clear();
        seedCardWidgets.clear();
        zombieCardWidgets.clear();
        seedBankTable.top().left();

        Game modelGame = gameController.getGame();
        if (modelGame != null && modelGame.getActiveMiniGame() instanceof IZombie) {
            IZombie iz = (IZombie) modelGame.getActiveMiniGame();
            int cardIndex = 0;
            for (String zName : iz.getAvailableZombieTypes()) {
                int cost = iz.getZombieCost(zName);
                ZombieSeedCard zCard = new ZombieSeedCard(game, zName, cost, pamPlayer, plantCardFaceRegion, badgeRegion, game.getSkin());
                zombieCardWidgets.add(zCard);
                attachHoverEffect(zCard, 1.06f);

                zCard.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (screen.isPaused()) return;
                        AudioManager.getInstance().playButtonClick();

                        if (cooldownTimers.getOrDefault(zName, 0f) > 0f) {
                            screen.enqueueLog("Zombie is on cooldown!", true);
                            return;
                        }
                        if (modelGame.getSunCount() < cost) {
                            screen.enqueueLog("Not enough suns to deploy " + zName + "!", true);
                            return;
                        }

                        Plant fakePlant = new Plant(9999, zName, "ZOMBIE", null, cost, 200, 20, 0, 0, null, 0, null, 0);
                        if (screen.getSelectedPlantToPlant() != null && screen.getSelectedPlantToPlant().getName().equalsIgnoreCase(zName) && screen.getCurrentToolMode() == GamePlayScreen.ToolMode.PLANTING) {
                            screen.setToolMode(GamePlayScreen.ToolMode.NONE);
                            return;
                        }

                        screen.setSelectedPlantToPlant(fakePlant);
                        screen.setToolMode(GamePlayScreen.ToolMode.PLANTING);

                        for (ZombieSeedCard c : zombieCardWidgets) c.setSelected(c == zCard);

                        if (cursorGhostActor != null) cursorGhostActor.remove();
                        ZombieType zType = ZombieType.fromId(zName);
                        cursorGhostActor = new PamActor(pamPlayer, zType.getPamPath(), "anim_idle", 0.26f, zType.getOffsetX(), zType.getOffsetY());
                        cursorGhostActor.getColor().a = 0.55f;
                        cursorGhostActor.setTouchable(Touchable.disabled);
                        cursorGhostActor.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                        stage.addActor(cursorGhostActor);
                    }
                });

                seedBankTable.add(zCard).size(110, 138).pad(2);
                cardIndex++;
                if (cardIndex % 2 == 0) seedBankTable.row();
            }
            return;
        }

        User user = UserSession.getCurrentUser();
        List<Plant> allPlants = PlantLoader.loadPlants();
        boolean isFreePlantingMode = modelGame != null &&
            (modelGame.getActiveMiniGame() instanceof Vasebreaker ||
                modelGame.getActiveMiniGame() instanceof WallnutBowling);

        int cardIndex = 0;
        for (String plantName : screen.getSelectedPlants()) {
            Plant plant = null;
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(plantName)) {
                    plant = p;
                    break;
                }
            }
            if (plant == null) plant = PlantFactory.createPlant(plantName);

            if (plant != null) {
                int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
                boolean boosted = user != null && user.getGreenhouseBoosts() != null && user.getGreenhouseBoosts().getOrDefault(plant.getName(), false);

                PlantSeedCard card = new PlantSeedCard(game, plant, currentLevel, boosted, pamPlayer, plantCardFaceRegion, badgeRegion, game.getSkin());
                seedCardWidgets.add(card);
                attachHoverEffect(card, 1.06f);

                final Plant finalPlant = plant;
                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (screen.isPaused()) return;
                        AudioManager.getInstance().playButtonClick();
                        Game mg = gameController.getGame();
                        if (mg != null && !isFreePlantingMode && mg.getSunCount() < finalPlant.getCost()) {
                            screen.enqueueLog("Not enough sun!", true);
                            return;
                        }
                        if (!isFreePlantingMode && cooldownTimers.getOrDefault(finalPlant.getName(), 0f) > 0f) {
                            screen.enqueueLog("Plant is on cooldown!", true);
                            return;
                        }

                        if (screen.getSelectedPlantToPlant() == finalPlant && screen.getCurrentToolMode() == GamePlayScreen.ToolMode.PLANTING) {
                            screen.setToolMode(GamePlayScreen.ToolMode.NONE);
                            return;
                        }

                        screen.setSelectedPlantToPlant(finalPlant);
                        screen.setToolMode(GamePlayScreen.ToolMode.PLANTING);

                        for (PlantSeedCard c : seedCardWidgets) c.setSelected(c == card);

                        if (cursorGhostActor != null) cursorGhostActor.remove();
                        cursorGhostActor = new PamActor(pamPlayer, finalPlant.getPamPath(), "anim_idle", 0.32f);
                        cursorGhostActor.getColor().a = 0.55f;
                        cursorGhostActor.setTouchable(Touchable.disabled);
                        cursorGhostActor.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                        stage.addActor(cursorGhostActor);
                    }
                });

                seedBankTable.add(card).size(110, 138).pad(2);
                cardIndex++;
                if (cardIndex % 2 == 0) seedBankTable.row();
            }
        }
    }

    public void updateCooldowns(float delta, float speedMultiplier) {
        if (gameController != null && gameController.isCooldownCheatActive()) {
            cooldownTimers.clear();
        }

        for (String name : new ArrayList<>(cooldownTimers.keySet())) {
            float cd = cooldownTimers.get(name) - (delta * speedMultiplier);
            if (cd <= 0f) cooldownTimers.remove(name);
            else cooldownTimers.put(name, cd);
        }

        Game mg = gameController.getGame();
        if (mg != null && mg.getActiveMiniGame() instanceof IZombie) {
            int curSun = mg.getSunCount();
            for (ZombieSeedCard zCard : zombieCardWidgets) {
                float cd = cooldownTimers.getOrDefault(zCard.getZombieName(), 0f);
                zCard.updateCooldownState(cd, curSun);
            }
            return;
        }

        boolean isFreePlantingMode = mg != null &&
            (mg.getActiveMiniGame() instanceof Vasebreaker ||
                mg.getActiveMiniGame() instanceof WallnutBowling);
        int curSun = isFreePlantingMode ? 999999 : (mg != null ? mg.getSunCount() : 0);
        for (PlantSeedCard card : seedCardWidgets) {
            float cd = isFreePlantingMode ? 0f : cooldownTimers.getOrDefault(card.getPlant().getName(), 0f);
            card.updateCooldownState(cd, curSun);
        }
    }

    public void putCooldown(String name, float duration) {
        if (gameController != null && gameController.isCooldownCheatActive()) return;
        cooldownTimers.put(name, duration);
    }

    public void clearSelection() {
        if (cursorGhostActor != null) {
            cursorGhostActor.remove();
            cursorGhostActor = null;
        }
        for (PlantSeedCard card : seedCardWidgets) card.setSelected(false);
        for (ZombieSeedCard card : zombieCardWidgets) card.setSelected(false);
    }

    public void updateGhostPosition(float mouseX, float mouseY) {
        if (cursorGhostActor != null) {
            cursorGhostActor.setPosition(mouseX - (GameGrid.TILE_WIDTH / 2f), mouseY - (GameGrid.TILE_HEIGHT / 2f));
            cursorGhostActor.toFront();
        }
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public Table getRoot() {
        Table wrapper = new Table();
        if (seedBankBgTexture != null) wrapper.setBackground(new TextureRegionDrawable(seedBankBgTexture));
        wrapper.add(seedBankTable).pad(6);
        return wrapper;
    }

    public void dispose() {
        if (seedBankBgTexture != null) seedBankBgTexture.dispose();
    }
}
