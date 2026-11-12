package com.disha.tracker.service;

import com.disha.tracker.dto.CodeforcesProfileResponse;
import com.disha.tracker.dto.SyncSummaryResponse;
import com.disha.tracker.dto.codeforces.CodeforcesProblem;
import com.disha.tracker.dto.codeforces.CodeforcesResponse;
import com.disha.tracker.dto.codeforces.CodeforcesSubmission;
import com.disha.tracker.entity.Problem;
import com.disha.tracker.entity.User;
import com.disha.tracker.exception.ResourceNotFoundException;
import com.disha.tracker.repository.ProblemRepository;
import com.disha.tracker.repository.UserRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CodeforcesSyncService {

    private final RestClient codeforcesRestClient;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public CodeforcesSyncService(RestClient codeforcesRestClient,
                                 ProblemRepository problemRepository,
                                 UserRepository userRepository) {
        this.codeforcesRestClient = codeforcesRestClient;
        this.problemRepository = problemRepository;
        this.userRepository = userRepository;
    }

    // Helper: extracts the currently authenticated user from SecurityContextHolder
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BadCredentialsException("User is not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    // 1. Get the saved Codeforces handle for the logged-in user
    public CodeforcesProfileResponse getHandle() {
        User user = getCurrentUser();
        return new CodeforcesProfileResponse(user.getCodeforcesHandle(), null);
    }

    // 2. Save or update the Codeforces handle for the logged-in user
    public CodeforcesProfileResponse saveHandle(String rawHandle) {
        User user = getCurrentUser();
        String cleanHandle = (rawHandle != null) ? rawHandle.trim() : "";

        if (cleanHandle.isBlank()) {
            throw new IllegalArgumentException("Codeforces handle cannot be blank");
        }

        user.setCodeforcesHandle(cleanHandle);
        userRepository.save(user);

        return new CodeforcesProfileResponse(cleanHandle, "Codeforces handle saved successfully");
    }

    // 3. Sync solved problems from Codeforces for the logged-in user
    public SyncSummaryResponse syncCodeforces() {
        User user = getCurrentUser();
        String handle = user.getCodeforcesHandle();

        if (handle == null || handle.trim().isBlank()) {
            throw new IllegalArgumentException("Please connect and save your Codeforces handle before syncing.");
        }

        handle = handle.trim();

        // Step A: Call Codeforces API
        CodeforcesResponse response;
        try {
            final String targetHandle = handle;
            response = codeforcesRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/user.status")
                            .queryParam("handle", targetHandle)
                            .build())
                    .retrieve()
                    .body(CodeforcesResponse.class);
        } catch (HttpClientErrorException ex) {
            String responseBody = ex.getResponseBodyAsString();
            if (responseBody != null && responseBody.toLowerCase().contains("not found")) {
                throw new IllegalArgumentException("Codeforces user not found: " + handle);
            }
            throw new IllegalArgumentException("Codeforces error for handle '" + handle + "': " + ex.getStatusText());
        } catch (ResourceAccessException ex) {
            throw new IllegalStateException("Codeforces API timed out or is unreachable. Please check your connection and try again.");
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sync with Codeforces: " + ex.getMessage());
        }

        if (response == null || !"OK".equalsIgnoreCase(response.status())) {
            String comment = (response != null && response.comment() != null)
                    ? response.comment()
                    : "Unknown error occurred while contacting Codeforces.";
            throw new IllegalArgumentException("Codeforces API failed: " + comment);
        }

        List<CodeforcesSubmission> submissions = response.result();
        if (submissions == null || submissions.isEmpty()) {
            return new SyncSummaryResponse(0, 0, "No submissions found for handle: " + handle);
        }

        // Step B: Calculate 60 days cutoff timestamp
        long cutoffSeconds = Instant.now().minus(60, ChronoUnit.DAYS).getEpochSecond();

        // Step C: Filter for verdict "OK" and find the earliest (first) accepted submission per problem
        Map<String, CodeforcesSubmission> earliestAcceptedByProblem = new LinkedHashMap<>();

        for (CodeforcesSubmission sub : submissions) {
            if (sub.verdict() == null || !"OK".equalsIgnoreCase(sub.verdict())) {
                continue;
            }

            CodeforcesProblem problem = sub.problem();
            if (problem == null || problem.index() == null || sub.creationTimeSeconds() == null) {
                continue;
            }

            String externalId = (problem.contestId() != null ? problem.contestId() : "") + problem.index().trim();

            CodeforcesSubmission existing = earliestAcceptedByProblem.get(externalId);
            if (existing == null || sub.creationTimeSeconds() < existing.creationTimeSeconds()) {
                earliestAcceptedByProblem.put(externalId, sub);
            }
        }

        // Step D: Filter for problems whose first accepted submission was within the last 60 days
        List<CodeforcesSubmission> recentFirstSolves = new ArrayList<>();
        for (CodeforcesSubmission sub : earliestAcceptedByProblem.values()) {
            if (sub.creationTimeSeconds() >= cutoffSeconds) {
                recentFirstSolves.add(sub);
            }
        }

        // Step E: Fetch all existing external IDs already imported for this user to deduplicate
        Set<String> existingExternalIds = problemRepository.findExternalIdsByUserIdAndPlatform(user.getId(), "CODEFORCES");

        int importedCount = 0;
        int skippedCount = 0;
        List<Problem> problemsToSave = new ArrayList<>();

        for (CodeforcesSubmission sub : recentFirstSolves) {
            CodeforcesProblem cfProblem = sub.problem();
            String externalId = (cfProblem.contestId() != null ? cfProblem.contestId() : "") + cfProblem.index().trim();

            if (existingExternalIds.contains(externalId)) {
                skippedCount++;
                continue;
            }

            // Map fields
            String title = (cfProblem.name() != null && !cfProblem.name().isBlank())
                    ? cfProblem.name().trim()
                    : ("Problem " + externalId);

            String topic = CodeforcesTopicMapper.mapTopic(cfProblem.tags());
            String difficulty = CodeforcesTopicMapper.mapDifficulty(cfProblem.rating());

            String link = (cfProblem.contestId() != null)
                    ? "https://codeforces.com/problemset/problem/" + cfProblem.contestId() + "/" + cfProblem.index().trim()
                    : "https://codeforces.com/problemset";

            LocalDate solvedDate = Instant.ofEpochSecond(sub.creationTimeSeconds())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            Problem problem = new Problem();
            problem.setTitle(title);
            problem.setTopic(topic);
            problem.setDifficulty(difficulty);
            problem.setLink(link);
            problem.setStatus("Solved");
            problem.setNotes("Imported from Codeforces (" + externalId + ")");
            problem.setSolvedDate(solvedDate);
            problem.setLastRevisedDate(null);
            problem.setRevisionCount(0);
            problem.setPlatform("CODEFORCES");
            problem.setExternalId(externalId);
            problem.setUser(user);

            problemsToSave.add(problem);
            // Also add to existingExternalIds to prevent duplicate within the same sync batch
            existingExternalIds.add(externalId);
        }

        if (!problemsToSave.isEmpty()) {
            problemRepository.saveAll(problemsToSave);
            importedCount = problemsToSave.size();
        }

        String summaryMessage = "Imported " + importedCount + " new problems"
                + (skippedCount > 0 ? " (" + skippedCount + " skipped)" : "");

        return new SyncSummaryResponse(importedCount, skippedCount, summaryMessage);
    }
}

