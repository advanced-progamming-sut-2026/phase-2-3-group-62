package view.menu.playMenu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.CommandParser;
import controller.menu.MenuController;
import controller.menu.ShopController;
import main.Maini;
import model.greenhouse.Greenhouse;
import model.shop.Item;
import model.shop.Shop;
import model.user.User;
import model.user.UserSession;
import util.ParsedCommand;
import view.ui.Toast;
import view.ui.WalletBar;

import java.util.List;
import java.util.Map;

public class ShopScreen implements Screen {
    private final Maini game;
    private final MenuController menuController;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private TextureRegion cardBgRegion;
    private TextureRegion selectedPacketRegion;
    private TextureRegion goldCoinRegion;
    private TextureRegion diamondRegion;

    private Shop shop;
    private Greenhouse greenhouse;
    private ShopController shopController;
    private CommandParser parser;

    private Table cardsTable;
    private WalletBar walletBar;
    private Window.WindowStyle dialogWindowStyle;

    public ShopScreen(Maini game, MenuController menuController, Skin skin) {
        this.game = game;
        this.menuController = menuController != null ? menuController : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_STORE_GACHA_PINATA_LEGENDARY_CARD");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("DELAYLOAD_BACKGROUND_ZEN_768_00");
        }

        cardBgRegion = game.getTextureBank().region("IMAGE_UI_CARDS_STORE_PROMO_RIBBON_10");
        if (cardBgRegion == null) {
            cardBgRegion = game.getTextureBank().region("IMAGE_UI_GENERIC_GUARANTEED_BG_10");
        }

        selectedPacketRegion = game.getTextureBank().region("IMAGE_UI_PACKETS_SELECTED");
        goldCoinRegion = game.getTextureBank().region("IMAGE_EFFECTS_COIN_GOLD_COIN_GOLD_98X95");
        diamondRegion = game.getTextureBank().region("IMAGE_EFFECTS_COIN_DIAMOND_COIN_DIAMOND_141X146");

        BitmapFont font;
        try {
            font = skin.getFont("BRIANNETOD");
        } catch (Exception e) {
            font = skin.getFont("default");
        }
        dialogWindowStyle = new Window.WindowStyle(font, Color.WHITE, skin.getDrawable("image_ui_mainmenu_mm_settings_tab_10"));

        shop = new Shop();
        greenhouse = new Greenhouse();
        shopController = new ShopController(shop, greenhouse);
        parser = new CommandParser();

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
        backBtn.getLabel().setFontScale(1.1f);
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
                game.setScreen(new GreenhouseScreen(game, menuController, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(110, 46).left();

        Label title = new Label("STORE", skin, "big_outline");
        topRow.add(title).expandX().center().padLeft(60);

        walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).right();

        root.add(topRow).fillX().pad(40, 25, 10, 25).row();

        cardsTable = new Table();
        rebuildCards();

        ScrollPane scroll = new ScrollPane(cardsTable, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(false, true);

        root.add(scroll).expand().fill().pad(15, 20, 25, 20);
    }

    private void rebuildCards() {
        cardsTable.clear();
        cardsTable.left().top();

        shop.checkAndRefreshDailyOffers();
        for (Map.Entry<String, Shop.DailyOffer> entry : shop.getDailyOffers().entrySet()) {
            Shop.DailyOffer offer = entry.getValue();
            ShopCard card = new ShopCard(entry.getKey(), offer.getPlantType() + "\n(Daily Offer - 10 Packets)", offer.getDiscountedPrice(), "coin", offer.isPurchased(), true, null);
            cardsTable.add(card).size(210, 290).pad(10);
        }

        for (Item item : shop.getPermanentItems()) {
            String desc = getItemDescription(item);
            ShopCard card = new ShopCard(item.getId(), item.getName() + (desc.isEmpty() ? "" : "\n" + desc), item.getPrice(), item.getCurrencyType(), false, false, item);
            cardsTable.add(card).size(210, 290).pad(10);
        }
    }

    private String getItemDescription(Item item) {
        if (item.getQuantity() > 1 && !item.getId().equals("currency_exchange")) {
            return "Bundle: x" + item.getQuantity();
        } else if (item.getId().equals("currency_exchange")) {
            return "Exchange: " + item.getQuantity() + " Coins";
        }
        return "";
    }

    private class ShopCard extends Stack {
        public ShopCard(String id, String displayName, int price, String currency, boolean purchased, boolean isDaily, Item item) {
            setTransform(true);
            setOrigin(Align.center);

            if (selectedPacketRegion != null) {
                Image baseBg = new Image(selectedPacketRegion);
                baseBg.setScaling(Scaling.stretch);
                add(baseBg);
            } else if (cardBgRegion != null) {
                Image baseBg = new Image(cardBgRegion);
                baseBg.setScaling(Scaling.stretch);
                add(baseBg);
            }

            Table content = new Table();
            content.pad(16);

            Label nameLabel = new Label(displayName, skin, "big");
            nameLabel.setFontScale(0.56f);
            nameLabel.setWrap(true);
            nameLabel.setAlignment(Align.center);
            content.add(nameLabel).width(180).expand().center().row();

            if (purchased) {
                Label soldLabel = new Label("SOLD OUT", skin, "big");
                soldLabel.setFontScale(0.62f);
                soldLabel.setColor(Color.RED);
                content.add(soldLabel).height(44).bottom();
            } else {
                String styleName = currency.equalsIgnoreCase("gem") ? "purple" : "green";
                TextButton.TextButtonStyle btnStyle = skin.get(styleName, TextButton.TextButtonStyle.class);
                Button buyBtn = new Button(btnStyle);

                Table btnContent = new Table();
                Label priceLbl = new Label(String.valueOf(price), skin, "big");
                priceLbl.setFontScale(0.62f);
                btnContent.add(priceLbl).padRight(6);

                TextureRegion currRegion = currency.equalsIgnoreCase("gem") ? diamondRegion : goldCoinRegion;
                if (currRegion != null) {
                    Image currIcon = new Image(currRegion);
                    currIcon.setScaling(Scaling.fit);
                    btnContent.add(currIcon).size(24, 24);
                }

                buyBtn.add(btnContent).center();

                buyBtn.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        if (id.equals("choice_seed_pack")) {
                            showPlantChoiceDialog(id, price, currency);
                        } else {
                            showConfirmDialog(id, displayName.replace("\n", " "), price, currency, null);
                        }
                    }
                });
                content.add(buyBtn).size(165, 46).bottom();
            }

            add(content);

            addListener(new InputListener() {
                @Override
                public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                    if (pointer == -1) {
                        clearActions();
                        addAction(Actions.scaleTo(1.04f, 1.04f, 0.08f));
                    }
                }
                @Override
                public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                    if (pointer == -1) {
                        clearActions();
                        addAction(Actions.scaleTo(1f, 1f, 0.08f));
                    }
                }
            });
        }
    }

    private void showConfirmDialog(String itemId, String itemName, int price, String currency, String plantType) {
        Dialog dialog = new Dialog("", dialogWindowStyle) {
            @Override
            protected void result(Object object) {
                if (Boolean.TRUE.equals(object)) {
                    executePurchase(itemId, 1, plantType);
                }
            }
        };

        dialog.getTitleLabel().setAlignment(Align.center);

        Label titleLbl = new Label("Purchase Confirmation", skin, "big_outline");
        titleLbl.setFontScale(0.75f);
        dialog.getContentTable().add(titleLbl).padTop(15).row();

        Table msgTable = new Table();
        Label msgPrefix = new Label("Would you like to purchase " + itemName + " for " + price + " ", skin, "default");
        msgTable.add(msgPrefix);

        TextureRegion currRegion = currency.equalsIgnoreCase("gem") ? diamondRegion : goldCoinRegion;
        if (currRegion != null) {
            Image currIcon = new Image(currRegion);
            currIcon.setScaling(Scaling.fit);
            msgTable.add(currIcon).size(22, 22).padLeft(2).padRight(2);
        }
        Label msgSuffix = new Label("?", skin, "default");
        msgTable.add(msgSuffix);

        dialog.getContentTable().add(msgTable).pad(15).row();

        TextButton yesBtn = new TextButton("Yes", skin, "green");
        TextButton noBtn = new TextButton("No", skin, "brown");

        dialog.button(yesBtn, true);
        dialog.button(noBtn, false);
        dialog.getButtonTable().padBottom(15);

        dialog.show(stage);
    }

    private void showPlantChoiceDialog(String itemId, int price, String currency) {
        User user = UserSession.getCurrentUser();
        if (user == null || user.getUnlockedPlants().isEmpty()) {
            Toast.show(stage, skin, "Error: No plants unlocked to choose from!", true);
            return;
        }

        Dialog dialog = new Dialog("", dialogWindowStyle);

        Label titleLbl = new Label("Choose Plant for Seed Pack", skin, "big_outline");
        titleLbl.setFontScale(0.7f);
        dialog.getContentTable().add(titleLbl).padTop(10).row();

        Table listTable = new Table();
        List<String> unlocked = user.getUnlockedPlants();
        for (String pName : unlocked) {
            TextButton pBtn = new TextButton(pName, skin, "green_small");
            pBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    dialog.hide();
                    showConfirmDialog(itemId, "Choice Pack (" + pName + ")", price, currency, pName);
                }
            });
            listTable.add(pBtn).size(150, 36).pad(4).row();
        }

        ScrollPane scroll = new ScrollPane(listTable, skin);
        dialog.getContentTable().add(scroll).size(280, 180).pad(10).row();

        TextButton cancelBtn = new TextButton("Cancel", skin, "brown");
        dialog.button(cancelBtn, false);
        dialog.show(stage);
    }

    private void executePurchase(String itemId, int count, String plantType) {
        String rawCommand = "shop buy -i " + itemId + " -n " + count;
        if (plantType != null && !plantType.isEmpty()) {
            rawCommand += " -t " + plantType;
        }

        ParsedCommand cmd = parser.parse(rawCommand);
        String result = shopController.buyItem(cmd);

        boolean isError = result.startsWith("Error");
        Toast.show(stage, skin, result, isError);

        walletBar.updateValues();
        rebuildCards();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.12f, 0.15f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        batch.setColor(Color.WHITE);
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            float zoom = 1.20f;
            float drawW = worldW * zoom;
            float drawH = worldH * zoom;
            float drawX = (worldW - drawW) / 2f;
            float drawY = (worldH - drawH) / 2f;
            batch.draw(bgRegion, drawX, drawY, drawW, drawH);
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
