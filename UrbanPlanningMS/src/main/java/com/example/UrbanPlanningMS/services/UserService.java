package com.example.UrbanPlanningMS.services;

import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public User register(String email, String password, String role) throws Exception {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new Exception("Email already in use");
        }
        return userRepository.save(new User(email, password, role));
    }

    public User login(String email, String password, HttpSession session) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isPresent() && optionalUser.get().getPassword().equals(password)) {
            session.setAttribute("user", optionalUser.get());
            return optionalUser.get();
        } else {
            throw new Exception("Invalid email or password");
        }
    }

    public void resetPassword(String email, String newPassword) throws Exception {
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if (optionalUser.isEmpty()) throw new Exception("User not found");

        User user = optionalUser.get();
        user.setPassword(newPassword);
        userRepository.save(user);
    }

    public User getCurrentUser(HttpSession session) throws Exception {
        User user = (User) session.getAttribute("user");
        if (user == null) throw new Exception("Not logged in");
        return user;
    }
}
