package com.testcasesgenerator.generator.Model;

import java.time.LocalDateTime;

public class TestStep {
    
    private int stepNumber;
    private String stepDescription;
    private String stepAction;
    private String expectedResult;
    private String actualResult;
    private String status; // PASSED, FAILED, SKIPPED
    private String screenshotPath;
    private LocalDateTime executionTime;
    private long executionDurationMs;
    private String errorMessage;
    
    // Default constructor
    public TestStep() {
    }
    
    // Constructor with basic fields
    public TestStep(int stepNumber, String stepDescription, String stepAction, String expectedResult) {
        this.stepNumber = stepNumber;
        this.stepDescription = stepDescription;
        this.stepAction = stepAction;
        this.expectedResult = expectedResult;
        this.executionTime = LocalDateTime.now();
    }
    
    // Getters and Setters
    public int getStepNumber() {
        return stepNumber;
    }
    
    public void setStepNumber(int stepNumber) {
        this.stepNumber = stepNumber;
    }
    
    public String getStepDescription() {
        return stepDescription;
    }
    
    public void setStepDescription(String stepDescription) {
        this.stepDescription = stepDescription;
    }
    
    public String getStepAction() {
        return stepAction;
    }
    
    public void setStepAction(String stepAction) {
        this.stepAction = stepAction;
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
    
    public String getScreenshotPath() {
        return screenshotPath;
    }
    
    public void setScreenshotPath(String screenshotPath) {
        this.screenshotPath = screenshotPath;
    }
    
    public LocalDateTime getExecutionTime() {
        return executionTime;
    }
    
    public void setExecutionTime(LocalDateTime executionTime) {
        this.executionTime = executionTime;
    }
    
    public long getExecutionDurationMs() {
        return executionDurationMs;
    }
    
    public void setExecutionDurationMs(long executionDurationMs) {
        this.executionDurationMs = executionDurationMs;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
    
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
} 