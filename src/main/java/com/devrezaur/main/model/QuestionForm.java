package com.devrezaur.main.model;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class QuestionForm {
    
    private List<Question> questions;

    // Standard Manual Getter (NetBeans validation ke liye)
    public List<Question> getQuestions() {
        return questions;
    }

    // Standard Manual Setter (Isko save karte hi QuizService ka error gayab ho jayega)
    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }
}