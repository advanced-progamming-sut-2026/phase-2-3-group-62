package view.game;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import model.entities.ZombieType;
import model.entities.zombie.Zombie;
import pvz.libpvz.pam.PamPlayer;

public class ZombieActor extends Actor {
    public enum State { WALK, ATTACK, DIE }

    private final PamPlayer player;
    private final Zombie modelZombie;
    private final ZombieType type;
    private State currentState = State.WALK;
    private float stateTime = 0f;

    public ZombieActor(PamPlayer player, Zombie modelZombie) {
        this.player = player;
        this.modelZombie = modelZombie;
        this.type = ZombieType.fromId(modelZombie.getName());
        setSize(100, 140);
    }

    public void setState(State state) {
        if (this.currentState != state) {
            this.currentState = state;
            this.stateTime = 0f;
        }
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        float speedMultiplier = (float) (modelZombie.getEffectiveSpeed() / 0.185);
        if (speedMultiplier <= 0f) speedMultiplier = 1f;
        stateTime += delta * speedMultiplier;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        if (player == null || type == null) return;

        float centerX = getX() + getWidth() / 2f;
        float centerY = getY();

        Matrix4 oldMatrix = batch.getTransformMatrix().cpy();
        Matrix4 newMatrix = new Matrix4(oldMatrix);
        newMatrix.translate(centerX, centerY, 0);
        newMatrix.scale(type.getScale(), type.getScale(), 1f);
        batch.setTransformMatrix(newMatrix);

        boolean loop = (currentState != State.DIE);

        try {
            player.draw(batch, type.getPamPath(), null, stateTime, type.getOffsetX(), type.getOffsetY(), loop);
        } catch (Exception ignored) {}

        batch.setTransformMatrix(oldMatrix);
    }

    public Zombie getModelZombie() { return modelZombie; }
    public ZombieType getType() { return type; }
}
