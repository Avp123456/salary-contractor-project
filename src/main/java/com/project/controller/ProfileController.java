package com.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ProfileController {
	 //time stamp
    String time = java.time.LocalDateTime.now().toString();


    @GetMapping("/contractor/profile")
    public String profile() {
        System.out.println("[INFO] Profile Page Visited "+ time);

        return "contractor/profile";
    }

}
