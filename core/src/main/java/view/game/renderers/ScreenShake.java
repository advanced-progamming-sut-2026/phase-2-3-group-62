package view.game.renderers;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

public class ScreenShake {
    private static float shakeTime = 0f;
    private static float shakeIntensity = 0f;
    private static final Vector3 originalPos = new Vector3();
    private static boolean isInitialized = false;

    public static void shake(float intensity, float duration) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeTime = Math.max(shakeTime, duration);
    }

    public static void update(Camera camera, float delta) {
        if (camera == null) return;

        if (!isInitialized) {
            originalPos.set(camera.position);
            isInitialized = true;
        }

        if (shakeTime > 0) {
            shakeTime -= delta;
            float currentIntensity = shakeIntensity * (shakeTime > 0 ? (shakeTime / 0.5f) : 0f);
            float offsetX = MathUtils.random(-currentIntensity, currentIntensity);
            float offsetY = MathUtils.random(-currentIntensity, currentIntensity);
            camera.position.set(originalPos.x + offsetX, originalPos.y + offsetY, originalPos.z);
            camera.update();

            if (shakeTime <= 0) {
                shakeTime = 0;
                shakeIntensity = 0;
                camera.position.set(originalPos);
                camera.update();
            }
        }
    }
}
