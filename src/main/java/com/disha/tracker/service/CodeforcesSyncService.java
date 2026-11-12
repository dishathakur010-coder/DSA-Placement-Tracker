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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CodeforcesSyncService {

    private static final Logger log = LoggerFactory.getLogger(CodeforcesSyncService.class);
    private static final int BATCH_SIZE = 100;

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

    // 3. Sync solved problems from Codeforces for the logged-in user (full history)
    public SyncSummaryResponse syncCodeforces() {
        User user = getCurrentUser();
        String handle = user.getCodeforcesHandle();

        if (handle == null || handle.trim().isBlank()) {
            throw new IllegalArgumentException("Please connect and save your Codeforces handle before syncing.");
        }

        handle = handle.trim();

        // Step A: Call Codeforces API with 1 retry on timeout and backend logging
        CodeforcesResponse response = null;
        int maxAttempts = 2; // Initial attempt + 1 retry on timeout

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                final String targetHandle = handle;
                response = codeforcesRestClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/user.status")
                                .queryParam("handle", targetHandle)
                                .build())
                        .retrieve()
                        .body(CodeforcesResponse.class);
                break; // Request succeeded
            } catch (HttpClientErrorException ex) {
                log.error("Codeforces API error for handle {}: status={}, body={}",
                        handle, ex.getStatusCode(), ex.getResponseBodyAsString(), ex);
                String responseBody = ex.getResponseBodyAsString();
                if (responseBody != null && responseBody.toLowerCase().contains("not found")) {
                    throw new IllegalArgumentException("Codeforces user not found: " + handle);
                }
                throw new IllegalArgumentException("Codeforces error for handle '" + handle + "': " + ex.getStatusText());
            } catch (ResourceAccessException ex) {
                log.warn("Codeforces API timeout on attempt {}/{} for handle {}: {}",
                        attempt, maxAttempts, handle, ex.getMessage(), ex);
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(1000); // 1-second pause before retry
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue; // Retry
                }
                log.error("Codeforces API timed out after {} attempts for handle {}", maxAttempts, handle, ex);
                throw new IllegalStateException("Codeforces API timed out or is unreachable. Please check your connection and try again.");
            } catch (Exception ex) {
                log.error("Unexpected error fetching Codeforces submissions for handle {}: {}", handle, ex.getMessage(), ex);
                throw new IllegalStateException("Failed to sync with Codeforces: " + ex.getMessage());
            }
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

        // Step B: Filter for verdict "OK" and find the earliest (first accepted) submission per problem
        // across the FULL submission history.
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

        // Step C: Fetch existing external IDs already imported for this user to deduplicate
        Set<String> existingExternalIds = problemRepository.findExternalIdsByUserIdAndPlatform(user.getId(), "CODEFORCES");

        int importedCount = 0;
        int skippedCount = 0;
        List<Problem> problemsToSave = new ArrayList<>();

        for (CodeforcesSubmission sub : earliestAcceptedByProblem.values()) {
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

            // First accepted solve date
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
            // Also add to existingExternalIds to prevent duplicates within the same run
            existingExternalIds.add(externalId);
        }

        // Step D: Save in batches of 100 to handle large submission histories reliably
        if (!problemsToSave.isEmpty()) {
            for (int i = 0; i < problemsToSave.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, problemsToSave.size());
                problemRepository.saveAll(problemsToSave.subList(i, end));
            }
            importedCount = problemsToSave.size();
        }

        String summaryMessage = "Imported " + importedCount + " new problems"
                + (skippedCount > 0 ? " (" + skippedCount + " skipped)" : "");

        return new SyncSummaryResponse(importedCount, skippedCount, summaryMessage);
    }
}
