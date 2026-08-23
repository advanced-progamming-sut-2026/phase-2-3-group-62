package view.menu.playMenu;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
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
import com.badlogic.gdx.utils.Scaling;
import controller.menu.MenuController;
import main.Maini;
import network.Message;
import network.NetworkManager;
import view.menu.seedChooser.MultiplayerSeedChooserScreen;

public class IZombieMultiplayerDialog {
    private final Maini game;
    private final MenuController controller;
    private final Skin skin;
    private final Stage stage;

    private Table overlayTable;
    private Table windowWrapper;
    private Table contentTable;
    private Image dimImage;
    private Label statusLabel;
    private TextField targetUserField;
    private Texture dimTexture;
    private boolean isClosing = false;

    public IZombieMultiplayerDialog(Maini game, MenuController controller, Skin skin, Stage stage) {
        this.game = game;
        this.controller = controller;
        this.skin = skin;
        this.stage = stage;
        this.dimTexture = createDimTexture();
    }

    public void show() {
        isClosing = false;
        overlayTable = new Table() {
            @Override
            public void act(float delta) {
                super.act(delta);
                checkNetworkMessages();
            }
        };
        overlayTable.setFillParent(true);
        overlayTable.setTouchable(Touchable.enabled);

        dimImage = new Image(dimTexture);
        dimImage.setFillParent(true);
        dimImage.setColor(1f, 1f, 1f, 0f);
        dimImage.addAction(Actions.fadeIn(0.2f, Interpolation.fade));
        overlayTable.addActor(dimImage);

        windowWrapper = new Table();
        windowWrapper.setTransform(true);
        windowWrapper.setOrigin(Align.center);
        windowWrapper.setScale(0.75f);
        windowWrapper.setColor(1f, 1f, 1f, 0f);

        Stack stack = new Stack();
        TextureRegion borderRegion = game.getTextureBank().region("IMAGE_UI_QUESTS_QUESTBORDER");
        if (borderRegion != null) {
            Image borderImg = new Image(new TextureRegionDrawable(borderRegion));
            borderImg.setScaling(Scaling.stretch);
            stack.add(borderImg);
        }

        contentTable = new Table();
        contentTable.top().pad(48, 45, 30, 45);

        if (!NetworkManager.getInstance().isConnected()) {
            buildOfflineNotice();
        } else {
            buildInitialOptions();
        }

        stack.add(contentTable);
        windowWrapper.add(stack).size(660, 490);
        overlayTable.add(windowWrapper).center().padBottom(30);

        windowWrapper.addAction(Actions.parallel(
            Actions.fadeIn(0.22f, Interpolation.fade),
            Actions.scaleTo(1f, 1f, 0.28f, Interpolation.swingOut)
        ));

        stage.addActor(overlayTable);
    }

    private void attachHoverEffect(Actor actor, float targetScale) {
        if (actor instanceof com.badlogic.gdx.scenes.scene2d.Group) {
            ((com.badlogic.gdx.scenes.scene2d.Group) actor).setTransform(true);
        }
        actor.setOrigin(Align.center);
        actor.addListener(new InputListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                if (pointer == -1 && !isClosing) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(targetScale, targetScale, 0.1f));
                }
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1 && !isClosing) {
                    actor.clearActions();
                    actor.addAction(Actions.scaleTo(1f, 1f, 0.1f));
                }
            }
        });
    }

    private void buildOfflineNotice() {
        contentTable.clearChildren();

        Label title = new Label("MULTIPLAYER LOBBY", skin, "big_outline");
        title.setFontScale(1.15f);
        contentTable.add(title).padTop(10).padBottom(25).row();

        Label offlineLbl = new Label("Server is offline!\nConnect to the server to play multiplayer.", skin, "big");
        offlineLbl.setFontScale(0.9f);
        offlineLbl.setColor(Color.RED);
        offlineLbl.setAlignment(Align.center);
        contentTable.add(offlineLbl).padBottom(30).row();

        TextButton closeBtn = new TextButton("Close", skin);
        attachHoverEffect(closeBtn, 1.08f);
        closeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        contentTable.add(closeBtn).size(150, 44).row();
    }

    private void buildInitialOptions() {
        contentTable.clearChildren();

        Label title = new Label("I, ZOMBIE MULTIPLAYER", skin, "big_outline");
        title.setFontScale(1.15f);
        contentTable.add(title).padTop(5).padBottom(10).row();

        statusLabel = new Label("Select matchmaking mode:", skin, "big");
        statusLabel.setFontScale(0.85f);
        statusLabel.setColor(Color.WHITE);
        contentTable.add(statusLabel).padBottom(16).row();

        TextButton randomBtn = new TextButton("Random Matchmaking", skin, "green");
        randomBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(randomBtn, 1.05f);
        randomBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                startRandomMatchmaking();
            }
        });
        contentTable.add(randomBtn).size(340, 52).padBottom(16).row();

        Table directTable = new Table();
        targetUserField = new TextField("", skin);
        targetUserField.setMessageText("Opponent username...");
        directTable.add(targetUserField).size(210, 46).padRight(12);

        TextButton directBtn = new TextButton("Challenge", skin, "brown");
        directBtn.getLabel().setFontScale(0.95f);
        attachHoverEffect(directBtn, 1.06f);
        directBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                sendDirectChallenge();
            }
        });
        directTable.add(directBtn).size(130, 46);
        contentTable.add(directTable).padBottom(20).row();

        TextButton cancelBtn = new TextButton("Cancel", skin);
        attachHoverEffect(cancelBtn, 1.08f);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                close();
            }
        });
        contentTable.add(cancelBtn).size(140, 44).row();
    }

    private void startRandomMatchmaking() {
        contentTable.clearChildren();
        Label searching = new Label("Waiting in queue for an opponent...", skin, "big_outline");
        searching.setFontScale(1.05f);
        searching.setColor(Color.YELLOW);
        searching.setAlignment(Align.center);
        searching.addAction(Actions.forever(
            Actions.sequence(
                Actions.color(Color.WHITE, 0.6f, Interpolation.sine),
                Actions.color(Color.YELLOW, 0.6f, Interpolation.sine)
            )
        ));
        contentTable.add(searching).padTop(55).padBottom(20).row();

        NetworkManager.getInstance().sendAsync(new Message(Message.Type.MATCHMAKING_JOIN));

        TextButton cancelBtn = new TextButton("Leave Queue", skin);
        attachHoverEffect(cancelBtn, 1.08f);
        cancelBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NetworkManager.getInstance().sendAsync(new Message(Message.Type.MATCHMAKING_LEAVE));
                buildInitialOptions();
            }
        });
        contentTable.add(cancelBtn).size(180, 46).padTop(20).row();
    }

    private void sendDirectChallenge() {
        String target = targetUserField.getText().trim();
        if (target.isEmpty()) {
            statusLabel.setText("Please enter target username.");
            statusLabel.setColor(Color.RED);
            return;
        }

        Message req = new Message(Message.Type.CHALLENGE_REQUEST).put("target_username", target);
        Message resp = NetworkManager.getInstance().sendRequest(req);

        if (resp.getType() == Message.Type.SUCCESS) {
            contentTable.clearChildren();
            Label waiting = new Label("Invitation sent to " + target + "!\nWaiting for response...", skin, "big_outline");
            waiting.setFontScale(1.0f);
            waiting.setColor(Color.GREEN);
            waiting.setAlignment(Align.center);
            waiting.addAction(Actions.forever(
                Actions.sequence(
                    Actions.scaleTo(1.03f, 1.03f, 0.7f, Interpolation.sine),
                    Actions.scaleTo(0.97f, 0.97f, 0.7f, Interpolation.sine)
                )
            ));
            contentTable.add(waiting).padTop(55).padBottom(20).row();

            TextButton cancelBtn = new TextButton("Cancel", skin);
            attachHoverEffect(cancelBtn, 1.08f);
            cancelBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    buildInitialOptions();
                }
            });
            contentTable.add(cancelBtn).size(150, 44).padTop(20).row();
        } else {
            statusLabel.setText(resp.get("message"));
            statusLabel.setColor(Color.RED);
        }
    }

    private void showMatchFoundConfirmationPopup(String opponent, String role) {
        Table matchOverlay = new Table();
        matchOverlay.setFillParent(true);
        matchOverlay.setTouchable(Touchable.enabled);

        Image dim = new Image(dimTexture);
        dim.setFillParent(true);
        dim.setColor(1f, 1f, 1f, 0f);
        dim.addAction(Actions.fadeIn(0.18f));
        matchOverlay.addActor(dim);

        Table popupWrapper = new Table();
        popupWrapper.setTransform(true);
        popupWrapper.setOrigin(Align.center);
        popupWrapper.setScale(0.7f);
        popupWrapper.setColor(1f, 1f, 1f, 0f);

        Stack stack = new Stack();
        TextureRegion borderRegion = game.getTextureBank().region("IMAGE_UI_QUESTS_QUESTBORDER");
        if (borderRegion != null) {
            Image bg = new Image(new TextureRegionDrawable(borderRegion));
            bg.setScaling(Scaling.stretch);
            stack.add(bg);
        }

        Table content = new Table();
        content.pad(35, 30, 25, 30);

        Label titleLbl = new Label("MATCH FOUND!", skin, "big_outline");
        titleLbl.setFontScale(1.15f);
        titleLbl.setColor(Color.GREEN);
        titleLbl.setAlignment(Align.center);
        content.add(titleLbl).padBottom(8).row();

        Label msgLbl = new Label("Opponent found: " + opponent + "\nDo you want to play with this player?", skin, "big");
        msgLbl.setFontScale(0.95f);
        msgLbl.setAlignment(Align.center);
        content.add(msgLbl).padBottom(22).row();

        Table btnRow = new Table();
        TextButton acceptBtn = new TextButton("Play!", skin, "green");
        acceptBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(acceptBtn, 1.08f);
        acceptBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                matchOverlay.remove();
                close();
                game.setScreen(new MultiplayerSeedChooserScreen(game, controller, skin, opponent, role));
            }
        });
        btnRow.add(acceptBtn).size(140, 46).padRight(15);

        TextButton declineBtn = new TextButton("Decline", skin, "brown");
        declineBtn.getLabel().setFontScale(1.05f);
        attachHoverEffect(declineBtn, 1.08f);
        declineBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                NetworkManager.getInstance().sendAsync(new Message(Message.Type.MATCHMAKING_LEAVE));
                popupWrapper.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.scaleTo(0.7f, 0.7f, 0.16f, Interpolation.sineIn),
                        Actions.fadeOut(0.15f)
                    ),
                    Actions.run(() -> {
                        matchOverlay.remove();
                        buildInitialOptions();
                    })
                ));
            }
        });
        btnRow.add(declineBtn).size(140, 46);

        content.add(btnRow).row();
        stack.add(content);
        popupWrapper.add(stack).size(560, 300);
        matchOverlay.add(popupWrapper).center().padBottom(30);

        popupWrapper.addAction(Actions.parallel(
            Actions.fadeIn(0.2f),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut)
        ));

        stage.addActor(matchOverlay);
    }

    private void showInvitationPopup(String fromUser) {
        Table inviteOverlay = new Table();
        inviteOverlay.setFillParent(true);
        inviteOverlay.setTouchable(Touchable.enabled);

        Image dim = new Image(dimTexture);
        dim.setFillParent(true);
        dim.setColor(1f, 1f, 1f, 0f);
        dim.addAction(Actions.fadeIn(0.18f));
        inviteOverlay.addActor(dim);

        Table popupWrapper = new Table();
        popupWrapper.setTransform(true);
        popupWrapper.setOrigin(Align.center);
        popupWrapper.setScale(0.7f);
        popupWrapper.setColor(1f, 1f, 1f, 0f);

        Stack stack = new Stack();
        TextureRegion borderRegion = game.getTextureBank().region("IMAGE_UI_QUESTS_QUESTBORDER");
        if (borderRegion != null) {
            Image bg = new Image(new TextureRegionDrawable(borderRegion));
            bg.setScaling(Scaling.stretch);
            stack.add(bg);
        }

        Table content = new Table();
        content.pad(35, 30, 25, 30);

        Label lbl = new Label("Challenge Request!\n" + fromUser + " wants to play with you!", skin, "big_outline");
        lbl.setFontScale(1.05f);
        lbl.setAlignment(Align.center);
        content.add(lbl).padBottom(22).row();

        Table btnRow = new Table();
        TextButton accept = new TextButton("Accept", skin, "green");
        attachHoverEffect(accept, 1.08f);
        accept.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Message resp = new Message(Message.Type.CHALLENGE_RESPONSE)
                    .put("from_username", fromUser)
                    .put("response", "ACCEPT");
                NetworkManager.getInstance().sendAsync(resp);
                inviteOverlay.remove();
            }
        });
        btnRow.add(accept).size(130, 44).padRight(15);

        TextButton reject = new TextButton("Decline", skin, "brown");
        attachHoverEffect(reject, 1.08f);
        reject.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Message resp = new Message(Message.Type.CHALLENGE_RESPONSE)
                    .put("from_username", fromUser)
                    .put("response", "REJECT");
                NetworkManager.getInstance().sendAsync(resp);
                popupWrapper.addAction(Actions.sequence(
                    Actions.parallel(
                        Actions.scaleTo(0.7f, 0.7f, 0.16f, Interpolation.sineIn),
                        Actions.fadeOut(0.15f)
                    ),
                    Actions.run(inviteOverlay::remove)
                ));
            }
        });
        btnRow.add(reject).size(130, 44);

        content.add(btnRow).row();
        stack.add(content);
        popupWrapper.add(stack).size(530, 280);
        inviteOverlay.add(popupWrapper).center().padBottom(30);

        popupWrapper.addAction(Actions.parallel(
            Actions.fadeIn(0.2f),
            Actions.scaleTo(1f, 1f, 0.25f, Interpolation.swingOut)
        ));

        stage.addActor(inviteOverlay);
    }

    private void checkNetworkMessages() {
        Message msg = NetworkManager.getInstance().pollPushMessage();
        if (msg == null) return;

        if (msg.getType() == Message.Type.MATCHMAKING_FOUND) {
            String opponent = msg.get("opponent_username");
            String role = msg.get("role");
            showMatchFoundConfirmationPopup(opponent, role);
        } else if (msg.getType() == Message.Type.CHALLENGE_ACCEPTED) {
            String opponent = msg.get("opponent_username");
            String role = msg.get("role");

            close();
            game.setScreen(new MultiplayerSeedChooserScreen(game, controller, skin, opponent, role));
        } else if (msg.getType() == Message.Type.CHALLENGE_RECEIVED) {
            showInvitationPopup(msg.get("from_username"));
        } else if (msg.getType() == Message.Type.CHALLENGE_REJECTED) {
            buildInitialOptions();
            statusLabel.setText("Your challenge was rejected by the player.");
            statusLabel.setColor(Color.RED);
        }
    }

    public void close() {
        if (isClosing || overlayTable == null) return;
        isClosing = true;
        overlayTable.setTouchable(Touchable.disabled);

        if (dimImage != null) {
            dimImage.addAction(Actions.fadeOut(0.18f));
        }

        if (windowWrapper != null) {
            windowWrapper.addAction(Actions.sequence(
                Actions.parallel(
                    Actions.scaleTo(0.8f, 0.8f, 0.18f, Interpolation.sineIn),
                    Actions.fadeOut(0.16f)
                ),
                Actions.run(() -> {
                    if (overlayTable != null) {
                        overlayTable.remove();
                    }
                })
            ));
        }
    }

    private Texture createDimTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0.72f));
        pixmap.fill();
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }
}
