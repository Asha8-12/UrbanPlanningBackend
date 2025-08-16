package com.example.UrbanPlanningMS.services;

import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import com.example.UrbanPlanningMS.Utils.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Register new user
    public User register(String email, String password, String role) throws Exception {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already registered");
        }

        // 👇 HASHA password kabla ya ku-save
        String hashedPassword = PasswordUtil.hashPassword(password);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);  // save hash, not plain
        user.setRole(role);

        return userRepository.save(user);
    }

    // Login
    public User login(String email, String password, HttpSession session) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User not found"));

        // 👇 Compare plain password (hashed ndani ya PasswordUtil)
        if (!PasswordUtil.matchPassword(password, user.getPassword())) {
            throw new Exception("Invalid password");
        }

        session.setAttribute("user", user);
        return user;
    }

    // Reset password
    public void resetPassword(String email, String newPassword) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User not found"));

        // 👇 Save hashed reset password
        user.setPassword(PasswordUtil.hashPassword(newPassword));
        userRepository.save(user);
    }

    // Get logged in user
    public User getCurrentUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            throw new Exception("Not logged in");
        }
        return user;
    }
}
