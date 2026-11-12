package com.disha.tracker.dto;

public class CodeforcesProfileResponse {

    private String handle;
    private String message;

    public CodeforcesProfileResponse() {
    }

    public CodeforcesProfileResponse(String handle, String message) {
        this.handle = handle;
        this.message = message;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

