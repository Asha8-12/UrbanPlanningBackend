package com.example.UrbanPlanningMS.services;

import com.example.UrbanPlanningMS.models.Applicant;
import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.ApplicantRepository;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ApplicantService {

    @Autowired
    private ApplicantRepository applicantRepository;

    @Autowired
    private UserRepository userRepository;

    public Applicant register(Applicant applicant, String password, String role) throws Exception {
        // Check if email already exists in user table
        if (userRepository.findByEmail(applicant.getEmail()).isPresent()) {
            throw new Exception("Email already in use");
        }

        // Save user data
        User user = new User();
        user.setEmail(applicant.getEmail());
        user.setPassword(password);
        user.setRole(role);
        userRepository.save(user);

        // Save applicant data
        return applicantRepository.save(applicant);
    }

    public List<Applicant> getApplicants() {
        return applicantRepository.findAll();
    }

    public Optional<Applicant> getById(Integer id) {
        return applicantRepository.findById(id);
    }


    public Applicant updateApplicantAndUser(String oldEmail, String name, String phone, String newEmail) throws Exception {
        Optional<User> userOpt = userRepository.findByEmail(oldEmail);
        Optional<Applicant> applicantOpt = applicantRepository.findByEmail(oldEmail);

        if (userOpt.isEmpty() || applicantOpt.isEmpty()) {
            throw new Exception("User not found with email: " + oldEmail);
        }

        // Check if new email is different and unique
        if (newEmail != null && !newEmail.equalsIgnoreCase(oldEmail)) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                throw new Exception("New email is already in use");
            }
        }

        User user = userOpt.get();
        Applicant applicant = applicantOpt.get();

        // Update name if provided
        if (name != null && !name.isBlank()) {
            applicant.setName(name);
        }

        // Update phone if provided
        if (phone != null && !phone.isBlank()) {
            applicant.setPhone(phone);
        }

        // Update email if provided and different
        if (newEmail != null && !newEmail.equalsIgnoreCase(oldEmail)) {
            user.setEmail(newEmail);
            applicant.setEmail(newEmail);
        }

        userRepository.save(user);
        return applicantRepository.save(applicant);
    }

    public long countApplicants() {
        return applicantRepository.count();
    }




    public Optional<Applicant> getByEmail(String email) {
        return applicantRepository.findByEmail(email);
    }

    public void deleteApplicantAndUserByEmail(String email) throws Exception {
        Optional<Applicant> applicantOpt = applicantRepository.findByEmail(email);
        Optional<User> userOpt       = userRepository.findByEmail(email);

        applicantOpt.ifPresent(applicantRepository::delete);
        userOpt.ifPresent(userRepository::delete);

        if (applicantOpt.isEmpty() && userOpt.isEmpty()) {
            throw new Exception("No record found for email: " + email);
        }
    }

    public Optional<Applicant> getById(int id) {
        return applicantRepository.findById(id);
    }


}

