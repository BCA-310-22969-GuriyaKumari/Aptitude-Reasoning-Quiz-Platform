package com.devrezaur.main.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "questions")
public class Question {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ques_id")
  private int quesId;
  private String title;

  @Column(name = "optionA")
  private String optionA;

  @Column(name = "optionB")
  private String optionB;

  @Column(name = "optionC")
  private String optionC;

  @Column(name = "ans")
  private int correctAns;

  @Column(name = "selected_ans")
  private int selectedAns;
  private String category;
  
  public String getTitle() {
        return title;
    }

    public String getOptionA() {
        return optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public String getOptionC() {
        return optionC;
    }

}
