package com.disha.tracker.controller;

import com.disha.tracker.dto.SyncSummaryResponse;
import com.disha.tracker.service.CodeforcesSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sync", description = "Platform synchronization")
@RestController
@RequestMapping("/api/sync")
public class SyncController {

    private final CodeforcesSyncService codeforcesSyncService;

    public SyncController(CodeforcesSyncService codeforcesSyncService) {
        this.codeforcesSyncService = codeforcesSyncService;
    }

    // POST /api/sync/codeforces - Synchronize solved problems from Codeforces
    @Operation(summary = "Synchronize solved problems from Codeforces")
    @PostMapping("/codeforces")
    public ResponseEntity<SyncSummaryResponse> syncCodeforces() {
        SyncSummaryResponse response = codeforcesSyncService.syncCodeforces();
        return ResponseEntity.ok(response);
    }
}

