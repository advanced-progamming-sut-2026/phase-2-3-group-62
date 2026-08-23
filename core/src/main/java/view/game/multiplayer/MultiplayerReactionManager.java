package view.game.multiplayer;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import network.Message;
import network.NetworkManager;
import pvz.libpvz.pam.PamPlayer;
import view.ui.PamActor;

import java.util.HashMap;
import java.util.Map;

public class MultiplayerReactionManager {
    private final Stage stage;
    private final Skin skin;
    private final PamPlayer pamPlayer;
    private final String myUsername;
    private final String opponentUsername;
    private final Texture bgTexture;
    private final Texture greenHighlightTexture;

    private Table reactionDrawerTable;
    private Table reactionToggleTable;
    private boolean isReactionDrawerOpen = false;
    private Table reactionDisplayContainer;
    private final Map<String, Texture> staticEmojiTextures = new HashMap<>();

    public MultiplayerReactionManager(Stage stage, Skin skin, PamPlayer pamPlayer, String myUsername, String opponentUsername, Texture bgTexture, Texture greenHighlightTexture) {
        this.stage = stage;
        this.skin = skin;
        this.pamPlayer = pamPlayer;
        this.myUsername = myUsername;
        this.opponentUsername = opponentUsername;
        this.bgTexture = bgTexture;
        this.greenHighlightTexture = greenHighlightTexture;

        loadStaticEmojiTextures();
        buildUI();
    }

    private void loadStaticEmojiTextures() {
        String[] files = {
            "face_with_symbols_on_mouth.png",
            "joy.png",
            "kissing_heart.png",
            "moyai.png",
            "ok_hand.png",
            "waving_white_flag.png"
        };
        for (String f : files) {
            try {
                if (Gdx.files.internal("assets/emoji/" + f).exists()) {
                    staticEmojiTextures.put(f, new Texture(Gdx.files.internal("assets/emoji/" + f)));
                } else if (Gdx.files.internal("emoji/" + f).exists()) {
                    staticEmojiTextures.put(f, new Texture(Gdx.files.internal("emoji/" + f)));
                }
            } catch (Exception ignored) {}
        }
    }

    public void buildUI() {
        reactionDisplayContainer = new Table();
        reactionDisplayContainer.top().right();
        reactionDisplayContainer.setFillParent(true);
        reactionDisplayContainer.padTop(90).padRight(30);
        reactionDisplayContainer.setTouchable(Touchable.disabled);
        stage.addActor(reactionDisplayContainer);

        reactionToggleTable = new Table();
        reactionToggleTable.bottom().right();
        reactionToggleTable.setFillParent(true);
        reactionToggleTable.padBottom(18).padRight(25);

        TextButton emojiMenuBtn = new TextButton("EMOTES", skin, "green");
        MultiplayerHud.attachHoverEffect(emojiMenuBtn, 1.08f);
        emojiMenuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                toggleReactionDrawer();
            }
        });
        reactionToggleTable.add(emojiMenuBtn).size(130, 48);
        stage.addActor(reactionToggleTable);

        reactionDrawerTable = new Table();
        reactionDrawerTable.setBackground(new TextureRegionDrawable(bgTexture));
        reactionDrawerTable.pad(12);
        reactionDrawerTable.setVisible(false);
        reactionDrawerTable.setPosition(stage.getViewport().getWorldWidth() - 440, 75);
        reactionDrawerTable.setSize(415, 365);

        Label drawerTitle = new Label("QUICK REACTIONS", skin, "big_outline");
        drawerTitle.setFontScale(0.85f);
        drawerTitle.setColor(Color.YELLOW);
        reactionDrawerTable.add(drawerTitle).padBottom(8).center().row();

        String[] texts = {"Good Luck!", "Well Played!", "Nice Move!"};
        Table textBtnRow = new Table();
        for (String t : texts) {
            TextButton tb = new TextButton(t, skin);
            tb.getLabel().setFontScale(0.65f);
            MultiplayerHud.attachHoverEffect(tb, 1.05f);
            tb.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    sendReaction("TEXT", t);
                    triggerLocalReaction("TEXT", t, myUsername);
                    toggleReactionDrawer();
                }
            });
            textBtnRow.add(tb).size(125, 36).pad(2);
        }
        reactionDrawerTable.add(textBtnRow).padBottom(10).row();

        Table emojiGrid = new Table();
        String[] emojiKeys = {"joy.png", "face_with_symbols_on_mouth.png", "kissing_heart.png", "moyai.png", "ok_hand.png", "waving_white_flag.png"};
        int col = 0;
        for (String k : emojiKeys) {
            Texture tex = staticEmojiTextures.get(k);
            if (tex != null) {
                ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
                style.imageUp = new TextureRegionDrawable(new TextureRegion(tex));
                ImageButton ib = new ImageButton(style);
                MultiplayerHud.attachHoverEffect(ib, 1.12f);
                ib.addListener(new ClickListener() {
                    @Override
                    public void clicked(InputEvent event, float x, float y) {
                        sendReaction("EMOJI", k);
                        triggerLocalReaction("EMOJI", k, myUsername);
                        toggleReactionDrawer();
                    }
                });
                emojiGrid.add(ib).size(48, 48).pad(6);
                col++;
                if (col % 6 == 0) emojiGrid.row();
            }
        }
        reactionDrawerTable.add(emojiGrid).padBottom(10).row();

        Table animGrid = new Table();
        String[][] anims = {
            {"Shadowshroom", "768/FULL/PLANT/SHADOWSHROOM/SHADOWSHROOM.PAM"},
            {"Phatbeet", "768/FULL/PLANT/PHATBEETS/PHATBEETS.PAM"},
            {"Chefster", "768/FULL/ZOMBIE/ZOMBIE_CHEFSTER/ZOMBIE_CHEFSTER.PAM"}
        };

        for (String[] a : anims) {
            Table animBtn = new Table();
            animBtn.setTouchable(Touchable.enabled);
            animBtn.setTransform(true);
            animBtn.setOrigin(Align.center);
            animBtn.setBackground(new TextureRegionDrawable(greenHighlightTexture));

            PamActor previewAnim = new PamActor(pamPlayer, a[1], "idle", 0.32f);
            previewAnim.setTouchable(Touchable.disabled);
            animBtn.add(previewAnim).size(65, 65).center().row();

            Label titleLbl = new Label(a[0], skin, "big");
            titleLbl.setFontScale(0.48f);
            titleLbl.setColor(Color.WHITE);
            titleLbl.setTouchable(Touchable.disabled);
            animBtn.add(titleLbl).padTop(2).center();

            MultiplayerHud.attachHoverEffect(animBtn, 1.08f);
            animBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    sendReaction("ANIM", a[1]);
                    triggerLocalReaction("ANIM", a[1], myUsername);
                    toggleReactionDrawer();
                }
            });

            animGrid.add(animBtn).size(122, 95).pad(4);
        }
        reactionDrawerTable.add(animGrid).center();

        stage.addActor(reactionDrawerTable);
    }

    public void toggleReactionDrawer() {
        isReactionDrawerOpen = !isReactionDrawerOpen;
        reactionDrawerTable.setVisible(isReactionDrawerOpen);
        if (isReactionDrawerOpen) reactionDrawerTable.toFront();
    }

    private void sendReaction(String category, String content) {
        Message msg = new Message(Message.Type.GAME_REACTION)
            .put("opponent_username", opponentUsername)
            .put("category", category)
            .put("content", content)
            .put("from_username", myUsername);
        NetworkManager.getInstance().sendAsync(msg);
    }

    public void triggerLocalReaction(String category, String content, String senderName) {
        Table bubble = new Table();
        bubble.setBackground(new TextureRegionDrawable(bgTexture));
        bubble.pad(16, 24, 16, 24);

        Label sender = new Label(senderName + ":", skin, "big_outline");
        sender.setFontScale(1.1f);
        sender.setColor(Color.YELLOW);
        bubble.add(sender).padRight(16);

        if ("TEXT".equalsIgnoreCase(category)) {
            Label txt = new Label(content, skin, "big");
            txt.setFontScale(1.15f);
            txt.setColor(Color.WHITE);
            bubble.add(txt);
        } else if ("EMOJI".equalsIgnoreCase(category)) {
            Texture tex = staticEmojiTextures.get(content);
            if (tex != null) {
                Image img = new Image(new TextureRegionDrawable(new TextureRegion(tex)));
                bubble.add(img).size(96, 96);
            }
        } else if ("ANIM".equalsIgnoreCase(category)) {
            PamActor anim = new PamActor(pamPlayer, content, "idle", 0.65f);
            anim.setSize(120, 120);
            bubble.add(anim).size(120, 120);
        }

        reactionDisplayContainer.clearChildren();
        reactionDisplayContainer.add(bubble).row();

        bubble.setColor(1f, 1f, 1f, 0f);
        bubble.setScale(0.6f);
        bubble.setOrigin(Align.center);
        bubble.addAction(Actions.sequence(
            Actions.parallel(
                Actions.fadeIn(0.2f),
                Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut)
            ),
            Actions.delay(3.2f),
            Actions.parallel(
                Actions.fadeOut(0.3f),
                Actions.scaleTo(0.6f, 0.6f, 0.3f)
            ),
            Actions.run(reactionDisplayContainer::clearChildren)
        ));
    }

    public void dispose() {
        for (Texture t : staticEmojiTextures.values()) {
            if (t != null) t.dispose();
        }
    }
}
