package view.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import pvz.libpvz.pam.PamPlayer;

import java.util.Map;

public class PamActor extends Actor {
    private final PamPlayer player;
    private final String pamPath;
    private final String clipName;
    private float stateTime;
    private boolean isHovered;
    private float scaleFactor;
    private float offsetX;
    private float offsetY;
    private Map<String, Boolean> layerVisibility;

    public PamActor(PamPlayer player, String pamPath, String clipName, float scaleFactor) {
        this(player, pamPath, clipName, scaleFactor, 0f, 0f, null);
    }

    public PamActor(PamPlayer player, String pamPath, String clipName, float scaleFactor, float offsetX, float offsetY) {
        this(player, pamPath, clipName, scaleFactor, offsetX, offsetY, null);
    }

    public PamActor(PamPlayer player, String pamPath, String clipName, float scaleFactor, float offsetX, float offsetY, Map<String, Boolean> layerVisibility) {
        this.player = player;
        this.pamPath = pamPath;
        this.clipName = clipName;
        this.scaleFactor = scaleFactor;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.layerVisibility = layerVisibility;
        this.stateTime = 0f;
    }

    public void setHovered(boolean hovered) {
        this.isHovered = hovered;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public void setOffset(float offsetX, float offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public void setLayerVisibility(Map<String, Boolean> layerVisibility) {
        this.layerVisibility = layerVisibility;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta * (isHovered ? 1.3f : 1.0f);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null || pamPath == null) return;

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;

        Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
        Matrix4 newMatrix = new Matrix4(oldMatrix);
        newMatrix.translate(centerX, centerY, 0);
        newMatrix.scale(scaleFactor, scaleFactor, 1f);
        batch.setTransformMatrix(newMatrix);

        try {
            if (layerVisibility != null) {
                player.draw(batch, pamPath, clipName, stateTime, offsetX, offsetY, true, layerVisibility);
            } else {
                player.draw(batch, pamPath, clipName, stateTime, offsetX, offsetY, true);
            }
        } catch (Exception e) {
            try {
                if (layerVisibility != null) {
                    player.draw(batch, pamPath, null, stateTime, offsetX, offsetY, true, layerVisibility);
                } else {
                    player.draw(batch, pamPath, null, stateTime, offsetX, offsetY, true);
                }
            } catch (Exception ignored) {}
        }

        batch.setTransformMatrix(oldMatrix);
    }
}
