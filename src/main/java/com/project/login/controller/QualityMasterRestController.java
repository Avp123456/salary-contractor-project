package com.project.login.controller;

import com.project.login.entity.QualityMasterEntity;
import com.project.login.service.QualityMasterService;
import com.project.security.CustomUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quality")
public class QualityMasterRestController {

    private final QualityMasterService service;

    public QualityMasterRestController(QualityMasterService service) {
        this.service = service;
    }

    // ✅ LIST (GET ALL FOR LOGGED USER)
    @GetMapping
    public ResponseEntity<List<QualityMasterEntity>> list(Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        List<QualityMasterEntity> list = service.findByUser(userId);

        return ResponseEntity.ok(list);
    }

    // ✅ CREATE / SAVE
    @PostMapping
    public ResponseEntity<QualityMasterEntity> save(
            @RequestBody QualityMasterEntity quality,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        quality.setUserId(userDetails.getId());

        QualityMasterEntity saved = service.save(quality);

        return ResponseEntity.ok(saved);
    }

    // ✅ GET BY ID (FOR EDIT)
    @GetMapping("/{id}")
    public ResponseEntity<QualityMasterEntity> getById(
            @PathVariable Long id,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        QualityMasterEntity quality = service
                .findByIdAndUser(id, userId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        return ResponseEntity.ok(quality);
    }

    // ✅ UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<QualityMasterEntity> update(
            @PathVariable Long id,
            @RequestBody QualityMasterEntity quality,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        QualityMasterEntity existing = service
                .findByIdAndUser(id, userId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        quality.setId(id);
        quality.setUserId(userId);

        QualityMasterEntity updated = service.save(quality);

        return ResponseEntity.ok(updated);
    }

    // ✅ DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(
            @PathVariable Long id,
            Authentication authentication) {

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        Long userId = userDetails.getId();

        service.delete(id, userId);

        return ResponseEntity.ok("Deleted Successfully");
    }
}
