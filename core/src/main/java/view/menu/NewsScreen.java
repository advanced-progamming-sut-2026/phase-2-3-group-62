package view.menu;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.user.NewsItem;
import model.user.User;
import model.user.UserSession;
import util.ParsedCommand;
import view.ui.WalletBar;

public class NewsScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private Texture roundedBrownBgTexture;
    private Texture newsCardBgTexture;

    public NewsScreen(Maini game, MenuController controller, Skin skin) {
        this.game = game;
        this.controller = controller != null ? controller : game.getMenuController();
        this.skin = skin != null ? skin : game.getSkin();
        this.batch = game.getBatch();
    }

    @Override
    public void show() {
        stage = new Stage(game.getViewport(), batch);
        Gdx.input.setInputProcessor(stage);

        bgRegion = game.getTextureBank().region("IMAGE_UI_STORE_MINISTORE_BG");
        if (bgRegion == null) {
            bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(1120, 680, 28, new Color(0.18f, 0.11f, 0.06f, 0.96f));
        newsCardBgTexture = createRoundedRectangleTexture(980, 70, 14, new Color(0.12f, 0.07f, 0.04f, 0.85f));

        buildUI();
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

    private void buildUI() {
        stage.clear();

        Table root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topRow = new Table();
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.3f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, controller, skin));
                dispose();
            }
        });
        topRow.add(backBtn).size(145, 58).left();

        Label title = new Label("NEWS & MAILBOX", skin, "big_outline");
        title.setFontScale(1.45f);
        topRow.add(title).expandX().center().padLeft(40);

        WalletBar walletBar = new WalletBar(game, skin);
        topRow.add(walletBar).right();

        root.add(topRow).fillX().pad(12, 30, 4, 30).row();

        Table centerWrapper = new Table();
        centerWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        Table contentTable = new Table();
        contentTable.top().pad(28, 36, 28, 36);

        Table newsContentTable = new Table();
        newsContentTable.top().left();

        User currentUser = UserSession.getCurrentUser();

        if (currentUser == null || currentUser.getNews() == null || currentUser.getNews().isEmpty()) {
            Label emptyLabel = new Label("No news available at the moment.", skin, "big");
            emptyLabel.setFontScale(0.9f);
            emptyLabel.setColor(Color.LIGHT_GRAY);
            newsContentTable.add(emptyLabel).center().padTop(180);
        } else {
            for (NewsItem item : currentUser.getNews()) {
                Stack cardStack = new Stack();

                if (newsCardBgTexture != null) {
                    Image cardBg = new Image(newsCardBgTexture);
                    cardBg.setScaling(Scaling.stretch);
                    cardStack.add(cardBg);
                }

                Table rowCard = new Table();
                rowCard.left().pad(14, 20, 14, 20);

                Label tagLabel = new Label(item.isRead() ? "[Read]" : "[NEW!]", skin, "big");
                tagLabel.setFontScale(0.78f);
                tagLabel.setColor(item.isRead() ? new Color(0.65f, 0.65f, 0.65f, 1f) : Color.YELLOW);

                Label textLabel = new Label(item.getContent(), skin, "big");
                textLabel.setFontScale(0.74f);
                textLabel.setColor(Color.WHITE);
                textLabel.setWrap(true);

                rowCard.add(tagLabel).top().left().padRight(16);
                rowCard.add(textLabel).width(840).left();

                cardStack.add(rowCard);
                newsContentTable.add(cardStack).width(980).padBottom(10).row();
            }
            controller.processNews(new ParsedCommand("show-all"), "show all");
        }

        ScrollPane scrollPane = new ScrollPane(newsContentTable, skin);
        scrollPane.setFadeScrollBars(false);
        scrollPane.setScrollingDisabled(true, false);

        contentTable.add(scrollPane).width(1040).height(590).expand().fill();
        boxStack.add(contentTable);

        centerWrapper.add(boxStack).size(1120, 680);
        root.add(centerWrapper).expand().center().padBottom(14);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.06f, 0.07f, 0.09f, 1f);

        game.getTextureBank().update();
        game.getViewport().getCamera().update();
        batch.setProjectionMatrix(game.getViewport().getCamera().combined);

        batch.begin();
        batch.setColor(0.38f, 0.38f, 0.38f, 1f);
        if (bgRegion != null) {
            float worldW = game.getViewport().getWorldWidth();
            float worldH = game.getViewport().getWorldHeight();
            float bgW = bgRegion.getRegionWidth();
            float bgH = bgRegion.getRegionHeight();

            float zoomFactor = 1.15f;
            float scale = Math.max(worldW / bgW, worldH / bgH) * zoomFactor;
            float drawW = bgW * scale;
            float drawH = bgH * scale;
            float drawX = (worldW - drawW) / 2f;
            float drawY = (worldH - drawH) / 2f;

            batch.draw(bgRegion, drawX, drawY, drawW, drawH);
        }
        batch.setColor(Color.WHITE);
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
        if (newsCardBgTexture != null) newsCardBgTexture.dispose();
        if (stage != null) stage.dispose();
    }
}
