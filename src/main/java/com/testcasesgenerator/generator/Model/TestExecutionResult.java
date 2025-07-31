package com.testcasesgenerator.generator.Model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "test_execution_results")
public class TestExecutionResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "test_case_id", nullable = false)
    private String testCaseId;

    @Column(name = "automation_script_id", nullable = false)
    private Long automationScriptId;

    @Column(name = "execution_status", nullable = false)
    private String executionStatus; // PASSED, FAILED, SKIPPED, ERROR

    @Column(name = "execution_start_time", nullable = false)
    private LocalDateTime executionStartTime;

    @Column(name = "execution_end_time")
    private LocalDateTime executionEndTime;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "stack_trace", columnDefinition = "TEXT")
    private String stackTrace;

    @ElementCollection
    @Column(name = "screenshot_paths", columnDefinition = "TEXT")
    private List<String> screenshotPaths;

    @ElementCollection
    @Column(name = "step_results", columnDefinition = "TEXT")
    private List<String> stepResults; // JSON format for each step result

    @Column(name = "browser_type")
    private String browserType;

    @Column(name = "browser_version")
    private String browserVersion;

    @Column(name = "test_environment")
    private String testEnvironment;

    @Column(name = "report_path")
    private String reportPath;

    // Default constructor
    public TestExecutionResult() {
    }

    // Constructor with basic fields
    public TestExecutionResult(String ticketId, String testCaseId, Long automationScriptId, String executionStatus) {
        this.ticketId = ticketId;
        this.testCaseId = testCaseId;
        this.automationScriptId = automationScriptId;
        this.executionStatus = executionStatus;
        this.executionStartTime = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(String testCaseId) {
        this.testCaseId = testCaseId;
    }

    public Long getAutomationScriptId() {
        return automationScriptId;
    }

    public void setAutomationScriptId(Long automationScriptId) {
        this.automationScriptId = automationScriptId;
    }

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
    }

    public LocalDateTime getExecutionStartTime() {
        return executionStartTime;
    }

    public void setExecutionStartTime(LocalDateTime executionStartTime) {
        this.executionStartTime = executionStartTime;
    }

    public LocalDateTime getExecutionEndTime() {
        return executionEndTime;
    }

    public void setExecutionEndTime(LocalDateTime executionEndTime) {
        this.executionEndTime = executionEndTime;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

    public List<String> getScreenshotPaths() {
        return screenshotPaths;
    }

    public void setScreenshotPaths(List<String> screenshotPaths) {
        this.screenshotPaths = screenshotPaths;
    }

    public List<String> getStepResults() {
        return stepResults;
    }

    public void setStepResults(List<String> stepResults) {
        this.stepResults = stepResults;
    }

    public String getBrowserType() {
        return browserType;
    }

    public void setBrowserType(String browserType) {
        this.browserType = browserType;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public void setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
    }

    public String getTestEnvironment() {
        return testEnvironment;
    }

    public void setTestEnvironment(String testEnvironment) {
        this.testEnvironment = testEnvironment;
    }

    public String getReportPath() {
        return reportPath;
    }

    public void setReportPath(String reportPath) {
        this.reportPath = reportPath;
    }
} 