package controller.menu;

import com.google.gson.Gson;
import controller.CommandParser;
import controller.NewsController;
import controller.Validator;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import network.Message;
import network.NetworkManager;
import util.FileManager;
import util.HashUtil;
import util.ParsedCommand;
import view.game.phase1.TerminalView;

public class MenuController {
    private final TerminalView view = new TerminalView();
    private final CommandParser parser = new CommandParser();
    private String currentForgetPasswordUsername;
    private String currentSecurityAnswerHash;
    private final NewsController newsController = new NewsController();
    private final CollectionController collectionController = new CollectionController(this);
    private final LeaderboardController leaderboardController = new LeaderboardController(view);
    private final Gson gson = new Gson();

    public void addNews(String content) {
        newsController.addNewsTrigger(content);
    }

    public String processRegister(ParsedCommand cmd) {
        Validator validator = new Validator();
        Validator.ValidationResult res;

        String username = cmd.getArg("-u");
        res = validator.validateUsername(username);
        if (res == Validator.ValidationResult.EMPTY_OR_NULL) return "Username cannot be empty.";
        if (res == Validator.ValidationResult.INVALID_FORMAT) return "Username can only contain letters, numbers, and dashes.";
        if (res == Validator.ValidationResult.INVALID_LENGTH) return "Username must be between 3 and 15 characters.";

        String nickname = cmd.getArg("-n");
        res = validator.validateNickname(nickname);
        if (res == Validator.ValidationResult.EMPTY_OR_NULL) return "Nickname cannot be empty.";
        if (res == Validator.ValidationResult.INVALID_LENGTH) return "Nickname must be between 3 and 30 characters.";

        String email = cmd.getArg("-e");
        res = validator.validateEmail(email);
        if (res != Validator.ValidationResult.VALID) return "Invalid email format.";

        String gender = cmd.getArg("-g");
        res = validator.validateGender(gender);
        if (res != Validator.ValidationResult.VALID) return "Gender must be either male or female.";

        String passwordArg = cmd.getArg("-p");
        String password = null;
        String passwordConfirm = null;

        if (passwordArg != null && passwordArg.contains(" ")) {
            String[] passwords = passwordArg.split(" ");
            password = passwords[0];
            passwordConfirm = passwords[1];
        } else if (passwordArg != null) {
            password = passwordArg;
        }

        res = validator.validatePassword(password, passwordConfirm);
        if (res == Validator.ValidationResult.PASSWORD_MISMATCH) return "Passwords do not match!";
        if (res == Validator.ValidationResult.INVALID_LENGTH) return "Password must be at least 8 characters.";
        if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_UPPER) return "Password must contain at least one uppercase letter.";
        if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_LOWER) return "Password must contain at least one lowercase letter.";
        if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_DIGIT) return "Password must contain at least one digit.";
        if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_SPECIAL) return "Password must contain at least one special character.";

        if (cmd.getArg("-q") != null && cmd.getArg("-a") != null && cmd.getArg("-c") != null) {
            if (!cmd.getArg("-a").equals(cmd.getArg("-c"))) {
                return "Security answer confirmation does not match!";
            }

            Message req = new Message(Message.Type.REGISTER)
                .put("username", username)
                .put("password", password)
                .put("nickname", nickname)
                .put("email", email)
                .put("gender", gender)
                .put("question", cmd.getArg("-q"))
                .put("answer", cmd.getArg("-a"));

            Message resp = NetworkManager.getInstance().sendRequest(req);
            if (resp.getType() == Message.Type.SUCCESS) {
                return "SUCCESS";
            } else {
                return resp.get("message");
            }
        }

        return "VALID_STEP_1";
    }

    public String processLogin(ParsedCommand cmd) {
        if (!cmd.hasFlag("-u") || !cmd.hasFlag("-p")) {
            return "Invalid command format. Username and password are required.";
        }

        String username = cmd.getArg("-u");
        String password = cmd.getArg("-p");
        boolean stayLoggedIn = cmd.hasFlag("-stay-logged-in");

        Message req = new Message(Message.Type.LOGIN)
            .put("username", username)
            .put("password", password);

        Message resp = NetworkManager.getInstance().sendRequest(req);

        if (resp.getType() == Message.Type.SUCCESS) {
            User user = gson.fromJson(resp.get("user_json"), User.class);
            UserSession.setCurrentUser(user);

            if (stayLoggedIn) {
                Settings settings = FileManager.loadSettings();
                settings.setAutoLoginUsername(user.getUsername());
                FileManager.saveSettings(settings);
            }

            return "Login successful!";
        } else {
            return resp.get("message");
        }
    }

    public String processForgetPassword(ParsedCommand cmd) {
        if (cmd.getAction().equals("forget password")) {
            if (!cmd.hasFlag("-u") || !cmd.hasFlag("-e")) {
                return "Username and email are required.";
            }

            String username = cmd.getArg("-u");
            String email = cmd.getArg("-e");

            Message req = new Message(Message.Type.FORGET_PASSWORD)
                .put("username", username)
                .put("email", email);

            Message resp = NetworkManager.getInstance().sendRequest(req);
            if (resp.getType() == Message.Type.SUCCESS) {
                currentForgetPasswordUsername = username;
                currentSecurityAnswerHash = resp.get("answer_hash");
                return "SUCCESS_username and email check";
            }
            return resp.get("message");
        }

        if (cmd.getAction().equals("answer")) {
            if (!cmd.hasFlag("-a")) {
                return "Answer is required.";
            }

            if (currentForgetPasswordUsername == null || currentSecurityAnswerHash == null) {
                return "Please enter username and email first!";
            }

            String inputAnswer = cmd.getArg("-a");
            if (currentSecurityAnswerHash.equals(HashUtil.sha256(inputAnswer))) {
                return "SUCCESS_answer get";
            } else {
                return "Answer is incorrect!";
            }
        }

        if (cmd.getAction().equals("new password")) {
            if (!cmd.hasFlag("-p") || !cmd.hasFlag("-c")) {
                return "Password and confirmation are required.";
            }

            if (currentForgetPasswordUsername == null) {
                return "Please verify your identity first!";
            }

            Validator validator = new Validator();
            Validator.ValidationResult res = validator.validatePassword(cmd.getArg("-p"), cmd.getArg("-c"));
            if (res == Validator.ValidationResult.PASSWORD_MISMATCH) return "Passwords do not match!";
            if (res == Validator.ValidationResult.INVALID_LENGTH) return "Password must be at least 8 characters.";
            if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_UPPER) return "Password must contain an uppercase letter.";
            if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_LOWER) return "Password must contain a lowercase letter.";
            if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_DIGIT) return "Password must contain a digit.";
            if (res == Validator.ValidationResult.WEAK_PASSWORD_NO_SPECIAL) return "Password must contain a special character.";

            Message req = new Message(Message.Type.RESET_PASSWORD)
                .put("username", currentForgetPasswordUsername)
                .put("new_password", cmd.getArg("-p"));

            Message resp = NetworkManager.getInstance().sendRequest(req);
            if (resp.getType() == Message.Type.SUCCESS) {
                currentForgetPasswordUsername = null;
                currentSecurityAnswerHash = null;
                return "SUCCESS_password changed";
            }
            return resp.get("message");
        }

        return "Invalid action";
    }

    public String processLogOut(ParsedCommand cmd) {
        String username = UserSession.getCurrentUser().getUsername();
        UserSession.clear();

        Settings settings = FileManager.loadSettings();
        settings.setAutoLoginUsername(null);
        FileManager.saveSettings(settings);
        FileManager.saveCachedUser(null);

        return "User " + username + " logged out successfully!";
    }

    public String processPlay(ParsedCommand cmd, String action) {
        if (cmd.getArg("-c") != null && cmd.getArg("-c").equalsIgnoreCase("test")) {
            return "ok";
        }
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }
        if (action.equalsIgnoreCase("coin-wallet")) {
            return "Your current balance: " + currentUser.getCoins() + " coins.";
        }
        if (action.equalsIgnoreCase("gem-wallet")) {
            return "Your current balance: " + currentUser.getGems() + " gems.";
        }
        if (action.equalsIgnoreCase("cheat add")) {
            int amount = 0;
            String currency = "";

            if (cmd.hasFlag("-n")) {
                String[] parts = cmd.getArg("-n").split(" ");
                amount = Integer.parseInt(parts[0]);
                currency = parts.length > 1 ? parts[1] : "coin";
            } else if (cmd.getArg("VALUE") != null) {
                String[] parts = cmd.getArg("VALUE").split(" ");
                amount = Integer.parseInt(parts[0]);
                currency = parts.length > 1 ? parts[1] : "coin";
            } else {
                return "Error: Invalid cheat format. Use: cheat add -n <amount> <currency>";
            }

            if (currency.toLowerCase().contains("coin")) {
                currentUser.setCoins(currentUser.getCoins() + amount);
            } else {
                currentUser.setGems(currentUser.getGems() + amount);
            }

            FileManager.updateUser(currentUser);
            UserSession.setCurrentUser(currentUser);

            return "Cheat activated: Added " + amount + " " + currency + "s.";
        }
        return "no";
    }

    public String processSetting(ParsedCommand cmd) {
        if (cmd.getArg("-l") != null) {
            Settings settings = FileManager.loadSettings();
            int newDifficulty = Integer.parseInt(cmd.getArg("-l"));
            settings.setDifficulty(newDifficulty);

            FileManager.saveSettings(settings);
            return "new difficulty: " + newDifficulty;
        }
        return "error";
    }

    public String processNews(ParsedCommand cmd, String action) {
        return newsController.processNews(cmd, action);
    }

    public String processProfile(ParsedCommand cmd, String action) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user is logged in.";
        }
        Validator validator = new Validator();
        Validator.ValidationResult res;
        if (action.equalsIgnoreCase("change-username")) {
            String newUsername = cmd.getArg("-u");
            res = validator.validateUsername(newUsername);
            if (res != Validator.ValidationResult.VALID) {
                return "Invalid username format or length.";
            }
            if (FileManager.isUsernameExists(newUsername)) {
                return "Username already exists.";
            }
            currentUser.setUsername(newUsername);
            FileManager.updateUser(currentUser);
            return "Username updated successfully to: " + newUsername;
        } else if (action.equalsIgnoreCase("change-nickname")) {
            String newNickname = cmd.getArg("-n");
            res = validator.validateNickname(newNickname);
            if (res != Validator.ValidationResult.VALID) {
                return "Nickname must be between 3 and 30 characters.";
            }
            currentUser.setNickname(newNickname);
            FileManager.updateUser(currentUser);
            return "Nickname updated successfully to: " + newNickname;
        } else if (action.equalsIgnoreCase("change-email")) {
            String newEmail = cmd.getArg("-e");
            res = validator.validateEmail(newEmail);
            if (res != Validator.ValidationResult.VALID) {
                return "Invalid email format.";
            }
            currentUser.setEmail(newEmail);
            FileManager.updateUser(currentUser);
            return "Email updated successfully to: " + newEmail;
        } else if (action.equalsIgnoreCase("change-password")) {
            String oldPassword = cmd.getArg("-o");
            String newPassword = cmd.getArg("-p");
            if (oldPassword == null || newPassword == null) {
                return "Both old password (-o) and new password (-p) are required.";
            }
            String hashedOld = HashUtil.sha256(oldPassword);
            if (!currentUser.getPassword().equals(hashedOld)) {
                return "Old password is incorrect.";
            }
            res = validator.validatePassword(newPassword, null);
            if (res != Validator.ValidationResult.VALID) {
                return "New password does not meet security requirements.";
            }
            String hashedNew = HashUtil.sha256(newPassword);
            if (hashedNew.equals(currentUser.getPassword())) {
                return "New password cannot be the same as old password.";
            }
            currentUser.setPassword(hashedNew);
            FileManager.updateUser(currentUser);
            return "Password updated successfully.";
        } else if (action.equalsIgnoreCase("show-info")) {
            StringBuilder info = new StringBuilder();
            info.append("Username: ").append(currentUser.getUsername()).append("\n");
            info.append("Nickname: ").append(currentUser.getNickname()).append("\n");
            info.append("Email: ").append(currentUser.getEmail()).append("\n");
            info.append("Gender: ").append(currentUser.getGender()).append("\n");
            info.append("Score: ").append(currentUser.getScore());
            return info.toString();
        }
        return "error";
    }

    public String processCollection(ParsedCommand cmd, String action) {
        return collectionController.processCollection(cmd, action);
    }

    public void handleLeaderboardMenuInput(String input) {
        if (input.equalsIgnoreCase("back")) {
            return;
        }
        ParsedCommand cmd = parser.parse(input);
        if (cmd.getAction().equalsIgnoreCase("menu leaderboard") || cmd.getAction().equalsIgnoreCase("show")) {
            leaderboardController.handleLeaderboardMenuInput(cmd);
        } else {
            view.showMessage("Unknown command in Leaderboard Menu.");
        }
    }

    public String processClaimQuests(ParsedCommand cmd) {
        User currentUser = UserSession.getCurrentUser();
        if (currentUser == null) {
            return "Error: No user logged in.";
        }
        return currentUser.claimAllCompletedQuests();
    }

    public void handleCollectionMenuInput(String input) {
        if (input.equalsIgnoreCase("back")) {
            return;
        }

        ParsedCommand cmd = parser.parse(input);
        String action = cmd.getAction();

        if (action.equalsIgnoreCase("menu collection show-plants")) {
            view.showMessage(processCollection(cmd, "show-plants"));
        } else if (action.equalsIgnoreCase("menu collection show-all-plants")) {
            view.showMessage(processCollection(cmd, "show-all-plants"));
        } else if (action.equalsIgnoreCase("menu collection show-zombies")) {
            view.showMessage(processCollection(cmd, "show-zombies"));
        } else if (action.equalsIgnoreCase("menu collection show-all-zombies")) {
            view.showMessage(processCollection(cmd, "show-all-zombies"));
        } else if (action.equalsIgnoreCase("menu collection show-plant")) {
            view.showMessage(processCollection(cmd, "show-plant"));
        } else if (action.equalsIgnoreCase("menu collection show-zombie")) {
            view.showMessage(processCollection(cmd, "show-zombie"));
        } else if (action.equalsIgnoreCase("menu collection upgrade-plant")) {
            view.showMessage(processCollection(cmd, "upgrade-plant"));
        } else if (action.equalsIgnoreCase("menu collection purchase-plant")) {
            view.showMessage(processCollection(cmd, "purchase-plant"));
        } else {
            view.showMessage("Invalid command inside Collection Menu.");
        }
    }
}
