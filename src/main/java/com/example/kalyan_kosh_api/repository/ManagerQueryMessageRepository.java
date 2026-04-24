package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.ManagerQuery;
import com.example.kalyan_kosh_api.entity.ManagerQueryMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagerQueryMessageRepository extends JpaRepository<ManagerQueryMessage, Long> {

    List<ManagerQueryMessage> findByQueryOrderByCreatedAtAsc(ManagerQuery query);

    long countByQuery(ManagerQuery query);
}