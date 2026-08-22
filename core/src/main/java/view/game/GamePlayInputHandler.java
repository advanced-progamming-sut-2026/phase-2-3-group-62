package view.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import controller.game.GameController;
import main.Maini;
import model.Game;
import model.board.DroppedItem;
import model.board.Sun;
import model.board.Tile;
import model.entities.plant.Plant;
import model.enums.SpecialLevelType;
import model.handler.PlantAbilityHandler;
import model.minigame.Vasebreaker;

import java.util.ArrayList;

public class GamePlayInputHandler extends InputListener {
    private final GamePlayScreen screen;
    private final Maini game;
    private final GameController gameController;

    public GamePlayInputHandler(GamePlayScreen screen, Maini game, GameController gameController) {
        this.screen = screen;
        this.game = game;
        this.gameController = gameController;
    }

    @Override
    public boolean keyDown(InputEvent event, int keycode) {
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.P) {
            screen.togglePause();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
        if (screen.isPaused() || (screen.getHud().getGameOverOverlay() != null && screen.getHud().getGameOverOverlay().isShown())) {
            return false;
        }

        Vector2 mouseWorld = game.getViewport().unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
        Game modelGame = gameController.getGame();

        if (button == 1) {
            screen.setToolMode(GamePlayScreen.ToolMode.NONE);
            return true;
        }

        int hoveredCol = screen.getHoveredCol();
        int hoveredRow = screen.getHoveredRow();

        if (modelGame != null && modelGame.getActiveMiniGame() instanceof Vasebreaker && hoveredCol != -1 && hoveredRow != -1) {
            Vasebreaker vb = (Vasebreaker) modelGame.getActiveMiniGame();
            Tile t = modelGame.getBoard().getTile(hoveredRow, hoveredCol);

            if (t != null && t.getTemporarySeedPacket() != null) {
                String res = gameController.pickupPacket(hoveredCol, hoveredRow);
                if (res != null) {
                    screen.enqueueLog(res, res.startsWith("Error"));
                }
                return true;
            }

            if (vb.hasVase(hoveredRow, hoveredCol) && !vb.isVaseBroken(hoveredRow, hoveredCol)) {
                String res = gameController.smashVase(hoveredCol, hoveredRow);
                if (res != null) {
                    screen.enqueueLog(res, res.startsWith("Error"));
                }
                return true;
            }
        }

        if (modelGame != null && screen.getCurrentToolMode() == GamePlayScreen.ToolMode.NONE) {
            for (Sun sun : new ArrayList<>(modelGame.getSuns())) {
                Vector2 sunCenter = GameGrid.getTileCenterPosition(sun.getRow(), sun.getColumn());
                if (mouseWorld.dst(sunCenter) <= 60f) {
                    String res = gameController.collectSun(sun.getColumn(), sun.getRow());
                    if (res != null && !res.startsWith("Error")) {
                        screen.enqueueLog(res, false);
                    }
                    return true;
                }
            }

            if (modelGame.getDroppedItems() != null) {
                for (DroppedItem item : new ArrayList<>(modelGame.getDroppedItems())) {
                    Vector2 itemCenter = GameGrid.getTileCenterPosition(item.getRow(), item.getColumn());
                    if (mouseWorld.dst(itemCenter) <= 60f) {
                        modelGame.getDroppedItems().remove(item);
                        if (item.getType() == DroppedItem.ItemType.PLANT_FOOD) {
                            if (modelGame.getPlantFoodCount() < 3) {
                                modelGame.addPlantFood();
                                screen.enqueueLog("Collected Plant Food!", false);
                            } else {
                                screen.enqueueLog("Plant Food inventory is full (3/3)!", true);
                            }
                        } else if (item.getType() == DroppedItem.ItemType.COIN) {
                            modelGame.addCoins(50);
                            screen.enqueueLog("Collected 50 Coins!", false);
                        } else if (item.getType() == DroppedItem.ItemType.DIAMOND) {
                            modelGame.addDiamonds(1);
                            screen.enqueueLog("Collected 1 Diamond!", false);
                        } else if (item.getType() == DroppedItem.ItemType.POT) {
                            if (modelGame.getGreenhouse() != null) {
                                modelGame.getGreenhouse().addPot(new model.greenhouse.Pot(0, 0));
                            }
                            screen.enqueueLog("Collected a Greenhouse Pot!", false);
                        }
                        return true;
                    }
                }
            }

            for (Plant p : new ArrayList<>(modelGame.getActivePlants())) {
                if (p.isHasSunToCollect()) {
                    Vector2 pCenter = GameGrid.getTileCenterPosition(p.getY(), p.getX());
                    if (mouseWorld.dst(pCenter) <= 60f) {
                        String res = gameController.collectSun(p.getX(), p.getY());
                        if (res != null && !res.startsWith("Error")) {
                            screen.enqueueLog(res, false);
                        }
                        return true;
                    }
                }
            }
        }

        if (hoveredCol != -1 && hoveredRow != -1 && modelGame != null) {
            if (screen.getCurrentToolMode() == GamePlayScreen.ToolMode.PLANTING && screen.getSelectedPlantToPlant() != null) {
                tryPlant(screen.getSelectedPlantToPlant(), hoveredRow, hoveredCol);
                return true;
            } else if (screen.getCurrentToolMode() == GamePlayScreen.ToolMode.SHOVEL) {
                if (modelGame.getLevel().getSpecialLevelType() == SpecialLevelType.SAVE_OUR_SEEDS) {
                    for (int[] pos : modelGame.getLevel().getSeedProtectionPositions()) {
                        if (pos[0] == hoveredRow && pos[1] == hoveredCol) {
                            screen.enqueueLog("Cannot remove protected plant!", true);
                            screen.setToolMode(GamePlayScreen.ToolMode.NONE);
                            return true;
                        }
                    }
                }
                Tile t = modelGame.getBoard().getTile(hoveredRow, hoveredCol);
                if (t != null && (t.getPlant() != null || t.getPumpkinPlant() != null)) {
                    String res = gameController.pluckPlant(hoveredCol, hoveredRow);
                    if (res != null) {
                        screen.enqueueLog(res, res.startsWith("Error"));
                    }
                    screen.setToolMode(GamePlayScreen.ToolMode.NONE);
                    return true;
                }
            } else if (screen.getCurrentToolMode() == GamePlayScreen.ToolMode.PLANT_FOOD) {
                Tile t = modelGame.getBoard().getTile(hoveredRow, hoveredCol);
                if (t != null && (t.getPlant() != null || t.getPumpkinPlant() != null)) {
                    Plant p = t.getPlant() != null ? t.getPlant() : t.getPumpkinPlant();
                    if (modelGame.getPlantFoodCount() > 0) {
                        modelGame.usePlantFood();
                        PlantAbilityHandler abilityHandler = new PlantAbilityHandler();
                        String effectMsg = abilityHandler.applyPlantFood(p, modelGame);
                        screen.enqueueLog(effectMsg, false);
                        screen.setToolMode(GamePlayScreen.ToolMode.NONE);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private void tryPlant(Plant plant, int row, int col) {
        Game modelGame = gameController.getGame();
        if (modelGame == null) return;

        String res = gameController.plantPlant(plant.getName(), col, row);

        if (res != null && res.startsWith("Error")) {
            screen.enqueueLog(res, true);
        } else {
            if (res != null) {
                screen.enqueueLog(res, false);
            }
            if (!gameController.isCooldownCheatActive()) {
                screen.getHud().putCooldown(plant.getName(), (float) plant.getRecharge());
            }
            screen.setToolMode(GamePlayScreen.ToolMode.NONE);
        }
    }
}
