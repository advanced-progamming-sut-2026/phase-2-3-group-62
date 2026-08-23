package main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.gson.Gson;
import controller.menu.MenuController;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import network.Message;
import network.NetworkManager;
import pvz.libpvz.textures.TextureBank;
import pvz.skin.PvzSkin;
import util.FileManager;
import view.menu.account.LoginScreen;
import view.menu.mainMenu.MainMenuScreen;

public class Maini extends Game {
    private SpriteBatch batch;
    private Viewport viewport;
    private Skin skin;
    private TextureBank textureBank;
    private MenuController menuController;
    private final Gson gson = new Gson();

    @Override
    public void create() {
        batch = new SpriteBatch();
        viewport = new ScreenViewport();

        skin = PvzSkin.get();
        textureBank = new TextureBank("768", Gdx.files.internal("assets"));

        NetworkManager.getInstance().connect("127.0.0.1", 8080);
        menuController = new MenuController();

        Settings settings = FileManager.loadSettings();
        if (settings != null && settings.getAutoLoginUsername() != null) {
            String autoUsername = settings.getAutoLoginUsername();
            User userToLogin = null;
            User cachedUser = FileManager.loadCachedUser();

            if (NetworkManager.getInstance().isConnected()) {
                if (cachedUser != null && cachedUser.getUsername().equalsIgnoreCase(autoUsername)) {
                    userToLogin = cachedUser;
                    FileManager.updateUser(cachedUser);
                } else {
                    Message req = new Message(Message.Type.GET_USER).put("username", autoUsername);
                    Message resp = NetworkManager.getInstance().sendRequest(req);
                    if (resp != null && resp.getType() == Message.Type.SUCCESS && resp.get("user_json") != null) {
                        userToLogin = gson.fromJson(resp.get("user_json"), User.class);
                        FileManager.saveCachedUser(userToLogin);
                    }
                }
            } else {
                if (cachedUser != null && cachedUser.getUsername().equalsIgnoreCase(autoUsername)) {
                    userToLogin = cachedUser;
                }
            }

            if (userToLogin != null) {
                UserSession.setCurrentUser(userToLogin);
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
        NetworkManager.getInstance().disconnect();
        if (batch != null) batch.dispose();
        if (skin != null) skin.dispose();
        if (getScreen() != null) getScreen().dispose();
        System.exit(0);
    }
}
