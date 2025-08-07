package com.testcasesgenerator.generator.Model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "automation_scripts")
public class AutomationScript {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false)
    private String ticketId;

    @Column(name = "test_case_id", nullable = false)
    private String testCaseId;

    @Column(name = "script_content", columnDefinition = "LONGTEXT")
    private String scriptContent;

    @Column(name = "script_language", nullable = false)
    private String scriptLanguage; // JAVA_SELENIUM, PYTHON_SELENIUM, PLAYWRIGHT_JS, etc.

    @Column(name = "script_type", nullable = false)
    private String scriptType; // WEB, API, MOBILE

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "status")
    private String status; // GENERATED, EXECUTED, FAILED

    @Column(name = "base_url")
    private String baseUrl;

    @Column(name = "test_scenario", columnDefinition = "TEXT")
    private String testScenario;

    // Default constructor
    public AutomationScript() {
    }

    // Constructor with fields
    public AutomationScript(String ticketId, String testCaseId, String scriptContent, 
                           String scriptLanguage, String scriptType, String testScenario) {
        this.ticketId = ticketId;
        this.testCaseId = testCaseId;
        this.scriptContent = scriptContent;
        this.scriptLanguage = scriptLanguage;
        this.scriptType = scriptType;
        this.testScenario = testScenario;
        this.creationDate = LocalDateTime.now();
        this.status = "GENERATED";
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

    public String getScriptContent() {
        return scriptContent;
    }

    public void setScriptContent(String scriptContent) {
        this.scriptContent = scriptContent;
    }

    public String getScriptLanguage() {
        return scriptLanguage;
    }

    public void setScriptLanguage(String scriptLanguage) {
        this.scriptLanguage = scriptLanguage;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getTestScenario() {
        return testScenario;
    }

    public void setTestScenario(String testScenario) {
        this.testScenario = testScenario;
    }
} 