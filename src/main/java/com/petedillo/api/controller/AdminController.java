package com.petedillo.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/manage")
public class AdminController {

    @GetMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("login-page");
    }

    @GetMapping("/posts")
    public ResponseEntity<String> posts() {
        return ResponseEntity.ok("posts-page");
    }

    @PostMapping("/posts")
    public ResponseEntity<String> createPost() {
        return ResponseEntity.ok("created");
    }

    @PostMapping("/api/media/upload")
    public ResponseEntity<Map<String, String>> uploadMedia() {
        return ResponseEntity.badRequest().body(Map.of("error", "no file"));
    }
}
