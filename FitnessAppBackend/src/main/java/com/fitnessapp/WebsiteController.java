package com.fitnessapp;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebsiteController {

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", "MusclePlay");
        model.addAttribute("tagline", "Smart AI-Powered Fitness & Real-Time Workout Tracking");
        return "pages/index";
    }
}