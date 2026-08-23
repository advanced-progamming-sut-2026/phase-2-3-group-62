package view.game.mainGame;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import controller.game.GameController;
import main.Maini;
import pvz.libpvz.pam.PamPlayer;
import view.game.hud.GamePlayProgressBar;
import view.game.hud.GamePlaySeedBank;
import view.game.hud.GamePlayTopHud;
import view.game.hud.PauseOverlayDialog;
import view.ui.GameOverOverlay;

public class GamePlayHud {
    private final GamePlayScreen screen;
    private final Stage stage;
    private final Skin skin;
    private final GameController gameController;

    private Texture greenHighlightTexture;
    private Texture redHighlightTexture;
    private TextureRegion shovelCursorRegion;
    private TextureRegion plantFoodIconRegion;

    private Image tileHighlightImage;
    private Image cursorIconImage = null;

    private GamePlayTopHud topHud;
    private GamePlayProgressBar progressBar;
    private GamePlaySeedBank seedBank;
    private PauseOverlayDialog pauseDialog;
    private GameOverOverlay gameOverOverlay;

    public GamePlayHud(GamePlayScreen screen, Stage stage, Skin skin, PamPlayer pamPlayer, Maini game, GameController gameController) {
        this.screen = screen;
        this.stage = stage;
        this.skin = skin;
        this.gameController = gameController;

        initTextures(game);
        buildUI(pamPlayer, game);
    }

    private void initTextures(Maini game) {
        greenHighlightTexture = createSolidTexture(new Color(0.15f, 0.9f, 0.2f, 0.38f));
        redHighlightTexture = createSolidTexture(new Color(0.95f, 0.15f, 0.15f, 0.42f));
        shovelCursorRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_SHOVEL_ICON");
        plantFoodIconRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON_DOWN");
    }

    private void buildUI(PamPlayer pamPlayer, Maini game) {
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

        topHud = new GamePlayTopHud(screen, stage, skin, game, gameController, this::updateGameStateUI);
        root.add(topHud.getRoot()).fillX().pad(14, 16, 0, 16).row();

        Table centerArea = new Table();
        centerArea.top().left();

        seedBank = new GamePlaySeedBank(screen, stage, game, pamPlayer, gameController);
        centerArea.add(seedBank.getRoot()).left().top().padLeft(12).padTop(8);
        root.add(centerArea).expand().fill().row();

        progressBar = new GamePlayProgressBar(game);
        root.add(progressBar.getRoot()).fillX().bottom().center();

        pauseDialog = new PauseOverlayDialog(skin, screen);
        stage.addActor(pauseDialog.getRoot());

        gameOverOverlay = new GameOverOverlay(skin, screen::restartLevel, screen::saveAndExit);
        stage.addActor(gameOverOverlay);
    }

    public void rebuildSeedBank() {
        if (seedBank != null) seedBank.rebuildSeedBank();
    }

    public void updateCooldowns(float delta, float speedMultiplier) {
        if (seedBank != null) seedBank.updateCooldowns(delta, speedMultiplier);
        if (progressBar != null && gameController != null) progressBar.update(gameController.getGame(), delta, speedMultiplier);
    }

    public void updateGameStateUI() {
        if (topHud != null) topHud.update();
    }

    public void updateToolModeVisuals(GamePlayScreen.ToolMode mode) {
        if (mode != GamePlayScreen.ToolMode.PLANTING && seedBank != null) {
            seedBank.clearSelection();
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
        if (seedBank != null) seedBank.updateGhostPosition(mouseX, mouseY);
        if (cursorIconImage != null) {
            cursorIconImage.setPosition(mouseX - cursorIconImage.getWidth() / 2f, mouseY - cursorIconImage.getHeight() / 2f);
            cursorIconImage.toFront();
        }
    }

    public void putCooldown(String name, float duration) {
        if (seedBank != null) seedBank.putCooldown(name, duration);
    }

    public void showPauseOverlay(boolean show) {
        if (pauseDialog != null) pauseDialog.setVisible(show);
    }

    public GameOverOverlay getGameOverOverlay() {
        return gameOverOverlay;
    }

    private Texture createSolidTexture(Color color) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    public void dispose() {
        if (greenHighlightTexture != null) greenHighlightTexture.dispose();
        if (redHighlightTexture != null) redHighlightTexture.dispose();
        if (seedBank != null) seedBank.dispose();
        if (progressBar != null) progressBar.dispose();
        if (pauseDialog != null) pauseDialog.dispose();
        if (gameOverOverlay != null) gameOverOverlay.dispose();
    }
}
