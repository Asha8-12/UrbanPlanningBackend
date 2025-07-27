package com.example.UrbanPlanningMS.repositories;

import com.example.UrbanPlanningMS.models.Officer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OfficerRepo extends JpaRepository<Officer, Integer> {
    boolean existsByEmail(String email);
    Optional<Officer> findByEmail(String email);
}
