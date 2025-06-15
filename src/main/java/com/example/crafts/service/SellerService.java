package com.example.crafts.service;

import com.example.crafts.entity.Seller;
import com.example.crafts.repository.SellerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SellerService {

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String registerSeller(Seller seller) {
        if (sellerRepository.findByEmail(seller.getEmail()).isPresent()) {
            throw new RuntimeException("Seller already exists");
        }
        // ✅ Encode password here once
        seller.setPassword(passwordEncoder.encode(seller.getPassword()));
        return sellerRepository.save(seller).getName() + " registered successfully!";
    }

    public Seller getSellerByEmail(String email) {
        return sellerRepository.findByEmail(email).orElse(null);
    }

    public void updateProfile(String email, Seller updatedSeller) {
        Seller seller = getSellerByEmail(email);
        if (seller != null) {
            seller.setName(updatedSeller.getName());
            seller.setPassword(passwordEncoder.encode(updatedSeller.getPassword()));
            sellerRepository.save(seller);
        }
    }

    public String getStatus(String email) {
        Seller seller = getSellerByEmail(email);
        return (seller != null) ? seller.getStatus() : null;
    }

    public List<Seller> getPendingSellers() {
        return sellerRepository.findByStatus("PENDING");
    }

    public List<Seller> getApprovedSellers() {
        return sellerRepository.findByStatus("APPROVED");
    }

    public boolean approveSeller(int id) {
        Seller seller = sellerRepository.findById(id).orElse(null);
        if (seller != null && seller.getStatus().equals("PENDING")) {
            seller.setStatus("APPROVED");
            sellerRepository.save(seller);
            return true;
        }
        return false;
    }
}
