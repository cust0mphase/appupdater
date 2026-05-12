package com.example.appupdater.dto;

import com.example.appupdater.models.Platform;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class UpdateStatsDTO {
    private String version;
    private Map<Platform, Integer> usersCount;
    private Double globalUpdateRate;
}
