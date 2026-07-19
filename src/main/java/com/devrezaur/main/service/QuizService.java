package com.devrezaur.main.service;

import com.devrezaur.main.model.Question;
import com.devrezaur.main.model.QuestionForm;
import com.devrezaur.main.model.Result;
import com.devrezaur.main.repository.QuestionRepo;
import com.devrezaur.main.repository.ResultRepo;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

    private final QuestionRepo questionRepo;
    private final ResultRepo resultRepo;
    
    private final int NUM_OF_QUES = 10;

    // Manual Constructor (Lombok ke bina - NetBeans isko turant accept karega)
    public QuizService(QuestionRepo questionRepo, ResultRepo resultRepo) {
        this.questionRepo = questionRepo;
        this.resultRepo = resultRepo;
    }

    public QuestionForm getQuestions(String category) {
        String normalizedCategory = category == null ? "" : category.trim();
        List<Question> categoryQuestions = questionRepo.findByCategoryIgnoreCase(normalizedCategory);
        if (categoryQuestions == null || categoryQuestions.isEmpty()) {
            categoryQuestions = questionRepo.findAll();
        }

        List<Question> selectedQuestions = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < NUM_OF_QUES && !categoryQuestions.isEmpty(); i++) {
            int randomIndex = random.nextInt(categoryQuestions.size());
            selectedQuestions.add(categoryQuestions.get(randomIndex));
            categoryQuestions.remove(randomIndex);
        }

        QuestionForm qForm = new QuestionForm();
        qForm.setQuestions(selectedQuestions);
        return qForm;
    }
    
    public List<Question> getAllQuestions() {
        return questionRepo.findAll();
    }

    public Result saveResult(Result result) {
        String username = result.getUsername() == null || result.getUsername().isBlank()
                ? "Anonymous"
                : result.getUsername().trim();
        String category = result.getCategory() == null || result.getCategory().isBlank()
                ? "APTITUDE"
                : result.getCategory().trim().toUpperCase();

        result.setUsername(username);
        result.setCategory(category);

        Result existingResult = resultRepo.findByUsernameIgnoreCaseAndCategoryIgnoreCase(username, category);
        if (existingResult != null) {
            existingResult.setTotalCorrect(result.getTotalCorrect());
            return resultRepo.save(existingResult);
        }

        return resultRepo.save(result);
    }

    public List<Result> getTopScore() {
        return resultRepo.findAll();
    }

    public List<Result> getLatestResults() {
        return resultRepo.findAll().stream()
                .filter(r -> r.getUsername() != null && r.getCategory() != null)
                .collect(Collectors.toMap(
                        r -> r.getUsername().trim().toLowerCase() + "|" + r.getCategory().trim().toLowerCase(),
                        r -> r,
                        BinaryOperator.maxBy(Comparator.comparingInt(Result::getId))
                ))
                .values().stream()
                .sorted(Comparator.comparing(Result::getUsername).thenComparing(Result::getCategory))
                .collect(Collectors.toList());
    }
}