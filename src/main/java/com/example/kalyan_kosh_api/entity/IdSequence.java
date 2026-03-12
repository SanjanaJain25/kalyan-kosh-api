package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "id_sequence")
@Getter
@Setter
@NoArgsConstructor
public class IdSequence {

    @Id
    @Column(name = "sequence_name", length = 50)
    private String sequenceName;

    @Column(name = "current_value", nullable = false)
    private Long currentValue;

    public IdSequence(String sequenceName, Long currentValue) {
        this.sequenceName = sequenceName;
        this.currentValue = currentValue;
    }
}

