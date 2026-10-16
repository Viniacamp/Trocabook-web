package com.trocabook.Trocabook.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String indexAdmin() {
        return "admin/indexAdmin";
    }

    @GetMapping("/usuarios")
    public String usuarios() {
        return "admin/listaUsuarios";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/ajuda")
    public String ajuda() {
        return "admin/ajudaAdmin";
    }
}