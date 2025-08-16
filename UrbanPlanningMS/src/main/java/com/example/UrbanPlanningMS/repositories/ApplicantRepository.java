package com.example.UrbanPlanningMS.repositories;


import com.example.UrbanPlanningMS.models.Applicant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicantRepository extends JpaRepository<Applicant, Integer> {
    Optional<Applicant> findByEmail(String email);

}
