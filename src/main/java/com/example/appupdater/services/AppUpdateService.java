package com.example.appupdater.services;

import com.example.appupdater.dto.UpdateStatsDTO;
import com.example.appupdater.models.AppVersion;
import com.example.appupdater.models.Platform;
import com.example.appupdater.models.UserDevice;
import com.example.appupdater.repositories.AppVersionRepository;
import com.example.appupdater.repositories.UserDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class AppUpdateService {

    private final AppVersionRepository versionRepository;
    private final UserDeviceRepository deviceRepository;

    public AppVersion getLatestVersion(Platform platform) {
        List<AppVersion> versions = versionRepository.findAll();
        return versions.stream()
                .filter(v -> v.getPlatform() == platform && v.isActive())
                .max(Comparator.comparing(AppVersion::getVersion))
                .orElse(null);
    }

    public void updateUserDevice(String userId, Platform platform, String currentVersion) {
        UserDevice device = deviceRepository.findByUserIdAndPlatform(userId, platform)
                .orElse(new UserDevice());

        device.setUserId(userId);
        device.setPlatform(platform);
        device.setCurrentVersion(currentVersion);
        device.setLastSeen(LocalDateTime.now());

        deviceRepository.save(device);
    }

    public boolean isUpdateNeeded(String current, String latest) {
        if (latest == null) return false;
        return current.compareTo(latest) < 0;
    }

    public void logUpdateInstallation(String userId, Platform platform, String newVersion) {
        updateUserDevice(userId, platform, newVersion);
    }

    public UpdateStatsDTO getStatsForVersion(String version) {
        List<UserDevice> allDevices = deviceRepository.findAll();

        if (allDevices.isEmpty()) {
            return new UpdateStatsDTO(version, Map.of(), 0.0);
        }

        Map<Platform, Integer> platformCounts = new HashMap<>();
        int updatedUsers = 0;

        for (UserDevice device : allDevices) {
            if (version.equals(device.getCurrentVersion())) {
                platformCounts.put(device.getPlatform(), platformCounts.getOrDefault(device.getPlatform(), 0) + 1);
                updatedUsers++;
            }
        }

        double rate = (double) updatedUsers / allDevices.size() * 100;

        return new UpdateStatsDTO(version, platformCounts, rate);
    }
}