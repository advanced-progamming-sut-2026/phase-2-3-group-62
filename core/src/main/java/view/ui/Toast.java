package view.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class Toast extends Table {
    private static Texture backgroundTexture;
    private final Label messageLabel;

    public Toast(Skin skin) {
        if (backgroundTexture == null) {
            backgroundTexture = createRoundedRectangleTexture(400, 100, 18, Color.WHITE);
        }

        if (skin.has("bundle_reward_multiplier", Label.LabelStyle.class)) {
            messageLabel = new Label("", skin, "bundle_reward_multiplier");
        } else {
            messageLabel = new Label("", skin);
        }
        messageLabel.setFontScale(1.35f);
        messageLabel.setAlignment(Align.center);
        messageLabel.setWrap(true);

        add(messageLabel).pad(20, 36, 20, 36).minWidth(360).maxWidth(750);
        pack();
        getColor().a = 0f;
    }

    private static Texture createRoundedRectangleTexture(int width, int height, int radius, Color color) {
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

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.setColor(0.08f, 0.08f, 0.1f, 0.92f * getColor().a * parentAlpha);
        batch.draw(backgroundTexture, getX(), getY(), getWidth(), getHeight());

        batch.setColor(Color.WHITE);
        super.draw(batch, parentAlpha);
        batch.setColor(Color.WHITE);
    }

    public static void show(Stage stage, Skin skin, String message, boolean isError) {
        Toast toast = new Toast(skin);
        toast.messageLabel.setColor(isError ? new Color(1f, 0.35f, 0.35f, 1f) : new Color(0.35f, 1f, 0.45f, 1f));
        toast.messageLabel.setText(message);
        toast.pack();

        float x = (stage.getWidth() - toast.getWidth()) / 2f;
        float y = stage.getHeight() - toast.getHeight() - 45f;
        toast.setPosition(x, y);

        stage.addActor(toast);

        toast.addAction(Actions.sequence(
            Actions.fadeIn(0.2f),
            Actions.delay(2.2f),
            Actions.fadeOut(0.3f),
            Actions.removeActor()
        ));
    }
}
