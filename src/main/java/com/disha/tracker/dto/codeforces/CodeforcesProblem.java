package com.disha.tracker.dto.codeforces;

import java.util.List;

public record CodeforcesProblem(
        Long contestId,
        String index,
        String name,
        String type,
        Integer rating,
        List<String> tags
) {
}

