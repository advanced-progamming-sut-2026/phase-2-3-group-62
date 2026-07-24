package controller.menu;

import model.user.User;
import model.user.UserSession;
import model.entities.plant.Plant;
import model.entities.plant.loader.PlantLoader;
import util.FileManager;
import util.ParsedCommand;
import java.util.ArrayList;
import java.util.List;

public class PreGameController {
    public static String activeChapterName = null;
    private final List<String> selectedPlants = new ArrayList<>();
    private final List<String> boostedPlants = new ArrayList<>();
    private final int maxSlots = 8;

    public String processCommand(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }

        if (action.equalsIgnoreCase("menu enter chapter")) {
            String chapter = cmd.getArg("-c");
            if (chapter == null) {
                return "Error: Please specify a chapter using -c <chaptername>";
            }

            String lowerCh = cleanString(chapter).toLowerCase();
            int seasonNum = 1;
            int levelNum = 1;

            if (lowerCh.equals("ancientegypt") || lowerCh.equals("egypt")) {
                activeChapterName = "AncientEgypt";
                seasonNum = 1; levelNum = 1;
            } else if (lowerCh.equals("ancientegypt2")) {
                activeChapterName = "AncientEgypt2";
                seasonNum = 1; levelNum = 2;
            } else if (lowerCh.equals("ancientegypt3")) {
                activeChapterName = "AncientEgypt3";
                seasonNum = 1; levelNum = 3;
            } else if (lowerCh.equals("frostbitecaves") || lowerCh.equals("caves")) {
                activeChapterName = "FrostbiteCaves";
                seasonNum = 2; levelNum = 1;
            } else if (lowerCh.equals("frostbitecaves2")) {
                activeChapterName = "FrostbiteCaves2";
                seasonNum = 2; levelNum = 2;
            } else if (lowerCh.equals("frostbitecaves3")) {
                activeChapterName = "FrostbiteCaves3";
                seasonNum = 2; levelNum = 3;
            } else if (lowerCh.equals("bigwavebeach") || lowerCh.equals("beach")) {
                activeChapterName = "BigWaveBeach";
                seasonNum = 3; levelNum = 1;
            } else if (lowerCh.equals("bigwavebeach2")) {
                activeChapterName = "BigWaveBeach2";
                seasonNum = 3; levelNum = 2;
            } else if (lowerCh.equals("bigwavebeach3")) {
                activeChapterName = "BigWaveBeach3";
                seasonNum = 3; levelNum = 3;
            } else if (lowerCh.equals("darkages")) {
                activeChapterName = "DarkAges";
                seasonNum = 4; levelNum = 1;
            } else if (lowerCh.equals("darkages2")) {
                activeChapterName = "DarkAges2";
                seasonNum = 4; levelNum = 2;
            } else if (lowerCh.equals("darkages3")) {
                activeChapterName = "DarkAges3";
                seasonNum = 4; levelNum = 3;
            } else {
                return "Error: Unknown chapter name.";
            }

            currentUser.setLastSeasonCompleted(seasonNum);
            currentUser.setLastLevelCompleted(levelNum);
            FileManager.updateUser(currentUser);

            String formattedOutput = activeChapterName;
            if (activeChapterName.contains("2")) {
                formattedOutput = activeChapterName.replace("2", "") + " (Level 2)";
            } else if (activeChapterName.contains("3")) {
                formattedOutput = activeChapterName.replace("3", "") + " (Level 3)";
            } else {
                formattedOutput = activeChapterName + " (Level 1)";
            }

            return "Successfully entered chapter: " + formattedOutput;
        }

        if (action.equalsIgnoreCase("show all plants")) {
            List<Plant> allPlants = PlantLoader.loadPlants();
            StringBuilder sb = new StringBuilder("All game plants:\n");
            for (Plant p : allPlants) {
                sb.append("- ").append(p.getName()).append("\n");
            }
            return sb.toString().trim();
        }

        if (action.equalsIgnoreCase("show available plants")) {
            List<String> unlocked = currentUser.getUnlockedPlants();
            if (unlocked.isEmpty()) {
                return "You have no unlocked plants.";
            }
            StringBuilder sb = new StringBuilder("Your available plants:\n");
            for (String plant : unlocked) {
                int level = currentUser.getPlantLevels().getOrDefault(plant, 1);
                sb.append("- ").append(cleanString(plant)).append(" (Level ").append(level).append(")");
                if (boostedPlants.contains(plant)) {
                    sb.append(" [BOOSTED]");
                }
                sb.append("\n");
            }
            return sb.toString().trim();
        }

        if (action.equalsIgnoreCase("add plant")) {
            String plantName = cmd.getArg("-t");
            if (plantName == null) return "Invalid format. Use: add plant -t <type>";
            plantName = cleanString(plantName);

            String finalPlantName = plantName;
            Plant matchingPlantInDb = PlantLoader.loadPlants().stream()
                    .filter(p -> normalizeName(p.getName()).equalsIgnoreCase(normalizeName(finalPlantName)))
                    .findFirst().orElse(null);

            if (matchingPlantInDb == null) {
                return "Error: Plant type does not exist in the game.";
            }

            String exactPlantName = findExactPlantName(currentUser.getUnlockedPlants(), matchingPlantInDb.getName());
            if (exactPlantName == null) {
                return "Error: This plant is locked! Purchase it from the collection menu.";
            }

            if (selectedPlants.contains(exactPlantName)) {
                return "Error: " + exactPlantName + " is already selected.";
            }

            if (selectedPlants.size() >= maxSlots) {
                return "Error: Your selection slots are full (Max " + maxSlots + ").";
            }

            selectedPlants.add(exactPlantName);
            return "Plant " + exactPlantName + " added. (" + selectedPlants.size() + "/" + maxSlots + ")";
        }

        if (action.equalsIgnoreCase("menu enter minigame")) {
            String mgName = cmd.getArg("-m");
            if (mgName == null) {
                return "Error: Please specify a minigame using -m <name>";
            }
            mgName = cleanString(mgName);

            if (mgName.equalsIgnoreCase("Vasebreaker")) {
                activeChapterName = "Vasebreaker_MG";
                return "Successfully selected minigame: Vasebreaker. Ready to start!";
            } else if (mgName.equalsIgnoreCase("WallnutBowling") || mgName.equalsIgnoreCase("Bowling")) {
                activeChapterName = "WallnutBowling_MG";
                return "Successfully selected minigame: Wallnut Bowling. Ready to start!";
            } else if (mgName.equalsIgnoreCase("IZombie")) {
                activeChapterName = "IZombie_MG";
                return "Successfully selected minigame: IZombie. Ready to start!";
            } else if (mgName.equalsIgnoreCase("Beghoul")) {
                activeChapterName = "Beghoul_MG";
                return "Successfully selected minigame: Beghoul. Ready to start!";
            } else if (mgName.equalsIgnoreCase("Zombotany")) {
                activeChapterName = "Zombotany_MG";
                return "Successfully selected minigame: Zombotany. Ready to start!";
            } else {
                return "Error: Unknown minigame name.";
            }
        }

        if (action.equalsIgnoreCase("remove plant")) {
            String plantName = cmd.getArg("-t");
            if (plantName == null) return "Invalid format. Use: remove plant -t <type>";
            plantName = cleanString(plantName);

            String exactPlantName = findExactPlantName(selectedPlants, plantName);
            if (exactPlantName == null) {
                return "Error: This plant is not in your selected list.";
            }

            selectedPlants.remove(exactPlantName);
            boostedPlants.remove(exactPlantName);
            return "Plant " + exactPlantName + " removed. (" + selectedPlants.size() + "/" + maxSlots + ")";
        }

        if (action.equalsIgnoreCase("boost plant")) {
            String plantName = cmd.getArg("-t");
            if (plantName == null) return "Invalid format. Use: boost plant -t <type>";
            plantName = cleanString(plantName);

            String exactPlantName = findExactPlantName(currentUser.getUnlockedPlants(), plantName);
            if (exactPlantName == null) {
                return "Error: You can only boost plants you own.";
            }

            if (boostedPlants.contains(exactPlantName)) {
                return "Error: This plant is already boosted for this game.";
            }

            if (currentUser.getGems() < 2) {
                return "Error: Insufficient gems! Boost costs 2 gems. You have: " + currentUser.getGems();
            }

            currentUser.setGems(currentUser.getGems() - 2);
            FileManager.updateUser(currentUser);
            UserSession.setCurrentUser(currentUser);

            boostedPlants.add(exactPlantName);
            return "Plant " + exactPlantName + " boosted successfully! 2 gems deducted.";
        }

        if (action.equalsIgnoreCase("start game")) {
            if (activeChapterName == null) {
                return "Error: You must select a chapter or minigame first before starting.";
            }
            if (!activeChapterName.endsWith("_MG") && selectedPlants.isEmpty()) {
                return "Error: You must select at least one plant to start the game.";
            }
            return "START_GAME_CONFIRMED";
        }

        return "invalid command";
    }

    private String cleanString(String input) {
        if (input == null) return "";
        return input.replaceAll("^\"|\"$", "").trim();
    }

    private String normalizeName(String name) {
        if (name == null) return "";
        return cleanString(name).toLowerCase().replace(" ", "").replace("-", "");
    }

    private String findExactPlantName(List<String> list, String searchName) {
        String target = normalizeName(searchName);
        for (String s : list) {
            if (normalizeName(s).equalsIgnoreCase(target)) {
                return cleanString(s);
            }
        }
        return null;
    }

    public List<String> getSelectedPlants() { return selectedPlants; }
    public List<String> getBoostedPlants() { return boostedPlants; }
}