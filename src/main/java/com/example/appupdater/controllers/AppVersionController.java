package com.example.appupdater.controllers;

import com.example.appupdater.dto.UpdateResponseDTO;
import com.example.appupdater.dto.UpdateStatsDTO;
import com.example.appupdater.models.AppVersion;
import com.example.appupdater.models.Platform;
import com.example.appupdater.repositories.AppVersionRepository;
import com.example.appupdater.services.AppUpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
@Tag(name = "Управление обновлениями", description = "API для работы с версиями и статистикой")
public class AppVersionController {

    private final AppVersionRepository repository;
    private final AppUpdateService updateService;

    @PostMapping
    public AppVersion createVersion(@RequestBody AppVersion version) {
        version.setReleaseDate(LocalDateTime.now());
        return repository.save(version);
    }

    @GetMapping
    public List<AppVersion> getAllVersions() {
        return repository.findAll();
    }

    @GetMapping("/check")
    public UpdateResponseDTO checkUpdate(
            @RequestParam String userId,
            @RequestParam String currentVersion,
            @RequestParam Platform platform) {
        updateService.updateUserDevice(userId, platform, currentVersion);
        AppVersion latest = updateService.getLatestVersion(platform);
        boolean isNeeded = updateService.isUpdateNeeded(currentVersion, latest != null ? latest.getVersion() : currentVersion);
        String message = isNeeded ? "Ваша версия устарела - необходимо обновление!" : "У вас актуальная версия";
        return new UpdateResponseDTO(isNeeded, isNeeded ? latest : null, message);
    }

    @PostMapping("/log")
    @Operation(summary = "Лог установки", description = "Записать информацию о том, что пользователь обновился")
    public void logUpdate(
            @RequestParam String userId,
            @RequestParam Platform platform,
            @RequestParam String newVersion) {
        updateService.logUpdateInstallation(userId, platform, newVersion);
    }

    @GetMapping("/stats")
    @Operation(summary = "Статистика", description = "Получить статистику установок конкретной версии")
    public UpdateStatsDTO getStats(@RequestParam String version) {
        return updateService.getStatsForVersion(version);
    }
}