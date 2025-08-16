package com.example.UrbanPlanningMS.controllers;

import com.example.UrbanPlanningMS.models.Officer;
import com.example.UrbanPlanningMS.models.User;
import com.example.UrbanPlanningMS.repositories.OfficerRepo;
import com.example.UrbanPlanningMS.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    @Autowired
    private OfficerRepo officerRepo;

    @Autowired
    private UserRepository userRepo;

    // ------------------ MD5 Hashing ------------------
    private String md5Hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    private boolean md5Match(String plainPassword, String hashedPassword) {
        return md5Hash(plainPassword).equals(hashedPassword);
    }

    // ------------------ Register Officer ------------------
    @PostMapping("/register")
    public ResponseEntity<?> registerOfficer(@RequestBody Officer officer) {
        String email = officer.getEmail();

        if (userRepo.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email already in use");
        }

        // Create User with MD5 password
        User user = new User();
        user.setEmail(email);
        user.setPassword(md5Hash(officer.getUser().getPassword())); // hash here
        user.setRole("OFFICER");
        user.setPosition(officer.getPosition());

        userRepo.save(user);
        officer.setUser(user);

        officerRepo.save(officer);
        return ResponseEntity.ok("Officer registered successfully");
    }

    // ------------------ Officer Login ------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String email,
                                   @RequestParam String password,
                                   HttpSession session) {
        Optional<User> userOpt = userRepo.findByEmail(email);
        if (userOpt.isPresent() && md5Match(password, userOpt.get().getPassword())) {
            session.setAttribute("user", userOpt.get());
            return ResponseEntity.ok("Login successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    // ------------------ Get All Officers ------------------
    @GetMapping("/all")
    public ResponseEntity<List<Officer>> getAllOfficers() {
        return ResponseEntity.ok(officerRepo.findAll());
    }

    // ------------------ Get Officer by Email ------------------
    @GetMapping("/{email}")
    public ResponseEntity<?> getOfficerByEmail(@PathVariable String email) {
        Optional<Officer> officerOpt = officerRepo.findByEmail(email);
        return officerOpt
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Officer not found"));
    }

    // ------------------ Count Officers ------------------
    @GetMapping("/count")
    public long countOfficer() {
        return officerRepo.count();
    }

    // ------------------ Delete Officer ------------------
    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteOfficer(@PathVariable String email) {
        Optional<Officer> officerOpt = officerRepo.findByEmail(email);
        if (officerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Officer not found");

        Officer officer = officerOpt.get();
        User user = officer.getUser();

        officerRepo.delete(officer);
        userRepo.delete(user);

        return ResponseEntity.ok("Officer deleted successfully");
    }

    // ------------------ Update Officer ------------------
    @PutMapping("/update/{oldEmail}")
    public ResponseEntity<?> updateOfficer(@PathVariable String oldEmail, @RequestBody Officer updatedOfficer) {
        Optional<Officer> officerOpt = officerRepo.findByEmail(oldEmail);
        if (officerOpt.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Officer not found");

        Officer existingOfficer = officerOpt.get();
        User user = existingOfficer.getUser();

        // Check email conflict
        if (!oldEmail.equals(updatedOfficer.getEmail()) && userRepo.findByEmail(updatedOfficer.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("New email already in use");
        }

        // Update User
        user.setEmail(updatedOfficer.getEmail());
        user.setPassword(md5Hash(updatedOfficer.getUser().getPassword())); // hash password
        user.setPosition(updatedOfficer.getPosition());
        userRepo.save(user);

        // Update Officer
        existingOfficer.setFullName(updatedOfficer.getFullName());
        existingOfficer.setEmail(updatedOfficer.getEmail());
        existingOfficer.setPhone(updatedOfficer.getPhone());
        existingOfficer.setAddress(updatedOfficer.getAddress());
        existingOfficer.setPosition(updatedOfficer.getPosition());
        existingOfficer.setUser(user);

        officerRepo.save(existingOfficer);
        return ResponseEntity.ok(existingOfficer);
    }
}
