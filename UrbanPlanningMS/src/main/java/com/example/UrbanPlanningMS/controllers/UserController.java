package com.example.UrbanPlanningMS.controllers;

import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.services.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public User register(@RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String role) throws Exception {
        return userService.register(email, password, role);
    }

    @PostMapping("/login")
    public User login(@RequestParam String email,
                      @RequestParam String password,
                      HttpSession session) throws Exception {
        return userService.login(email, password, session);
    }

    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword) throws Exception {
        userService.resetPassword(email, newPassword);
        return "Password updated successfully";
    }

    @GetMapping("/me")
    public User me(HttpSession session) throws Exception {
        return userService.getCurrentUser(session);
    }
}
