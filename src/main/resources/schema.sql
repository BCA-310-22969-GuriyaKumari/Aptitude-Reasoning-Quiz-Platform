DROP TABLE IF EXISTS questions;

CREATE TABLE questions (
  ques_id INT NOT NULL AUTO_INCREMENT,
  title VARCHAR(1024) NOT NULL,
  optionA VARCHAR(255) NOT NULL,
  optionB VARCHAR(255) NOT NULL,
  optionC VARCHAR(255) NOT NULL,
  ans INT NOT NULL,
  selected_ans INT NOT NULL DEFAULT 0,
  category VARCHAR(255) NOT NULL,
  PRIMARY KEY (ques_id)
);

DROP TABLE IF EXISTS results;

CREATE TABLE results (
  id INT NOT NULL AUTO_INCREMENT,
  username VARCHAR(255),
  total_correct INT,
  category VARCHAR(255),
  PRIMARY KEY (id)
);
