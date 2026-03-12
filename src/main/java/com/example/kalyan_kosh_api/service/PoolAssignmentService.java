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

    List<User> users = userRepo.findAll();
    List<DeathCase> activePools =
            deathCaseRepo.findByStatusOrderByIdAsc(DeathCaseStatus.OPEN);

    // If 0/1 pool, nothing can "change"
    if (activePools.size() <= 1) return;

    // Build quick lookup: poolId -> index
    Map<Long, Integer> poolIndex = new HashMap<>();
    for (int idx = 0; idx < activePools.size(); idx++) {
        DeathCase dc = activePools.get(idx);
        if (dc.getId() != null) poolIndex.put(dc.getId(), idx);
    }

    // ✅ Always change each user's pool to the NEXT pool
    for (User u : users) {

        DeathCase current = u.getAssignedDeathCase();

        // overwrite=false => keep already assigned users as-is
        if (!overwrite && current != null) continue;

        int nextIdx;

        if (current != null && current.getId() != null && poolIndex.containsKey(current.getId())) {
            // move to next pool (guaranteed change)
            nextIdx = (poolIndex.get(current.getId()) + 1) % activePools.size();
        } else {
            // if user had no assignment or pool not found -> assign first pool
            nextIdx = 0;
        }

        u.setAssignedDeathCase(activePools.get(nextIdx));
    }

    userRepo.saveAll(users);
}
}