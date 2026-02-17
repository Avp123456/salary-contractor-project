package com.project.login.controller;

import com.project.login.entity.QualityMasterEntity;
import com.project.login.service.QualityMasterService;
import com.project.security.CustomUserDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/quality")
public class QualityMasterController {

    private final QualityMasterService service;

    public QualityMasterController(QualityMasterService service) {
        this.service = service;
    }

    @GetMapping
    public String list(Model model, Authentication authentication) {
    	System.out.println("Page Visited: Quality Master");
        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        model.addAttribute("list", service.findByUser(userId));

        return "QualityMaster/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
    	System.out.println("Button Clicked: create(quality master)");

        model.addAttribute("quality", new QualityMasterEntity());

        return "QualityMaster/form";
    }

    @PostMapping("/save")
    public String save(@ModelAttribute QualityMasterEntity quality,
                       Authentication authentication) {
    	System.out.println("Button Clicked: save(quality master)");

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        quality.setUserId(userDetails.getId());

        service.save(quality);

        return "redirect:/quality";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id,
                           Model model,
                           Authentication authentication) {
    	System.out.println("Button Clicked: edit(quality master)");

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        QualityMasterEntity quality = service.findByIdAndUser(id, userId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        model.addAttribute("quality", quality);

        return "QualityMaster/form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id,
                         Authentication authentication) {
    	System.out.println("Button Clicked: delete(quality master)");

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        service.delete(id, userId);

        return "redirect:/quality";
    }
}
