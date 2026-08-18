package view.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import model.season.Season;
import model.user.Settings;
import util.FileManager;

public class AudioManager {
    private static AudioManager instance;

    private Music currentMusic;
    private String currentMusicPath = "";
    private Sound clickSound;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private float musicVolume = 0.65f;
    private float soundVolume = 0.85f;

    private AudioManager() {
        loadSettingsFromStorage();
        loadClickSound();
    }

    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }

    public void loadSettingsFromStorage() {
        try {
            Settings settings = FileManager.loadSettings();
            if (settings != null) {
                this.musicVolume = settings.getMusicVolume();
                this.soundVolume = settings.getSfxVolume();
            }
        } catch (Exception ignored) {}
    }

    private void saveSettingsToStorage() {
        try {
            Settings settings = FileManager.loadSettings();
            if (settings == null) {
                settings = new Settings();
            }
            settings.setMusicVolume(this.musicVolume);
            settings.setSfxVolume(this.soundVolume);
            FileManager.saveSettings(settings);
        } catch (Exception ignored) {}
    }

    private FileHandle resolveFile(String fileName) {
        String[] possiblePaths = {
            "music/" + fileName,
            "assets/music/" + fileName,
            fileName
        };

        for (String path : possiblePaths) {
            try {
                FileHandle handle = Gdx.files.internal(path);
                if (handle.exists()) {
                    return handle;
                }
            } catch (Exception ignored) {}

            try {
                FileHandle handle = Gdx.files.absolute(path);
                if (handle.exists()) {
                    return handle;
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private void loadClickSound() {
        try {
            FileHandle handle = resolveFile("Voicy_Button Click.mp3");
            if (handle != null) {
                clickSound = Gdx.audio.newSound(handle);
            }
        } catch (Exception ignored) {}
    }

    public void playButtonClick() {
        if (!soundEnabled) return;
        if (clickSound == null) {
            loadClickSound();
        }
        if (clickSound != null) {
            try {
                clickSound.play(soundVolume);
            } catch (Exception ignored) {}
        }
    }

    public void playTitleMusic() {
        playMusic("Title_Screen.mp3");
    }

    public void playSeasonMusic(Season season) {
        if (season == null || season.getName() == null) {
            playMusic("Ancient_Egypt.mp3");
            return;
        }

        String name = season.getName().toLowerCase();
        if (name.contains("egypt")) {
            playMusic("Ancient_Egypt.mp3");
        } else if (name.contains("beach")) {
            playMusic("Big_Wave_Beach.mp3");
        } else if (name.contains("dark")) {
            playMusic("Dark_Ages.mp3");
        } else if (name.contains("cave") || name.contains("frost") || name.contains("ice")) {
            playMusic("Frostbite_Caves.mp3");
        } else {
            playMusic("Ancient_Egypt.mp3");
        }
    }

    public void playMusic(String fileName) {
        if (!musicEnabled) return;
        if (currentMusicPath.equals(fileName) && currentMusic != null && currentMusic.isPlaying()) {
            return;
        }

        stopMusic();

        FileHandle handle = resolveFile(fileName);
        if (handle == null) {
            return;
        }

        try {
            currentMusic = Gdx.audio.newMusic(handle);
            currentMusic.setLooping(true);
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
            currentMusicPath = fileName;
        } catch (Exception ignored) {}
    }

    public void stopMusic() {
        if (currentMusic != null) {
            try {
                currentMusic.stop();
                currentMusic.dispose();
            } catch (Exception ignored) {}
            currentMusic = null;
            currentMusicPath = "";
        }
    }

    public void pauseMusic() {
        if (currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.pause();
        }
    }

    public void resumeMusic() {
        if (musicEnabled && currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
        saveSettingsToStorage();
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
        saveSettingsToStorage();
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled) {
            pauseMusic();
        } else {
            resumeMusic();
        }
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public boolean isMusicEnabled() { return musicEnabled; }
    public boolean isSoundEnabled() { return soundEnabled; }
    public float getMusicVolume() { return musicVolume; }
    public float getSoundVolume() { return soundVolume; }

    public void dispose() {
        stopMusic();
        if (clickSound != null) {
            try {
                clickSound.dispose();
            } catch (Exception ignored) {}
            clickSound = null;
        }
    }
}
