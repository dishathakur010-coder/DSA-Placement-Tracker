package com.disha.tracker.dto.codeforces;

import java.util.List;

public record CodeforcesResponse(
        String status,
        String comment,
        List<CodeforcesSubmission> result
) {
}

