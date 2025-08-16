package com.example.UrbanPlanningMS.controllers;

import com.example.UrbanPlanningMS.models.Application;
import com.example.UrbanPlanningMS.models.ApplicationStatus;
import com.example.UrbanPlanningMS.services.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/applications")
public class ApplicationController {

    @Autowired
    private ApplicationService service;

    @PostMapping("/submit")
    public ResponseEntity<?> submit(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String propertyAddress,
            @RequestParam String propertySize,
            @RequestParam String plotNumber,
            @RequestParam Double latitude,
            @RequestParam Double longitude,
            @RequestParam String projectType,
            @RequestParam String projectDescription,
            @RequestParam String estimatedCost,
            @RequestParam String startDate,
            @RequestParam String duration,
            @RequestParam String currencyUnit,
            @RequestParam String durationUnit,
            @RequestParam("landTitle") MultipartFile landTitle,
            @RequestParam("architecture") MultipartFile architecture,
            @RequestParam("structure") MultipartFile structure,
            @RequestParam("support") MultipartFile support,
            @RequestParam("picture") MultipartFile picture
    ) {
        try {
            Application app = new Application();
            app.setFullName(fullName);
            app.setEmail(email);
            app.setPhone(phone);
            app.setPropertyAddress(propertyAddress);
            app.setPropertySize(propertySize);
            app.setPlotNumber(plotNumber);
            app.setLatitude(latitude);           // 📍 Set latitude
            app.setLongitude(longitude);         // 📍 Set longitude
            app.setProjectType(projectType);
            app.setProjectDescription(projectDescription);
            app.setEstimatedCost(estimatedCost);
            app.setStartDate(startDate);
            app.setDuration(duration);
            app.setCurrencyUnit(currencyUnit);
            app.setDurationUnit(durationUnit);


            return ResponseEntity.ok(service.submit(app, landTitle, architecture, structure, support, picture));
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Upload failed: " + e.getMessage());
        }
    }

    @PutMapping("/confirm-payment")
    public ResponseEntity<?> confirmPayment(@RequestParam String controlNumber) {
        try {
            return ResponseEntity.ok(service.confirmPayment(controlNumber));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/all")
    public List<Application> getAll() {
        return service.getAll();
    }

    @GetMapping("/my")
    public List<Application> getByEmail(@RequestParam String email) {
        return service.getByEmail(email);
    }

    @GetMapping("/count/all")
    public ResponseEntity<Long> countAll() {
        return ResponseEntity.ok(service.count());
    }


//    @GetMapping("/status")
//    public List<Application> getByStatus(@RequestParam ApplicationStatus status) {
//        return service.getByStatus(status);
//    }

    @GetMapping("/pending")
    public List<Application> pendingByOfficer(@RequestParam String position) {
        return service.getByStatusAndPosition(ApplicationStatus.PENDING, position);
    }

    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approve(@PathVariable Long id, @RequestParam String position) {
        try {
            return ResponseEntity.ok(service.approve(id, position));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/reject/{id}")
    public ResponseEntity<?> reject(
            @PathVariable Long id,
            @RequestParam String position,
            @RequestParam String remarks
    ) {
        try {
            return ResponseEntity.ok(service.reject(id, position, remarks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/permit/{id}")
    public ResponseEntity<?> getPermit(@PathVariable Long id) {
        try {
            Application app = service.getApplicationById(id);

            // Ruhusu tu ikiwa status ni APPROVED
            if (app.getStatus() != ApplicationStatus.APPROVED) {
                return ResponseEntity.badRequest().body("Permit not available - application not yet approved.");
            }

            // Kama permitPath haipo au file limepotea, tengeneza permit mpya
            boolean permitMissing = app.getPermitPath() == null || !Files.exists(Paths.get(app.getPermitPath()));
            if (permitMissing) {
                String permitPath = service.generatePermit(app);
                app.setPermitPath(permitPath);
                service.saveApplication(app); // method hii inapaswa kuwepo kwenye ApplicationService
            }

            Path path = Paths.get(app.getPermitPath());
            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body("Permit file not found on server.");
            }

            byte[] fileBytes = Files.readAllBytes(path);
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=" + path.getFileName())
                    .header("Content-Type", "application/pdf")
                    .body(fileBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Internal server error: " + e.getMessage());
        }

    }

    @GetMapping("/rejected")
    public List<Application> rejectedByOfficer(@RequestParam String position) {
        return service.getByStatusAndPosition(ApplicationStatus.REJECTED, position);
    }
    @GetMapping("/status")
    public List<Application> getByStatus(
            @RequestParam ApplicationStatus status,
            @RequestParam(required = false) String position) {
        if (position == null || position.isEmpty()) {
            return service.getByStatus(status);
        } else {
            return service.getByStatusAndPosition(status, position);
        }
    }
    @GetMapping("/approved/all")
    public List<Application> getAllApproved() {
        return service.getByStatus(ApplicationStatus.APPROVED);
    }

    @GetMapping("/count/approved")
    public ResponseEntity<Long> countApproved() {
        return ResponseEntity.ok(service.getByStatus(ApplicationStatus.APPROVED).stream().count());
    }

    @GetMapping("/count/rejected")
    public ResponseEntity<Long> countRejected() {
        return ResponseEntity.ok(service.getByStatus(ApplicationStatus.REJECTED).stream().count());
    }

    @GetMapping("/pending-payments")
    public List<Application> getPendingPayments() {
        return service.getPendingPayments();
    }


}

