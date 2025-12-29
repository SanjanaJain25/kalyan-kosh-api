package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.IdSequence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.util.Optional;

public interface IdSequenceRepository extends JpaRepository<IdSequence, String> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM IdSequence s WHERE s.sequenceName = :sequenceName")
    Optional<IdSequence> findBySequenceNameWithLock(String sequenceName);
}

