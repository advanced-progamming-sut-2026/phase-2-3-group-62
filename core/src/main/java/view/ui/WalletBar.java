package view.ui;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import main.Maini;
import model.user.User;
import model.user.UserSession;
import pvz.libpvz.textures.TextureBank;

public class WalletBar extends Table {
    private final TextureBank textureBank;
    private final Skin skin;
    private Label coinLabel;
    private Label gemLabel;

    public WalletBar(Maini game, Skin skin) {
        this(skin, game.getTextureBank());
    }

    public WalletBar(Skin skin, TextureBank textureBank) {
        this.skin = skin;
        this.textureBank = textureBank;
        buildUI();
    }

    private void buildUI() {
        clear();
        top().right();

        TextureRegion coinRegion = textureBank != null ? textureBank.region("IMAGE_UI_GENERIC_BUTTONS_COIN_BUY_NORMAL") : null;
        TextureRegion gemRegion = textureBank != null ? textureBank.region("IMAGE_UI_GENERIC_BUTTONS_PREMIUM_NORMAL") : null;

        User user = UserSession.getCurrentUser();
        int coins = user != null ? user.getCoins() : 0;
        int gems = user != null ? user.getGems() : 0;

        Table coinBadge = new Table();
        if (coinRegion != null) {
            coinBadge.setBackground(new TextureRegionDrawable(coinRegion));
        }
        coinLabel = new Label(String.valueOf(coins), skin, "big");
        coinBadge.add(coinLabel).padLeft(38).padRight(32).center();
        add(coinBadge).height(42).padRight(12);

        Table gemBadge = new Table();
        if (gemRegion != null) {
            gemBadge.setBackground(new TextureRegionDrawable(gemRegion));
        }
        gemLabel = new Label(String.valueOf(gems), skin, "big");
        gemBadge.add(gemLabel).padLeft(38).padRight(32).center();
        add(gemBadge).height(42);

        pack();
    }

    public void updateValues() {
        updateAmounts();
    }

    public void updateBalances() {
        updateAmounts();
    }

    public void updateAmounts() {
        User user = UserSession.getCurrentUser();
        if (user != null) {
            if (coinLabel != null) coinLabel.setText(String.valueOf(user.getCoins()));
            if (gemLabel != null) gemLabel.setText(String.valueOf(user.getGems()));
        } else {
            if (coinLabel != null) coinLabel.setText("0");
            if (gemLabel != null) gemLabel.setText("0");
        }
    }
}
