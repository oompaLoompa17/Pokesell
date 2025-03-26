package com.example.Pokemon_TCG_TEST.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping(value = "/{path:[^\\.]*}")
    public String redirectToAngular() {
        System.out.println("\n\n\n\n Forwarding to index.html for path: \n\n\n\n" + System.currentTimeMillis());
        return "forward:/index.html";
    }
}