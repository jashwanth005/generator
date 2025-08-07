package com.testcasesgenerator.generator.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.CascadeType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_execution_results")
public class TestExecutionResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String ticketId;

    @Column(nullable = false)
    private String testCaseId;

    @Column(nullable = false)
    private String executionStatus; // RUNNING, PASSED, FAILED, ERROR

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    private Long executionDurationMs;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "test_execution_result_id")
    private List<TestStep> executedSteps = new ArrayList<>();

    // Default constructor
    public TestExecutionResult() {
        this.startTime = LocalDateTime.now();
    }

    // Constructor with basic fields
    public TestExecutionResult(String ticketId, String testCaseId, String executionStatus) {
        this.ticketId = ticketId;
        this.testCaseId = testCaseId;
        this.executionStatus = executionStatus;
        this.startTime = LocalDateTime.now();
    }

    // Getters and setters
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

    public String getExecutionStatus() {
        return executionStatus;
    }

    public void setExecutionStatus(String executionStatus) {
        this.executionStatus = executionStatus;
        if (executionStatus.equals("PASSED") || executionStatus.equals("FAILED") || executionStatus.equals("ERROR")) {
            this.endTime = LocalDateTime.now();
            this.executionDurationMs = java.time.Duration.between(startTime, endTime).toMillis();
        }
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
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

    public List<TestStep> getExecutedSteps() {
        return executedSteps;
    }

    public void setExecutedSteps(List<TestStep> executedSteps) {
        this.executedSteps = executedSteps;
    }

    public void addExecutedStep(TestStep step) {
        if (this.executedSteps == null) {
            this.executedSteps = new ArrayList<>();
        }
        this.executedSteps.add(step);
    }
} 