package view.game;

import java.util.Scanner;

public class View {
    private Scanner scanner = new Scanner(System.in);

    public void showMessage(String message) {
        System.out.println(message);
    }

    public String getInput(String prompt) {
        System.out.print(prompt + "> ");
        return scanner.nextLine().trim();
    }
}