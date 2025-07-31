package com.testcasesgenerator.generator.Controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testcasesgenerator.generator.Model.AutomationScript;
import com.testcasesgenerator.generator.Model.TestExecutionResult;
import com.testcasesgenerator.generator.Services.AutomationScriptGeneratorService;
import com.testcasesgenerator.generator.Services.ExcelService;
import com.testcasesgenerator.generator.Services.JiraService;
import com.testcasesgenerator.generator.Services.TestExecutionService;
import com.testcasesgenerator.generator.Services.TestReportGeneratorService;
import com.testcasesgenerator.generator.Services.ToqanAiService;
import com.testcasesgenerator.generator.Repository.TestExecutionResultRepository;

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
    private TestExecutionResultRepository testExecutionResultRepository;

    @Autowired
    private ExcelService excelService;

    @PostMapping("/generate-automation-scripts")
    public ObjectNode generateAutomationScripts(@RequestBody AutomationRequest request) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            // Generate automation scripts from test cases
            List<AutomationScript> scripts = automationScriptGeneratorService.generateAutomationScripts(
                request.getTicketId(),
                request.getTestCasesContent(),
                request.getBaseUrl(),
                request.getScriptLanguage()
            );

            response.put("success", true);
            response.put("message", "Generated " + scripts.size() + " automation scripts");
            response.put("scriptsGenerated", scripts.size());
            response.put("ticketId", request.getTicketId());

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/execute-automation-scripts")
    public ObjectNode executeAutomationScripts(@RequestParam String ticketId) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            // Execute all automation scripts for the ticket
            List<TestExecutionResult> results = testExecutionService.executeAllScriptsForTicket(ticketId);

            // Calculate statistics
            long totalTests = results.size();
            long passedTests = results.stream().filter(r -> "PASSED".equals(r.getExecutionStatus())).count();
            long failedTests = results.stream().filter(r -> "FAILED".equals(r.getExecutionStatus())).count();
            long errorTests = results.stream().filter(r -> "ERROR".equals(r.getExecutionStatus())).count();

            response.put("success", true);
            response.put("message", "Executed " + totalTests + " automation scripts");
            response.put("ticketId", ticketId);
            response.put("totalTests", totalTests);
            response.put("passedTests", passedTests);
            response.put("failedTests", failedTests);
            response.put("errorTests", errorTests);
            response.put("passRate", totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/generate-test-report")
    public ObjectNode generateTestReport(@RequestParam String ticketId) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            // Generate comprehensive test report
            String reportPath = testReportGeneratorService.generateTestReport(ticketId);

            response.put("success", true);
            response.put("message", "Test report generated successfully");
            response.put("ticketId", ticketId);
            response.put("reportPath", reportPath);
            response.put("reportUrl", "/api/ai-agent/view-report?reportPath=" + reportPath);

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
            // Step 1: Fetch Jira ticket details
            var issue = jiraService.fetchJiraTicket(request.getTicketId());
            String title = issue.getSummary();
            String description = issue.getDescription();

            // Step 2: Generate test cases using AI
            String testCases = toqanAiService.generateTestCasesWithToqanAi(title, description);
            
            // Step 3: Generate automation scripts
            List<AutomationScript> scripts = automationScriptGeneratorService.generateAutomationScripts(
                request.getTicketId(),
                testCases,
                request.getBaseUrl(),
                request.getScriptLanguage()
            );

            // Step 4: Execute automation scripts
            List<TestExecutionResult> results = testExecutionService.executeAllScriptsForTicket(request.getTicketId(), request.getHeadless());

            // Step 5: Generate test report
            String reportPath = testReportGeneratorService.generateTestReport(request.getTicketId());

            // Calculate final statistics
            long totalTests = results.size();
            long passedTests = results.stream().filter(r -> "PASSED".equals(r.getExecutionStatus())).count();
            long failedTests = results.stream().filter(r -> "FAILED".equals(r.getExecutionStatus())).count();
            long errorTests = results.stream().filter(r -> "ERROR".equals(r.getExecutionStatus())).count();

            response.put("success", true);
            response.put("message", "AI Agent workflow completed successfully");
            response.put("ticketId", request.getTicketId());
            response.put("testCasesGenerated", true);
            response.put("scriptsGenerated", scripts.size());
            response.put("testsExecuted", totalTests);
            response.put("passedTests", passedTests);
            response.put("failedTests", failedTests);
            response.put("errorTests", errorTests);
            response.put("passRate", totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);
            response.put("reportPath", reportPath);
            response.put("reportUrl", "/api/ai-agent/view-report?reportPath=" + reportPath);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            e.printStackTrace();
        }

        return response;
    }

    @GetMapping("/execution-status")
    public ObjectNode getExecutionStatus(@RequestParam String ticketId) {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        
        try {
            // Get execution results for the ticket
            List<TestExecutionResult> results = testExecutionResultRepository.findByTicketId(ticketId);

            if (results.isEmpty()) {
                response.put("success", true);
                response.put("status", "NO_EXECUTIONS");
                response.put("message", "No test executions found for this ticket");
                return response;
            }

            // Calculate statistics
            long totalTests = results.size();
            long passedTests = results.stream().filter(r -> "PASSED".equals(r.getExecutionStatus())).count();
            long failedTests = results.stream().filter(r -> "FAILED".equals(r.getExecutionStatus())).count();
            long errorTests = results.stream().filter(r -> "ERROR".equals(r.getExecutionStatus())).count();
            long runningTests = results.stream().filter(r -> "RUNNING".equals(r.getExecutionStatus())).count();

            String overallStatus = runningTests > 0 ? "RUNNING" : 
                                 (errorTests > 0 || failedTests > 0) ? "COMPLETED_WITH_ISSUES" : "COMPLETED_SUCCESS";

            response.put("success", true);
            response.put("ticketId", ticketId);
            response.put("status", overallStatus);
            response.put("totalTests", totalTests);
            response.put("passedTests", passedTests);
            response.put("failedTests", failedTests);
            response.put("errorTests", errorTests);
            response.put("runningTests", runningTests);
            response.put("passRate", totalTests > 0 ? (double) passedTests / totalTests * 100 : 0);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @GetMapping("/view-report")
    public String viewReport(@RequestParam String reportPath) {
        try {
            return java.nio.file.Files.readString(java.nio.file.Paths.get(reportPath));
        } catch (Exception e) {
            return "<html><body><h1>Error loading report</h1><p>" + e.getMessage() + "</p></body></html>";
        }
    }

    // Inner classes for request DTOs
    public static class AutomationRequest {
        private String ticketId;
        private String testCasesContent;
        private String baseUrl;
        private String scriptLanguage = "JAVA_SELENIUM"; // Default

        // Getters and setters
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        
        public String getTestCasesContent() { return testCasesContent; }
        public void setTestCasesContent(String testCasesContent) { this.testCasesContent = testCasesContent; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public String getScriptLanguage() { return scriptLanguage; }
        public void setScriptLanguage(String scriptLanguage) { this.scriptLanguage = scriptLanguage; }
    }

    public static class CompleteWorkflowRequest {
        private String ticketId;
        private String baseUrl;
        private String scriptLanguage = "JAVA_SELENIUM"; // Default
        private Boolean headless; // null means use default from application.properties

        // Getters and setters
        public String getTicketId() { return ticketId; }
        public void setTicketId(String ticketId) { this.ticketId = ticketId; }
        
        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        
        public String getScriptLanguage() { return scriptLanguage; }
        public void setScriptLanguage(String scriptLanguage) { this.scriptLanguage = scriptLanguage; }
        
        public Boolean getHeadless() { return headless; }
        public void setHeadless(Boolean headless) { this.headless = headless; }
    }
} 