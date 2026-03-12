package com.example.kalyan_kosh_api.service;

import com.example.kalyan_kosh_api.entity.IdSequence;
import com.example.kalyan_kosh_api.repository.IdSequenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdGeneratorService {

    private static final String USER_ID_SEQUENCE = "USER_ID";
    private static final String ID_PREFIX = "PMUMS20";
    private static final long INITIAL_VALUE = 3001L;

    private final IdSequenceRepository idSequenceRepository;

    public IdGeneratorService(IdSequenceRepository idSequenceRepository) {
        this.idSequenceRepository = idSequenceRepository;
    }

    /**
     * Generates the next user ID in format PMUMS2024XXXXX
     * Thread-safe using pessimistic locking
     */
    @Transactional
    public String generateNextUserId() {
        // Get or create sequence with lock
        IdSequence sequence = idSequenceRepository.findBySequenceNameWithLock(USER_ID_SEQUENCE)
                .orElseGet(() -> {
                    IdSequence newSeq = new IdSequence(USER_ID_SEQUENCE, INITIAL_VALUE - 1);
                    return idSequenceRepository.save(newSeq);
                });

        // Increment the counter
        long nextValue = sequence.getCurrentValue() + 1;
        sequence.setCurrentValue(nextValue);
        idSequenceRepository.save(sequence);

        // Generate ID: PMUMS2024 + number (e.g., PMUMS202458108)
        return ID_PREFIX + nextValue;
    }
}

