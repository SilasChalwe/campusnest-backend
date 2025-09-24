package com.nextinnomind.campusnestbackend.controller.Pages;

import com.nextinnomind.campusnestbackend.dto.auth.LoginRequest;
import com.nextinnomind.campusnestbackend.dto.auth.RegisterRequest;
import com.nextinnomind.campusnestbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthPageController {

    private final AuthService authService;

    @GetMapping("/register")
    public String showRegisterPage() {
        return "register"; // templates/register.html
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute RegisterRequest request, Model model) {
        var response = authService.register(request);
        model.addAttribute("message", "Registration successful!");
        model.addAttribute("user", response);
        return "register-success"; // create register-success.html template
    }

    @GetMapping("/login")
    public String showLoginPage() {
        return "login"; // templates/login.html
    }

    @PostMapping("/login")
    public String loginUser(@ModelAttribute LoginRequest request, Model model) {
        var response = authService.login(request);

        // Check if email/phone is verified before allowing dashboard access
        if (!response.getUser().getEmailVerified() || !response.getUser().getPhoneVerified()) {
            model.addAttribute("message", "Please verify your email and phone before accessing the dashboard.");
            return "verify-email"; // or a combined verification page
        }

        // Add user info to model to display in dashboard
        model.addAttribute("user", response.getUser());
        return "dashboard"; // templates/dashboard.html
    }

    @GetMapping("/verify/email")
    public String showVerifyEmailPage() {
        return "verify-email"; // templates/verify-email.html
    }

    @GetMapping("/verify/phone")
    public String showVerifyPhonePage() {
        return "verify-phone"; // templates/verify-phone.html
    }

    @GetMapping("/logout")
    public String logoutPage() {
        return "logout"; // templates/logout.html
    }
}
