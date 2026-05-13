package com.example.appupdater.models;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DeprecatedVersion {
    private String version;
    private Platform platform;
    private LocalDate blockDate;
}
