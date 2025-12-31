package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sambhag")
@Getter
@Setter
@NoArgsConstructor
public class Sambhag {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;  // "Chambal", "Indore", etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id", nullable = false)
    private State state;

    @OneToMany(mappedBy = "sambhag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<District> districts;
}

