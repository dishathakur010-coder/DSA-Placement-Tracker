package com.disha.tracker.controller;

import com.disha.tracker.dto.CodeforcesProfileRequest;
import com.disha.tracker.dto.CodeforcesProfileResponse;
import com.disha.tracker.service.CodeforcesSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Profile", description = "User profile and platform handle management")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final CodeforcesSyncService codeforcesSyncService;

    public ProfileController(CodeforcesSyncService codeforcesSyncService) {
        this.codeforcesSyncService = codeforcesSyncService;
    }

    // GET /api/profile/codeforces - Get current user's Codeforces handle
    @Operation(summary = "Get saved Codeforces handle")
    @GetMapping("/codeforces")
    public ResponseEntity<CodeforcesProfileResponse> getCodeforcesHandle() {
        CodeforcesProfileResponse response = codeforcesSyncService.getHandle();
        return ResponseEntity.ok(response);
    }

    // PUT /api/profile/codeforces - Save or update Codeforces handle
    @Operation(summary = "Save or update Codeforces handle")
    @PutMapping("/codeforces")
    public ResponseEntity<CodeforcesProfileResponse> saveCodeforcesHandle(
            @Valid @RequestBody CodeforcesProfileRequest request) {
        CodeforcesProfileResponse response = codeforcesSyncService.saveHandle(request.getHandle());
        return ResponseEntity.ok(response);
    }
}

