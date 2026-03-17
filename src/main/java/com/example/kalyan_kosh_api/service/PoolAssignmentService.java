package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.*;
import com.example.kalyan_kosh_api.repository.DeathCaseRepository;
import com.example.kalyan_kosh_api.repository.IdSequenceRepository;
import com.example.kalyan_kosh_api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.List;

@Service
public class PoolAssignmentService {

    private static final String POOL_RR_SEQUENCE = "POOL_RR_INDEX";

    private final DeathCaseRepository deathCaseRepo;
    private final UserRepository userRepo;
    private final IdSequenceRepository idSequenceRepo;

    public PoolAssignmentService(DeathCaseRepository deathCaseRepo,
                                 UserRepository userRepo,
                                 IdSequenceRepository idSequenceRepo) {
        this.deathCaseRepo = deathCaseRepo;
        this.userRepo = userRepo;
        this.idSequenceRepo = idSequenceRepo;
    }

    /**
     * Assign pool to NEW user using Round Robin across OPEN death cases.
     */
    @Transactional
    public void assignPoolToNewUser(User user) {
        List<DeathCase> activePools = deathCaseRepo.findByStatusOrderByIdAsc(DeathCaseStatus.OPEN);

        if (activePools.isEmpty()) {
            user.setAssignedDeathCase(null);
            return;
        }

        // Use existing IdSequence table to do safe round-robin
        IdSequence seq = idSequenceRepo.findBySequenceNameWithLock(POOL_RR_SEQUENCE)
                .orElseGet(() -> {
                    IdSequence s = new IdSequence();
                    s.setSequenceName(POOL_RR_SEQUENCE);
                    s.setCurrentValue(-1L);
                    return idSequenceRepo.save(s);
                });

        long next = seq.getCurrentValue() + 1;
        seq.setCurrentValue(next);
        idSequenceRepo.save(seq);

        int index = (int) (next % activePools.size());
        user.setAssignedDeathCase(activePools.get(index));
    }

    /**
     * Admin action: equally distribute ROLE_USER across all OPEN death cases.
     */
 @Transactional
public void rebalanceAllUsersAcrossPools(boolean overwrite) {

    List<DeathCase> activePools = deathCaseRepo.findByStatusOrderByIdAsc(DeathCaseStatus.OPEN);

    if (activePools.isEmpty()) {
        return;
    }

    List<User> users = userRepo.findAll();

    // Only rebalance normal users
    List<User> eligibleUsers = users.stream()
            .filter(u -> u.getRole() == Role.ROLE_USER)
            .filter(u -> overwrite || u.getAssignedDeathCase() == null ||
                    u.getAssignedDeathCase().getStatus() != DeathCaseStatus.OPEN)
            .toList();

    if (eligibleUsers.isEmpty()) {
        return;
    }

    // Distribute users evenly across active pools
    for (int i = 0; i < eligibleUsers.size(); i++) {
        User user = eligibleUsers.get(i);
        DeathCase pool = activePools.get(i % activePools.size());
        user.setAssignedDeathCase(pool);
    }

    userRepo.saveAll(eligibleUsers);
}
}