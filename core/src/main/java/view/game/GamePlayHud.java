package view.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import controller.game.GameController;
import main.Maini;
import model.Game;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import model.enums.SpecialLevelType;
import model.entities.zombie.Spawner;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import util.FileManager;
import view.audio.AudioManager;
import view.ui.CheatWidget;
import view.ui.GameOverOverlay;
import view.ui.PamActor;
import view.ui.PlantSeedCard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GamePlayHud {
    private final GamePlayScreen screen;
    private final Stage stage;
    private final Skin skin;
    private final PamPlayer pamPlayer;
    private final GameController gameController;

    private TextureRegion badgeRegion;
    private TextureRegion sunIconRegion;
    private TextureRegion plantFoodIconRegion;
    private TextureRegion plantCardFaceRegion;
    private TextureRegion pauseBtnRegion;
    private TextureRegion shovelBtnRegion;
    private TextureRegion shovelCursorRegion;
    private TextureRegion waveFlagRegion;

    private Texture seedBankBgTexture;
    private Texture greenHighlightTexture;
    private Texture redHighlightTexture;
    private Texture dimOverlayTexture;
    private Texture pauseDialogBgTexture;
    private Texture progressTrackTexture;
    private Texture progressFillTexture;

    private Label sunCountLabel;
    private Label plantFoodLabel;
    private Label missionTitleLabel;
    private Label missionDetailLabel;
    private Image tileHighlightImage;
    private Table pauseOverlay;
    private GameOverOverlay gameOverOverlay;

    private Image progressFillImage;
    private Group flagGroup;
    private static final float PROGRESS_BAR_WIDTH = 540f;
    private static final float PROGRESS_BAR_HEIGHT = 38f;
    private static final float BORDER_PADDING = 4f;
    private static final float MAX_FILL_WIDTH = PROGRESS_BAR_WIDTH - (BORDER_PADDING * 2f);
    private static final float FILL_HEIGHT = PROGRESS_BAR_HEIGHT - (BORDER_PADDING * 2f);

    private float continuousProgressFraction = 0f;
    private float timeInCurrentWave = 0f;
    private int currentTrackedWave = 1;
    private static final float ESTIMATED_WAVE_DURATION = 25f;

    private final Table seedBankTable = new Table();
    private final List<PlantSeedCard> seedCardWidgets = new ArrayList<>();
    private final Map<String, Float> cooldownTimers = new HashMap<>();

    private PamActor cursorGhostActor = null;
    private Image cursorIconImage = null;
    private int builtFlagsTotalWaves = -1;

    public GamePlayHud(GamePlayScreen screen, Stage stage, Skin skin, PamPlayer pamPlayer, Maini game, GameController gameController) {
        this.screen = screen;
        this.stage = stage;
        this.skin = skin;
        this.pamPlayer = pamPlayer;
        this.gameController = gameController;

        initTextures(game);
        buildUI();
    }

    private void initTextures(Maini game) {
        badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        sunIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SUN");
        plantFoodIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON_DOWN");
        plantCardFaceRegion = game.getTextureBank().region("IMAGE_DANGERROOM_CARD_FACE");
        pauseBtnRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_PAUSE_BUTTON");
        shovelBtnRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SHOVEL_BUTTON_DOWN");
        shovelCursorRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SHOVEL_ICON");
        waveFlagRegion = game.getTextureBank().region("IMAGE_ZOMBIE_ZOMBIE_BIGHEAD_FLAG_ZOMBIE_BIGHEAD_FLAG_123X95");

        seedBankBgTexture = createSolidTexture(new Color(0.12f, 0.08f, 0.05f, 0.88f));
        greenHighlightTexture = createSolidTexture(new Color(0.15f, 0.9f, 0.2f, 0.38f));
        redHighlightTexture = createSolidTexture(new Color(0.95f, 0.15f, 0.15f, 0.42f));
        dimOverlayTexture = createSolidTexture(new Color(0f, 0f, 0f, 0.65f));
        pauseDialogBgTexture = createRoundedRectangleTexture(new Color(0.24f, 0.14f, 0.08f, 0.98f));
        progressTrackTexture = createProgressTrackTexture(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        progressFillTexture = createProgressFillTexture();
    }

    private Texture createProgressTrackTexture(float width, float height) {
        int w = (int) width;
        int h = (int) height;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.96f);
        pixmap.fillRectangle(0, 0, w, h);
        pixmap.setColor(0.14f, 0.14f, 0.16f, 0.95f);
        pixmap.fillRectangle((int) BORDER_PADDING, (int) BORDER_PADDING, (int) (w - BORDER_PADDING * 2), (int) (h - BORDER_PADDING * 2));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createProgressFillTexture() {
        int w = 1;
        int h = 30;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float t = (float) y / (float) h;
            float r = 0.42f + 0.35f * (1f - Math.abs(t - 0.5f) * 2f);
            float g = 0.96f;
            float b = 0.32f;
            pixmap.setColor(r, g, b, 1f);
            pixmap.drawPixel(0, y);
        }
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private void buildUI() {
        stage.clear();

        tileHighlightImage = new Image(new TextureRegionDrawable(greenHighlightTexture));
        tileHighlightImage.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
        tileHighlightImage.setVisible(false);
        tileHighlightImage.setTouchable(Touchable.disabled);
        stage.addActor(tileHighlightImage);

        Table root = new Table();
        root.setFillParent(true);
        root.top().left();
        stage.addActor(root);

        Table topHud = new Table();
        topHud.left();

        Table sunBadge = new Table();
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
        topHud.add(sunBadge).height(64).padRight(18);

        Table pfBadge = new Table();
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
        topHud.add(pfBadge).height(64);

        if (shovelBtnRegion != null) {
            ImageButton.ImageButtonStyle shStyle = new ImageButton.ImageButtonStyle();
            shStyle.imageUp = new TextureRegionDrawable(shovelBtnRegion);
            ImageButton shovelBtn = new ImageButton(shStyle);
            attachHoverEffect(shovelBtn, 1.1f);
            shovelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    if (screen.isPaused()) return;
                    AudioManager.getInstance().playButtonClick();
                    screen.setToolMode(screen.getCurrentToolMode() == GamePlayScreen.ToolMode.SHOVEL ? GamePlayScreen.ToolMode.NONE : GamePlayScreen.ToolMode.SHOVEL);
                }
            });
            topHud.add(shovelBtn).size(68, 68).padLeft(18);
        }

        CheatWidget inGameCheatWidget = new CheatWidget(skin, stage, CheatWidget.Context.INGAME, gameController, this::updateGameStateUI);
        topHud.add(inGameCheatWidget).padLeft(20);

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

        topHud.add(rightHudGroup).expandX().right();
        root.add(topHud).fillX().pad(14, 16, 0, 16).row();

        Table centerArea = new Table();
        centerArea.top().left();

        Table sideSeedBankWrapper = new Table();
        if (seedBankBgTexture != null) {
            sideSeedBankWrapper.setBackground(new TextureRegionDrawable(seedBankBgTexture));
        }
        rebuildSeedBank();
        sideSeedBankWrapper.add(seedBankTable).pad(6);
        centerArea.add(sideSeedBankWrapper).left().top().padLeft(12).padTop(8);
        root.add(centerArea).expand().fill().row();

        Table bottomHud = new Table();
        bottomHud.center();

        WidgetGroup progressCustomGroup = new WidgetGroup();
        progressCustomGroup.setSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

        Image progressBg = new Image(new TextureRegionDrawable(progressTrackTexture));
        progressBg.setBounds(0, 0, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        progressBg.setScaling(Scaling.stretch);
        progressBg.setTouchable(Touchable.disabled);
        progressCustomGroup.addActor(progressBg);

        progressFillImage = new Image(new TextureRegionDrawable(progressFillTexture));
        progressFillImage.setBounds(PROGRESS_BAR_WIDTH - BORDER_PADDING, BORDER_PADDING, 0, FILL_HEIGHT);
        progressFillImage.setTouchable(Touchable.disabled);
        progressCustomGroup.addActor(progressFillImage);

        flagGroup = new Group();
        flagGroup.setBounds(0, 0, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        flagGroup.setTouchable(Touchable.disabled);
        progressCustomGroup.addActor(flagGroup);

        bottomHud.add(progressCustomGroup).size(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT).padBottom(16);
        root.add(bottomHud).fillX().bottom().center();

        buildPauseOverlay();

        gameOverOverlay = new GameOverOverlay(skin, screen::restartLevel, screen::saveAndExit);
        stage.addActor(gameOverOverlay);
    }

    private void buildPauseOverlay() {
        pauseOverlay = new Table();
        pauseOverlay.setFillParent(true);
        pauseOverlay.setVisible(false);
        pauseOverlay.setTouchable(Touchable.enabled);

        if (dimOverlayTexture != null) {
            pauseOverlay.setBackground(new TextureRegionDrawable(dimOverlayTexture));
        }

        Stack dialogStack = new Stack();
        if (pauseDialogBgTexture != null) {
            Image dialogBg = new Image(pauseDialogBgTexture);
            dialogBg.setScaling(Scaling.stretch);
            dialogStack.add(dialogBg);
        }

        Table dialogContent = new Table();
        dialogContent.top().pad(24);

        Label title = new Label("GAME PAUSED", skin, "big_outline");
        title.setFontScale(1.35f);
        title.setColor(Color.YELLOW);
        dialogContent.add(title).padTop(8).padBottom(16).center().row();

        Settings settings = FileManager.loadSettings();
        if (settings == null) settings = new Settings();

        Table audioControls = new Table();
        audioControls.padBottom(16);

        Label mLabel = new Label("Music:", skin, "big");
        mLabel.setFontScale(0.8f);
        Slider mSlider = new Slider(0f, 1f, 0.05f, false, skin);
        mSlider.setValue(AudioManager.getInstance().getMusicVolume());
        Label mVal = new Label(String.format("%.0f%%", mSlider.getValue() * 100), skin, "big");
        mVal.setFontScale(0.75f);
        mVal.setColor(Color.CYAN);

        final Settings finalSettings = settings;
        mSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = mSlider.getValue();
                AudioManager.getInstance().setMusicVolume(val);
                finalSettings.setMusicVolume(val);
                mVal.setText(String.format("%.0f%%", val * 100));
                FileManager.saveSettings(finalSettings);
            }
        });

        audioControls.add(mLabel).width(110).left();
        audioControls.add(mSlider).width(240).padRight(12);
        audioControls.add(mVal).width(60).left().row();

        Label sLabel = new Label("SFX:", skin, "big");
        sLabel.setFontScale(0.8f);
        Slider sSlider = new Slider(0f, 1f, 0.05f, false, skin);
        sSlider.setValue(AudioManager.getInstance().getSoundVolume());
        Label sVal = new Label(String.format("%.0f%%", sSlider.getValue() * 100), skin, "big");
        sVal.setFontScale(0.75f);
        sVal.setColor(Color.CYAN);

        sSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                float val = sSlider.getValue();
                AudioManager.getInstance().setSoundVolume(val);
                finalSettings.setSfxVolume(val);
                sVal.setText(String.format("%.0f%%", val * 100));
                FileManager.saveSettings(finalSettings);
            }
        });

        audioControls.add(sLabel).width(110).left().padTop(8);
        audioControls.add(sSlider).width(240).padRight(12).padTop(8);
        audioControls.add(sVal).width(60).left().padTop(8);

        dialogContent.add(audioControls).center().row();

        TextButton resumeBtn = new TextButton("RESUME", skin, "green");
        resumeBtn.getLabel().setFontScale(1.15f);
        attachHoverEffect(resumeBtn, 1.06f);
        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                screen.togglePause();
            }
        });
        dialogContent.add(resumeBtn).size(340, 58).padBottom(12).center().row();

        TextButton restartBtn = new TextButton("RESTART", skin, "brown");
        restartBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(restartBtn, 1.06f);
        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                screen.restartLevel();
            }
        });
        dialogContent.add(restartBtn).size(340, 58).padBottom(12).center().row();

        TextButton exitBtn = new TextButton("SAVE & EXIT", skin);
        exitBtn.getLabel().setFontScale(1.1f);
        attachHoverEffect(exitBtn, 1.06f);
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                screen.saveAndExit();
            }
        });
        dialogContent.add(exitBtn).size(340, 58).center();

        dialogStack.add(dialogContent);
        pauseOverlay.add(dialogStack).size(560, 540).center();

        stage.addActor(pauseOverlay);
    }

    public void rebuildSeedBank() {
        seedBankTable.clear();
        seedCardWidgets.clear();
        seedBankTable.top().left();

        User user = UserSession.getCurrentUser();
        List<Plant> allPlants = PlantLoader.loadPlants();

        int cardIndex = 0;
        for (String plantName : screen.getSelectedPlants()) {
            Plant plant = null;
            for (Plant p : allPlants) {
                if (p.getName().equalsIgnoreCase(plantName)) {
                    plant = p;
                    break;
                }
            }

            if (plant != null) {
                int currentLevel = user != null ? user.getPlantLevels().getOrDefault(plant.getName(), 1) : 1;
                boolean boosted = user != null && user.getGreenhouseBoosts() != null && user.getGreenhouseBoosts().getOrDefault(plant.getName(), false);

                PlantSeedCard card = new PlantSeedCard(plant, currentLevel, boosted, pamPlayer, plantCardFaceRegion, badgeRegion, skin);
                seedCardWidgets.add(card);
                attachHoverEffect(card, 1.06f);

                final Plant finalPlant = plant;
                card.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (screen.isPaused() || (gameOverOverlay != null && gameOverOverlay.isShown())) return;
                        AudioManager.getInstance().playButtonClick();
                        Game modelGame = gameController.getGame();
                        if (modelGame != null && modelGame.getSunCount() < finalPlant.getCost()) {
                            screen.enqueueLog("Not enough sun!", true);
                            return;
                        }
                        if (cooldownTimers.getOrDefault(finalPlant.getName(), 0f) > 0f) {
                            screen.enqueueLog("Plant is on cooldown!", true);
                            return;
                        }

                        if (screen.getSelectedPlantToPlant() == finalPlant && screen.getCurrentToolMode() == GamePlayScreen.ToolMode.PLANTING) {
                            screen.setToolMode(GamePlayScreen.ToolMode.NONE);
                            return;
                        }

                        screen.setSelectedPlantToPlant(finalPlant);
                        screen.setToolMode(GamePlayScreen.ToolMode.PLANTING);

                        for (PlantSeedCard c : seedCardWidgets) {
                            c.setSelected(c == card);
                        }

                        if (cursorGhostActor != null) {
                            cursorGhostActor.remove();
                        }
                        cursorGhostActor = new PamActor(pamPlayer, finalPlant.getPamPath(), "anim_idle", 0.32f);
                        cursorGhostActor.getColor().a = 0.55f;
                        cursorGhostActor.setTouchable(Touchable.disabled);
                        cursorGhostActor.setSize(GameGrid.TILE_WIDTH, GameGrid.TILE_HEIGHT);
                        stage.addActor(cursorGhostActor);
                    }
                });

                seedBankTable.add(card).size(110, 138).pad(2);
                cardIndex++;
                if (cardIndex % 2 == 0) {
                    seedBankTable.row();
                }
            }
        }
    }

    public void updateCooldowns(float delta, float speedMultiplier) {
        for (String plantName : new ArrayList<>(cooldownTimers.keySet())) {
            float cd = cooldownTimers.get(plantName) - (delta * speedMultiplier);
            if (cd <= 0f) {
                cooldownTimers.remove(plantName);
            } else {
                cooldownTimers.put(plantName, cd);
            }
        }

        Game mg = gameController.getGame();
        int curSun = mg != null ? mg.getSunCount() : 0;
        for (PlantSeedCard card : seedCardWidgets) {
            float cd = cooldownTimers.getOrDefault(card.getPlant().getName(), 0f);
            card.updateCooldownState(cd, curSun);
        }

        if (mg != null && mg.getSpawner() != null) {
            Spawner spawner = mg.getSpawner();
            int totalWaves = Math.max(1, spawner.getTotalWaves());
            int currentWave = Math.max(1, spawner.getCurrentWave());

            if (currentWave != currentTrackedWave) {
                currentTrackedWave = currentWave;
                timeInCurrentWave = 0f;
            } else {
                timeInCurrentWave += (delta * speedMultiplier);
            }

            float waveLocalFraction = Math.min(1.0f, timeInCurrentWave / ESTIMATED_WAVE_DURATION);
            float targetFraction = ((currentWave - 1) + waveLocalFraction) / (float) totalWaves;
            targetFraction = Math.min(1.0f, Math.max(0f, targetFraction));

            continuousProgressFraction = continuousProgressFraction + (targetFraction - continuousProgressFraction) * Math.min(delta * 4f, 1f);

            if (progressFillImage != null) {
                float currentWidth = MAX_FILL_WIDTH * continuousProgressFraction;
                float startX = (PROGRESS_BAR_WIDTH - BORDER_PADDING) - currentWidth;
                progressFillImage.setBounds(startX, BORDER_PADDING, currentWidth, FILL_HEIGHT);
            }
        }
    }

    private void updateFlagsOnBar(int totalWaves) {
        if (totalWaves <= 0 || flagGroup == null) return;
        if (builtFlagsTotalWaves == totalWaves) return;

        flagGroup.clear();
        builtFlagsTotalWaves = totalWaves;

        for (int wave = 1; wave <= totalWaves; wave++) {
            float waveFraction = (float) wave / (float) totalWaves;
            float flagX = (PROGRESS_BAR_WIDTH - BORDER_PADDING) - (MAX_FILL_WIDTH * waveFraction) - 18f;

            if (waveFlagRegion != null) {
                Image flagImage = new Image(waveFlagRegion);
                flagImage.setSize(44f, 36f);
                flagImage.setPosition(flagX, 2f);
                flagImage.setTouchable(Touchable.disabled);
                flagGroup.addActor(flagImage);
            }
        }
    }

    public void updateGameStateUI() {
        Game modelGame = gameController.getGame();
        if (modelGame == null) return;

        if (sunCountLabel != null) {
            sunCountLabel.setText(String.valueOf(modelGame.getSunCount()));
        }

        if (plantFoodLabel != null) {
            plantFoodLabel.setText(modelGame.getPlantFoodCount() + "/3");
        }

        SpecialLevelType type = modelGame.getLevel().getSpecialLevelType();
        if (missionTitleLabel != null && missionDetailLabel != null) {
            if (type == SpecialLevelType.TIMED_WAR) {
                missionTitleLabel.setText("TIMED WAR");
                int remainingTicks = Math.max(0, modelGame.getLevel().getTimeLimitTicks() - modelGame.getTickCount());
                int remainingSec = remainingTicks / 10;
                missionDetailLabel.setText("Time: " + remainingSec + "s | Kills: "
                    + modelGame.getZombiesKilledInLevel() + "/" + modelGame.getLevel().getTargetZombiesToKill()
                    + " | Sun: " + modelGame.getSunCount() + "/" + modelGame.getLevel().getTargetSunsToProduce());
            } else if (type == SpecialLevelType.DEAD_LINE) {
                missionTitleLabel.setText("DEADLINE MODE");
                missionDetailLabel.setText("Defend line at Col " + modelGame.getLevel().getDeadlineColumn());
            } else if (type == SpecialLevelType.SAVE_OUR_SEEDS) {
                missionTitleLabel.setText("SAVE OUR SEEDS");
                missionDetailLabel.setText("Protect endangered plants!");
            } else if (type == SpecialLevelType.NIGHT_OPS) {
                missionTitleLabel.setText("NIGHT OPS");
                missionDetailLabel.setText("No natural sun falls from sky.");
            } else {
                missionTitleLabel.setText("ADVENTURE");
                missionDetailLabel.setText("Defend your brains!");
            }
        }

        Spawner spawner = modelGame.getSpawner();
        if (spawner != null) {
            int totalWaves = Math.max(1, spawner.getTotalWaves());
            updateFlagsOnBar(totalWaves);
        }
    }

    public void updateToolModeVisuals(GamePlayScreen.ToolMode mode) {
        if (mode != GamePlayScreen.ToolMode.PLANTING) {
            if (cursorGhostActor != null) {
                cursorGhostActor.remove();
                cursorGhostActor = null;
            }
            for (PlantSeedCard card : seedCardWidgets) {
                card.setSelected(false);
            }
        }

        if (cursorIconImage != null) {
            cursorIconImage.remove();
            cursorIconImage = null;
        }

        if (mode == GamePlayScreen.ToolMode.SHOVEL && shovelCursorRegion != null) {
            cursorIconImage = new Image(shovelCursorRegion);
            cursorIconImage.setSize(84, 84);
            cursorIconImage.setOrigin(42, 42);
            cursorIconImage.setTouchable(Touchable.disabled);
            stage.addActor(cursorIconImage);
        } else if (mode == GamePlayScreen.ToolMode.PLANT_FOOD && plantFoodIconRegion != null) {
            cursorIconImage = new Image(plantFoodIconRegion);
            cursorIconImage.setSize(76, 76);
            cursorIconImage.setOrigin(38, 38);
            cursorIconImage.setTouchable(Touchable.disabled);
            stage.addActor(cursorIconImage);
        }
    }

    public void setHighlight(boolean visible, float x, float y, boolean isValid) {
        if (tileHighlightImage != null) {
            tileHighlightImage.setVisible(visible);
            if (visible) {
                tileHighlightImage.setPosition(x, y);
                tileHighlightImage.setDrawable(new TextureRegionDrawable(isValid ? greenHighlightTexture : redHighlightTexture));
            }
        }
    }

    public void updateCursorPositions(float mouseX, float mouseY) {
        if (cursorGhostActor != null) {
            cursorGhostActor.setPosition(mouseX - (GameGrid.TILE_WIDTH / 2f), mouseY - (GameGrid.TILE_HEIGHT / 2f));
            cursorGhostActor.toFront();
        }
        if (cursorIconImage != null) {
            cursorIconImage.setPosition(mouseX - cursorIconImage.getWidth() / 2f, mouseY - cursorIconImage.getHeight() / 2f);
            cursorIconImage.toFront();
        }
    }

    public void putCooldown(String plantName, float duration) {
        cooldownTimers.put(plantName, duration);
    }

    public void showPauseOverlay(boolean show) {
        if (pauseOverlay != null) {
            pauseOverlay.setVisible(show);
            if (show) pauseOverlay.toFront();
        }
    }

    public GameOverOverlay getGameOverOverlay() {
        return gameOverOverlay;
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

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Texture createRoundedRectangleTexture(Color color) {
        int width = 560;
        int height = 540;
        int radius = 28;
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fillRectangle(radius, 0, width - 2 * radius, height);
        pixmap.fillRectangle(0, radius, width, height - 2 * radius);
        pixmap.fillCircle(radius, radius, radius);
        pixmap.fillCircle(width - radius - 1, radius, radius);
        pixmap.fillCircle(radius, height - radius - 1, radius);
        pixmap.fillCircle(width - radius - 1, height - radius - 1, radius);
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public void dispose() {
        if (seedBankBgTexture != null) seedBankBgTexture.dispose();
        if (greenHighlightTexture != null) greenHighlightTexture.dispose();
        if (redHighlightTexture != null) redHighlightTexture.dispose();
        if (dimOverlayTexture != null) dimOverlayTexture.dispose();
        if (pauseDialogBgTexture != null) pauseDialogBgTexture.dispose();
        if (progressTrackTexture != null) progressTrackTexture.dispose();
        if (progressFillTexture != null) progressFillTexture.dispose();
        if (gameOverOverlay != null) gameOverOverlay.dispose();
    }
}
