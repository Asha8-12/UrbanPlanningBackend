package com.example.UrbanPlanningMS.migration;

import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import com.example.UrbanPlanningMS.Utils.PasswordUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PasswordMigrationRunner implements CommandLineRunner {

    private final UserRepository userRepository;

    public PasswordMigrationRunner(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            String password = user.getPassword();

            // Encode kama bado haijawa hash
            if (!PasswordUtil.isHashed(password)) {
                String encoded = PasswordUtil.hashPassword(password);
                user.setPassword(encoded);
                userRepository.save(user);
                System.out.println("Password hashed for: " + user.getEmail());
            }
        }

        System.out.println("Password migration completed!");
    }
}
