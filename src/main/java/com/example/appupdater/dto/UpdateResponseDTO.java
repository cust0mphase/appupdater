package com.example.appupdater.dto;

import com.example.appupdater.models.AppVersion;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateResponseDTO {
    private boolean isUpdateAvailable;
    private AppVersion latestVersion;
    private String message;
}
