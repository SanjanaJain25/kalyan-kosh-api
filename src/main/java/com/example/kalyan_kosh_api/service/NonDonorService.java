package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.User;
import com.example.kalyan_kosh_api.repository.ReceiptRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NonDonorService {

    private final UserRepository userRepo;
    private final ReceiptRepository receiptRepo;

    public NonDonorService(UserRepository userRepo, ReceiptRepository receiptRepo) {
        this.userRepo = userRepo;
        this.receiptRepo = receiptRepo;
    }

    public List<User> getNonDonors(int month, int year) {

        List<User> allUsers = userRepo.findAll();
        List<User> donors = receiptRepo.findDonors(month, year);

        return allUsers.stream()
                .filter(u -> !donors.contains(u))
                .toList();
    }
}
