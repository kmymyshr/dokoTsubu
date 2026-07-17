package com.example.dokotsubu.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootPageController {

    @GetMapping("/")
    String redirectToLoginPage() {
        return "redirect:/index.jsp";
    }
}
