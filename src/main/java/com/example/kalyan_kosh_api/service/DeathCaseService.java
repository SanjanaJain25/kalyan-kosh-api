package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.dto.CreateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.UpdateDeathCaseRequest;
import com.example.kalyan_kosh_api.dto.DeathCaseResponse;
import com.example.kalyan_kosh_api.entity.DeathCase;
import com.example.kalyan_kosh_api.entity.DeathCaseStatus;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeathCaseService {

    private final DeathCaseRepository repository;
    private final ModelMapper mapper;

    public DeathCaseService(DeathCaseRepository repository, ModelMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public DeathCaseResponse create(CreateDeathCaseRequest req, String userId) {

        DeathCase deathCase = DeathCase.builder()
                .deceasedName(req.getDeceasedName())
                .employeeCode(req.getEmployeeCode())
                .department(req.getDepartment())
                .district(req.getDistrict())
                .nomineeName(req.getNomineeName())
                .nomineeAccountNumber(req.getNomineeAccountNumber())
                .nomineeIfsc(req.getNomineeIfsc())
                .caseMonth(req.getCaseMonth())
                .caseYear(req.getCaseYear())
                .status(DeathCaseStatus.OPEN)
                .createdBy(userId) // Now receives userId instead of username
                .build();

        return mapper.map(repository.save(deathCase), DeathCaseResponse.class);
    }

    public List<DeathCaseResponse> getAll() {
        return repository.findAll()
                .stream()
                .map(dc -> mapper.map(dc, DeathCaseResponse.class))
                .toList();
    }

    public DeathCaseResponse getById(Long id) {
        DeathCase dc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death case not found"));
        return mapper.map(dc, DeathCaseResponse.class);
    }

    public DeathCaseResponse update(
            Long id,
            UpdateDeathCaseRequest req,
            String userId) { // Now receives userId instead of adminUsername

        DeathCase dc = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Death case not found"));

        dc.setDeceasedName(req.getDeceasedName());
        dc.setEmployeeCode(req.getEmployeeCode());
        dc.setDepartment(req.getDepartment());
        dc.setDistrict(req.getDistrict());
        dc.setNomineeName(req.getNomineeName());
        dc.setNomineeAccountNumber(req.getNomineeAccountNumber());
        dc.setNomineeIfsc(req.getNomineeIfsc());
        dc.setCaseMonth(req.getCaseMonth());
        dc.setCaseYear(req.getCaseYear());
        dc.setStatus(req.getStatus());
        dc.setUpdatedBy(userId); // Now receives userId instead of username

        return mapper.map(repository.save(dc), DeathCaseResponse.class);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Death case not found");
        }
        repository.deleteById(id);
    }
}
