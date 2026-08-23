package view.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.game.GameController;
import main.Maini;
import model.Game;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;
import model.enums.SpecialLevelType;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import model.minigame.MiniGame;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.minigame.Zombotany;
import model.season.Season;
import view.audio.AudioManager;
import view.game.mainGame.GamePlayScreen;
import view.ui.CheatWidget;

public class GamePlayTopHud {
    private final GamePlayScreen screen;
    private final Stage stage;
    private final Skin skin;
    private final GameController gameController;

    private final Table topHudTable = new Table();
    private Table sunBadge;
    private Table pfBadge;
    private ImageButton shovelBtn;
    private Label sunCountLabel;
    private Label plantFoodLabel;
    private Label missionTitleLabel;
    private Label missionDetailLabel;

    public GamePlayTopHud(GamePlayScreen screen, Stage stage, Skin skin, Maini game, GameController gameController, Runnable onStateUpdate) {
        this.screen = screen;
        this.stage = stage;
        this.skin = skin;
        this.gameController = gameController;

        buildUI(game, onStateUpdate);
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

    private void buildUI(Maini game, Runnable onStateUpdate) {
        topHudTable.left();

        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        TextureRegion sunIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SUN");
        TextureRegion plantFoodIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON_DOWN");
        TextureRegion pauseBtnRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_PAUSE_BUTTON");
        TextureRegion shovelBtnRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON_DOWN");

        sunBadge = new Table();
        sunBadge.setTouchable(Touchable.disabled);
        if (badgeRegion != null) sunBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        if (sunIconRegion != null) {
            Image sunIcon = new Image(sunIconRegion);
            sunIcon.setScaling(Scaling.fit);
            sunBadge.add(sunIcon).size(52, 52).padLeft(8).padRight(6);
        }
        int initialSun = gameController.getGame() != null ? gameController.getGame().getSunCount() : 50;
        sunCountLabel = new Label(String.valueOf(initialSun), skin, "big_outline");
        sunCountLabel.setFontScale(1.1f);
        sunCountLabel.setColor(Color.YELLOW);
        sunBadge.add(sunCountLabel).padRight(16);

        boolean isNoSunMode = gameController.getGame() != null &&
            (gameController.getGame().getActiveMiniGame() instanceof Vasebreaker ||
                gameController.getGame().getActiveMiniGame() instanceof WallnutBowling);
        sunBadge.setVisible(!isNoSunMode);
        topHudTable.add(sunBadge).height(64).padRight(18);

        pfBadge = new Table();
        pfBadge.setTouchable(Touchable.enabled);
        if (badgeRegion != null) pfBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        if (plantFoodIconRegion != null) {
            Image pfIcon = new Image(plantFoodIconRegion);
            pfIcon.setScaling(Scaling.fit);
            pfBadge.add(pfIcon).size(48, 48).padLeft(8).padRight(6);
        }
        int initialPf = gameController.getGame() != null ? gameController.getGame().getPlantFoodCount() : 0;
        plantFoodLabel = new Label(initialPf + "/3", skin, "big_outline");
        plantFoodLabel.setFontScale(1.05f);
        plantFoodLabel.setColor(Color.GREEN);
        pfBadge.add(plantFoodLabel).padRight(16);
        attachHoverEffect(pfBadge, 1.08f);
        pfBadge.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (screen.isPaused()) return;
                AudioManager.getInstance().playButtonClick();
                Game mg = gameController.getGame();
                if (mg != null && mg.getPlantFoodCount() > 0) {
                    screen.setToolMode(screen.getCurrentToolMode() == GamePlayScreen.ToolMode.PLANT_FOOD ? GamePlayScreen.ToolMode.NONE : GamePlayScreen.ToolMode.PLANT_FOOD);
                } else {
                    screen.enqueueLog("No Plant Food available!", true);
                }
            }
        });

        boolean isIZombie = gameController.getGame() != null && gameController.getGame().getActiveMiniGame() instanceof IZombie;
        pfBadge.setVisible(!isIZombie);
        topHudTable.add(pfBadge).height(64);

        if (shovelBtnRegion != null) {
            ImageButton.ImageButtonStyle shStyle = new ImageButton.ImageButtonStyle();
            shStyle.imageUp = new TextureRegionDrawable(shovelBtnRegion);
            shovelBtn = new ImageButton(shStyle);
            attachHoverEffect(shovelBtn, 1.1f);
            shovelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (screen.isPaused()) return;
                    AudioManager.getInstance().playButtonClick();
                    screen.setToolMode(screen.getCurrentToolMode() == GamePlayScreen.ToolMode.SHOVEL ? GamePlayScreen.ToolMode.NONE : GamePlayScreen.ToolMode.SHOVEL);
                }
            });
            shovelBtn.setVisible(!isIZombie);
            topHudTable.add(shovelBtn).size(68, 68).padLeft(18);
        }

        CheatWidget inGameCheatWidget = new CheatWidget(skin, stage, CheatWidget.Context.INGAME, gameController, onStateUpdate);
        topHudTable.add(inGameCheatWidget).padLeft(20);

        Table rightHudGroup = new Table();
        rightHudGroup.right();

        Table missionBox = new Table();
        if (badgeRegion != null) missionBox.setBackground(new TextureRegionDrawable(badgeRegion));
        missionTitleLabel = new Label("", skin, "medium_outline");
        missionTitleLabel.setFontScale(1.25f);
        missionTitleLabel.setColor(Color.YELLOW);
        missionTitleLabel.setAlignment(Align.center);

        missionDetailLabel = new Label("", skin, "big_outline");
        missionDetailLabel.setFontScale(1.15f);
        missionDetailLabel.setColor(Color.WHITE);
        missionDetailLabel.setAlignment(Align.center);

        missionBox.add(missionTitleLabel).padLeft(24).padRight(24).padTop(8).center().row();
        missionBox.add(missionDetailLabel).padLeft(24).padRight(24).padTop(4).padBottom(10).center();
        rightHudGroup.add(missionBox).minWidth(480).minHeight(84).padRight(24);

        if (pauseBtnRegion != null) {
            ImageButton.ImageButtonStyle pStyle = new ImageButton.ImageButtonStyle();
            pStyle.imageUp = new TextureRegionDrawable(pauseBtnRegion);
            ImageButton pauseBtn = new ImageButton(pStyle);
            attachHoverEffect(pauseBtn, 1.12f);
            pauseBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    screen.togglePause();
                }
            });
            rightHudGroup.add(pauseBtn).size(76, 76).padRight(20);
        }

        topHudTable.add(rightHudGroup).expandX().right();
    }

    public void update() {
        Game modelGame = gameController.getGame();
        if (modelGame == null) return;

        boolean isNoSunMode = modelGame.getActiveMiniGame() instanceof Vasebreaker ||
            modelGame.getActiveMiniGame() instanceof WallnutBowling;
        if (sunBadge != null) sunBadge.setVisible(!isNoSunMode);

        boolean isIZombie = modelGame.getActiveMiniGame() instanceof IZombie;
        if (pfBadge != null) pfBadge.setVisible(!isIZombie);
        if (shovelBtn != null) shovelBtn.setVisible(!isIZombie);

        if (sunCountLabel != null && !isNoSunMode) {
            sunCountLabel.setText(String.valueOf(modelGame.getSunCount()));
        }

        if (plantFoodLabel != null) {
            plantFoodLabel.setText(modelGame.getPlantFoodCount() + "/3");
        }

        if (missionTitleLabel != null && missionDetailLabel != null) {
            Zomboss activeBoss = null;
            if (modelGame.getActiveZombies() != null) {
                for (Zombie z : modelGame.getActiveZombies()) {
                    if (z instanceof Zomboss) {
                        activeBoss = (Zomboss) z;
                        break;
                    }
                }
            }

            if (modelGame.getLevel() != null && modelGame.getLevel().getNumber() == 4) {
                Season season = modelGame.getCurrentSeason();
                String seasonName = season != null ? season.getName().toUpperCase() : "CHAPTER";
                missionTitleLabel.setText(seasonName + " - LEVEL 4 (BOSS FIGHT)");

                if (activeBoss != null) {
                    missionDetailLabel.setText(activeBoss.getName().toUpperCase() + " [PHASE " + activeBoss.getCurrentPhase() + "/3]");
                } else {
                    missionDetailLabel.setText("DEFEAT DR. ZOMBOSS TO WIN!");
                }
            } else if (modelGame.getActiveMiniGame() != null) {
                MiniGame mg = modelGame.getActiveMiniGame();
                if (mg instanceof Beghoul) {
                    Beghoul bg = (Beghoul) mg;
                    int remainingMatches = Math.max(0, bg.getTargetMatches() - bg.getMatchesFormed());
                    missionTitleLabel.setText("MINI-GAME: BEGHOULED (STAGE " + bg.getStageLevel() + "/3)");
                    missionDetailLabel.setText("Matches Formed: " + bg.getMatchesFormed() + "/" + bg.getTargetMatches() + " (Remaining: " + remainingMatches + ")");
                } else if (mg instanceof Zombotany) {
                    missionTitleLabel.setText("MINI-GAME: ZOMBOTANY");
                    missionDetailLabel.setText("Defeat all hybrid plant-zombies!");
                } else {
                    missionTitleLabel.setText("MINI-GAME: " + mg.getName().toUpperCase());
                    missionDetailLabel.setText("Complete mini-game objectives!");
                }
            } else {
                SpecialLevelType type = modelGame.getLevel() != null ? modelGame.getLevel().getSpecialLevelType() : SpecialLevelType.NONE;
                Season season = modelGame.getCurrentSeason();
                String seasonName = season != null ? season.getName().toUpperCase() : "ADVENTURE";
                int lvlNum = modelGame.getLevel() != null ? modelGame.getLevel().getNumber() : 1;

                if (type == SpecialLevelType.TIMED_WAR) {
                    missionTitleLabel.setText(seasonName + " - LEVEL " + lvlNum + " [TIMED WAR]");
                    int remainingTicks = Math.max(0, modelGame.getLevel().getTimeLimitTicks() - modelGame.getTickCount());
                    int remainingSec = remainingTicks / 10;
                    missionDetailLabel.setText("Time: " + remainingSec + "s | Kills: "
                        + modelGame.getZombiesKilledInLevel() + "/" + modelGame.getLevel().getTargetZombiesToKill()
                        + " | Sun: " + modelGame.getSunCount() + "/" + modelGame.getLevel().getTargetSunsToProduce());
                } else if (type == SpecialLevelType.DEAD_LINE) {
                    missionTitleLabel.setText(seasonName + " - LEVEL " + lvlNum + " [DEADLINE MODE]");
                    missionDetailLabel.setText("Defend line at Col " + modelGame.getLevel().getDeadlineColumn());
                } else if (type == SpecialLevelType.SAVE_OUR_SEEDS) {
                    missionTitleLabel.setText(seasonName + " - LEVEL " + lvlNum + " [SAVE OUR SEEDS]");
                    missionDetailLabel.setText("Protect endangered plants!");
                } else if (type == SpecialLevelType.NIGHT_OPS) {
                    missionTitleLabel.setText(seasonName + " - LEVEL " + lvlNum + " [NIGHT OPS]");
                    missionDetailLabel.setText("No natural sun falls from sky.");
                } else if (type == SpecialLevelType.CONVEYOR_BELT) {
                    missionTitleLabel.setText(seasonName + " - LEVEL " + lvlNum + " [CONVEYOR BELT]");
                    missionDetailLabel.setText("Plant incoming seeds from belt!");
                } else {
                    missionTitleLabel.setText(seasonName + " - LEVEL " + lvlNum);
                    missionDetailLabel.setText("Defend your brains!");
                }
            }
        }
    }

    public Table getRoot() {
        return topHudTable;
    }
}
