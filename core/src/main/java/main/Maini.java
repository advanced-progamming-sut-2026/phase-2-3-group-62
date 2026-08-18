package main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import controller.menu.MenuController;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;
import util.FileManager;
import view.menu.LoginScreen;
import view.menu.MainMenuScreen;

public class Maini extends Game {
    private SpriteBatch batch;
    private Viewport viewport;
    private Skin skin;
    private TextureBank textureBank;
    private MenuController menuController;

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ScreenViewport();

        skin = PvzSkin.get();
        textureBank = new TextureBank("768", Gdx.files.internal("assets"));

        menuController = new MenuController();

        Settings settings = FileManager.loadSettings();
        if (settings != null && settings.getAutoLoginUsername() != null) {
            User autoUser = FileManager.getUser(settings.getAutoLoginUsername());
            if (autoUser != null) {
                UserSession.setCurrentUser(autoUser);
                setScreen(new MainMenuScreen(this));
                return;
            }
        }

        setScreen(new LoginScreen(this, menuController, skin));
    }

    public SpriteBatch getBatch() { return batch; }
    public Viewport getViewport() { return viewport; }
    public Skin getSkin() { return skin; }
    public TextureBank getTextureBank() { return textureBank; }
    public MenuController getMenuController() { return menuController; }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (skin != null) skin.dispose();
        if (getScreen() != null) getScreen().dispose();
    }
}
