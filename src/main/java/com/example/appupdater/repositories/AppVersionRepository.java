package com.example.appupdater.repositories;

import com.example.appupdater.models.AppVersion;
import com.example.appupdater.models.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AppVersionRepository extends JpaRepository<AppVersion, Long> {
    Optional<AppVersion> findFirstByPlatformAndIsActiveTrueOrderByReleaseDateDesc(Platform platform);
}
