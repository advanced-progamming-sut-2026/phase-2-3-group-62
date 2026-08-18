package view.greenhouse;

import model.greenhouse.Greenhouse;
import model.greenhouse.Pot;
import view.game.View;

public class GreenhouseView extends View {
    public void showGreenhouse(Greenhouse greenhouse) {
        showGreenhouseState(greenhouse);
    }

    public void showGreenhouseState(Greenhouse greenhouse) {
        if (greenhouse == null) return;
        showMessage("\n=== GREENHOUSE STATUS ===");
        for (Pot pot : greenhouse.getPots()) {
            String status;
            if (pot.isLocked()) {
                status = "[LOCKED]";
            } else if (pot.isEmpty()) {
                status = "[EMPTY]";
            } else if (pot.isReadyToHarvest()) {
                status = "[READY]";
            } else {
                int progress = (int) (pot.getGrowthProgress() * 100);
                long remainingMillis = pot.getRemainingTime();
                long remainingSecs = remainingMillis / 1000;
                long hours = remainingSecs / 3600;
                long mins = (remainingSecs % 3600) / 60;
                status = String.format("[GROWING: %d%%] (Time left: %02dh:%02dm)", progress, hours, mins);
            }
            showMessage(String.format("Pot (Row %d, Col %d): %s", pot.getRow(), pot.getColumn(), status));
        }
        showMessage("=========================\n");
    }
}