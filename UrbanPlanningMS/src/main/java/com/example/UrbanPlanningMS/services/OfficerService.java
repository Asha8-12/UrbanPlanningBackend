package com.example.UrbanPlanningMS.services;

import com.example.UrbanPlanningMS.models.Officer;
import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.OfficerRepo;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class OfficerService {

    @Autowired
    private OfficerRepo officerRepo;
    @Autowired private UserRepository userRepo;

    public Officer registerOfficer(Officer officer) {
        String email = officer.getEmail();

        if (userRepo.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Create User
        User user = new User();
        user.setEmail(email);
        user.setPassword(officer.getUser().getPassword());
        user.setRole("OFFICER");
        user.setPosition(officer.getPosition());

        userRepo.save(user);
        officer.setUser(user);

        return officerRepo.save(officer);
    }
//    public Officer getOfficerByEmail(String email) {
//        return officerRepo.findByEmail(email);
//    }

    public List<Officer> getAllOfficers() {
        return officerRepo.findAll();
    }

    // Get officer by email
    public Optional<Officer> getOfficerByEmail(String email) {
        return officerRepo.findByEmail(email);
    }
    public void deleteOfficerByEmail(String email) {
        Officer officer = officerRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        User user = officer.getUser();
        officerRepo.delete(officer);   // delete officer first due to FK
        userRepo.delete(user);         // then delete user
    }

    public Officer updateOfficer(String oldEmail, Officer updatedOfficer) {
        Officer existingOfficer = officerRepo.findByEmail(oldEmail)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        User user = existingOfficer.getUser();

        // Check for email conflict
        if (!oldEmail.equals(updatedOfficer.getEmail()) &&
                userRepo.findByEmail(updatedOfficer.getEmail()).isPresent()) {
            throw new RuntimeException("New email already in use");
        }

        // Update User
        user.setEmail(updatedOfficer.getEmail());
        user.setPassword(updatedOfficer.getUser().getPassword());
        user.setPosition(updatedOfficer.getPosition());
        userRepo.save(user);

        // Update Officer
        existingOfficer.setFullName(updatedOfficer.getFullName());
        existingOfficer.setEmail(updatedOfficer.getEmail());
        existingOfficer.setPhone(updatedOfficer.getPhone());
        existingOfficer.setAddress(updatedOfficer.getAddress());
        existingOfficer.setPosition(updatedOfficer.getPosition());
        existingOfficer.setUser(user);

        return officerRepo.save(existingOfficer);
    }
    public long count() {
        return officerRepo.count();
    }



}

