package com.devrezaur.main.repository;

import com.devrezaur.main.model.Result;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResultRepo extends JpaRepository<Result, Integer> {

    Result findByUsername(String username);

    Result findByUsernameAndCategory(String username, String category);

    Result findByUsernameIgnoreCaseAndCategoryIgnoreCase(String username, String category);

}


