package view.ui;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import controller.game.GameController;
import main.Maini;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import util.FileManager;

public class CheatWidget extends Table {
    private final Maini game;
    private final Skin skin;
    private final Stage stage;
    private final Table dropdownTable;
    private final TextButton mainBtn;
    private boolean isOpen = false;
    private static Texture panelBgTexture;

    private final TextureRegion coinRegion;
    private final TextureRegion gemRegion;
    private final TextureRegion sunRegion;
    private final TextureRegion plantFoodRegion;

    public enum Context {
        MENU,
        INGAME
    }

    public CheatWidget(Skin skin, Stage stage, Context context, GameController gameController, Runnable onCheatExecuted) {
        this.skin = skin;
        this.stage = stage;
        this.game = (Maini) com.badlogic.gdx.Gdx.app.getApplicationListener();

        this.coinRegion = game.getTextureBank().region("IMAGE_UI_DANGERROOM_COIN_MIDSIZE");
        this.gemRegion = game.getTextureBank().region("IMAGE_UI_QUESTS_GEM_ICON");
        this.sunRegion = game.getTextureBank().region("IMAGE_UI_SEASONS_UNCOMPRESSED_PVZ2_SEASONS_UIASSET_ICON_SUN");
        this.plantFoodRegion = game.getTextureBank().region("IMAGE_UI_HUD_INGAME_PLANTFOOD_BUTTON");

        if (panelBgTexture == null) {
            Pixmap p = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            p.setColor(0.12f, 0.08f, 0.05f, 0.95f);
            p.fill();
            panelBgTexture = new Texture(p);
            p.dispose();
        }

        this.dropdownTable = new Table();
        this.dropdownTable.setVisible(false);
        this.dropdownTable.setBackground(new TextureRegionDrawable(new TextureRegion(panelBgTexture)));
        this.dropdownTable.pad(8);
        this.dropdownTable.setTouchable(Touchable.enabled);

        Settings settings = FileManager.loadSettings();
        if (settings == null || !settings.isDebugMode()) {
            setVisible(false);
            mainBtn = null;
            return;
        }

        left();

        mainBtn = new TextButton("⚡ CHEATS", skin, "purple");
        mainBtn.getLabel().setFontScale(1.0f);
        mainBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleDropdown();
            }
        });
        add(mainBtn).size(130, 48).left();

        if (context == Context.MENU) {
            buildMenuCheats(onCheatExecuted);
        } else {
            buildInGameCheats(gameController, onCheatExecuted);
        }
    }

    private void toggleDropdown() {
        isOpen = !isOpen;
        dropdownTable.setVisible(isOpen);

        if (isOpen) {
            if (dropdownTable.getStage() == null) {
                stage.addActor(dropdownTable);
            }
            Vector2 stagePos = mainBtn.localToStageCoordinates(new Vector2(0, 0));
            dropdownTable.pack();
            dropdownTable.setPosition(stagePos.x, stagePos.y - dropdownTable.getHeight() - 4);
            dropdownTable.toFront();
        }
    }

    private Button createIconButton(String text, TextureRegion iconRegion, String styleName, ClickListener listener) {
        TextButton.TextButtonStyle style = skin.has(styleName, TextButton.TextButtonStyle.class) ? skin.get(styleName, TextButton.TextButtonStyle.class) : skin.get(TextButton.TextButtonStyle.class);
        Button btn = new Button(style);

        Label lbl = new Label(text, skin, "big_outline");
        lbl.setFontScale(0.72f);
        lbl.setTouchable(Touchable.disabled);
        btn.add(lbl).padRight(6);

        if (iconRegion != null) {
            Image img = new Image(iconRegion);
            img.setScaling(Scaling.fit);
            img.setTouchable(Touchable.disabled);
            btn.add(img).size(26, 26);
        }

        if (listener != null) {
            btn.addListener(listener);
        }
        return btn;
    }

    private void buildMenuCheats(Runnable onCheatExecuted) {
        dropdownTable.clear();
        dropdownTable.left();

        TextButton unlockLevelsBtn = new TextButton("Unlock All Levels", skin, "green");
        unlockLevelsBtn.getLabel().setFontScale(0.80f);
        unlockLevelsBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                User u = UserSession.getCurrentUser();
                if (u != null) {
                    u.setLastSeasonCompleted(4);
                    u.setLastLevelCompleted(3);
                    FileManager.updateUser(u);
                    Toast.show(stage, skin, "All 3 Levels Unlocked!", false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(unlockLevelsBtn).size(165, 44).padBottom(6).left().row();

        Button add1000CoinsBtn = createIconButton("+1000", coinRegion, "green", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                User u = UserSession.getCurrentUser();
                if (u != null) {
                    u.setCoins(u.getCoins() + 1000);
                    FileManager.updateUser(u);
                    Toast.show(stage, skin, "+1000 Coins added!", false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(add1000CoinsBtn).size(165, 44).padBottom(6).left().row();

        Button add10GemsBtn = createIconButton("+10", gemRegion, "green", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                User u = UserSession.getCurrentUser();
                if (u != null) {
                    u.setGems(u.getGems() + 10);
                    FileManager.updateUser(u);
                    Toast.show(stage, skin, "+10 Gems added!", false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(add10GemsBtn).size(165, 44).padBottom(6).left().row();

        Button add50GemsBtn = createIconButton("+50", gemRegion, "green", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                User u = UserSession.getCurrentUser();
                if (u != null) {
                    u.setGems(u.getGems() + 50);
                    FileManager.updateUser(u);
                    Toast.show(stage, skin, "+50 Gems added!", false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(add50GemsBtn).size(165, 44).left();
    }

    private void buildInGameCheats(GameController gameController, Runnable onCheatExecuted) {
        dropdownTable.clear();
        dropdownTable.left();

        Button addSunBtn = createIconButton("+500", sunRegion, "green", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameController != null) {
                    String res = gameController.addCheatSuns(500);
                    Toast.show(stage, skin, res, false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(addSunBtn).size(150, 44).padBottom(6).left().row();

        Button addPfBtn = createIconButton("+1", plantFoodRegion, "green", new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameController != null) {
                    String res = gameController.executeAddPlantFoodCheat();
                    Toast.show(stage, skin, res, false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(addPfBtn).size(150, 44).padBottom(6).left().row();

        TextButton cdCheatBtn = new TextButton("No Cooldown", skin, "green");
        cdCheatBtn.getLabel().setFontScale(0.85f);
        cdCheatBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameController != null) {
                    String res = gameController.executeRemoveCooldownCheat();
                    Toast.show(stage, skin, res, false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(cdCheatBtn).size(150, 44).padBottom(6).left().row();

        TextButton nukeBtn = new TextButton("💥 NUKE", skin, "brown");
        nukeBtn.getLabel().setFontScale(0.85f);
        nukeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                if (gameController != null) {
                    String res = gameController.executeNuke();
                    Toast.show(stage, skin, res, false);
                    if (onCheatExecuted != null) onCheatExecuted.run();
                }
            }
        });
        dropdownTable.add(nukeBtn).size(150, 44).left();
    }
}
