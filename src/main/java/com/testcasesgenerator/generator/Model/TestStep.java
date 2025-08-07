package com.testcasesgenerator.generator.Model;

import javax.persistence.*;

@Entity
@Table(name = "test_step")
public class TestStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "test_execution_result_id", nullable = false)
    private TestExecutionResult testExecutionResult;

    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String expectedResult;

    @Column(columnDefinition = "TEXT")
    private String actualResult;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "screenshot_path")
    private String screenshotPath;

    @Column(name = "execution_duration_ms")
    private Long executionDurationMs;

    // Default constructor
    public TestStep() {
    }

    // Constructor with fields
    public TestStep(TestExecutionResult testExecutionResult, Integer stepNumber, String description,
                   String expectedResult, String actualResult, String status) {
        this.testExecutionResult = testExecutionResult;
        this.stepNumber = stepNumber;
        this.description = description;
        this.expectedResult = expectedResult;
        this.actualResult = actualResult;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestExecutionResult getTestExecutionResult() {
        return testExecutionResult;
    }

    public void setTestExecutionResult(TestExecutionResult testExecutionResult) {
        this.testExecutionResult = testExecutionResult;
    }

    public Integer getStepNumber() {
        return stepNumber;
    }

    public void setStepNumber(Integer stepNumber) {
        this.stepNumber = stepNumber;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExpectedResult() {
        return expectedResult;
    }

    public void setExpectedResult(String expectedResult) {
        this.expectedResult = expectedResult;
    }

    public String getActualResult() {
        return actualResult;
    }

    public void setActualResult(String actualResult) {
        this.actualResult = actualResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getScreenshotPath() {
        return screenshotPath;
    }

    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }

    public Long getExecutionDurationMs() {
        return executionDurationMs;
    }

    public void setExecutionDurationMs(Long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
} 