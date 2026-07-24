package controller.menu;

import controller.CommandParser;
import controller.NewsController;
import controller.Validator;
import model.user.SecurityQuestions;
import model.user.Settings;
import model.user.User;
import model.user.UserSession;
import model.enums.Gender;
import util.FileManager;
import util.HashUtil;
import util.ParsedCommand;
import view.game.TerminalView;
import view.menu.MainMenu;
import view.menu.MenuManager;
import view.menu.PlayMenu;

import java.util.List;

import static util.FileManager.checkPassword;
import static util.FileManager.isUsernameExists;

public class MenuController {
    private final TerminalView view = new TerminalView();
    private final CommandParser parser = new CommandParser();
    private String currentForgetPasswordUsername;
    private final NewsController newsController = new NewsController();
    private final CollectionController collectionController = new CollectionController(this);
    private final LeaderboardController leaderboardController = new LeaderboardController(view);

    public void addNews(String content) {
        newsController.addNewsTrigger(content);
    }

    public String processRegister(ParsedCommand cmd) {
        Validator validator = new Validator();
        Validator.ValidationResult res;
        boolean hasError = false;

        res = validator.validateUsername(cmd.getArg("-u"));
        if (res != Validator.ValidationResult.VALID) {
            view.showUsernameError(res);
            hasError = true;
        }

        if (isUsernameExists(cmd.getArg("-u"))) {
            view.showUsernameExistsError();
            hasError = true;
        }

        res = validator.validateEmail(cmd.getArg("-e"));
        if (res != Validator.ValidationResult.VALID) {
            view.showEmailError(res);
            hasError = true;
        }

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
        if (res != Validator.ValidationResult.VALID) {
            view.showPasswordError(res);
            hasError = true;
        }

        res = validator.validateNickname(cmd.getArg("-n"));
        if (res != Validator.ValidationResult.VALID) {
            view.showInvalidDisplayNameError();
            hasError = true;
        }

        res = validator.validateGender(cmd.getArg("-g"));
        if (res != Validator.ValidationResult.VALID) {
            view.showInvalidGenderError();
            hasError = true;
        }

        if (hasError) return "invalid";

        if (cmd.getArg("-q") != null && cmd.getArg("-a") != null && cmd.getArg("-c") != null) {
            if (!cmd.getArg("-a").equals(cmd.getArg("-c"))) {
                view.showMessage("Security answer confirmation does not match!");
                return "invalid";
            }

            User newUser = new User(
                    cmd.getArg("-u"),
                    HashUtil.sha256(password),
                    cmd.getArg("-n"),
                    cmd.getArg("-e"),
                    Gender.valueOf(cmd.getArg("-g").toUpperCase()),
                    cmd.getArg("-q"),
                    HashUtil.sha256(cmd.getArg("-a"))
            );

            List<User> users = FileManager.loadUsers();
            users.add(newUser);
            FileManager.saveUsers(users);

            return "SUCCESS";
        }

        List<String> questions = SecurityQuestions.getAll();
        view.showChoseQuestion(questions);
        return "VALID_STEP_1";
    }

    public String processLogin(ParsedCommand cmd) {
        if (!cmd.hasFlag("-u") || !cmd.hasFlag("-p")) {
            return "Invalid command format. Username and password are required.";
        }

        String username = cmd.getArg("-u");
        String password = cmd.getArg("-p");
        boolean stayLoggedIn = cmd.hasFlag("-stay-logged-in");
        boolean usernameIsUniq = !isUsernameExists(username);

        if (usernameIsUniq) {
            return "Username doesn't exist!";
        }

        boolean passwordIsTrue = checkPassword(username, HashUtil.sha256(password));

        if (!passwordIsTrue) {
            return "Password incorrect!";
        }

        User user = FileManager.getUser(username);
        UserSession.setCurrentUser(user);

        if (stayLoggedIn) {
            Settings settings = FileManager.loadSettings();
            settings.setAutoLoginUsername(user.getUsername());
            FileManager.saveSettings(settings);
        }

        return "Login successful!";
    }

    public String processForgetPassword(ParsedCommand cmd) {
        if (cmd.getAction().equals("forget password")) {
            if (!cmd.hasFlag("-u") || !cmd.hasFlag("-e")) {
                return "Invalid command format. Username and email are required.";
            }

            String username = cmd.getArg("-u");
            String email = cmd.getArg("-e");
            User user = FileManager.getUser(username);

            if (user == null) {
                return "Username doesn't exist!";
            }

            if (!user.getEmail().equalsIgnoreCase(email)) {
                return "Username and email doesn't match!";
            }

            currentForgetPasswordUsername = username;

            String questionNum = user.getSecurityQuestion();
            int questionIndex = Integer.parseInt(questionNum);
            String questionText = SecurityQuestions.getQuestionByIndex(questionIndex - 1);

            view.showSecurityQuestion(questionText);
            return "SUCCESS_username and email check";
        }

        if (cmd.getAction().equals("answer")) {
            if (!cmd.hasFlag("-a")) {
                return "Invalid command format. Answer is required.";
            }

            if (currentForgetPasswordUsername == null) {
                return "Please enter forget password command first!";
            }

            User user = FileManager.getUser(currentForgetPasswordUsername);
            if (user == null) {
                return "Username doesn't exist!";
            }

            String answer = user.getSecurityAnswer();
            String inputAnswer = cmd.getArg("-a");

            if (answer.equals(HashUtil.sha256(inputAnswer))) {
                return "SUCCESS_answer get";
            } else {
                return "Answer is incorrect!";
            }
        }

        if (cmd.getAction().equals("new password")) {
            if (!cmd.hasFlag("-p") || !cmd.hasFlag("-c")) {
                return "Invalid command format. Password and confirmation are required.";
            }

            if (currentForgetPasswordUsername == null) {
                return "Please verify your identity first!";
            }

            User user = FileManager.getUser(currentForgetPasswordUsername);
            if (user == null) {
                return "Username doesn't exist!";
            }

            Validator validator = new Validator();
            Validator.ValidationResult res = validator.validatePassword(cmd.getArg("-p"), cmd.getArg("-c"));
            if (res != Validator.ValidationResult.VALID) {
                view.showPasswordError(res);
                return "invalid password";
            }

            String newPassword = cmd.getArg("-p");
            String hashedPassword = HashUtil.sha256(newPassword);

            if (hashedPassword.equals(user.getPassword())) {
                return "Please enter a new password which is different from your current password.";
            }

            user.setPassword(hashedPassword);
            FileManager.updateUser(user);

            currentForgetPasswordUsername = null;
            return "SUCCESS_password changed";
        }

        return "invalid action";
    }

    public String processLogOut(ParsedCommand cmd) {
        String username = UserSession.getCurrentUser().getUsername();
        UserSession.clear();

        Settings settings = FileManager.loadSettings();
        settings.setAutoLoginUsername(null);
        FileManager.saveSettings(settings);

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
        Validator.ValidationResult res = null;
        if (action.equalsIgnoreCase("change-username")) {
            String newUsername = cmd.getArg("-u");
            res = validator.validateUsername(newUsername);
            if (res != Validator.ValidationResult.VALID) {
                view.showUsernameError(res);
                return "invalid username";
            }
            if (isUsernameExists(newUsername)) {
                view.showUsernameExistsError();
                return "username exists";
            }
            currentUser.setUsername(newUsername);
            FileManager.updateUser(currentUser);
            return "Username updated successfully to: " + newUsername;
        } else if (action.equalsIgnoreCase("change-nickname")) {
            String newNickname = cmd.getArg("-n");
            res = validator.validateNickname(newNickname);
            if (res != Validator.ValidationResult.VALID) {
                view.showInvalidDisplayNameError();
                return "invalid nickname";
            }
            currentUser.setNickname(newNickname);
            FileManager.updateUser(currentUser);
            return "Nickname updated successfully to: " + newNickname;
        } else if (action.equalsIgnoreCase("change-email")) {
            String newEmail = cmd.getArg("-e");
            res = validator.validateEmail(newEmail);
            if (res != Validator.ValidationResult.VALID) {
                view.showEmailError(res);
                return "invalid email";
            }
            currentUser.setEmail(newEmail);
            FileManager.updateUser(currentUser);
            return "Email updated successfully to: " + newEmail;
        } else if (action.equalsIgnoreCase("change-password")) {
            String oldPassword = cmd.getArg("-o");
            String newPassword = cmd.getArg("-p");
            if (oldPassword == null || newPassword == null) {
                return "Error: Both old password (-o) and new password (-p) are required.";
            }
            String hashedOld = HashUtil.sha256(oldPassword);
            if (!currentUser.getPassword().equals(hashedOld)) {
                return "Error: Old password is incorrect.";
            }
            res = validator.validatePassword(newPassword, null);
            if (res != Validator.ValidationResult.VALID) {
                view.showPasswordError(res);
                return "invalid password";
            }
            String hashedNew = HashUtil.sha256(newPassword);
            if (hashedNew.equals(currentUser.getPassword())) {
                return "Error: New password cannot be the same as old password.";
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
            MenuManager.getInstance().setCurrentMenu(new PlayMenu(this));
            return;
        }
        ParsedCommand cmd = parser.parse(input);
        if (cmd.getAction().equalsIgnoreCase("menu leaderboard") || cmd.getAction().equalsIgnoreCase("show")) {
            leaderboardController.handleLeaderboardMenuInput(cmd);
        } else {
            view.showMessage("Unknown command in Leaderboard Menu. Usage: menu leaderboard [-s score/level/minigame/dailyquest/nondailyquest/scoring] [-o asc/desc]");
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
            MenuManager.getInstance().setCurrentMenu(new PlayMenu(this));
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