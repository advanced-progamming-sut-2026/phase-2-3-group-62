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
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.ScreenUtils;
import controller.menu.MenuController;
import main.Maini;
import model.leaderboard.Leaderboard;
import model.user.User;
import model.user.UserSession;
import util.FileManager;
import view.ui.WalletBar;

import java.util.List;

public class LeaderboardScreen implements Screen {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final SpriteBatch batch;
    private Stage stage;

    private TextureRegion bgRegion;
    private Texture roundedBrownBgTexture;

    private Table root;
    private Table tableContent;
    private SelectBox<String> sortCriteriaBox;
    private CheckBox ascendingBox;

    public LeaderboardScreen(Maini game, MenuController controller, Skin skin) {
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
            bgRegion = game.getTextureBank().region("IMAGE_UI_THYMED_EVENTS_SPRINGENING_EVENT_BG");
            if (bgRegion == null) {
                bgRegion = game.getTextureBank().region("IMAGE_BACKGROUNDS_ZEN_GARDEN");
            }
        }

        roundedBrownBgTexture = createRoundedRectangleTexture(1120, 520, 24, new Color(0.18f, 0.11f, 0.06f, 0.96f));

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

        root = new Table();
        root.setFillParent(true);
        root.top();
        stage.addActor(root);

        Table topBar = new Table();
        TextButton backBtn = new TextButton("Back", skin);
        backBtn.getLabel().setFontScale(1.2f);
        attachHoverEffect(backBtn, 1.06f);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game, controller, skin));
                dispose();
            }
        });
        topBar.add(backBtn).size(130, 50).left();

        Label title = new Label("GLOBAL LEADERBOARD", skin, "big_outline");
        title.setFontScale(1.35f);
        topBar.add(title).expandX().center();

        WalletBar walletBar = new WalletBar(game, skin);
        topBar.add(walletBar).right();

        root.add(topBar).fillX().pad(12, 30, 6, 30).row();

        Table controlsTable = new Table();
        Label sortLabel = new Label("Sort By: ", skin, "big");
        sortLabel.setFontScale(0.85f);
        controlsTable.add(sortLabel).padRight(8);

        sortCriteriaBox = new SelectBox<>(skin);
        sortCriteriaBox.setItems(
            "Total Score",
            "Level / Stage",
            "Mini-Games",
            "Daily Quests",
            "Non-Daily Quests",
            "Scoring High"
        );
        sortCriteriaBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshLeaderboard();
            }
        });
        controlsTable.add(sortCriteriaBox).width(190).padRight(20);

        ascendingBox = new CheckBox(" Ascending", skin);
        ascendingBox.getLabel().setFontScale(0.85f);
        ascendingBox.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                refreshLeaderboard();
            }
        });
        controlsTable.add(ascendingBox);

        root.add(controlsTable).padBottom(10).row();

        Table centerWrapper = new Table();
        centerWrapper.center();

        Stack boxStack = new Stack();
        if (roundedBrownBgTexture != null) {
            Image boxBg = new Image(roundedBrownBgTexture);
            boxBg.setScaling(Scaling.stretch);
            boxStack.add(boxBg);
        }

        tableContent = new Table();
        tableContent.top().pad(20, 28, 20, 28);
        refreshLeaderboard();

        boxStack.add(tableContent);
        centerWrapper.add(boxStack).size(1120, 520);
        root.add(centerWrapper).expand().center().padBottom(15);
    }

    private void refreshLeaderboard() {
        tableContent.clear();

        List<User> allUsers = FileManager.loadUsers();
        Leaderboard leaderboard = new Leaderboard();
        for (User u : allUsers) {
            leaderboard.addUser(u);
        }

        Leaderboard.SortType sortType = switch (sortCriteriaBox.getSelectedIndex()) {
            case 1 -> Leaderboard.SortType.BY_LEVEL;
            case 2 -> Leaderboard.SortType.BY_MINI_GAMES;
            case 3 -> Leaderboard.SortType.BY_DAILY_QUESTS;
            case 4 -> Leaderboard.SortType.BY_NON_DAILY_QUESTS;
            case 5 -> Leaderboard.SortType.BY_SCORING_GAME;
            default -> Leaderboard.SortType.BY_TOTAL_SCORE;
        };

        boolean isAscending = ascendingBox.isChecked();
        leaderboard.setSortType(sortType, isAscending);
        List<User> sortedUsers = leaderboard.getSortedUsers();

        Table headerRow = new Table();
        headerRow.add(createHeaderLabel("Rank")).width(80).padRight(12).left();
        headerRow.add(createHeaderLabel("Username")).width(180).padRight(12).left();
        headerRow.add(createHeaderLabel("Score")).width(115).padRight(12).left();
        headerRow.add(createHeaderLabel("Level")).width(125).padRight(12).left();
        headerRow.add(createHeaderLabel("Minigames")).width(135).padRight(12).left();
        headerRow.add(createHeaderLabel("Daily")).width(105).padRight(12).left();
        headerRow.add(createHeaderLabel("Non-Daily")).width(125).padRight(12).left();
        headerRow.add(createHeaderLabel("High Score")).width(120).left();
        tableContent.add(headerRow).fillX().padBottom(14).row();

        User currentSessionUser = UserSession.getCurrentUser();
        int rank = 1;

        for (User u : sortedUsers) {
            Table row = new Table();
            boolean isCurrent = currentSessionUser != null && currentSessionUser.getUsername().equalsIgnoreCase(u.getUsername());
            Color rowColor = isCurrent ? Color.GOLD : Color.WHITE;

            Label rankLbl = createRowLabel("#" + rank, rowColor);
            Label userLbl = createRowLabel(u.getUsername(), rowColor);
            Label scoreLbl = createRowLabel(String.valueOf(u.getScore()), rowColor);
            Label lvlLbl = createRowLabel("S" + u.getLastSeasonCompleted() + "-L" + u.getLastLevelCompleted(), rowColor);
            Label mgLbl = createRowLabel(String.valueOf(u.getCompletedMiniGames()), rowColor);
            Label dailyLbl = createRowLabel(String.valueOf(u.getCompletedDailyQuests()), rowColor);
            Label nonDailyLbl = createRowLabel(String.valueOf(u.getCompletedNonDailyQuests()), rowColor);
            Label highScoreLbl = createRowLabel(String.valueOf(u.getHighestScoreInScoringGame()), rowColor);

            row.add(rankLbl).width(80).padRight(12).left();
            row.add(userLbl).width(180).padRight(12).left();
            row.add(scoreLbl).width(115).padRight(12).left();
            row.add(lvlLbl).width(125).padRight(12).left();
            row.add(mgLbl).width(135).padRight(12).left();
            row.add(dailyLbl).width(105).padRight(12).left();
            row.add(nonDailyLbl).width(125).padRight(12).left();
            row.add(highScoreLbl).width(120).left();

            tableContent.add(row).fillX().padTop(3).padBottom(3).row();
            rank++;
        }
    }

    private Label createHeaderLabel(String text) {
        Label label = new Label(text, skin, "big");
        label.setFontScale(0.9f);
        label.setColor(Color.YELLOW);
        return label;
    }

    private Label createRowLabel(String text, Color color) {
        Label label = new Label(text, skin, "big");
        label.setFontScale(0.8f);
        label.setColor(color);
        return label;
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
        if (stage != null) stage.dispose();
    }
}
