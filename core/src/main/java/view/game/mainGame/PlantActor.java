package view.game.mainGame;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import model.entities.PlantType;
import model.entities.plant.Plant;
import pvz.libpvz.pam.PamPlayer;

public class PlantActor extends Actor {
    private final PamPlayer player;
    private final Plant modelPlant;
    private final PlantType type;
    private float stateTime = 0f;

    public PlantActor(PamPlayer player, Plant modelPlant) {
        this.player = player;
        this.modelPlant = modelPlant;
        this.type = PlantType.fromName(modelPlant.getName());
        setSize(90, 110);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null || type == null) return;

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY() + getHeight() / 2f;

        Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
        Matrix4 newMatrix = new Matrix4(oldMatrix);
        newMatrix.translate(centerX, centerY, 0);
        newMatrix.scale(type.getScale(), type.getScale(), 1f);
        batch.setTransformMatrix(newMatrix);

        try {
            player.draw(batch, type.getPamPath(), null, stateTime, 0f, 0f, true);
        } catch (Exception ignored) {}

        batch.setTransformMatrix(oldMatrix);
    }

    public Plant getModelPlant() { return modelPlant; }
    public PlantType getType() { return type; }
}
