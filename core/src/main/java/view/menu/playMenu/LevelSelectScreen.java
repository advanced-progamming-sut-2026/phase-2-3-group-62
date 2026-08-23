package view.menu.playMenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
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
import controller.menu.MenuController;
import main.Maini;
import model.game.ChapterType;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.menu.seedChooser.SeedChooserScreen;
import view.ui.CheatWidget;
import view.ui.PamActor;
import view.ui.Toast;
import view.ui.WalletBar;

public class LevelSelectScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private final ChapterType chapter;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgTileRegion;
    private Texture roundedBrownBgTexture;

    private Table root;
    private Table levelsGrid;
    private WalletBar walletBar;
    private boolean isTransitioning = false;

    public LevelSelectScreen(Maini game, MenuController controller, Skin skin, ChapterType chapter) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
        this.chapter = chapter != null ? chapter : ChapterType.ANCIENT_EGYPT;
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgTileRegion = game.getTextureBank().region(chapter.getBgRegionName());
        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(1220, 320, 24, new Color(0.18f, 0.11f, 0.06f, 0.96f));

        buildUI();
    }

    private void changeScreenWithTransition(Screen nextScreen) {
        if (isTransitioning) return;
        isTransitioning = true;
        stage.getRoot().setTouchable(Touchable.disabled);

        if (root != null) {
            root.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.scaleTo(0.96f, 0.96f, 0.16f, Interpolation.sineIn),
                    Actions.fadeOut(0.15f, Interpolation.fade)
                ),
                Actions.run(() -> {
                    game.setScreen(nextScreen);
                    dispose();
                })
            ));
        }
    }

    private Texture createRoundedRectangleTexture(int width, int height, int radius, Color color) {
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

    private void attachHoverEffect(Actor actor, float targetScale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(targetScale, targetScale, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }

    private Table createLevelNode(int levelNum, boolean isUnlocked, boolean isCompleted) {
        Table nodeTable = new Table();
        nodeTable.setTransform(true);
        nodeTable.setTouchable(Touchable.enabled);

        boolean isBoss = (levelNum == 4);

        PamActor pamActor = new PamActor(
            pamPlayer,
            chapter.getNodePamPath(),
            null,
            chapter.getNodePamScale(),
            chapter.getNodeOffsetX(),
            chapter.getNodeOffsetY()
        );
        pamActor.setSize(120, 120);
        pamActor.setTouchable(Touchable.disabled);

        if (!isUnlocked) {
            pamActor.setColor(0.4f, 0.4f, 0.4f, 0.75f);
        } else if (isBoss) {
            pamActor.setColor(1f, 0.6f, 0.6f, 1f);
        }

        Table badge = new Table();
        badge.setTouchable(Touchable.disabled);
        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        if (badgeRegion != null) {
            badge.setBackground(new TextureRegionDrawable(badgeRegion));
        }

        String labelText = isBoss ? "Boss" : ("Day " + levelNum);
        if (!isUnlocked) {
            labelText += " [Locked]";
        } else if (isCompleted) {
            labelText += " [Cleared]";
        }

        Label levelLabel = new Label(labelText, skin, "big");
        levelLabel.setFontScale(0.72f);
        levelLabel.setTouchable(Touchable.disabled);

        if (!isUnlocked) {
            levelLabel.setColor(Color.GRAY);
        } else if (isCompleted) {
            levelLabel.setColor(Color.GREEN);
        } else if (isBoss) {
            levelLabel.setColor(Color.MAGENTA);
        } else {
            levelLabel.setColor(new Color(0.9f, 0.95f, 1f, 1f));
        }

        badge.add(levelLabel).padLeft(12).padRight(12).center();

        nodeTable.add(pamActor).size(120, 120).center().row();
        nodeTable.add(badge).height(40).width(170).padTop(10).center();
        nodeTable.pack();
        nodeTable.setOrigin(Align.center);

        nodeTable.addListener(new InputListener() {
            private float defaultY = -1;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    if (fromActor != null && (fromActor == nodeTable || nodeTable.isAscendantOf(fromActor))) {
                        return;
                    }
                    if (defaultY == -1) {
                        defaultY = nodeTable.getY();
                    }
                    if (isUnlocked) {
                        pamActor.setHovered(true);
                        levelLabel.setColor(Color.YELLOW);
                        nodeTable.clearActions();
                        nodeTable.addAction(Actions.parallel(
                            Actions.scaleTo(1.08f, 1.08f, 0.1f),
                            Actions.moveTo(nodeTable.getX(), defaultY + 10f, 0.1f)
                        ));
                    }
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    if (toActor != null && (toActor == nodeTable || nodeTable.isAscendantOf(toActor))) {
                        return;
                    }
                    if (defaultY != -1 && isUnlocked) {
                        pamActor.setHovered(false);
                        levelLabel.setColor(isCompleted ? Color.GREEN : (isBoss ? Color.MAGENTA : new Color(0.9f, 0.95f, 1f, 1f)));
                        nodeTable.clearActions();
                        nodeTable.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.1f),
                            Actions.moveTo(nodeTable.getX(), defaultY, 0.1f)
                        ));
                    }
                }
            }
        });

        nodeTable.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (!isUnlocked) {
                    AudioManager.getInstance().playButtonClick();
                    Toast.show(stage, skin, "This level is locked! Complete previous levels first.", true);
                    return;
                }
                AudioManager.getInstance().playButtonClick();
                launchGameLevel(levelNum);
            }
        });

        return nodeTable;
    }

    private void launchGameLevel(int levelNum) {
        changeScreenWithTransition(new SeedChooserScreen(game, controller, skin, chapter, levelNum));
    }

    private void rebuildLevelsGrid() {
        if (levelsGrid == null) return;
        levelsGrid.clear();

        User user = UserSession.getCurrentUser();
        int userSeason = user != null ? user.getLastSeasonCompleted() : 0;
        int userLevel = user != null ? user.getLastLevelCompleted() : 0;

        int levelsCount = 4;

        for (int i = 1; i <= levelsCount; i++) {
            boolean isUnlocked = false;
            boolean isCompleted = false;

            if (chapter.getSeasonIndex() < userSeason) {
                isUnlocked = true;
                isCompleted = true;
            } else if (chapter.getSeasonIndex() == userSeason) {
                if (i <= userLevel) {
                    isUnlocked = true;
                    isCompleted = true;
                } else if (i == userLevel + 1) {
                    isUnlocked = true;
                    isCompleted = false;
                }
            } else if (chapter.getSeasonIndex() == userSeason + 1 && userLevel >= 4 && i == 1) {
                isUnlocked = true;
                isCompleted = false;
            } else if (chapter.getSeasonIndex() == 1 && userSeason == 0 && userLevel == 0 && i == 1) {
                isUnlocked = true;
                isCompleted = false;
            }

            Table node = createLevelNode(i, isUnlocked, isCompleted);
            levelsGrid.add(node).width(190).padLeft(20).padRight(20);
        }
    }

    private void buildUI() {
        root = new Table();
        root.setFillParent(true);
        root.top();
        root.setTransform(true);
        root.setOrigin(Align.center);
        root.setScale(0.96f);
        root.setColor(1f, 1f, 1f, 0f);
        stage.addActor(root);

        Table topRow = new Table();
        topRow.left();

        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                changeScreenWithTransition(new PlayScreen(game, controller, skin));
            }
        });
        topRow.add(backBtn).size(100, 42).left();

        CheatWidget cheatWidget = new CheatWidget(skin, stage, CheatWidget.Context.MENU, null, () -> {
            if (walletBar != null) walletBar.updateBalances();
            rebuildLevelsGrid();
        });
        topRow.add(cheatWidget).padLeft(20);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).expandX().right();
        root.add(topRow).fillX().pad(15).row();

        Label title = new Label(chapter.getTitle() + " - Levels", skin, "big");
        title.setFontScale(1.3f);
        root.add(title).padTop(0).padBottom(15).center().row();

        Table centerWrapper = new Table();
        centerWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        levelsGrid = new Table();
        levelsGrid.center().pad(30, 20, 30, 20);
        rebuildLevelsGrid();

        boxStack.add(levelsGrid);
        centerWrapper.add(boxStack).size(1220, 320);
        root.add(centerWrapper).expand().center().padBottom(30).row();

        root.addAction(Actions.parallel(
            Actions.scaleTo(1f, 1f, 0.2f, Interpolation.sineOut),
            Actions.fadeIn(0.18f, Interpolation.fade)
        ));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.12f, 0.14f, 0.18f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        if (bgTileRegion != null) {
            float tileW = bgTileRegion.getRegionWidth();
            float tileH = bgTileRegion.getRegionHeight();
            if (tileW <= 0) tileW = 128;
            if (tileH <= 0) tileH = 128;

            float screenW = game.getViewport().getWorldWidth();
            float screenH = game.getViewport().getWorldHeight();

            for (float x = 0; x < screenW; x += tileW) {
                for (float y = 0; y < screenH; y += tileH) {
                    batch.draw(bgTileRegion, x, y, tileW, tileH);
                }
            }
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
        if (roundedBrownBgTexture != null) roundedBrownBgTexture.dispose();
        if (stage != null) stage.dispose();
    }
}
