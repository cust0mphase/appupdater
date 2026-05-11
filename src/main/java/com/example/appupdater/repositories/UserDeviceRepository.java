package com.example.appupdater.repositories;

import com.example.appupdater.models.Platform;
import com.example.appupdater.models.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserDeviceRepository extends JpaRepository<UserDevice, Long> {
    Optional<UserDevice> findByUserIdAndPlatform(String userId, Platform platform);
}
