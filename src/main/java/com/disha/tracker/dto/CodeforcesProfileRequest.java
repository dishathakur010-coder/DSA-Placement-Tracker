package com.disha.tracker.dto;

import jakarta.validation.constraints.NotBlank;

public class CodeforcesProfileRequest {

    @NotBlank(message = "Codeforces handle cannot be blank")
    private String handle;

    public CodeforcesProfileRequest() {
    }

    public CodeforcesProfileRequest(String handle) {
        this.handle = handle;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }
}

