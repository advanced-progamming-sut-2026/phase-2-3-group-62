package view.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import model.minigame.Beghoul;
import model.minigame.IZombie;
import model.minigame.MiniGame;
import model.minigame.Vasebreaker;
import model.minigame.WallnutBowling;
import model.minigame.Zombotany;
import model.season.Season;
import model.user.Settings;
import util.FileManager;

public class AudioManager {
    private static AudioManager instance;

    private Music currentMusic;
    private String currentMusicPath = "";
    private Sound clickSound;
    private Sound plantSound;
    private boolean musicEnabled = true;
    private boolean soundEnabled = true;
    private float musicVolume = 0.35f;
    private float soundVolume = 0.75f;

    private static final float MUSIC_GAIN_MULTIPLIER = 0.40f;
    private static final float SFX_GAIN_MULTIPLIER = 0.65f;

    private AudioManager() {
        loadSettingsFromStorage();
        loadClickSound();
        loadPlantSound();
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
                this.musicVolume = Math.max(0f, settings.getMusicVolume());
                this.soundVolume = Math.max(0f, settings.getSfxVolume());
                if (this.musicVolume <= 0.005f) {
                    this.musicVolume = 0f;
                }
                if (this.soundVolume <= 0.005f) {
                    this.soundVolume = 0f;
                }
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
            "music/mini game/" + fileName,
            "assets/music/mini game/" + fileName,
            "music/effects/" + fileName,
            "assets/music/effects/" + fileName,
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

    private void loadPlantSound() {
        try {
            FileHandle handle = resolveFile("effects/plant.mp3");
            if (handle == null) {
                handle = resolveFile("plant.mp3");
            }
            if (handle != null) {
                plantSound = Gdx.audio.newSound(handle);
            }
        } catch (Exception ignored) {}
    }

    public void playButtonClick() {
        if (!soundEnabled || soundVolume <= 0.001f) return;
        if (clickSound == null) {
            loadClickSound();
        }
        if (clickSound != null) {
            try {
                clickSound.play(soundVolume * SFX_GAIN_MULTIPLIER);
            } catch (Exception ignored) {}
        }
    }

    public void playPlantSound() {
        if (!soundEnabled || soundVolume <= 0.001f) return;
        if (plantSound == null) {
            loadPlantSound();
        }
        if (plantSound != null) {
            try {
                plantSound.play(soundVolume * SFX_GAIN_MULTIPLIER);
            } catch (Exception ignored) {}
        }
    }

    public void playTitleMusic() {
        playMusic("Title_Screen.mp3");
    }

    public void playMiniGameMusic(MiniGame miniGame) {
        if (miniGame == null) {
            playMusic("Title_Screen.mp3");
            return;
        }

        if (miniGame instanceof Vasebreaker) {
            playMusic("mini game/vasebreaker.Mp3");
        } else if (miniGame instanceof WallnutBowling) {
            playMusic("mini game/wall nut bowling.Mp3");
        } else if (miniGame instanceof Zombotany) {
            playMusic("mini game/Zombotany.Mp3");
        } else if (miniGame instanceof IZombie || miniGame instanceof Beghoul) {
            playMusic("mini game/i, zombie  beghouled.Mp3");
        } else {
            playMusic("Title_Screen.mp3");
        }
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
        if (!musicEnabled || musicVolume <= 0.001f) {
            stopMusic();
            return;
        }
        if (currentMusicPath.equals(fileName) && currentMusic != null && currentMusic.isPlaying()) {
            currentMusic.setVolume(musicVolume * MUSIC_GAIN_MULTIPLIER);
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
            currentMusic.setVolume(musicVolume * MUSIC_GAIN_MULTIPLIER);
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
        if (musicEnabled && musicVolume > 0.001f && currentMusic != null && !currentMusic.isPlaying()) {
            currentMusic.play();
        }
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0f, volume);
        if (this.musicVolume <= 0.005f) {
            this.musicVolume = 0f;
        }

        if (currentMusic != null) {
            if (this.musicVolume <= 0f) {
                currentMusic.setVolume(0f);
                currentMusic.pause();
            } else {
                currentMusic.setVolume(this.musicVolume * MUSIC_GAIN_MULTIPLIER);
                if (musicEnabled && !currentMusic.isPlaying()) {
                    currentMusic.play();
                }
            }
        }
        saveSettingsToStorage();
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0f, volume);
        if (this.soundVolume <= 0.005f) {
            this.soundVolume = 0f;
        }
        saveSettingsToStorage();
    }

    public void setMusicEnabled(boolean enabled) {
        this.musicEnabled = enabled;
        if (!enabled || musicVolume <= 0.001f) {
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
        if (plantSound != null) {
            try {
                plantSound.dispose();
            } catch (Exception ignored) {}
            plantSound = null;
        }
    }
}
