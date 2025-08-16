package com.example.UrbanPlanningMS.controllers;

import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // =======================
    // Password Utilities
    // =======================
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashedBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean matchPassword(String plainPassword, String hashedPassword) {
        return hashPassword(plainPassword).equals(hashedPassword);
    }

    // =======================
    // Registration
    // =======================
    @PostMapping("/register")
    public User register(@RequestParam String email,
                         @RequestParam String password,
                         @RequestParam String role) throws Exception {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already registered");
        }

        String hashedPassword = hashPassword(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRole(role);

        return userRepository.save(user);
    }

    // =======================
    // Login
    // =======================
    @PostMapping("/login")
    public User login(@RequestParam String email,
                      @RequestParam String password,
                      HttpSession session) throws Exception {

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent() && matchPassword(password, optionalUser.get().getPassword())) {
            session.setAttribute("user", optionalUser.get());
            return optionalUser.get();
        } else {
            throw new Exception("Invalid email or password");
        }
    }

    // =======================
    // Reset Password
    // =======================
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String email,
                                @RequestParam String newPassword) throws Exception {

        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) throw new Exception("User not found");

        User user = optionalUser.get();
        String hashedPassword = hashPassword(newPassword);
        user.setPassword(hashedPassword);
        userRepository.save(user);

        return "Password updated successfully";
    }

    // =======================
    // Get Current User
    // =======================
    @GetMapping("/me")
    public User me(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new Exception("Not logged in");
        return user;
    }
}
