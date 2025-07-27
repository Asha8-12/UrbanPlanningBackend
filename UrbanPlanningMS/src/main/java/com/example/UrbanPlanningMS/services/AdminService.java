package com.example.UrbanPlanningMS.services;

import com.example.UrbanPlanningMS.repositories.AdminRepo;
import org.springframework.stereotype.Service;
import com.example.UrbanPlanningMS.models.Admin;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

@Service
public class AdminService {
    @Autowired
    public AdminRepo adminRepo;

    public Admin post (Admin admin){
        return adminRepo.save(admin);
    }


    public List<Admin> getAdmin() {
        return adminRepo.findAll();
    }

    public Optional<Admin> getById(Integer id){
        return adminRepo.findById(id);
    }
    public void deleteById(Integer id){
        adminRepo.deleteById(id);
    }
    public Admin updateAdmin(int id, Admin adminDetails){
        Optional<Admin> optionalAdmin = adminRepo.findById(id);
        if (optionalAdmin.isPresent()){
            Admin admin = optionalAdmin.get();
            admin.setName(adminDetails.getName());
            admin.setEmail(adminDetails.getEmail());
            admin.setPassword(adminDetails.getPassword());
            return adminRepo.save(admin);
        }
        else {
            return null;
        }
    }

}
