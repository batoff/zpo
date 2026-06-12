package com.project.controller;

import com.project.exception.HttpException;
import com.project.model.Student;
import com.project.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/studentList")
    public String studentList(Model model, Pageable pageable,
                              @RequestParam(required = false) String nazwisko) {
        Page<Student> page;
        if (nazwisko != null && !nazwisko.isBlank()) {
            page = studentService.searchByNazwisko(nazwisko, pageable);
        } else {
            page = studentService.getStudenci(pageable);
        }
        model.addAttribute("studenci", page.getContent());
        model.addAttribute("page", page);
        model.addAttribute("nazwisko", nazwisko);
        return "studentList";
    }

    @GetMapping("/studentEdit")
    public String studentEdit(@RequestParam(name = "studentId", required = false) Integer studentId, Model model) {
        if (studentId != null) {
            model.addAttribute("student", studentService.getStudent(studentId).get());
        } else {
            model.addAttribute("student", new Student());
        }
        return "studentEdit";
    }

    @PostMapping(path = "/studentEdit")
    public String studentEditSave(@ModelAttribute @Valid Student student, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "studentEdit";
        }
        try {
            student = studentService.setStudent(student);
        } catch (HttpException e) {
            bindingResult.rejectValue("", "error", e.getMessage());
            return "studentEdit";
        }
        return "redirect:/studentList";
    }

    @PostMapping(params = "cancel", path = "/studentEdit")
    public String studentEditCancel() {
        return "redirect:/studentList";
    }

    @PostMapping(params = "delete", path = "/studentEdit")
    public String studentEditDelete(@ModelAttribute Student student) {
        studentService.deleteStudent(student.getStudentId());
        return "redirect:/studentList";
    }
}
