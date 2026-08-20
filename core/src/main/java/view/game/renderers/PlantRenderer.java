package view.game.renderers;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import model.Game;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import pvz.libpvz.pam.PamPlayer;
import view.game.GamePlayScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantRenderer {
    private final PamPlayer pamPlayer;
    private final Map<String, String> pamPathCache = new HashMap<>();
    private final BitmapFont timerFont = new BitmapFont();
    private final GlyphLayout glyphLayout = new GlyphLayout();

    public PlantRenderer(PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
        this.timerFont.setColor(Color.YELLOW);
        this.timerFont.getData().setScale(0.9f);
        List<Plant> loaded = PlantLoader.loadPlants();
        if (loaded != null) {
            for (Plant p : loaded) {
                if (p.getName() != null && p.getPamPath() != null) {
                    pamPathCache.put(normalize(p.getName()), p.getPamPath());
                }
            }
        }
    }

    private String normalize(String name) {
        return name == null ? "" : name.replaceAll("[\\s_-]", "").toLowerCase();
    }

    private void drawWithFallback(SpriteBatch batch, String pamPath, String[] clipCandidates, float animTime, float x, float y) {
        for (String clip : clipCandidates) {
            try {
                pamPlayer.draw(batch, pamPath, clip, animTime, x, y, true);
                return;
            } catch (Exception ignored) {}
        }
        try {
            pamPlayer.draw(batch, pamPath, null, animTime, x, y, true);
        } catch (Exception ignored) {}
    }

    public void renderRow(SpriteBatch batch, Game game, int row, float stateTime, float delta) {
        if (game == null || game.getActivePlants() == null) return;

        List<Plant> rowPlants = new ArrayList<>();
        for (Plant plant : game.getActivePlants()) {
            if (plant.getY() == row) {
                plant.updateAnimState(delta);
                rowPlants.add(plant);
            }
        }

        for (Plant plant : rowPlants) {
            if (!normalize(plant.getName()).equals("pumpkin")) {
                renderPlant(batch, plant, stateTime);
            }
        }

        for (Plant plant : rowPlants) {
            if (normalize(plant.getName()).equals("pumpkin")) {
                renderPlant(batch, plant, stateTime);
            }
        }
    }

    private void renderPlant(SpriteBatch batch, Plant plant, float stateTime) {
        Vector2 pPos = GamePlayScreen.getTileCenterPosition(plant.getY(), plant.getX());

        String pamPath = plant.getPamPath();
        if (pamPath == null || pamPath.isEmpty() || (pamPath.contains("PEASHOOTER.PAM") && !plant.getName().equalsIgnoreCase("Peashooter"))) {
            String cached = pamPathCache.get(normalize(plant.getName()));
            if (cached != null && !cached.isEmpty()) {
                pamPath = cached;
            }
        }

        if (pamPath != null && !pamPath.isEmpty()) {
            String cleanName = normalize(plant.getName());
            if (cleanName.contains("potatomine")) {
                String state = plant.getAnimState();
                String[] clips;

                if ("attack".equals(state)) {
                    clips = new String[]{"attack", "anim_attack", "idle", "plant_idle"};
                } else if (!plant.isArmed()) {
                    clips = new String[]{"plant_idle", "anim_plant_idle", "unarmed", "idle"};
                } else {
                    clips = new String[]{"idle", "anim_idle", "armed", "plant_idle"};
                }

                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);

                if (!plant.isArmed() && plant.getArmTimerTicks() > 0) {
                    float secondsLeft = plant.getArmTimerTicks() * 0.1f;
                    String timeText = String.format("%.1fs", secondsLeft);
                    glyphLayout.setText(timerFont, timeText);
                    timerFont.draw(batch, timeText, pPos.x - (glyphLayout.width / 2f), pPos.y - 35f);
                }
            } else if (cleanName.contains("squash")) {
                String state = plant.getAnimState();
                String[] clips;
                if ("jump_down_right".equals(state) || "attack".equals(state)) {
                    clips = new String[]{"jump_down_right", "anim_jump_down_right", "attack", "idle"};
                } else {
                    clips = new String[]{"idle", "anim_idle"};
                }
                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.contains("tanglekelp")) {
                String state = plant.getAnimState();
                String[] clips;
                if ("plantfood".equals(state) || "plantfood_on".equals(state)) {
                    clips = new String[]{"plantfood", "anim_plantfood", "special", "anim_special", "attack", "idle"};
                } else if ("attack".equals(state)) {
                    clips = new String[]{"attack", "anim_attack", "grab", "idle"};
                } else {
                    clips = new String[]{"idle", "anim_idle"};
                }
                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.contains("endurian")) {
                String state = plant.getAnimState();
                String[] clips;
                if ("plantfood_on".equals(state) || "plantfood".equals(state)) {
                    clips = new String[]{"plantfood_on", "anim_plantfood_on", "plantfood", "idle"};
                } else if ("attack_loop".equals(state) || "attack".equals(state)) {
                    clips = new String[]{"attack_loop", "anim_attack_loop", "attack", "idle"};
                } else {
                    clips = new String[]{"idle", "anim_idle"};
                }
                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.contains("chomper")) {
                String state = plant.getAnimState();
                String[] clips;
                if ("plantfood".equals(state)) {
                    clips = new String[]{"plantfood", "anim_plantfood", "special", "bite", "idle"};
                } else if ("bite".equals(state) || "attack".equals(state)) {
                    clips = new String[]{"bite", "anim_bite", "attack", "idle"};
                } else if ("special".equals(state) || plant.isDigesting()) {
                    clips = new String[]{"special", "anim_special", "chew", "anim_chew", "idle"};
                } else {
                    clips = new String[]{"idle", "anim_idle"};
                }
                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.contains("phatbeet")) {
                String state = plant.getAnimState();
                String[] clips;
                if ("plantfood".equals(state)) {
                    clips = new String[]{"plantfood", "anim_plantfood", "attack", "idle"};
                } else if ("attack".equals(state)) {
                    clips = new String[]{"attack", "anim_attack", "pulse", "idle"};
                } else {
                    clips = new String[]{"idle", "anim_idle"};
                }
                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.contains("kiwibeast")) {
                int stage = Math.max(1, Math.min(3, plant.getPlantStage()));
                String suffix = (stage == 1) ? "" : String.valueOf(stage);
                String state = plant.getAnimState();
                String[] clips;
                if ("plantfood".equals(state)) {
                    clips = new String[]{"plantfood" + suffix, "anim_plantfood" + suffix, "attack" + suffix, "idle" + suffix};
                } else if ("attack".equals(state)) {
                    clips = new String[]{"attack" + suffix, "anim_attack" + suffix, "stomp" + suffix, "idle" + suffix};
                } else {
                    clips = new String[]{"idle" + suffix, "anim_idle" + suffix, "idle"};
                }
                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.contains("mint")) {
                int remainingTicks = plant.getLifespanTicks();
                int elapsedTicks = 60 - remainingTicks;
                float animTime;
                String[] clips;

                if (elapsedTicks <= 15) {
                    animTime = elapsedTicks * 0.1f;
                    clips = new String[]{"intro", "anim_intro", "appear", "spawn", "anim_idle", "idle"};
                } else if (remainingTicks <= 15) {
                    animTime = (15 - remainingTicks) * 0.1f;
                    clips = new String[]{"outro", "anim_outro", "leave", "disappear", "anim_disappear", "anim_idle", "idle"};
                } else {
                    animTime = (elapsedTicks - 15) * 0.1f;
                    clips = new String[]{"loop", "anim_loop", "active", "anim_active", "anim_idle", "idle"};
                }

                drawWithFallback(batch, pamPath, clips, animTime, pPos.x, pPos.y);
            } else if (cleanName.equals("goldbloom")) {
                int remainingTicks = plant.getLifespanTicks();
                int elapsedTicks = 20 - remainingTicks;
                float animTime = elapsedTicks * 0.1f;
                String[] clips = new String[]{"anim_produce", "produce", "anim_attack", "attack", "anim_idle", "idle"};
                drawWithFallback(batch, pamPath, clips, animTime, pPos.x, pPos.y);
            } else if (cleanName.equals("sunshroom")) {
                int stage = plant.getPlantStage();
                if (stage < 1) stage = 1;
                if (stage > 3) stage = 3;

                String state = plant.getAnimState();
                String[] clips;

                if ("growth".equals(state)) {
                    clips = new String[]{"growth_stage" + stage, "growth" + stage, "growth", "idle_stage" + stage};
                } else if ("plantfood".equals(state)) {
                    clips = new String[]{"plantfood_stage" + stage, "plantfood" + stage, "plantfood", "special_stage" + stage, "idle_stage" + stage};
                } else if ("attack".equals(state)) {
                    clips = new String[]{"special_stage" + stage, "special" + stage, "special", "anim_produce_stage" + stage, "idle_stage" + stage};
                } else {
                    clips = new String[]{"idle_stage" + stage, "idle" + stage, "anim_idle_stage" + stage, "idle"};
                }

                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else if (cleanName.equals("peapod")) {
                int h = Math.max(1, Math.min(5, plant.getPeaPodHeads()));
                String headSuffix = (h == 1) ? "" : String.valueOf(h);
                String state = plant.getAnimState();
                String[] clips;

                if ("plantfood".equals(state)) {
                    clips = new String[]{
                        "plantfood" + headSuffix,
                        "anim_plantfood" + headSuffix,
                        "plantfood",
                        "attack" + headSuffix,
                        "idle" + headSuffix
                    };
                } else if ("attack".equals(state)) {
                    String targetAttackClip = "attack" + headSuffix;
                    String targetIdleClip = "idle" + headSuffix;
                    clips = new String[]{
                        targetAttackClip,
                        "anim_" + targetAttackClip,
                        targetIdleClip
                    };
                } else {
                    String targetIdleClip = "idle" + headSuffix;
                    clips = new String[]{
                        targetIdleClip,
                        "anim_" + targetIdleClip
                    };
                }

                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            } else {
                String state = plant.getAnimState();
                String[] clips;
                if ("plantfood".equals(state)) {
                    clips = new String[]{"anim_plantfood", "plantfood", "anim_power", "anim_super", "anim_shooting", "anim_idle"};
                } else if ("attack".equals(state)) {
                    clips = new String[]{"anim_shooting", "anim_shoot", "shooting", "attack", "anim_attack", "anim_sun", "anim_produce", "anim_idle"};
                } else {
                    clips = new String[]{"anim_idle", "idle", null};
                }

                drawWithFallback(batch, pamPath, clips, stateTime, pPos.x, pPos.y);
            }
        }

        if (plant.isHasSunToCollect()) {
            try {
                pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", "animation", stateTime, pPos.x, pPos.y + 40f, true);
            } catch (Exception e) {
                try {
                    pamPlayer.draw(batch, "768/INITIAL/EFFECTS/SUN/SUN.PAM", null, stateTime, pPos.x, pPos.y + 40f, true);
                } catch (Exception ignored) {}
            }
        }
    }
}
