package model.greenhouse;

import model.user.User;
import model.user.UserSession;
import java.util.ArrayList;
import java.util.List;

public class Greenhouse {
    private final List<Pot> pots;
    private static final int MAX_POTS = 20;
    private static final int ROWS = 4;
    private static final int COLS = 5;

    public Greenhouse() {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser != null) {
            this.pots = currentUser.getGreenhousePots();
        } else {
            this.pots = new ArrayList<>();
            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    Pot pot = new Pot(row, col);
                    if (row > 0) {
                        pot.setLocked(true);
                    }
                    pots.add(pot);
                }
            }
        }
    }

    public void addPot(Pot pot) {
        if (pots.size() < MAX_POTS) {
            pots.add(pot);
        }
    }

    public Pot getPot(int row, int column) {
        if (row < 0 || row >= ROWS || column < 0 || column >= COLS) {
            return null;
        }
        int index = row * COLS + column;
        if (index >= 0 && index < pots.size()) {
            return pots.get(index);
        }
        return null;
    }

    public boolean unlockPot(int row, int column) {
        Pot pot = getPot(row, column);
        if (pot != null && pot.isLocked()) {
            pot.setLocked(false);
            return true;
        }
        return false;
    }

    public int getUnlockedPotCount() {
        int count = 0;
        for (Pot pot : pots) {
            if (!pot.isLocked()) {
                count++;
            }
        }
        return count;
    }

    public int getLockedPotCount() {
        return pots.size() - getUnlockedPotCount();
    }

    public List<Pot> getPots() { return pots; }
    public List<Pot> getAvailablePots() {
        List<Pot> available = new ArrayList<>();
        for (Pot pot : pots) {
            if (!pot.isLocked() && pot.isEmpty()) {
                available.add(pot);
            }
        }
        return available;
    }

    
    public List<Pot> getPots() { return pots; }
    public List<Pot> getAvailablePots() {
        List<Pot> available = new ArrayList<>();
        for (Pot pot : pots) {
            if (!pot.isLocked() && pot.isEmpty()) {
                available.add(pot);
            }
        }
        return available;
    }


    public void updateAllPots() {
        for (Pot pot : pots) {
            pot.update();
        }
    }

    public static int getMaxPots() { return MAX_POTS; }
    public static int getRows() { return ROWS; }
    public static int getCols() { return COLS; }
}
