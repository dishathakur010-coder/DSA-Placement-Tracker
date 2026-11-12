package com.disha.tracker.dto.codeforces;

public record CodeforcesSubmission(
        Long id,
        Long contestId,
        Long creationTimeSeconds,
        String verdict,
        CodeforcesProblem problem
) {
}

