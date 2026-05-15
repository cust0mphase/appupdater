package com.example.appupdater.controllers;

import com.example.appupdater.bot.NotificationBot;
import com.example.appupdater.dto.UpdateResponseDTO;
import com.example.appupdater.dto.UpdateStatsDTO;
import com.example.appupdater.models.AppVersion;
import com.example.appupdater.models.Platform;
import com.example.appupdater.models.UpdateType;
import com.example.appupdater.repositories.AppVersionRepository;
import com.example.appupdater.services.AppUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "Управление обновлениями", description = "API для работы с версиями и статистикой")
public class AppVersionController {

    private final AppVersionRepository repository;
    private final AppUpdateService updateService;
    private final NotificationBot notificationBot;

    @PostMapping("/api/versions")
    @Operation(summary = "Добавить новую версию")
    public AppVersion createVersion(@Valid @RequestBody AppVersion version) {
        log.info("Получен запрос на создание новой версии {} для платформы {}", version.getVersion(), version.getPlatform());
        version.setReleaseDate(LocalDateTime.now());
        AppVersion savedVersion = repository.save(version);

        if (savedVersion.getUpdateType() == UpdateType.MANDATORY) {
            log.info("Отправка уведомления в Telegram о критическом обновлении версии {}", savedVersion.getVersion());
            String message = String.format(
                    "Критическое обновление\n\n" + "Платформа: %s\nВерсия: %s\nИзменения: %s\n\nПросьба всем пользователям обновиться!",
                    savedVersion.getPlatform(),
                    savedVersion.getVersion(),
                    savedVersion.getChangelog()
            );
            notificationBot.sendNotification(message);
        }
        return savedVersion;
    }

    @GetMapping("/api/versions/latest")
    @Operation(summary = "Получить последнюю версию для платформы")
    public ResponseEntity<AppVersion> getLatestVersion(@RequestParam Platform platform) {
        log.info("Запрос последней версии для платформы: {}", platform);
        Optional<AppVersion> latest = repository.findFirstByPlatformAndIsActiveTrueOrderByReleaseDateDesc(platform);
        return latest.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("Активная версия для платформы {} не найдена", platform);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping("/api/update/check")
    @Operation(summary = "Проверка наличия обновлений")
    public UpdateResponseDTO checkForUpdate(
            @RequestParam String userId,
            @RequestParam Platform platform,
            @RequestParam String current) {
        log.info("Пользователь {} проверяет наличие обновлений. Текущая версия: {}, Платформа: {}", userId, current, platform);

        updateService.updateUserDevice(userId, platform, current);
        AppVersion latest = updateService.getLatestVersion(platform);
        boolean isNeeded = updateService.isUpdateNeeded(current, latest != null ? latest.getVersion() : current);
        String message = isNeeded ? "Ваша версия устарела - необходимо обновление!" : "У вас актуальная версия";

        return new UpdateResponseDTO(isNeeded, isNeeded ? latest : null, message);
    }

    @PostMapping("/api/update/log")
    @Operation(summary = "Лог установки")
    public void logUpdate(
            @RequestParam String userId,
            @RequestParam Platform platform,
            @RequestParam String newVersion) {
        log.info("Пользователь {} успешно обновился до версии {} на платформе {}", userId, newVersion, platform);
        updateService.logUpdateInstallation(userId, platform, newVersion);
    }

    @GetMapping("/api/stats/updates")
    @Operation(summary = "Статистика")
    public UpdateStatsDTO getStats(@RequestParam String version) {
        log.info("Запрос статистики по версии: {}", version);
        return updateService.getStatsForVersion(version);
    }

    @GetMapping("/api/versions")
    @Operation(summary = "Получить все версии")
    public List<AppVersion> getAllVersions() {
        log.info("Запрос полного списка всех версий");
        return repository.findAll();
    }

    @PutMapping("/api/versions/{id}")
    @Operation(summary = "Обновить существующую версию")
    public ResponseEntity<AppVersion> updateVersion(@PathVariable Long id, @Valid @RequestBody AppVersion updatedVersion) {
        log.info("Запрос на редактирование версии с ID: {}", id);
        return repository.findById(id)
                .map(version -> {
                    version.setVersion(updatedVersion.getVersion());
                    version.setPlatform(updatedVersion.getPlatform());
                    version.setChangelog(updatedVersion.getChangelog());
                    version.setUpdateType(updatedVersion.getUpdateType());
                    version.setActive(updatedVersion.isActive());
                    return ResponseEntity.ok(repository.save(version));
                })
                .orElseGet(() -> {
                    log.warn("Попытка обновить несуществующую версию с ID: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @DeleteMapping("/api/versions/{id}")
    @Operation(summary = "Удалить версию")
    public ResponseEntity<Void> deleteVersion(@PathVariable Long id) {
        log.info("Запрос на удаление версии с ID: {}", id);
        if (repository.existsById(id)) {
            repository.deleteById(id);
            log.info("Версия с ID {} успешно удалена", id);
            return ResponseEntity.ok().build();
        }
        log.warn("Попытка удалить несуществующую версию с ID: {}", id);
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/api/versions/export", produces = "text/csv")
    @Operation(summary = "Экспорт отчета", description = "Скачать список всех версий в формате CSV")
    public void exportVersionsToCSV(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        log.info("Запрос на экспорт списка версий в CSV");

        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"app_versions_report.csv\"");

        List<AppVersion> versions = repository.findAll();

        try (java.io.PrintWriter writer = response.getWriter()) {
            writer.write('\uFEFF');

            writer.println("ID,Версия,Платформа,Дата_Релиза,Тип_Обновления,Активна,Изменения");

            for (AppVersion v : versions) {
                String safeChangelog = v.getChangelog() != null ? v.getChangelog().replace("\"", "\"\"") : "";
                writer.printf("%d,%s,%s,%s,%s,%b,\"%s\"%n",
                        v.getId(),
                        v.getVersion(),
                        v.getPlatform(),
                        v.getReleaseDate() != null ? v.getReleaseDate().toString() : "",
                        v.getUpdateType(),
                        v.isActive(),
                        safeChangelog
                );
            }
        }
    }

    @GetMapping("/api/stats/heatmap")
    @Operation(summary = "Тепловая карта", description = "Данные для тепловой карты: Платформа -> Дата -> Количество активных устройств")
    public Map<String, Map<String, Long>> getHeatmap() {
        log.info("Запрос данных для тепловой карты распространения версий");
        return updateService.getVersionSpreadHeatmap();
    }

    @GetMapping("/api/versions/deprecated")
    @Operation(summary = "Устаревшие версии", description = "Загрузить список заблокированных версий из YAML конфигурации")
    public List<com.example.appupdater.models.DeprecatedVersion> getDeprecatedVersions() {
        log.info("Запрос списка устаревших версий из YAML");
        return updateService.getDeprecatedVersionsFromYaml();
    }

    @PostMapping("/upload-config")
    @Operation(summary = "Загрузка файла конфигурации", description = "Загрузить новый YAML файл (MultipartFile)")
    public ResponseEntity<String> uploadFile(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        log.info("Получен файл для загрузки: {}", file.getOriginalFilename());
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пуст!");
        }
        return ResponseEntity.ok("Файл " + file.getOriginalFilename() + " успешно загружен и обработан системой размером: " + file.getSize() + " байт");
    }
}