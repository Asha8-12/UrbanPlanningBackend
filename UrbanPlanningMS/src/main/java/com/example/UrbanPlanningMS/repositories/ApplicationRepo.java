package com.example.UrbanPlanningMS.repositories;

import com.example.UrbanPlanningMS.models.Application;
import com.example.UrbanPlanningMS.models.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepo extends JpaRepository<Application, Long> {

    List<Application> findByEmail(String email);
    List<Application> findByCurrentStep(String step);
    List<Application> findByStatus(ApplicationStatus status);
    List<Application> findByStatusAndCurrentStep(ApplicationStatus status, String step);
}
