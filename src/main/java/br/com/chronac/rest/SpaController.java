package br.com.chronac.rest;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {
    @GetMapping({"/login", "/calendar"})
    public String app() {
        return "forward:/index.html";
    }
}
