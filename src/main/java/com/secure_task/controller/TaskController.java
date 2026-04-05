package com.secure_task.controller;

import com.secure_task.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    // ──────────────────────────────────────────────────
    // GET /api/tasks
    // Requires: Authorization: Bearer <token>
    // Returns: tasks for the logged-in user
    // ──────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getTasks(
            @AuthenticationPrincipal User currentUser) {

        // currentUser = loaded from JWT automatically by JwtAuthFilter
        // In real app: query DB with taskRepository.findByUserId(currentUser.getId())
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "title", "Learn Spring Security",
                        "completed", false, "userId", currentUser.getId()),
                Map.of("id", 2, "title", "Build OAuth2 login",
                        "completed", true, "userId", currentUser.getId())
        ));
    }

    // ──────────────────────────────────────────────────
    // DELETE /api/admin/tasks/{id}
    // Requires: ROLE_ADMIN
    // @PreAuthorize checks role BEFORE the method runs
    // ──────────────────────────────────────────────────
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")   // Only admins can delete any task
    public ResponseEntity<String> deleteTask(@PathVariable Long id) {

        System.out.println("Task deleted by admin==  "+ id);
        return ResponseEntity.ok("Task " + id + " deleted by admin");
    }
}

