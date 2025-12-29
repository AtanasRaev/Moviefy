package com.moviefy.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthCheckController {
    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        System.out.println("pong");
        return ResponseEntity.ok("pong");
    }
}
