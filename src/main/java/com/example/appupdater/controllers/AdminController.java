package com.example.appupdater.controllers;

import com.example.appupdater.exceptions.ResourceNotFoundException;
import com.example.appupdater.models.Permission;
import com.example.appupdater.models.Role;
import com.example.appupdater.models.User;
import com.example.appupdater.repositories.PermissionRepository;
import com.example.appupdater.repositories.RoleRepository;
import com.example.appupdater.repositories.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Админ-панель", description = "CRUD для управления пользователями, ролями и правами")
public class AdminController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @GetMapping("/users")
    @Operation(summary = "Получить всех пользователей")
    public List<User> getAllUsers() {
        log.info("Запрос списка всех пользователей");
        return userRepository.findAll();
    }

    @PutMapping("/users/{userId}/roles/{roleId}")
    @Operation(summary = "Назначить роль пользователю")
    public ResponseEntity<User> assignRoleToUser(@PathVariable Long userId, @PathVariable Long roleId) {
        log.info("Назначение роли ID {} пользователю ID {}", roleId, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Пользователь с ID " + userId + " не найден"));

        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Роль с ID " + roleId + " не найдена"));

        user.getRoles().add(role);
        return ResponseEntity.ok(userRepository.save(user));
    }

    @DeleteMapping("/users/{id}")
    @Operation(summary = "Удалить пользователя")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Запрос на удаление пользователя с ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Пользователь с ID " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь успешно удален");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/roles")
    @Operation(summary = "Получить все роли")
    public List<Role> getAllRoles() {
        log.info("Запрос списка всех ролей");
        return roleRepository.findAll();
    }

    @PostMapping("/roles")
    @Operation(summary = "Создать новую роль")
    public Role createRole(@RequestBody Role role) {
        log.info("Создание новой роли: {}", role.getTitle());
        return roleRepository.save(role);
    }

    @GetMapping("/permissions")
    @Operation(summary = "Получить все права (permissions)")
    public List<Permission> getAllPermissions() {
        log.info("Запрос списка всех прав");
        return permissionRepository.findAll();
    }
}