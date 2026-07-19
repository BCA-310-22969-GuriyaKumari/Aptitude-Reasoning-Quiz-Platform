package com.devrezaur.main.repository;
import java.util.List;

import com.devrezaur.main.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Integer> {

    List<Question> findByCategory(String category);
    List<Question> findByCategoryIgnoreCase(String category);
}