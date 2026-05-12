package com.example.appupdater.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "app_versions")
@Data
public class AppVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Номер версии не может быть пустым")
    private String version;

    @NotNull(message = "Платформа обязательна")
    @Enumerated(EnumType.STRING)
    private Platform platform;

    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime releaseDate;

    @NotBlank(message = "Описание изменений (changelog) обязательно")
    private String changelog;

    @NotNull(message = "Тип обновления обязателен")
    @Enumerated(EnumType.STRING)
    private UpdateType updateType;

    @JsonProperty("isActive")
    private boolean isActive;

    @JsonIgnore
    public boolean isActive() {
        return isActive;
    }
}
