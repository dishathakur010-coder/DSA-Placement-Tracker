package com.disha.tracker.dto;

public class SyncSummaryResponse {

    private int importedCount;
    private int skippedCount;
    private String message;

    public SyncSummaryResponse() {
    }

    public SyncSummaryResponse(int importedCount, int skippedCount, String message) {
        this.importedCount = importedCount;
        this.skippedCount = skippedCount;
        this.message = message;
    }

    public int getImportedCount() {
        return importedCount;
    }

    public void setImportedCount(int importedCount) {
        this.importedCount = importedCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public void setSkippedCount(int skippedCount) {
        this.skippedCount = skippedCount;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

