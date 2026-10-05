package com.example.kalyan_kosh_api.repository;

import com.example.kalyan_kosh_api.entity.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    List<BlogPost> findAllByOrderByCreatedAtDesc();

    List<BlogPost> findByActiveTrueOrderByCreatedAtDesc();

    Optional<BlogPost> findByIdAndActiveTrue(Long id);
}
