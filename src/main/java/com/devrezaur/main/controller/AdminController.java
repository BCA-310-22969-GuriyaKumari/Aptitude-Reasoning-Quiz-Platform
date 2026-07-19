package com.devrezaur.main.controller;

import com.devrezaur.main.model.Question;
import com.devrezaur.main.model.Result;
import com.devrezaur.main.repository.QuestionRepo;
import com.devrezaur.main.repository.ResultRepo;
import com.devrezaur.main.service.QuizService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.beans.factory.annotation.Autowired;

@Controller
public class AdminController {

    @Autowired
    private QuestionRepo questionRepo;

    @Autowired
    private ResultRepo resultRepo;

    @Autowired
    private QuizService quizService;

    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "AdMin$1002";

    private void setNoCacheHeaders(HttpServletResponse response) {
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setDateHeader("Expires", 0);
    }

    private void setLoginLogoutHeaders(HttpServletResponse response) {
        setNoCacheHeaders(response);
        response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\", \"executionContexts\"");
    }

    private boolean isAdminLoggedIn(HttpSession session) {
        Boolean isAdmin = (Boolean) session.getAttribute("isAdmin");
        return isAdmin != null && isAdmin;
    }

    private Path getDataSqlPath() throws IOException {
        Path current = Paths.get("").toAbsolutePath();
        while (current != null) {
            Path candidate = current.resolve("src/main/resources/data.sql");
            if (Files.exists(candidate) && Files.isWritable(candidate)) {
                return candidate;
            }
            candidate = current.resolve("target/classes/data.sql");
            if (Files.exists(candidate) && Files.isWritable(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        // Fallback to project source path relative to working directory, but do not silently create an unrelated file
        Path fallback = Paths.get("src/main/resources/data.sql").toAbsolutePath();
        if (Files.exists(fallback)) {
            return fallback;
        }
        throw new IOException("Unable to locate writable data.sql in project directories.");
    }

    private String escapeSql(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    private void rewriteDataSql(List<Question> questions) throws IOException {
        StringBuilder sql = new StringBuilder();
        sql.append("-- Update questions SQL (do not clear results on startup)\n");
        sql.append("DELETE FROM questions;\n\n");

        for (Question question : questions) {
            sql.append("INSERT INTO questions (title, optionA, optionB, optionC, ans, category) VALUES ('")
                    .append(escapeSql(question.getTitle())).append("', '")
                    .append(escapeSql(question.getOptionA())).append("', '")
                    .append(escapeSql(question.getOptionB())).append("', '")
                    .append(escapeSql(question.getOptionC())).append("', ")
                    .append(question.getCorrectAns()).append(", '")
                    .append(escapeSql(question.getCategory())).append("');\n");
        }

        Files.writeString(getDataSqlPath(), sql.toString(), StandardCharsets.UTF_8);
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(HttpSession session, HttpServletResponse response) {
        // Ensure any existing admin session is cleared when reaching the login page
        if (session != null) {
            session.invalidate();
        }
        setLoginLogoutHeaders(response);
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String adminLogin(@RequestParam String username, @RequestParam String password, Model model, HttpSession session, HttpServletResponse response) {
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            session.setAttribute("isAdmin", true);
            setNoCacheHeaders(response);
            return "redirect:/admin/dashboard";
        }
        setLoginLogoutHeaders(response);
        model.addAttribute("errorMessage", "Invalid admin credentials. Please try again.");
        return "admin-login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model, HttpSession session, HttpServletResponse response) {
        setNoCacheHeaders(response);
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        long totalQuestions = questionRepo.count();
        List<Result> latestResults = quizService.getLatestResults();
        Set<String> studentNames = latestResults.stream()
                .map(Result::getUsername)
                .collect(Collectors.toSet());

        model.addAttribute("totalQuestions", totalQuestions);
        model.addAttribute("totalStudentsAttempted", studentNames.size());
        model.addAttribute("totalResults", latestResults.size());
        return "admin-dashboard";
    }

    @GetMapping("/admin/questions")
    public String adminQuestions(Model model, HttpSession session, HttpServletResponse response) {
        setNoCacheHeaders(response);
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }
        model.addAttribute("questions", questionRepo.findAll());
        return "admin-questions";
    }

    @PostMapping("/admin/questions/add")
    public String addQuestion(com.devrezaur.main.model.Question question, HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        setNoCacheHeaders(response);
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        questionRepo.save(question);
        try {
            rewriteDataSql(questionRepo.findAll());
        } catch (IOException e) {
            System.err.println("Failed to update data.sql after adding question: " + e.getMessage());
        }
        redirectAttributes.addFlashAttribute("successMessage", "The Question is added successfully");
        return "redirect:/admin/questions";
    }

    @PostMapping("/admin/questions/delete")
    public String deleteQuestion(@RequestParam Integer id, HttpSession session, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        setNoCacheHeaders(response);
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        questionRepo.deleteById(id);
        try {
            rewriteDataSql(questionRepo.findAll());
        } catch (IOException e) {
            System.err.println("Failed to update data.sql after deleting question: " + e.getMessage());
        }
        redirectAttributes.addFlashAttribute("deleteMessage", "Question Removed Successfully");
        return "redirect:/admin/questions";
    }

    @GetMapping("/admin/results")
    public String adminResults(Model model, HttpSession session, HttpServletResponse response) {
        setNoCacheHeaders(response);
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        model.addAttribute("results", quizService.getLatestResults());
        return "admin-results";
    }

    @GetMapping("/admin/results/analytics")
    public String adminResultsAnalytics(Model model, HttpSession session, HttpServletResponse response) {
        setNoCacheHeaders(response);
        if (!isAdminLoggedIn(session)) {
            return "redirect:/admin/login";
        }

        List<Result> results = quizService.getLatestResults();
        long reasoningCount = results.stream()
                .filter(result -> "REASONING".equalsIgnoreCase(result.getCategory()))
                .count();
        long aptitudeCount = results.stream()
                .filter(result -> "APTITUDE".equalsIgnoreCase(result.getCategory()))
                .count();

        Map<String, Long> attemptsByCategory = results.stream()
                .collect(Collectors.groupingBy(result -> result.getCategory() == null ? "UNKNOWN" : result.getCategory(), Collectors.counting()));

        Map<String, Double> averageScoreByCategory = results.stream()
                .collect(Collectors.groupingBy(result -> result.getCategory() == null ? "UNKNOWN" : result.getCategory(),
                        Collectors.averagingInt(Result::getTotalCorrect)));

        String mostDifficultSubject = averageScoreByCategory.entrySet().stream()
                .min(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String mostAttemptedSubject = attemptsByCategory.entrySet().stream()
                .max(Comparator.comparingLong(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse("N/A");

        model.addAttribute("results", results);
        model.addAttribute("reasoningCount", reasoningCount);
        model.addAttribute("aptitudeCount", aptitudeCount);
        model.addAttribute("mostDifficultSubject", mostDifficultSubject);
        model.addAttribute("mostAttemptedSubject", mostAttemptedSubject);
        model.addAttribute("averageScoreByCategory", averageScoreByCategory);
        return "admin-analytics";
    }

    @GetMapping("/admin/results/export")
    public void exportResultsToCsv(HttpServletResponse response, HttpSession session) throws IOException {
        if (!isAdminLoggedIn(session)) {
            response.sendRedirect("/admin/login");
            return;
        }

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=student-results-report.csv");
        response.setCharacterEncoding("UTF-8");

        List<Result> results = quizService.getLatestResults();
        try (var writer = response.getWriter()) {
            writer.append("ID,Student Name,Subject Name,Marks\n");
            for (Result result : results) {
                String category = result.getCategory() == null ? "" : result.getCategory();
                writer.append(String.valueOf(result.getId())).append(',')
                        .append(escapeCsv(result.getUsername())).append(',')
                        .append(escapeCsv(category)).append(',')
                        .append(String.valueOf(result.getTotalCorrect())).append('\n');
            }
            writer.flush();
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\r") || escaped.contains("\"") || escaped.contains(";")) {
            return '"' + escaped + '"';
        }
        return escaped;
    }

    @GetMapping("/admin/logout")
    public String adminLogout(HttpSession session, HttpServletResponse response) {
        setLoginLogoutHeaders(response);
        if (session != null) {
            session.invalidate();
        }
        return "redirect:/admin/login";
    }
}