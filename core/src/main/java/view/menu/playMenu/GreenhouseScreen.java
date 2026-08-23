package view.menu.playMenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.GreenhouseController;
import controller.menu.MenuController;
import main.Maini;
import model.greenhouse.Greenhouse;
import model.greenhouse.Pot;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.ui.PamActor;
import view.ui.Toast;
import view.ui.WalletBar;

public class GreenhouseScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private TextureRegion slotRegion;
    private TextureRegion timerBgRegion;
    private TextureRegion gemBtnRegion;
    private TextureRegion storeBtnRegion;

    private Greenhouse greenhouse;
    private GreenhouseController ghController;

    private Table gridTable;

    private static final float[] ROW_PAD_TOP = { 150f, 100f, 100f };
    private static final float[] ROW_PAD_BOTTOM = { 0f, 1f, 10f };

    public GreenhouseScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("DELAYLOAD_BACKGROUND_ZEN_768_00");
        }

        slotRegion = game.getTextureBank().region("IMAGE_ZEN_GARDEN_GROWING_PLANT_SLOT_GROWING_PLANT_SLOT_184X161");
        timerBgRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_BACKGROUND_3SLICE");
        gemBtnRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_GEMSBUYBUTTON_DOWN");
        storeBtnRegion = game.getTextureBank().region("IMAGE_UI_HUD_WORLDMAP_BUTTONS_HUD_STORE_NORMAL");

        greenhouse = new Greenhouse();
        ghController = new GreenhouseController(greenhouse, null);

        buildUI();
    }

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topRow = new Table();
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.3f);
        backBtn.setOrigin(Align.center);
        backBtn.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1) {
                    backBtn.clearActions();
                    backBtn.addAction(Actions.scaleTo(1.06f, 1.06f, 0.1f));
                }
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    backBtn.clearActions();
                    backBtn.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                game.setScreen(new PlayScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(140, 58).left();

        if (storeBtnRegion != null) {
            ImageButton storeBtn = new ImageButton(new TextureRegionDrawable(storeBtnRegion));
            storeBtn.setTransform(true);
            storeBtn.setOrigin(Align.center);
            storeBtn.addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        storeBtn.clearActions();
                        storeBtn.addAction(Actions.scaleTo(1.1f, 1.1f, 0.08f));
                    }
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        storeBtn.clearActions();
                        storeBtn.addAction(Actions.scaleTo(1f, 1f, 0.08f));
                    }
                }
            });
            storeBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    AudioManager.getInstance().playButtonClick();
                    game.setScreen(new ShopScreen(game, controller, skin));
                    dispose();
                }
            });
            topRow.add(storeBtn).size(72, 72).padLeft(20).left();
        }

        WalletBar walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).expandX().right();

        root.add(topRow).fillX().pad(20, 20, 0, 20).row();

        gridTable = new Table();
        rebuildGrid();

        root.add(gridTable).expand().center().padTop(30).padBottom(20);
    }

    private void rebuildGrid() {
        gridTable.clear();

        for (int r = 0; r < 3; r++) {
            float padT = (r < ROW_PAD_TOP.length) ? ROW_PAD_TOP[r] : 20f;
            float padB = (r < ROW_PAD_BOTTOM.length) ? ROW_PAD_BOTTOM[r] : 20f;

            for (int c = 0; c < 4; c++) {
                Pot pot = greenhouse.getPot(r, c);
                if (pot != null) {
                    PotWidget widget = new PotWidget(pot, c + 1, r + 1);
                    float padR = (c == 1) ? 22f : 6f;
                    gridTable.add(widget).size(165, 140).pad(padT, 6f, padB, padR);
                }
            }
            gridTable.row();
        }
    }

    private class PotWidget extends Stack {
        private final Pot pot;
        private final int gridX;
        private final int gridY;
        private Label timerLabel;
        private Label costLabel;
        private Stack gemBtnStack;

        public PotWidget(Pot pot, int gridX, int gridY) {
            this.pot = pot;
            this.gridX = gridX;
            this.gridY = gridY;

            setTransform(true);
            setOrigin(Align.center);

            if (slotRegion != null) {
                Image potBg = new Image(slotRegion);
                potBg.setScaling(Scaling.fit);
                if (pot.isLocked()) {
                    potBg.setColor(0.45f, 0.45f, 0.45f, 1f);
                }
                add(potBg);
            }

            Table overlay = new Table();
            add(overlay);

            if (pot.isLocked()) {
                Label unlockLbl = new Label("200 Coins", skin, "big");
                unlockLbl.setFontScale(0.55f);
                unlockLbl.setColor(Color.YELLOW);
                overlay.add(unlockLbl).center();

                addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        AudioManager.getInstance().playButtonClick();
                        String result = ghController.unlockPot(gridX, gridY);
                        Toast.show(stage, skin, result, result.startsWith("Error"));
                        rebuildGrid();
                    }
                });

            } else if (pot.isEmpty()) {
                Label lbl = new Label("Tap to Plant", skin, "big");
                lbl.setFontScale(0.52f);
                lbl.setColor(Color.LIGHT_GRAY);
                overlay.add(lbl).center();

                addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        AudioManager.getInstance().playButtonClick();
                        String result = ghController.plantPot(gridX, gridY);
                        Toast.show(stage, skin, result, result.startsWith("Error"));
                        rebuildGrid();
                    }
                });

            } else if (pot.isGrowing() && !pot.isReadyToHarvest()) {
                PamActor plantAnim = new PamActor(pamPlayer, pot.getPlant().getPamPath(), "idle", 0.48f);
                plantAnim.setSize(120, 120);
                plantAnim.setTouchable(Touchable.disabled);
                overlay.add(plantAnim).size(120, 120).padTop(2).center().row();

                Table hudWrapper = new Table();

                Table timerTable = new Table();
                if (timerBgRegion != null) {
                    timerTable.setBackground(new TextureRegionDrawable(timerBgRegion));
                }
                timerLabel = new Label(formatTime(pot.getRemainingTime()), skin, "big");
                timerLabel.setFontScale(0.52f);
                timerTable.add(timerLabel).pad(3, 8, 3, 8);
                hudWrapper.add(timerTable).height(30).padRight(6);

                gemBtnStack = new Stack();
                gemBtnStack.setTransform(true);
                gemBtnStack.setOrigin(Align.center);
                gemBtnStack.setTouchable(Touchable.enabled);

                float btnH = 34f;
                float btnW = 46f;

                if (gemBtnRegion != null) {
                    float aspect = (float) gemBtnRegion.getRegionWidth() / (float) gemBtnRegion.getRegionHeight();
                    btnW = btnH * aspect;
                    Image gemBtnImg = new Image(gemBtnRegion);
                    gemBtnImg.setScaling(Scaling.fit);
                    gemBtnStack.add(gemBtnImg);
                }

                Table gemContent = new Table();
                costLabel = new Label(String.valueOf(pot.getDiamondCostToAccelerate()), skin, "big");
                costLabel.setFontScale(0.58f);
                gemContent.add(costLabel).center().padLeft(8f);
                gemBtnStack.add(gemContent);

                gemBtnStack.addListener(new InputListener() {
                    @Override
                    public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                        if (pointer == -1) {
                            gemBtnStack.clearActions();
                            gemBtnStack.addAction(Actions.scaleTo(1.15f, 1.15f, 0.08f));
                        }
                    }

                    @Override
                    public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                        if (pointer == -1) {
                            gemBtnStack.clearActions();
                            gemBtnStack.addAction(Actions.scaleTo(1f, 1f, 0.08f));
                        }
                    }
                });

                gemBtnStack.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        AudioManager.getInstance().playButtonClick();
                        String result = ghController.acceleratePot(gridX, gridY);
                        Toast.show(stage, skin, result, result.startsWith("Error"));
                        rebuildGrid();
                    }
                });

                hudWrapper.add(gemBtnStack).size(btnW, btnH);
                overlay.add(hudWrapper).padTop(4);

            } else if (pot.isReadyToHarvest()) {
                PamActor plantAnim = new PamActor(pamPlayer, pot.getPlant().getPamPath(), "idle", 0.58f);
                plantAnim.setSize(125, 125);
                plantAnim.setTouchable(Touchable.disabled);
                overlay.add(plantAnim).size(125, 125).padTop(2).center().row();

                Label readyLbl = new Label("Ready!", skin, "big");
                readyLbl.setFontScale(0.65f);
                readyLbl.setColor(Color.GREEN);
                overlay.add(readyLbl).padTop(2);

                addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        AudioManager.getInstance().playButtonClick();
                        String result = ghController.collectPot(gridX, gridY);
                        Toast.show(stage, skin, result, result.startsWith("Error"));
                        rebuildGrid();
                    }
                });
            }

            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        if (gemBtnStack != null && event.getTarget() != null && event.getTarget().isDescendantOf(gemBtnStack)) {
                            return;
                        }
                        clearActions();
                        addAction(Actions.scaleTo(1.05f, 1.05f, 0.1f));
                    }
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        clearActions();
                        addAction(Actions.scaleTo(1f, 1f, 0.1f));
                    }
                }
            });
        }

        @Override
        public void act(float delta) {
            super.act(delta);
            if (pot != null && pot.isGrowing() && !pot.isReadyToHarvest() && timerLabel != null) {
                pot.update();
                long remaining = pot.getRemainingTime();
                if (remaining <= 0) {
                    rebuildGrid();
                } else {
                    timerLabel.setText(formatTime(remaining));
                    costLabel.setText(String.valueOf(pot.getDiamondCostToAccelerate()));
                }
            }
        }
    }

    private String formatTime(long millis) {
        long s = millis / 1000;
        long h = s / 3600;
        long m = (s % 3600) / 60;
        long secs = s % 60;
        if (h > 0) {
            return String.format("%dh %dm", h, m);
        } else {
            return String.format("%dm %ds", m, secs);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.24f, 0.15f, 0.08f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        batch.setColor(Color.WHITE);
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            batch.draw(bgRegion, 0, 0, worldW, worldH);
        }
        batch.end();

        stage.act(Math.min(delta, 1 / 30f));
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        game.getViewport().update(width, height, true);
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
    }
}
