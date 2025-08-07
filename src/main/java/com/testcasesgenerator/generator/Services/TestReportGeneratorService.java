package com.testcasesgenerator.generator.Services;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.testcasesgenerator.generator.Model.TestExecutionResult;
import com.testcasesgenerator.generator.Model.TestStep;
import com.testcasesgenerator.generator.Repository.TestExecutionResultRepository;

@Service
public class TestReportGeneratorService {

    @Autowired
    private TestExecutionResultRepository testExecutionResultRepository;

    private static final String REPORTS_BASE_DIR = "test-reports/html";

    public String generateTestReport(String ticketId) throws IOException {
        List<TestExecutionResult> executionResults = testExecutionResultRepository.findByTicketIdOrderByStartTimeDesc(ticketId);
        
        if (executionResults.isEmpty()) {
            throw new IllegalArgumentException("No execution results found for ticket: " + ticketId);
        }

        String reportPath = createReportDirectory(ticketId);
        String htmlContent = generateHtmlReport(ticketId, executionResults);
        
        String reportFileName = reportPath + "/test_report.html";
        try (FileWriter writer = new FileWriter(reportFileName)) {
            writer.write(htmlContent);
        }

        System.out.println("Test report generated: " + reportFileName);
        return reportFileName;
    }

    private String createReportDirectory(String ticketId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String reportDir = REPORTS_BASE_DIR + "/" + ticketId + "/" + timestamp;
        
        Path dirPath = Paths.get(reportDir);
        Files.createDirectories(dirPath);
        
        return reportDir;
    }

    private String generateHtmlReport(String ticketId, List<TestExecutionResult> executionResults) {
        StringBuilder html = new StringBuilder();
        
        // Calculate statistics
        long totalTests = executionResults.size();
        long passedTests = executionResults.stream().filter(r -> "PASSED".equals(r.getExecutionStatus())).count();
        long failedTests = executionResults.stream().filter(r -> "FAILED".equals(r.getExecutionStatus())).count();
        long errorTests = executionResults.stream().filter(r -> "ERROR".equals(r.getExecutionStatus())).count();
        
        double passRate = totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
        
        // Start HTML document
        html.append(getHtmlHeader(ticketId));
        
        // Add summary section
        html.append(generateSummarySection(ticketId, totalTests, passedTests, failedTests, errorTests, passRate));
        
        // Add test results section
        html.append(generateTestResultsSection(executionResults));
        
        // Add footer
        html.append(getHtmlFooter());
        
        return html.toString();
    }

    private String generateTestResultsSection(List<TestExecutionResult> executionResults) {
        StringBuilder html = new StringBuilder();
        html.append("<div class=\"test-results\">\n");
        html.append("<h2>Test Execution Details</h2>\n");

        for (TestExecutionResult result : executionResults) {
            html.append(generateTestCaseHtml(result));
        }

        html.append("</div>\n");
        return html.toString();
    }

    private String generateTestCaseHtml(TestExecutionResult result) {
        String statusClass = result.getExecutionStatus().toLowerCase();
        String duration = result.getExecutionDurationMs() != null ? 
            result.getExecutionDurationMs() + "ms" : "N/A";

        StringBuilder html = new StringBuilder();
        html.append("<div class=\"test-case\">\n");
        
        // Test case header
        html.append(String.format(
            "<div class=\"test-case-header %s\" onclick=\"toggleTestCase(this)\">\n" +
            "<h3>%s</h3>\n" +
            "<span class=\"status-badge status-%s\">%s</span>\n" +
            "<span style=\"float: right; color: #666;\">Duration: %s</span>\n" +
            "</div>\n",
            statusClass, result.getTestCaseId(), statusClass, result.getExecutionStatus(), duration
        ));

        // Test case content
        html.append("<div class=\"test-case-content\">\n");
        
        // Execution info
        html.append("<div class=\"execution-info\">\n");
        html.append(String.format(
            "<div class=\"info-item\">\n" +
            "<div class=\"info-label\">Start Time</div>\n" +
            "<div class=\"info-value\">%s</div>\n" +
            "</div>\n",
            result.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        ));
        
        if (result.getEndTime() != null) {
            html.append(String.format(
                "<div class=\"info-item\">\n" +
                "<div class=\"info-label\">End Time</div>\n" +
                "<div class=\"info-value\">%s</div>\n" +
                "</div>\n",
                result.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            ));
        }
        
        html.append("</div>\n");

        // Error message if exists
        if (result.getErrorMessage() != null) {
            html.append(String.format(
                "<div class=\"step-details\" style=\"background: #ffebee;\">\n" +
                "<h4 style=\"color: #d32f2f;\">Error Details</h4>\n" +
                "<p>%s</p>\n" +
                "</div>\n",
                escapeHtml(result.getErrorMessage())
            ));
        }

        // Test Steps and Screenshots
        if (result.getExecutedSteps() != null && !result.getExecutedSteps().isEmpty()) {
            html.append("<h4>Test Steps</h4>\n");
            html.append("<div class=\"test-steps\">\n");
            
            for (TestStep step : result.getExecutedSteps()) {
                html.append(generateTestStepHtml(step));
            }
            
            html.append("</div>\n");
        }

        html.append("</div>\n");
        html.append("</div>\n");
        
        return html.toString();
    }

    private String generateTestStepHtml(TestStep step) {
        StringBuilder html = new StringBuilder();
        String statusClass = step.getStatus().toLowerCase();
        
        html.append(String.format(
            "<div class=\"test-step\">\n" +
            "<div class=\"step-header\">\n" +
            "<h5>Step %d: %s</h5>\n" +
            "<span class=\"status-badge status-%s\">%s</span>\n" +
            "</div>\n" +
            "<div class=\"step-details\">\n" +
            "<p><strong>Expected:</strong> %s</p>\n" +
            "<p><strong>Actual:</strong> %s</p>\n",
            step.getStepNumber(),
            escapeHtml(step.getDescription()),
            statusClass,
            step.getStatus(),
            escapeHtml(step.getExpectedResult()),
            escapeHtml(step.getActualResult())
        ));

        if (step.getErrorMessage() != null) {
            html.append(String.format(
                "<p class=\"error-message\"><strong>Error:</strong> %s</p>\n",
                escapeHtml(step.getErrorMessage())
            ));
        }

        // Add screenshot if available
        if (step.getScreenshotPath() != null) {
            String relativePath = convertToRelativePath(step.getScreenshotPath());
            String screenshotName = step.getScreenshotPath().substring(step.getScreenshotPath().lastIndexOf('/') + 1);
            html.append(String.format(
                "<div class=\"screenshot-item\">\n" +
                "<p class=\"screenshot-label\">Screenshot</p>\n" +
                "<img src=\"%s\" alt=\"%s\" class=\"screenshot\" onclick=\"openImageModal(this.src)\">\n" +
                "</div>\n",
                relativePath,
                screenshotName
            ));
        }

        html.append("</div>\n");
        html.append("</div>\n");
        
        return html.toString();
    }

    private String getHtmlHeader(String ticketId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return "<!DOCTYPE html>\n" +
               "<html lang=\"en\">\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\">\n" +
               "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
               "    <title>Test Automation Report - " + ticketId + "</title>\n" +
               "    <style>\n" +
               "        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f5f5f5; }\n" +
               "        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; }\n" +
               "        .header { text-align: center; margin-bottom: 30px; }\n" +
               "        .summary { display: flex; gap: 20px; margin-bottom: 30px; }\n" +
               "        .summary-card { background: #007acc; color: white; padding: 20px; border-radius: 8px; text-align: center; flex: 1; }\n" +
               "        .summary-card.passed { background: #4CAF50; }\n" +
               "        .summary-card.failed { background: #f44336; }\n" +
               "        .summary-card.error { background: #ff9800; }\n" +
               "        .test-case { border: 1px solid #ddd; margin-bottom: 20px; }\n" +
               "        .test-case-header { background: #f8f9fa; padding: 15px; cursor: pointer; }\n" +
               "        .test-case-content { padding: 20px; display: none; }\n" +
               "        .test-case-content.show { display: block; }\n" +
               "        .status-badge { padding: 4px 12px; border-radius: 20px; color: white; }\n" +
               "        .status-passed { background: #4CAF50; }\n" +
               "        .status-failed { background: #f44336; }\n" +
               "        .status-error { background: #ff9800; }\n" +
               "        .test-step { border: 1px solid #eee; margin: 10px 0; padding: 15px; }\n" +
               "        .step-header { display: flex; justify-content: space-between; align-items: center; }\n" +
               "        .step-details { margin-top: 10px; }\n" +
               "        .error-message { color: #f44336; }\n" +
               "        .screenshots-container { display: flex; flex-wrap: wrap; gap: 15px; margin: 15px 0; }\n" +
               "        .screenshot-item { text-align: center; margin-top: 15px; }\n" +
               "        .screenshot-label { font-size: 12px; color: #666; margin-bottom: 5px; }\n" +
               "        .screenshot { max-width: 300px; height: auto; border: 2px solid #ddd; border-radius: 8px; cursor: pointer; transition: transform 0.2s; }\n" +
               "        .screenshot:hover { transform: scale(1.05); border-color: #007acc; }\n" +
               "        .modal { display: none; position: fixed; z-index: 1000; left: 0; top: 0; width: 100%; height: 100%; background-color: rgba(0,0,0,0.9); }\n" +
               "        .modal-content { margin: auto; display: block; width: 80%; max-width: 1200px; padding: 20px; }\n" +
               "        .close { position: absolute; top: 15px; right: 35px; color: #f1f1f1; font-size: 40px; font-weight: bold; cursor: pointer; }\n" +
               "        .close:hover { color: #bbb; }\n" +
               "    </style>\n" +
               "    <script>\n" +
               "        function toggleTestCase(element) {\n" +
               "            const content = element.nextElementSibling;\n" +
               "            content.classList.toggle('show');\n" +
               "        }\n" +
               "        function openImageModal(src) {\n" +
               "            const modal = document.getElementById('imageModal');\n" +
               "            const modalImg = document.getElementById('modalImage');\n" +
               "            modal.style.display = 'block';\n" +
               "            modalImg.src = src;\n" +
               "        }\n" +
               "        function closeImageModal() {\n" +
               "            document.getElementById('imageModal').style.display = 'none';\n" +
               "        }\n" +
               "    </script>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <div class=\"container\">\n" +
               "        <div class=\"header\">\n" +
               "            <h1>Test Automation Report</h1>\n" +
               "            <div>Ticket ID: " + ticketId + " | Generated: " + timestamp + "</div>\n" +
               "        </div>\n";
    }

    private String generateSummarySection(String ticketId, long totalTests, long passedTests, 
                                        long failedTests, long errorTests, double passRate) {
        return "<div class=\"summary\">\n" +
               "    <div class=\"summary-card\">\n" +
               "        <h3>" + totalTests + "</h3>\n" +
               "        <p>Total Tests</p>\n" +
               "    </div>\n" +
               "    <div class=\"summary-card passed\">\n" +
               "        <h3>" + passedTests + "</h3>\n" +
               "        <p>Passed</p>\n" +
               "    </div>\n" +
               "    <div class=\"summary-card failed\">\n" +
               "        <h3>" + failedTests + "</h3>\n" +
               "        <p>Failed</p>\n" +
               "    </div>\n" +
               "    <div class=\"summary-card error\">\n" +
               "        <h3>" + errorTests + "</h3>\n" +
               "        <p>Errors</p>\n" +
               "    </div>\n" +
               "    <div class=\"summary-card\">\n" +
               "        <h3>" + String.format("%.1f", passRate) + "%</h3>\n" +
               "        <p>Pass Rate</p>\n" +
               "    </div>\n" +
               "</div>\n";
    }

    private String getHtmlFooter() {
        return "    </div>\n" +
               "    <!-- Image Modal -->\n" +
               "    <div id=\"imageModal\" class=\"modal\" onclick=\"closeImageModal()\">\n" +
               "        <span class=\"close\" onclick=\"closeImageModal()\">&times;</span>\n" +
               "        <img class=\"modal-content\" id=\"modalImage\">\n" +
               "    </div>\n" +
               "</body>\n" +
               "</html>\n";
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    private String convertToRelativePath(String absolutePath) {
        if (absolutePath.contains("screenshots/")) {
            String screenshotsPath = absolutePath.substring(absolutePath.indexOf("screenshots/") + "screenshots/".length());
            return "/static/screenshots/" + screenshotsPath;
        }
        
        if (absolutePath.startsWith("test-reports/screenshots/")) {
            String screenshotsPath = absolutePath.substring("test-reports/screenshots/".length());
            return "/static/screenshots/" + screenshotsPath;
        }
        
        return absolutePath;
    }
} 