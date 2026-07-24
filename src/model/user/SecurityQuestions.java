package model.user;

import java.util.List;

public class SecurityQuestions {
    private static final List<String> QUESTIONS = List.of(
            "What is your mother's maiden name?",
            "What was the name of your first pet?",
            "What was the name of your first school?"
    );

    public static List<String> getAll() {
        return QUESTIONS;
    }

    public static String getQuestionByIndex(int index) {
        return QUESTIONS.get(index);
    }
}