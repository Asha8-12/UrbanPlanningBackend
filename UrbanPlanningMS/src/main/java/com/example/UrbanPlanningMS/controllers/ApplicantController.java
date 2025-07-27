package com.example.UrbanPlanningMS.controllers;

import com.example.UrbanPlanningMS.models.Applicant;
import com.example.UrbanPlanningMS.services.ApplicantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/applicant")
public class ApplicantController {

    @Autowired
    private ApplicantService applicantService;

    // Register applicant and user
    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String password,
            @RequestParam(defaultValue = "applicant") String role
    ) {
        try {
            Applicant applicant = new Applicant();
            applicant.setName(name);
            applicant.setEmail(email);
            applicant.setPhone(phone);

            Applicant savedApplicant = applicantService.register(applicant, password, role);
            return ResponseEntity.ok(savedApplicant);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/by-email")
    public ResponseEntity<?> getByEmail(@RequestParam String email) {
        Optional<Applicant> applicantOpt = applicantService.getByEmail(email);

        if (applicantOpt.isPresent()) {
            return ResponseEntity.ok(applicantOpt.get());
        } else {
            return ResponseEntity.status(404).body("Applicant not found with email: " + email);
        }
    }

    @GetMapping("/all")
    public List<Applicant> getAll() {
        return applicantService.getApplicants();
    }

    // Admin or Applicant can update
    @PutMapping("/update/{oldEmail}")
    public ResponseEntity<?> update(@PathVariable String oldEmail,
                                    @RequestParam(required = false) String name,
                                    @RequestParam(required = false) String phone,
                                    @RequestParam(required = false) String newEmail) {
        try {
            Applicant updated = applicantService.updateApplicantAndUser(oldEmail, name, phone, newEmail);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<?> deleteByEmail(@PathVariable String email,
                                           @RequestParam String requesterRole) {
        if (!requesterRole.equalsIgnoreCase("ADMIN")) {
            return ResponseEntity.status(403).body("Only admin can delete accounts");
        }
        try {
            applicantService.deleteApplicantAndUserByEmail(email);
            return ResponseEntity.ok("Deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }


    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        Optional<Applicant> applicantOpt = applicantService.getById(id);

        if (applicantOpt.isPresent()) {
            return ResponseEntity.ok(applicantOpt.get());
        } else {
            return ResponseEntity.status(404).body("Applicant not found with ID: " + id);
        }
    }




}

