package view.game.hud;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import main.Maini;
import model.Game;
import model.entities.zombie.Spawner;
import model.entities.zombie.Zombie;
import model.entities.zombie.boss.Zomboss;

public class GamePlayProgressBar {
    private final Table bottomHud = new Table();
    private WidgetGroup normalProgressBarGroup;
    private WidgetGroup bossHealthBarGroup;

    private Texture progressTrackTexture;
    private Texture progressFillTexture;
    private Texture bossBarFrameTexture;
    private Texture bossSegmentFillTexture;
    private TextureRegion waveFlagRegion;

    private Image progressFillImage;
    private Group flagGroup;
    private final Image[] bossSegments = new Image[3];

    private static final float PROGRESS_BAR_WIDTH = 540f;
    private static final float PROGRESS_BAR_HEIGHT = 38f;
    private static final float BORDER_PADDING = 4f;
    private static final float MAX_FILL_WIDTH = PROGRESS_BAR_WIDTH - (BORDER_PADDING * 2f);
    private static final float FILL_HEIGHT = PROGRESS_BAR_HEIGHT - (BORDER_PADDING * 2f);
    private static final float ESTIMATED_WAVE_DURATION = 25f;

    private float continuousProgressFraction = 0f;
    private float timeInCurrentWave = 0f;
    private int currentTrackedWave = 1;
    private int builtFlagsTotalWaves = -1;

    public GamePlayProgressBar(Maini game) {
        initTextures(game);
        buildUI();
    }

    private void initTextures(Maini game) {
        waveFlagRegion = game.getTextureBank().region("IMAGE_ZOMBIE_ZOMBIE_BIGHEAD_FLAG_ZOMBIE_BIGHEAD_FLAG_123X95");
        progressTrackTexture = createProgressTrackTexture(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        progressFillTexture = createProgressFillTexture();
        bossBarFrameTexture = createBossBarTrackTexture(560f, 44f);
        bossSegmentFillTexture = createBossSegmentTexture();
    }

    private void buildUI() {
        bottomHud.center();

        normalProgressBarGroup = new WidgetGroup();
        normalProgressBarGroup.setSize(PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);

        Image progressBg = new Image(new TextureRegionDrawable(progressTrackTexture));
        progressBg.setBounds(0, 0, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        progressBg.setScaling(Scaling.stretch);
        progressBg.setTouchable(Touchable.disabled);
        normalProgressBarGroup.addActor(progressBg);

        progressFillImage = new Image(new TextureRegionDrawable(progressFillTexture));
        progressFillImage.setBounds(PROGRESS_BAR_WIDTH - BORDER_PADDING, BORDER_PADDING, 0, FILL_HEIGHT);
        progressFillImage.setTouchable(Touchable.disabled);
        normalProgressBarGroup.addActor(progressFillImage);

        flagGroup = new Group();
        flagGroup.setBounds(0, 0, PROGRESS_BAR_WIDTH, PROGRESS_BAR_HEIGHT);
        flagGroup.setTouchable(Touchable.disabled);
        normalProgressBarGroup.addActor(flagGroup);

        bossHealthBarGroup = new WidgetGroup();
        bossHealthBarGroup.setSize(560f, 44f);
        bossHealthBarGroup.setVisible(false);

        Image bossBg = new Image(new TextureRegionDrawable(bossBarFrameTexture));
        bossBg.setBounds(0, 0, 560f, 44f);
        bossBg.setScaling(Scaling.stretch);
        bossBg.setTouchable(Touchable.disabled);
        bossHealthBarGroup.addActor(bossBg);

        float segW = (560f - 16f) / 3f;
        for (int i = 0; i < 3; i++) {
            bossSegments[i] = new Image(new TextureRegionDrawable(bossSegmentFillTexture));
            bossSegments[i].setBounds(4f + (i * (segW + 4f)), 4f, segW, 36f);
            bossSegments[i].setScaling(Scaling.stretch);
            bossSegments[i].setTouchable(Touchable.disabled);
            bossHealthBarGroup.addActor(bossSegments[i]);
        }

        Stack bottomBarStack = new Stack();
        bottomBarStack.add(normalProgressBarGroup);
        bottomBarStack.add(bossHealthBarGroup);

        bottomHud.add(bottomBarStack).size(560f, 44f).padBottom(16);
    }

    public void update(Game modelGame, float delta, float speedMultiplier) {
        if (modelGame == null) return;

        if (modelGame.getActiveMiniGame() != null) {
            bottomHud.setVisible(false);
            normalProgressBarGroup.setVisible(false);
            bossHealthBarGroup.setVisible(false);
            return;
        }

        Zomboss activeBoss = null;
        if (modelGame.getActiveZombies() != null) {
            for (Zombie z : modelGame.getActiveZombies()) {
                if (z instanceof Zomboss) {
                    activeBoss = (Zomboss) z;
                    break;
                }
            }
        }

        if (activeBoss != null) {
            bottomHud.setVisible(true);
            normalProgressBarGroup.setVisible(false);
            bossHealthBarGroup.setVisible(true);

            int phase = activeBoss.getCurrentPhase();
            float segW = (560f - 16f) / 3f;
            float currentPhaseRatio = (float) activeBoss.getPhaseCurrentHealth() / (float) activeBoss.getPhaseMaxHealth();

            for (int i = 0; i < 3; i++) {
                int segPhaseIndex = 3 - i;
                if (phase < segPhaseIndex) {
                    bossSegments[i].setVisible(true);
                    bossSegments[i].setBounds(4f + (i * (segW + 4f)), 4f, segW, 36f);
                } else if (phase == segPhaseIndex) {
                    bossSegments[i].setVisible(true);
                    bossSegments[i].setBounds(4f + (i * (segW + 4f)), 4f, segW * currentPhaseRatio, 36f);
                } else {
                    bossSegments[i].setVisible(false);
                }
            }
        } else {
            bottomHud.setVisible(true);
            normalProgressBarGroup.setVisible(true);
            bossHealthBarGroup.setVisible(false);

            if (modelGame.getSpawner() != null) {
                Spawner spawner = modelGame.getSpawner();
                int totalWaves = Math.max(1, spawner.getTotalWaves());
                int currentWave = Math.max(1, spawner.getCurrentWave());

                updateFlagsOnBar(totalWaves);

                if (currentWave != currentTrackedWave) {
                    currentTrackedWave = currentWave;
                    timeInCurrentWave = 0f;
                } else {
                    timeInCurrentWave += (delta * speedMultiplier);
                }

                float waveLocalFraction = Math.min(1.0f, timeInCurrentWave / ESTIMATED_WAVE_DURATION);
                float targetFraction = ((currentWave - 1) + waveLocalFraction) / (float) totalWaves;
                targetFraction = Math.min(1.0f, Math.max(0f, targetFraction));

                continuousProgressFraction = continuousProgressFraction + (targetFraction - continuousProgressFraction) * Math.min(delta * 4f, 1f);

                if (progressFillImage != null) {
                    float currentWidth = MAX_FILL_WIDTH * continuousProgressFraction;
                    float startX = (PROGRESS_BAR_WIDTH - BORDER_PADDING) - currentWidth;
                    progressFillImage.setBounds(startX, BORDER_PADDING, currentWidth, FILL_HEIGHT);
                }
            }
        }
    }

    private void updateFlagsOnBar(int totalWaves) {
        if (totalWaves <= 0 || flagGroup == null || builtFlagsTotalWaves == totalWaves) return;

        flagGroup.clear();
        builtFlagsTotalWaves = totalWaves;

        for (int wave = 1; wave <= totalWaves; wave++) {
            float waveFraction = (float) wave / (float) totalWaves;
            float flagX = (PROGRESS_BAR_WIDTH - BORDER_PADDING) - (MAX_FILL_WIDTH * waveFraction) - 18f;

            if (waveFlagRegion != null) {
                Image flagImage = new Image(waveFlagRegion);
                flagImage.setSize(44f, 36f);
                flagImage.setPosition(flagX, 2f);
                flagImage.setTouchable(Touchable.disabled);
                flagGroup.addActor(flagImage);
            }
        }
    }

    private Texture createBossBarTrackTexture(float width, float height) {
        int w = (int) width;
        int h = (int) height;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.12f, 0.12f, 0.14f, 0.95f);
        pixmap.fillRectangle(0, 0, w, h);
        pixmap.setColor(0.35f, 0.35f, 0.38f, 1f);
        pixmap.drawRectangle(0, 0, w, h);
        pixmap.drawRectangle(1, 1, w - 2, h - 2);

        int segW = (w - 16) / 3;
        pixmap.setColor(0.04f, 0.04f, 0.05f, 1f);
        for (int i = 0; i < 3; i++) {
            pixmap.fillRectangle(4 + (i * (segW + 4)), 4, segW, h - 8);
        }
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createBossSegmentTexture() {
        int w = 1;
        int h = 30;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float t = (float) y / (float) h;
            float r = 0.85f + 0.15f * (1f - Math.abs(t - 0.5f) * 2f);
            pixmap.setColor(r, 0.12f, 0.12f, 1f);
            pixmap.drawPixel(0, y);
        }
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createProgressTrackTexture(float width, float height) {
        int w = (int) width;
        int h = (int) height;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pixmap.setColor(0f, 0f, 0f, 0.96f);
        pixmap.fillRectangle(0, 0, w, h);
        pixmap.setColor(0.14f, 0.14f, 0.16f, 0.95f);
        pixmap.fillRectangle((int) BORDER_PADDING, (int) BORDER_PADDING, (int) (w - BORDER_PADDING * 2), (int) (h - BORDER_PADDING * 2));
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    private Texture createProgressFillTexture() {
        int w = 1;
        int h = 30;
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        for (int y = 0; y < h; y++) {
            float t = (float) y / (float) h;
            float r = 0.42f + 0.35f * (1f - Math.abs(t - 0.5f) * 2f);
            float g = 0.96f;
            float b = 0.32f;
            pixmap.setColor(r, g, b, 1f);
            pixmap.drawPixel(0, y);
        }
        Texture tex = new Texture(pixmap);
        pixmap.dispose();
        return tex;
    }

    public Table getRoot() {
        return bottomHud;
    }

    public void dispose() {
        if (progressTrackTexture != null) progressTrackTexture.dispose();
        if (progressFillTexture != null) progressFillTexture.dispose();
        if (bossBarFrameTexture != null) bossBarFrameTexture.dispose();
        if (bossSegmentFillTexture != null) bossSegmentFillTexture.dispose();
    }
}
