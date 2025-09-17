package com.quiz.main.controller;

import com.quiz.main.model.Question;
import com.quiz.main.model.Quiz;
import com.quiz.main.service.QuestionService;
import com.quiz.main.service.QuizService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminQuizController {

    @Autowired
    private QuizService quizService;

    @Autowired
    private QuestionService questionService;

    @GetMapping("/quizzes/new")
    public String showCreateQuizForm(Model model) {
        model.addAttribute("quiz", new Quiz());
        return "admin/createQuiz";
    }

    @PostMapping("/quizzes")
    public String createQuiz(@ModelAttribute Quiz quiz, RedirectAttributes redirectAttributes) {
        Quiz savedQuiz = quizService.saveQuiz(quiz);
        redirectAttributes.addFlashAttribute("message", "Quiz created successfully!");
        return "redirect:/admin/quizzes/" + savedQuiz.getId() + "/questions/new";
    }

    @GetMapping("/quizzes/{quizId}/questions/new")
    public String showAddQuestionForm(@PathVariable Long quizId, Model model) {
        model.addAttribute("question", new Question());
        model.addAttribute("quizId", quizId);
        return "admin/addQuestion";
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public String addQuestionToQuiz(@PathVariable Long quizId, @ModelAttribute Question question, RedirectAttributes redirectAttributes) {
        Quiz quiz = quizService.getQuizById(quizId);
        question.setQuiz(quiz);
        questionService.saveQuestion(question);
        redirectAttributes.addFlashAttribute("message", "Question added successfully!");
        return "redirect:/admin/quizzes/" + quizId + "/questions/new";
    }

    @GetMapping("/quizzes")
    public String listQuizzes(Model model) {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes);
        return "/admin/manageQuizzes";
    }

    @GetMapping("/quizzes/{id}")
    public String editQuiz(@PathVariable Long id, Model model) {
        Quiz quiz = quizService.getQuizById(id);
        model.addAttribute("quiz", quiz);
        return "admin/editQuiz";
    }

    @PostMapping("/quizzes/{id}")
    public String updateQuiz(@PathVariable Long id, @ModelAttribute Quiz quiz, RedirectAttributes redirectAttributes) {
        // Logic to update the quiz
        quiz.setId(id); // Ensure the ID is set for the update operation
        quizService.saveQuiz(quiz); // Assuming saveQuiz handles both create and update
        redirectAttributes.addFlashAttribute("message", "Quiz updated successfully!");
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/quizzes/delete/{id}")
    public String deleteQuiz(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        quizService.deleteQuiz(id);
        redirectAttributes.addFlashAttribute("message", "Quiz deleted successfully!");
        return "redirect:/admin/quizzes";
    }

    @GetMapping("/questions/{questionId}/delete")
    public String deleteQuestion(@PathVariable Long questionId, RedirectAttributes redirectAttributes) {
        return questionService.findQuestionById(questionId).map(question -> {
            Long quizId = question.getQuiz().getId();
            questionService.deleteQuestion(questionId);
            redirectAttributes.addFlashAttribute("message", "Question deleted successfully!");
            return "redirect:/admin/quizzes/" + quizId + "/questions";
        }).orElse("redirect:/error-page"); // Redirect to a custom error page or an appropriate endpoint
    }


// Inside AdminQuizController

    @GetMapping("/quizzes/{quizId}/questions")
    public String listQuestionsForQuiz(@PathVariable Long quizId, Model model) {
        Quiz quiz = quizService.getQuizById(quizId);
        model.addAttribute("quiz", quiz);
        model.addAttribute("questions", quiz.getQuestions());
        return "admin/listQuestions";
    }

    @GetMapping("/questions/{questionId}/edit")
    public String showUpdateQuestionForm(@PathVariable Long questionId, Model model) {
        Question question = questionService.findQuestionById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question Id:" + questionId));
        model.addAttribute("question", question);
        return "admin/editQuestion";
    }

    @PostMapping("/questions/{id}/update")
    public String updateQuestion(@PathVariable Long id, @ModelAttribute("question") Question question, RedirectAttributes redirectAttributes) {
        // Perform the update operation here
        // For example, find the existing question by ID and update its fields
        Question existingQuestion = questionService.findQuestionById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid question Id:" + id));

        existingQuestion.setText(question.getText());
        existingQuestion.setOptionA(question.getOptionA());
        existingQuestion.setOptionB(question.getOptionB());
        existingQuestion.setOptionC(question.getOptionC());
        existingQuestion.setCorrectAnswer(question.getCorrectAnswer());

        questionService.saveQuestion(existingQuestion); // Assuming a save method that handles create/update

        redirectAttributes.addFlashAttribute("message", "Question updated successfully!");

        // Redirect back to the question list or wherever is appropriate
        return "redirect:/admin/quizzes/" + existingQuestion.getQuiz().getId() + "/questions";
    }



    @GetMapping("/manageQuizzes")
    public String manageQuizzes(Model model) {
        List<Quiz> quizzes = quizService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes);
        return "admin/manageQuizzes";

    }

    // Implement other admin endpoints as needed
}
