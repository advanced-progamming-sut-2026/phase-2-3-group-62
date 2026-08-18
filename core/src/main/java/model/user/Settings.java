package model.user;

public class Settings {
    private String autoLoginUsername;
    private int difficulty = 3;
    private int gameSpeed = 1;
    private boolean showGrid = false;
    private boolean debugMode = false;
    private float musicVolume = 0.8f;
    private float sfxVolume = 0.8f;

    public Settings() {
    }

    public String getAutoLoginUsername() {
        return autoLoginUsername;
    }

    public void setAutoLoginUsername(String autoLoginUsername) {
        this.autoLoginUsername = autoLoginUsername;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getGameSpeed() {
        return gameSpeed;
    }

    public void setGameSpeed(int gameSpeed) {
        this.gameSpeed = gameSpeed;
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float musicVolume) {
        this.musicVolume = musicVolume;
    }

    public float getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(float sfxVolume) {
        this.sfxVolume = sfxVolume;
    }
}
