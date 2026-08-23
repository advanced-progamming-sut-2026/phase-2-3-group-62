package view.game.renderers;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import model.Game;
import model.board.Tile;
import model.minigame.Vasebreaker;
import pvz.libpvz.pam.PamPlayer;
import pvz.libpvz.textures.TextureBank;
import view.game.mainGame.GameGrid;
import view.ui.PamActor;

import java.util.HashMap;
import java.util.Map;

public class VaseRenderer {
    private final Map<String, PamActor> actors = new HashMap<>();

    public void render(SpriteBatch batch, PamPlayer pamPlayer, TextureBank textureBank, Game game, float delta) {
        if (game == null || !(game.getActiveMiniGame() instanceof Vasebreaker) || pamPlayer == null) {
            return;
        }

        Vasebreaker vb = (Vasebreaker) game.getActiveMiniGame();
        int rows = game.getBoard().getRows();
        int cols = game.getBoard().getColumns();

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                Tile tile = game.getBoard().getTile(r, c);
                if (tile != null && tile.getTemporarySeedPacket() != null && textureBank != null) {
                    Vector2 pos = GameGrid.getTileCenterPosition(r, c);
                    String packetRegion = "IMAGE_UI_PACKETS_" + tile.getTemporarySeedPacket().toUpperCase().replace(" ", "").replace("-", "");
                    if (textureBank.region(packetRegion) != null) {
                        float pw = 80f;
                        float ph = 110f;
                        batch.draw(textureBank.region(packetRegion), pos.x - pw / 2f, pos.y - ph / 2f, pw, ph);
                    }
                }

                if (!vb.hasVase(r, c)) {
                    continue;
                }

                boolean isBroken = vb.isVaseBroken(r, c);
                if (isBroken && vb.getVaseAnimTimer(r, c) <= 0) {
                    continue;
                }

                String clip = isBroken ? "break" : "idle";
                Vasebreaker.VaseType type = vb.getVaseType(r, c);
                String pamPath = type.getPamPath();
                String actorKey = r + "_" + c + "_" + type.name() + "_" + clip;

                PamActor actor = actors.computeIfAbsent(actorKey, k ->
                    new PamActor(pamPlayer, pamPath, clip, 0.90f)
                );

                Vector2 center = GameGrid.getTileCenterPosition(r, c);
                actor.setPosition(center.x, center.y);
                actor.act(delta);
                actor.draw(batch, 1.0f);
            }
        }
    }

    public void clearCache() {
        actors.clear();
    }
}
