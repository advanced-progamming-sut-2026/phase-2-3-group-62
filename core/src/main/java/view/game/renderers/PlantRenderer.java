package view.game.renderers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import model.Game;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import pvz.libpvz.pam.PamPlayer;
import view.game.GamePlayScreen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlantRenderer {
    private final PamPlayer pamPlayer;
    private final Map<String, String> pamPathCache = new HashMap<>();

    public PlantRenderer(PamPlayer pamPlayer) {
        this.pamPlayer = pamPlayer;
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

    private void drawWithFallback(SpriteBatch batch, String pamPath, String[] clipCandidates, float stateTime, float x, float y) {
        for (String clip : clipCandidates) {
            try {
                pamPlayer.draw(batch, pamPath, clip, stateTime, x, y, true);
                return;
            } catch (Exception ignored) {}
        }
        try {
            pamPlayer.draw(batch, pamPath, null, stateTime, x, y, true);
        } catch (Exception ignored) {}
    }

    public void render(SpriteBatch batch, Game game, float stateTime, float delta) {
        if (game == null) return;

        for (Plant plant : game.getActivePlants()) {
            plant.updateAnimState(delta);
            Vector2 pPos = GamePlayScreen.getTileCenterPosition(plant.getY(), plant.getX());

            String pamPath = plant.getPamPath();
            if (pamPath == null || pamPath.isEmpty() || (pamPath.contains("PEASHOOTER.PAM") && !plant.getName().equalsIgnoreCase("Peashooter"))) {
                String cached = pamPathCache.get(normalize(plant.getName()));
                if (cached != null && !cached.isEmpty()) {
                    pamPath = cached;
                }
            }

            if (pamPath != null && !pamPath.isEmpty()) {
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
}
