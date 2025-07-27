package com.example.UrbanPlanningMS.controllers;

import com.example.UrbanPlanningMS.models.Officer;
import com.example.UrbanPlanningMS.services.OfficerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/officers")
public class OfficerController {

    @Autowired
    private OfficerService officerService;

    @PostMapping("/register")
    public ResponseEntity<?> registerOfficer(@RequestBody Officer officer) {
        try {
            Officer saved = officerService.registerOfficer(officer);
            return ResponseEntity.ok("Officer registered successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

//    @GetMapping("/{email}")
//    public ResponseEntity<Officer> getOfficer(@PathVariable String email) {
//        Officer officer = officerService.getOfficerByEmail(email);
//        if (officer == null) {
//            return ResponseEntity.notFound().build();
//        }
//        return ResponseEntity.ok(officer);
//    }
    @GetMapping("/all")
    public ResponseEntity<?> getAllOfficers() {
        return ResponseEntity.ok(officerService.getAllOfficers());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> getOfficerByEmail(@PathVariable String email) {
        Optional<Officer> officerOpt = officerService.getOfficerByEmail(email);
        return officerOpt
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body("Officer not found"));
    }

    @DeleteMapping("/delete/{email}")
    public ResponseEntity<String> deleteOfficer(@PathVariable String email) {
        try {
            officerService.deleteOfficerByEmail(email);
            return ResponseEntity.ok("Officer deleted successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PutMapping("/update/{oldEmail}")
    public ResponseEntity<?> updateOfficer(@PathVariable String oldEmail, @RequestBody Officer officer) {
        try {
            Officer updated = officerService.updateOfficer(oldEmail, officer);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


}
