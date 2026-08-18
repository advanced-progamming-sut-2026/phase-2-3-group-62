package view.game;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import pvz.libpvz.pam.PamPlayer;

public class WindEffect {
    private static final String PAM_PATH = "768/FULL/EFFECTS/FROSTBITE_CHILL_WIND/FROSTBITE_CHILL_WIND.PAM";
    private final float x;
    private final float y;
    private float timer;

    public WindEffect(float x, float y, float duration) {
        this.x = x;
        this.y = y;
        this.timer = duration;
    }

    public boolean update(float delta) {
        timer -= delta;
        return timer > 0;
    }

    public void render(SpriteBatch batch, PamPlayer pamPlayer, float stateTime) {
        if (pamPlayer == null) return;
        try {
            pamPlayer.draw(batch, PAM_PATH, "animation", stateTime, x, y, true);
        } catch (Exception e1) {
            try {
                pamPlayer.draw(batch, PAM_PATH, "idle", stateTime, x, y, true);
            } catch (Exception e2) {
                try {
                    pamPlayer.draw(batch, PAM_PATH, "", stateTime, x, y, true);
                } catch (Exception ignored) {}
            }
        }
    }
}
