package com.project.controller;

import com.project.exception.HttpException;
import com.project.model.Projekt;
import com.project.model.Zadanie;
import com.project.service.ZadanieService;
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
public class ZadanieController {

    private final ZadanieService zadanieService;

    public ZadanieController(ZadanieService zadanieService) {
        this.zadanieService = zadanieService;
    }

    @GetMapping("/zadanieList")
    public String zadanieList(Model model, Pageable pageable) {
        model.addAttribute("zadania", zadanieService.getZadania(pageable).getContent());
        return "zadanieList";
    }

    @GetMapping("/zadanieEdit")
    public String zadanieEdit(@RequestParam(name = "zadanieId", required = false) Integer zadanieId, Model model) {
        if (zadanieId != null) {
            model.addAttribute("zadanie", zadanieService.getZadanie(zadanieId).get());
        } else {
            Zadanie zadanie = new Zadanie();
            zadanie.setProjekt(new Projekt());
            model.addAttribute("zadanie", zadanie);
        }
        return "zadanieEdit";
    }

    @PostMapping(path = "/zadanieEdit")
    public String zadanieEditSave(@ModelAttribute @Valid Zadanie zadanie, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "zadanieEdit";
        }
        try {
            zadanie = zadanieService.setZadanie(zadanie);
        } catch (HttpException e) {
            bindingResult.rejectValue("", "error", e.getMessage());
            return "zadanieEdit";
        }
        return "redirect:/zadanieList";
    }

    @PostMapping(params = "cancel", path = "/zadanieEdit")
    public String zadanieEditCancel() {
        return "redirect:/zadanieList";
    }

    @PostMapping(params = "delete", path = "/zadanieEdit")
    public String zadanieEditDelete(@ModelAttribute Zadanie zadanie) {
        zadanieService.deleteZadanie(zadanie.getZadanieId());
        return "redirect:/zadanieList";
    }
}
