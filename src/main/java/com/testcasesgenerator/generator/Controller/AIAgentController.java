package com.testcasesgenerator.generator.Controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testcasesgenerator.generator.Model.AutomationScript;
import com.testcasesgenerator.generator.Model.TestExecutionResult;
import com.testcasesgenerator.generator.Services.*;
import com.testcasesgenerator.generator.Services.SmartTestCaseGeneratorService.TestCase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-agent")
@CrossOrigin(origins = "*")
public class AIAgentController {

    @Autowired
    private JiraService jiraService;

    @Autowired
    private ToqanAiService toqanAiService;

    @Autowired
    private AutomationScriptGeneratorService automationScriptGeneratorService;

    @Autowired
    private TestExecutionService testExecutionService;

    @Autowired
    private TestReportGeneratorService testReportGeneratorService;

    @Autowired
    private WebsiteAnalyzerService websiteAnalyzerService;

    @Autowired
    private SmartTestCaseGeneratorService smartTestCaseGeneratorService;

    @PostMapping("/analyze-website")
    public ObjectNode analyzeWebsite(@RequestBody WebsiteAnalysisRequest request) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            var elements = websiteAnalyzerService.analyzeWebsite(request.getBaseUrl());
            
            response.put("success", true);
            response.put("message", "Website analysis completed successfully");
            response.put("elementsFound", elements.size());
            
            // Add element types summary
            var elementTypes = JsonNodeFactory.instance.objectNode();
            elements.forEach(element -> {
                String type = element.getElementType();
                elementTypes.put(
                    type, 
                    elementTypes.has(type) ? elementTypes.get(type).asInt() + 1 : 1
                );
            });
            response.set("elementTypes", elementTypes);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/generate-smart-test-cases")
    public ObjectNode generateSmartTestCases(@RequestBody SmartTestCaseRequest request) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            List<TestCase> testCases = smartTestCaseGeneratorService.generateSmartTestCases(
                request.getBaseUrl(),
                request.getTicketId()
            );
            
            response.put("success", true);
            response.put("message", "Generated " + testCases.size() + " smart test cases");
            response.put("testCasesGenerated", testCases.size());
            response.put("ticketId", request.getTicketId());
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/execute-smart-test-cases")
    public ObjectNode executeSmartTestCases(@RequestBody SmartTestExecutionRequest request) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            // Generate test cases
            List<TestCase> testCases = smartTestCaseGeneratorService.generateSmartTestCases(
                request.getBaseUrl(),
                request.getTicketId()
            );
            
            // Execute each test case
            List<TestExecutionResult> results = testCases.stream()
                .map(testCase -> testExecutionService.executeSmartTestCase(
                    testCase,
                    request.getBaseUrl(),
                    "test-reports/" + request.getTicketId()
                ))
                .toList();
            
            // Generate test report
            String reportPath = testReportGeneratorService.generateTestReport(request.getTicketId());
            
            // Calculate statistics
            long totalTests = results.size();
            long passedTests = results.stream().filter(r -> "PASSED".equals(r.getExecutionStatus())).count();
            long failedTests = results.stream().filter(r -> "FAILED".equals(r.getExecutionStatus())).count();
            long errorTests = results.stream().filter(r -> "ERROR".equals(r.getExecutionStatus())).count();
            
            response.put("success", true);
            response.put("message", "Smart test execution completed");
            response.put("ticketId", request.getTicketId());
            response.put("totalTests", totalTests);
            response.put("passedTests", passedTests);
            response.put("failedTests", failedTests);
            response.put("errorTests", errorTests);
            response.put("reportPath", reportPath);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    @PostMapping("/run-complete-ai-agent-workflow")
    public ObjectNode runCompleteWorkflow(@RequestBody CompleteWorkflowRequest request) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            // Step 1: Analyze website
            var elements = websiteAnalyzerService.analyzeWebsite(request.getBaseUrl());
            
            // Step 2: Generate smart test cases
            List<TestCase> testCases = smartTestCaseGeneratorService.generateSmartTestCases(
                request.getBaseUrl(),
                request.getTicketId()
            );
            
            // Step 3: Execute test cases
            List<TestExecutionResult> results = testCases.stream()
                .map(testCase -> testExecutionService.executeSmartTestCase(
                    testCase,
                    request.getBaseUrl(),
                    "test-reports/" + request.getTicketId()
                ))
                .toList();
            
            // Step 4: Generate test report
            String reportPath = testReportGeneratorService.generateTestReport(request.getTicketId());
            
            // Calculate statistics
            long totalTests = results.size();
            long passedTests = results.stream().filter(r -> "PASSED".equals(r.getExecutionStatus())).count();
            long failedTests = results.stream().filter(r -> "FAILED".equals(r.getExecutionStatus())).count();
            long errorTests = results.stream().filter(r -> "ERROR".equals(r.getExecutionStatus())).count();
            
            response.put("success", true);
            response.put("message", "AI Agent workflow completed successfully");
            response.put("ticketId", request.getTicketId());
            response.put("elementsAnalyzed", elements.size());
            response.put("testCasesGenerated", testCases.size());
            response.put("totalTests", totalTests);
            response.put("passedTests", passedTests);
            response.put("failedTests", failedTests);
            response.put("errorTests", errorTests);
            response.put("reportPath", reportPath);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        
        return response;
    }

    // Request classes
    public static class WebsiteAnalysisRequest {
        private String baseUrl;
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class SmartTestCaseRequest {
        private String ticketId;
        private String baseUrl;
        
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    }

    public static class SmartTestExecutionRequest {
        private String ticketId;
        private String baseUrl;
        private Boolean headless;
        
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public Boolean getHeadless() { return headless; }
        public void setHeadless(Boolean headless) { this.headless = headless; }
    }

    public static class CompleteWorkflowRequest {
        private String ticketId;
        private String baseUrl;
        private Boolean headless;
        private String scriptLanguage;
        
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public Boolean getHeadless() { return headless; }
        public void setHeadless(Boolean headless) { this.headless = headless; }
        public String getScriptLanguage() { return scriptLanguage; }
        public void setScriptLanguage(String scriptLanguage) { this.scriptLanguage = scriptLanguage; }
    }
} 