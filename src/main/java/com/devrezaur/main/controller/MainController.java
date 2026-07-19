package com.devrezaur.main.controller;

import com.devrezaur.main.model.Question;
import com.devrezaur.main.model.QuestionForm;
import com.devrezaur.main.model.Result;
import com.devrezaur.main.service.QuizService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {

    private final QuizService quizService;

    // Manual Constructor (Bina Lombok ke - NetBeans errors se bachne ke liye)
    public MainController(QuizService quizService) {
        this.quizService = quizService;
    }

    private void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    // 1. Root Landing Page Layout
    @GetMapping("/")
    public String index(HttpSession session) {
        session.invalidate(); // Purani session clear karne ke liye
        return "index"; // templates/index.html
    }

    // 2. Admin Login Security Gate Component
    @GetMapping("/admin-login")
    public String adminLogin(HttpServletResponse response) {
        setNoCacheHeaders(response);
        return "redirect:/admin/login"; // delegate to AdminController
    }

    // 3. Admin Main Dashboard Metrics Panel
    @GetMapping("/admin-dashboard")
    public String adminDashboard(HttpServletResponse response) {
        setNoCacheHeaders(response);
        return "redirect:/admin/dashboard"; // delegate to AdminController
    }

    // 4. Student Dashboard Entry Point (Choose Your Challenge Screen)
    @GetMapping("/student-entry")
    public String studentEntry(Model model, HttpSession session) {
        String studentName = (String) session.getAttribute("studentName");
        model.addAttribute("studentName", studentName);
        return "home-page"; // use the updated student-entry template
    }

    // 5. Active Evaluation Session Route Handler (Actual Quiz Interface)
    @GetMapping("/quiz")
    public String startQuiz(@RequestParam("category") String category, Model model, HttpSession session) {
        QuestionForm qForm = quizService.getQuestions(category);
        model.addAttribute("questionForm", qForm); // template expects `questionForm`
        model.addAttribute("category", category);
        String studentName = (String) session.getAttribute("studentName");
        model.addAttribute("username", studentName);
        return "quiz-page"; // CORRECTION: Yeh ab seedhe templates/quiz-page.html kholega
    }

    // Handle the form from home-page.html when the user submits their name
    @PostMapping("/select-subject")
    public String selectSubject(@RequestParam String username, HttpSession session) {
        if (username != null && !username.isBlank()) {
            session.setAttribute("studentName", username);
        }
        return "redirect:/student-login";
    }

   // 1. Jab student name daal kar 'Select Subject' dabayega
    @PostMapping("/start-quiz")
    public String handleStartQuiz(@RequestParam String username, HttpSession session, Model model) {
        if (username != null && !username.isBlank()) {
            session.setAttribute("studentName", username);
        }
        // Name save karne ke baad direct 'Choose Your Challenge' (student-entry) page par bhejien
        return "redirect:/student-login"; 
    }

    // 2. Is method ko check kijiye, yeh aapka Subject Selection template load karega
    @GetMapping("/student-login")
    public String studentLogin(HttpSession session, Model model) {
        String studentName = (String) session.getAttribute("studentName");
        model.addAttribute("studentName", studentName);
        return "student-entry"; // Yeh aapka 'Choose Your Challenge' wala HTML file hai
    }

    // 6. Quiz Submission Handler (Calculate Score & Save Result)
    @PostMapping("/submit")
    public String submitQuiz(@ModelAttribute("questionForm") QuestionForm questionForm,
                              @RequestParam(name = "username", required = false) String username,
                              @RequestParam(name = "category", required = false) String category,
                              HttpSession session,
                              Model model) {
        // Use category from request or default
        if (category == null || category.isBlank()) {
            category = "APTITUDE";
        }

        // Calculate score: count correct answers
        int totalCorrect = 0;
        if (questionForm.getQuestions() != null) {
            for (Question q : questionForm.getQuestions()) {
                // selectedAns = user's answer, correctAns = correct answer
                if (q.getSelectedAns() == q.getCorrectAns()) {
                    totalCorrect++;
                }
            }
        }

        // Create and save result
        Result result = new Result();
        result.setUsername(username != null && !username.isBlank() ? username : (String) session.getAttribute("studentName"));
        result.setTotalCorrect(totalCorrect);
        result.setCategory(category);
        Result savedResult = quizService.saveResult(result);

        // Add result to model for result page
        model.addAttribute("result", savedResult);
        return "result-page";
    }

    // 7. Scoreboard Page - Display All Results
    @GetMapping("/scoreboard")
    public String scoreboard(Model model) {
        List<Result> results = quizService.getLatestResults();
        model.addAttribute("results", results);
        return "scoreboard-page";
    }
}