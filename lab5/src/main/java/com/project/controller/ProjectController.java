package com.project.controller;

import com.project.exception.HttpException;
import com.project.model.Projekt;
import com.project.service.ProjektService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProjectController {

    private final ProjektService projektService;

    public ProjectController(ProjektService projektService) {
        this.projektService = projektService;
    }

    @GetMapping("/projektList")
    public String projektList(Model model, Pageable pageable) {
        model.addAttribute("projekty", projektService.getProjekty(pageable).getContent());
        return "projektList";
    }

    @GetMapping("/projektEdit")
    public String projektEdit(@RequestParam(name = "projektId", required = false) Integer projektId, Model model) {
        if (projektId != null) {
            model.addAttribute("projekt", projektService.getProjekt(projektId).get());
        } else {
            model.addAttribute("projekt", new Projekt());
        }
        return "projektEdit";
    }

    @PostMapping(path = "/projektEdit")
    public String projektEditSave(@ModelAttribute @Valid Projekt projekt, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "projektEdit";
        }
        try {
            projekt = projektService.setProjekt(projekt);
        } catch (HttpException e) {
            bindingResult.rejectValue("", "error", e.getMessage());
            return "projektEdit";
        }
        return "redirect:/projektList";
    }

    @PostMapping(params = "cancel", path = "/projektEdit")
    public String projektEditCancel() {
        return "redirect:/projektList";
    }

    @PostMapping(params = "delete", path = "/projektEdit")
    public String projektEditDelete(@ModelAttribute Projekt projekt) {
        projektService.deleteProjekt(projekt.getProjektId());
        return "redirect:/projektList";
    }

    @GetMapping("/projektStudenci")
    public String projektStudenci(@RequestParam Integer projektId, Model model) {
        model.addAttribute("projekt", projektService.getProjekt(projektId).get());
        return "projektStudenci";
    }

    @PostMapping(params = "addStudent", path = "/projektStudenci")
    public String addStudentToProjekt(@RequestParam Integer projektId, @RequestParam Integer studentId) {
        projektService.addStudentToProjekt(projektId, studentId);
        return "redirect:/projektStudenci?projektId=" + projektId;
    }

    @PostMapping(params = "removeStudent", path = "/projektStudenci")
    public String removeStudentFromProjekt(@RequestParam Integer projektId, @RequestParam Integer studentId) {
        projektService.removeStudentFromProjekt(projektId, studentId);
        return "redirect:/projektStudenci?projektId=" + projektId;
    }
}
