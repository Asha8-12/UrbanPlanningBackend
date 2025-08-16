package com.example.UrbanPlanningMS.services;

import com.example.UrbanPlanningMS.models.Application;
import com.example.UrbanPlanningMS.models.ApplicationStatus;
import com.example.UrbanPlanningMS.repositories.ApplicationRepo;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApplicationService {

    @Autowired
    private ApplicationRepo repo;

    @Autowired
    private JavaMailSender mailSender;

    public Application submit(Application app, MultipartFile landTitle, MultipartFile architecture,
                              MultipartFile structure, MultipartFile support, MultipartFile picture) throws IOException {

        String basePath = "uploads/";
        Files.createDirectories(Paths.get(basePath));

        app.setLandTitlePath(saveFile(basePath, landTitle));
        app.setArchitectureDrawingPath(saveFile(basePath, architecture));
        app.setStructuralDrawingPath(saveFile(basePath, structure));
        app.setSupportLetterPath(saveFile(basePath, support));
        app.setPicture(saveFile(basePath, picture));

        double amount = calculateAmount(app.getPlotNumber(), app.getPropertySize());
        String controlNo = "20010611" + String.format("%02d", (int)(Math.random() * 90 + 10));

        app.setPaymentAmount(amount);
        app.setControlNumber(controlNo);
        app.setPaymentConfirmed(false);
        app.setStatus(ApplicationStatus.PENDING);
        app.setCurrentStep("Waiting for Payment");

        Application savedApp = repo.save(app);

        sendEmail(savedApp.getEmail(), "Control Number for Payment",
                "Hello " + savedApp.getFullName() + ",\n\nYour application was received.\n" +
                        "Control Number: " + savedApp.getControlNumber() + "\n" +
                        "Amount to Pay: TZS " + savedApp.getPaymentAmount() + "\n\n" +
                        "After payment, confirm using the control number.");

        return savedApp;
    }

    public Application confirmPayment(String controlNumber) {
        Application app = repo.findAll().stream()
                .filter(a -> controlNumber.equals(a.getControlNumber()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Invalid control number"));

        app.setPaymentConfirmed(true);
        app.setCurrentStep("Urban Planner");

        sendEmail(app.getEmail(), "Payment Confirmed",
                "Your payment has been confirmed. Your application is now under review.");

        return repo.save(app);
    }

    private String saveFile(String basePath, MultipartFile file) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = Paths.get(basePath, fileName);
        Files.write(filePath, file.getBytes());
        return filePath.toString();
    }

    private double calculateAmount(String plotNumber, String sizeStr) {
        double size = 1;
        try {
            if (sizeStr != null && sizeStr.contains("x")) {
                String[] parts = sizeStr.split("x");
                double length = Double.parseDouble(parts[0].trim());
                double width = Double.parseDouble(parts[1].trim());
                size = length * width;
            } else {
                size = Double.parseDouble(sizeStr.trim());
            }
        } catch (Exception e) {
            size = 1;
        }

        double baseRate = plotNumber.equalsIgnoreCase("none") ? 1000 : 2000;
        return size * baseRate;
    }

    public List<Application> getAll() { return repo.findAll(); }

    public List<Application> getByEmail(String email) { return repo.findByEmail(email); }

    public List<Application> getByStatus(ApplicationStatus status) { return repo.findByStatus(status); }

    public List<Application> getByStatusAndPosition(ApplicationStatus status, String step) {
        return repo.findByStatusAndCurrentStep(status, step);
    }

    public Application approve(Long id, String position) {
        Application app = repo.findById(id).orElseThrow();

        if (!app.getCurrentStep().equals(position)) {
            throw new RuntimeException("Not authorized to approve this application");
        }

        switch (position) {
            case "Urban Planner" -> app.setCurrentStep("Chief Architecture");
            case "Chief Architecture" -> app.setCurrentStep("Chief Engineer");
            case "Chief Engineer" -> {
                app.setCurrentStep("Completed");
                app.setStatus(ApplicationStatus.APPROVED);

                try {
                    Files.deleteIfExists(Paths.get("uploads/Permit_" + app.getId() + ".pdf"));
                } catch (IOException ignored) {}

                String permitPath = generatePermit(app);
                app.setPermitPath(permitPath);

                sendEmail(app.getEmail(), "Application Approved",
                        "Your construction permit has been approved.");
            }
        }

        return repo.save(app);
    }

    // ✅ NOW PUBLIC - for use in ApplicationController
    public String generatePermit(Application app) {
        String filename = "Permit_" + app.getId() + ".pdf";
        String path = "uploads/" + filename;

        try {
            Document doc = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(doc, new FileOutputStream(new File(path)));
            doc.open();

            Image logo = Image.getInstance("uploads/logo.png");
            logo.scaleToFit(100, 100);
            logo.setAlignment(Image.ALIGN_CENTER);
            doc.add(logo);

            Paragraph header = new Paragraph("THE REVOLUTIONARY GOVERNMENT OF ZANZIBAR\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
            header.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(header);

            Paragraph subHeader = new Paragraph("DEVELOPMENT CONTROL UNIT\nKAMATI YA USIMAMIZI NA UDHIBITI WA UJENZI\n\nDEVELOPMENT CONTROL REGULATION, 2015\n\n", FontFactory.getFont(FontFactory.HELVETICA, 12));
            subHeader.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(subHeader);

            doc.add(new Paragraph("Permit No: APP-2025-" + app.getId()));
            doc.add(new Paragraph("Approved On: " + LocalDateTime.now().toLocalDate()));
            doc.add(new Paragraph("\n"));

            Paragraph permitTitle = new Paragraph("A BUILDING PERMIT", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13));
            permitTitle.setAlignment(Paragraph.ALIGN_CENTER);
            doc.add(permitTitle);
            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("Name: " + app.getFullName()));
            doc.add(new Paragraph("Project: " + app.getProjectType()));
            doc.add(new Paragraph("Property: " + app.getPropertyAddress()));
            doc.add(new Paragraph("\n"));

            doc.add(new Paragraph("The following conditions shall apply:\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11)));
            doc.add(new Paragraph("1. The building activities shall start within 12 months from the date of issuance of this permit."));
            doc.add(new Paragraph("2. Any major alterations shall have the consent from the Building Permit and Control Technical Committee."));
            doc.add(new Paragraph("3. The Unit shall have the right to inspect the construction site."));
            doc.add(new Paragraph("4. The construction shall observe and abide by environmental regulations."));
            doc.add(new Paragraph("5. The project must comply with Government regulations and directives."));
            doc.add(new Paragraph("6. The Authority may cancel or demolish if not as per approved drawings."));
            doc.add(new Paragraph("7. This permit must be produced on site when requested.\n"));
            doc.add(new Paragraph("\n\nThank you\n\n"));

            try {
                Image signature = Image.getInstance("uploads/chairman_signature.png");
                signature.scaleToFit(100, 50);
                signature.setAlignment(Image.ALIGN_LEFT);
                doc.add(signature);
            } catch (Exception e) {
                System.err.println("⚠️ Chairman signature not found. Skipping.");
            }

            doc.add(new Paragraph("Chairman", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12)));
            doc.close();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return path;
    }

    public Application reject(Long id, String position, String remarks) {
        Application app = repo.findById(id).orElseThrow();

        if (!app.getCurrentStep().equals(position)) {
            throw new RuntimeException("Not authorized to reject this application");
        }

        app.setStatus(ApplicationStatus.REJECTED);
        app.setRemarks(remarks);

        // Hii ndiyo sehemu muhimu: kuweka tarehe ya kukataliwa sasa hivi
        app.setDateRejected(LocalDateTime.now());

        sendEmail(app.getEmail(), "Application Rejected",
                "Your application was rejected by " + position + ". Reason: " + remarks);

        return repo.save(app);
    }


    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setText(body);
        mailSender.send(msg);
    }
    // ✅ Count all applications
    public long count() {
        return repo.count();
    }

    public Application getApplicationById(Long id) {
        return repo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));
    }

    public List<Application> getPendingPayments() {
        return repo.findByPaymentConfirmedFalse();
    }

    public Long countByStatus(ApplicationStatus status) {
        return repo.countByStatus(status);
    }


    // ✅ NEW: For saving updated application in controller
    public Application saveApplication(Application app) {
        return repo.save(app);
    }


}
