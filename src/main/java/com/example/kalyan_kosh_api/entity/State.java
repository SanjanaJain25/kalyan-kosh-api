package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "state")
@Getter
@Setter
@NoArgsConstructor
public class State {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;  // "Madhya Pradesh"

    @Column(nullable = false, unique = true, length = 10)
    private String code;  // "MP"

    @OneToMany(mappedBy = "state", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Sambhag> sambhags;
}

