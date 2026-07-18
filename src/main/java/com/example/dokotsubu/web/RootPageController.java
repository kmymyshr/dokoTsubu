package com.example.dokotsubu.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootPageController {

    @GetMapping("/")
    String redirectToLoginPage() {
        // Phase22でindex.jspファイルは廃止したが、Spring SecurityのログインURLとして /index.jsp は維持する。
        return "redirect:/index.jsp";
    }
}
