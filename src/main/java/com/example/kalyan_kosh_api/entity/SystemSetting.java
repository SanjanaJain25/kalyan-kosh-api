package com.example.kalyan_kosh_api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "system_settings")
@Data
public class SystemSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "setting_key", unique = true)
    private String settingKey;

@Lob
@Column(name = "setting_value", columnDefinition = "LONGTEXT")
private String settingValue;

    private Instant updatedAt;
}