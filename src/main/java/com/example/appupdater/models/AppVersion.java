package com.example.appupdater.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "app_versions")
@Data
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String version;

    @Enumerated(EnumType.STRING)
    private Platform platform;

    private LocalDateTime releaseDate;
    private String changelog;

    @Enumerated(EnumType.STRING)
    private UpdateType updateType;

    @JsonProperty("isActive")
    private boolean isActive;
}
