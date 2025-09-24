package com.nextinnomind.campusnestbackend.controller.Pages;

import com.nextinnomind.campusnestbackend.dto.auth.AuthResponse;
import com.nextinnomind.campusnestbackend.entity.User;
import com.nextinnomind.campusnestbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
@RequestMapping("/auth/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;

    @GetMapping
    public String showDashboard() {
        // Serve Thymeleaf page; data will be loaded via JS API call
        return "dashboard"; // templates/dashboard.html
    }
}

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
class DashboardApiController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getDashboard(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }

        // Build dashboard response
        return ResponseEntity.ok().body(user); // send user info to frontend
    }
}
