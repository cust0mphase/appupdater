package com.example.appupdater.controllers;

import com.example.appupdater.models.AppVersion;
import com.example.appupdater.repositories.AppVersionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionRepository repository;

    @PostMapping
    public AppVersion createVersion(@RequestBody AppVersion version) {
        version.setReleaseDate(LocalDateTime.now());
        return repository.save(version);
    }

    @GetMapping
    public List<AppVersion> getAllVersions() {
        return repository.findAll();
    }
}
