package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
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
import com.badlogic.gdx.utils.ScreenUtils;
import controller.game.GameController;
import controller.menu.MenuController;
import controller.menu.PreGameController;
import main.Maini;
import model.Game;
import model.minigame.*;
import model.season.AncientEgypt;
import pvz.libpvz.pam.PamPlayer;
import view.audio.AudioManager;
import view.ui.WalletBar;

import java.util.ArrayList;
import java.util.List;

public class MiniGameSelectionScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private PamPlayer pamPlayer;
    private TextureRegion bgRegion;
    private Table root;
    private boolean isTransitioning = false;

    private static class MiniGameInfo {
        String name;
        String displayName;
        String iconRegion;
        String pamPath;
        float pamScale;
        float offsetX;
        float offsetY;

        MiniGameInfo(String name, String displayName, String iconRegion, String pamPath, float pamScale, float offsetX, float offsetY) {
            this.name = name;
            this.displayName = displayName;
            this.iconRegion = iconRegion;
            this.pamPath = pamPath;
            this.pamScale = pamScale;
            this.offsetX = offsetX;
            this.offsetY = offsetY;
        }
    }

    private final MiniGameInfo[] minigames = {
        // Vasebreaker now uses the same icon as Wall-nut Bowling (IMAGE_UI_GENERIC_BUTTON_HUD_MINIGAMES_ALT_SELECTED)
        new MiniGameInfo("Vasebreaker", "Vasebreaker", "IMAGE_UI_GENERIC_BUTTON_HUD_MINIGAMES_ALT_SELECTED", "768/FULL/WORLDMAP/ZOMBOSS_NODE_LOSTCITY/ZOMBOSS_NODE_LOSTCITY.PAM", 0.26f, 0f, 0f),
        new MiniGameInfo("IZombie", "I, Zombie", "IMAGE_UI_ALMANAC_TABS_ZOMBIES_ACTIVE", "768/FULL/WORLDMAP/ZOMBOSS_NODE_DARK/ZOMBOSS_NODE_DARK.PAM", 0.26f, 0f, 0f),
        new MiniGameInfo("Beghoul", "Beghouled", "IMAGE_UI_ALMANAC_TABS_UPGRADES_ACTIVE", "768/FULL/WORLDMAP/ZOMBOSS_NODE_ICEAGE/ZOMBOSS_NODE_ICEAGE.PAM", 0.26f, 0f, 0f),
        new MiniGameInfo("Zombotany", "Zombotany", "IMAGE_UI_ALMANAC_FILTER_BUTTON_DOWN", "768/FULL/WORLDMAP/ZOMBOSS_NODE_BEACH/ZOMBOSS_NODE_BEACH.PAM", 0.26f, 0f, 0f),
        new MiniGameInfo("WallnutBowling", "Wall-nut Bowling", "IMAGE_UI_GENERIC_BUTTON_HUD_MINIGAMES_ALT_SELECTED", "768/FULL/WORLDMAP/ZOMBOSS_NODE_DINO/ZOMBOSS_NODE_DINO.PAM", 0.26f, 0f, 0f)
    };

    public MiniGameSelectionScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_STORE_GACHA_PINATA_RARE_CARD");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_UI_THYMED_EVENTS_SPRINGENING_EVENT_BG");
            if (bgRegion == null) {
                bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
            }
        }

        try {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.internal("assets"));
        } catch (Exception e) {
            pamPlayer = new PamPlayer(game.getTextureBank(), Gdx.files.absolute("assets"));
        }

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

    private Table createMiniGameCard(MiniGameInfo info) {
        Table card = new Table();
        card.setTransform(true);
        card.setTouchable(Touchable.enabled);

        // Use PAM animation for visual
        view.ui.PamActor pamActor = new view.ui.PamActor(pamPlayer, info.pamPath, null, info.pamScale, info.offsetX, info.offsetY);
        pamActor.setSize(140, 140);
        pamActor.setTouchable(Touchable.disabled);

        // Name badge
        Table nameBadge = new Table();
        nameBadge.setTouchable(Touchable.disabled);
        TextureRegion badgeRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_BUTTON_GENERIC_LTECURRENCY");
        if (badgeRegion != null) {
            nameBadge.setBackground(new TextureRegionDrawable(badgeRegion));
        }

        Label nameLabel = new Label(info.displayName, skin, "big");
        nameLabel.setFontScale(0.72f);
        nameLabel.setColor(new Color(0.9f, 0.95f, 1f, 1f));
        nameLabel.setTouchable(Touchable.disabled);
        nameBadge.add(nameLabel).padLeft(14).padRight(14).center();

        card.add(pamActor).size(140, 140).center().row();
        card.add(nameBadge).height(46).width(210).padTop(14).center();
        card.pack();
        card.setOrigin(Align.center);

        final MiniGameInfo finalInfo = info;
        card.addListener(new InputListener() {
            private float defaultY = -1;

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isTransitioning) {
                    if (fromActor != null && (fromActor == card || card.isAscendantOf(fromActor))) {
                        return;
                    }
                    if (defaultY == -1) {
                        defaultY = card.getY();
                    }
                    pamActor.setHovered(true);
                    nameLabel.setColor(Color.YELLOW);
                    card.clearActions();
                    card.addAction(Actions.parallel(
                        Actions.scaleTo(1.08f, 1.08f, 0.1f),
                        Actions.moveTo(card.getX(), defaultY + 12f, 0.1f)
                    ));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isTransitioning) {
                    if (toActor != null && (toActor == card || card.isAscendantOf(toActor))) {
                        return;
                    }
                    if (defaultY != -1) {
                        pamActor.setHovered(false);
                        nameLabel.setColor(new Color(0.9f, 0.95f, 1f, 1f));
                        card.clearActions();
                        card.addAction(Actions.parallel(
                            Actions.scaleTo(1f, 1f, 0.1f),
                            Actions.moveTo(card.getX(), defaultY, 0.1f)
                        ));
                    }
                }
            }
        });

        card.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                AudioManager.getInstance().playButtonClick();
                if (!isTransitioning) {
                    launchMiniGame(finalInfo.name);
                }
            }
        });

        return card;
    }

    private void launchMiniGame(String minigameName) {
        PreGameController.activeChapterName = minigameName + "_MG";

        int diffVal = 3;
        Game modelGame = new Game(5, 9, 1, diffVal);

        MiniGame mg = null;
        if ("Vasebreaker".equalsIgnoreCase(minigameName)) {
            mg = new Vasebreaker();
            ((Vasebreaker) mg).setupVaseGrid(5, 9, 1);
        } else if ("IZombie".equalsIgnoreCase(minigameName)) {
            mg = new IZombie();
            ((IZombie) mg).setupStage(modelGame, 1);
        } else if ("Beghoul".equalsIgnoreCase(minigameName)) {
            mg = new Beghoul();
            ((Beghoul) mg).setupStage(modelGame, 1);
        } else if ("Zombotany".equalsIgnoreCase(minigameName)) {
            mg = new Zombotany();
        } else if ("WallnutBowling".equalsIgnoreCase(minigameName)) {
            mg = new WallnutBowling();
        }

        modelGame.setActiveMiniGame(mg);
        modelGame.setCurrentSeason(new AncientEgypt());
        modelGame.start();
        modelGame.setupSpecialLevelFeatures();
        modelGame.setSunCount(modelGame.getLevel().getInitialSunAmount());

        GameController gc = new GameController(controller);
        gc.setGame(modelGame);

        if ("Zombotany".equalsIgnoreCase(minigameName)) {
            changeScreenWithTransition(new SeedChooserForMinigameScreen(game, controller, skin, gc, minigameName));
        } else {
            changeScreenWithTransition(new MiniGamePlayScreen(game, gc, minigameName, new ArrayList<>()));
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

        // Top row with back button and wallet
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

        WalletBar walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).expandX().right();
        root.add(topRow).fillX().pad(15).row();

        Label title = new Label("SELECT MINI-GAME", skin, "big");
        title.setFontScale(1.3f);
        root.add(title).padTop(0).padBottom(15).center().row();

        // Minigame cards grid - 5 cards in a row
        Table cardsRow = new Table();
        for (MiniGameInfo info : minigames) {
            Table card = createMiniGameCard(info);
            cardsRow.add(card).width(210).padLeft(25).padRight(25);
        }
        root.add(cardsRow).expand().center().padBottom(30).row();

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
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            float zoomOffset = 140f;
            batch.draw(bgRegion, -zoomOffset, -zoomOffset, worldW + (zoomOffset * 2f), worldH + (zoomOffset * 2f));
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
