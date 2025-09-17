package com.quiz.main.controller;

import com.quiz.main.model.Question;
import com.quiz.main.model.Quiz;
import com.quiz.main.model.QuizSubmission;
import com.quiz.main.model.User;
import com.quiz.main.service.QuizService;
import com.quiz.main.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/quizzes")
public class UserQuizController {

    @Autowired
    private QuizService quizService;

    // Assuming you have a UserService to get the currently authenticated user
    @Autowired
    private UserService userService;

    @GetMapping
    public String listQuizzes(Model model) {
        model.addAttribute("quizzes", quizService.getAllQuizzes());
        return "userQuizzesList";
    }

    @GetMapping("/{id}")
    public String takeQuiz(@PathVariable Long id, Model model, HttpSession session) {
        Quiz quiz = quizService.getQuizById(id);
        if (quiz == null) {
            return "redirect:/quiz-not-found"; // or handle the error appropriately
        }

        // Store the start time in the session to calculate the duration on submission
        long startTime = System.currentTimeMillis();
        session.setAttribute("startTime", startTime);

        // Pass the quiz, its questions, and the duration to the view
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quiz.getQuestions());
        model.addAttribute("duration", quiz.getDuration()); // Assuming quiz.getDuration() gives you the duration in minutes
        model.addAttribute("quizSubmission", new QuizSubmission());

        return "takeQuiz";
    }

    @PostMapping("/{quizId}/submit")
    public String submitQuizAnswers(@PathVariable Long quizId, @ModelAttribute QuizSubmission submission, Model model, HttpSession session) {
        // Evaluate the submission to calculate the score
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            // No user in session, redirect to login or handle accordingly
            return "redirect:/login";
        }
        int score = evaluateSubmission(submission);

        // Fetch the quiz for additional information
        Quiz quiz = quizService.getQuizById(quizId);

        // Save the quiz result to the database
        quizService.saveQuizResult(currentUser, quiz, score); // Using currentUser obtained from session

        // Populate the model with results and correct answers for displaying to the user
        Map<Long, Character> correctAnswers = quizService.getCorrectAnswers(quizId);
        correctAnswers.forEach((key, value) -> System.out.println("Question ID: " + key + ", Correct Answer: " + value));

        model.addAttribute("score", score);
        model.addAttribute("totalQuestions", quiz.getQuestions().size());
        model.addAttribute("submittedAnswers", submission.getAnswers());
        model.addAttribute("correctAnswers", correctAnswers);

        // Redirect to a "results" view or a confirmation page
        return "quizResults";
    }


    private int evaluateSubmission(QuizSubmission submission) {
        int correctAnswers = 0;
        Quiz quiz = quizService.getQuizById(submission.getQuizId());
        for (Question question : quiz.getQuestions()) {
            // Assuming both are of Character type and not null
            if (Character.toUpperCase(submission.getAnswers().getOrDefault(question.getId(), ' ')) == Character.toUpperCase(question.getCorrectAnswer())) {
                correctAnswers++;
            }
        }
        return correctAnswers;
    }

}
