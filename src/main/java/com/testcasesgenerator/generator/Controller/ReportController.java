package com.testcasesgenerator.generator.Controller;

import com.testcasesgenerator.generator.Model.TestExecutionResult;
import com.testcasesgenerator.generator.Repository.TestExecutionResultRepository;
import com.testcasesgenerator.generator.Services.TestReportGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    @Autowired
    private TestExecutionResultRepository testExecutionResultRepository;

    @Autowired
    private TestReportGeneratorService testReportGeneratorService;

    @GetMapping("/executions")
    public ResponseEntity<?> getAllExecutions(
            @RequestParam(required = false) String ticketId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            List<TestExecutionResult> executions;
            if (ticketId != null && status != null) {
                executions = testExecutionResultRepository.findByTicketIdAndExecutionStatusOrderByStartTimeDesc(ticketId, status);
            } else if (ticketId != null) {
                executions = testExecutionResultRepository.findByTicketIdOrderByStartTimeDesc(ticketId);
            } else if (status != null) {
                executions = testExecutionResultRepository.findByExecutionStatusOrderByStartTimeDesc(status);
            } else {
                executions = testExecutionResultRepository.findAllByOrderByStartTimeDesc();
            }

            // Apply pagination
            int start = page * size;
            int end = Math.min(start + size, executions.size());
            List<TestExecutionResult> paginatedExecutions = executions.subList(start, end);

            Map<String, Object> response = new HashMap<>();
            response.put("executions", paginatedExecutions);
            response.put("currentPage", page);
            response.put("totalItems", executions.size());
            response.put("totalPages", (int) Math.ceil((double) executions.size() / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/executions/{id}")
    public ResponseEntity<?> getExecutionById(@PathVariable Long id) {
        try {
            Optional<TestExecutionResult> execution = testExecutionResultRepository.findById(id);
            if (execution.isPresent()) {
                return ResponseEntity.ok(execution.get());
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Execution not found with id: " + id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/executions/ticket/{ticketId}/latest")
    public ResponseEntity<?> getLatestExecutionByTicketId(@PathVariable String ticketId) {
        try {
            List<TestExecutionResult> executions = testExecutionResultRepository.findByTicketIdOrderByStartTimeDesc(ticketId);
            if (!executions.isEmpty()) {
                return ResponseEntity.ok(executions.get(0));
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "No executions found for ticket: " + ticketId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/executions/summary")
    public ResponseEntity<?> getExecutionsSummary(@RequestParam(required = false) String ticketId) {
        try {
            List<TestExecutionResult> executions;
            if (ticketId != null) {
                executions = testExecutionResultRepository.findByTicketIdOrderByStartTimeDesc(ticketId);
            } else {
                executions = testExecutionResultRepository.findAllByOrderByStartTimeDesc();
            }

            Map<String, Object> summary = new HashMap<>();
            summary.put("total", executions.size());
            summary.put("passed", executions.stream().filter(e -> "PASSED".equals(e.getExecutionStatus())).count());
            summary.put("failed", executions.stream().filter(e -> "FAILED".equals(e.getExecutionStatus())).count());
            summary.put("error", executions.stream().filter(e -> "ERROR".equals(e.getExecutionStatus())).count());

            // Calculate average duration
            double avgDuration = executions.stream()
                .filter(e -> e.getExecutionDurationMs() != null)
                .mapToLong(TestExecutionResult::getExecutionDurationMs)
                .average()
                .orElse(0.0);
            summary.put("averageDurationMs", avgDuration);

            // Get latest execution
            if (!executions.isEmpty()) {
                summary.put("latestExecution", executions.get(0));
            }

            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/executions/{id}")
    public ResponseEntity<?> deleteExecution(@PathVariable Long id) {
        try {
            if (testExecutionResultRepository.existsById(id)) {
                testExecutionResultRepository.deleteById(id);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/html/{ticketId}")
    public ResponseEntity<?> generateHtmlReport(@PathVariable String ticketId) {
        try {
            String reportPath = testReportGeneratorService.generateTestReport(ticketId);
            File reportFile = new File(reportPath);
            
            if (reportFile.exists()) {
                return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(new FileSystemResource(reportFile));
            } else {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Report file not found");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
} 