package com.testcasesgenerator.generator.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.testcasesgenerator.generator.Model.AutomationScript;
import com.testcasesgenerator.generator.Model.TestExecutionResult;
import com.testcasesgenerator.generator.Model.TestStep;
import com.testcasesgenerator.generator.Repository.AutomationScriptRepository;
import com.testcasesgenerator.generator.Repository.TestExecutionResultRepository;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.springframework.beans.factory.annotation.Value;

@Service
public class TestExecutionService {

    @Autowired
    private TestExecutionResultRepository testExecutionResultRepository;

    @Autowired
    private AutomationScriptRepository automationScriptRepository;

    @Autowired
    private ScreenshotService screenshotService;

    @Value("${browser.headless:true}")
    private boolean defaultHeadless;

    @Value("${browser.window.width:1920}")
    private int browserWidth;

    @Value("${browser.window.height:1080}")
    private int browserHeight;

    private static final String SCREENSHOTS_BASE_DIR = "test-reports/screenshots";
    private static final String SCRIPTS_BASE_DIR = "test-reports/scripts";

    public TestExecutionResult executeAutomationScript(Long scriptId) throws Exception {
        
        // Get the automation script
        AutomationScript script = automationScriptRepository.findById(scriptId)
            .orElseThrow(() -> new RuntimeException("Automation script not found with ID: " + scriptId));

        // Create execution result record
        TestExecutionResult executionResult = new TestExecutionResult(
            script.getTicketId(),
            script.getTestCaseId(),
            scriptId,
            "RUNNING"
        );

        try {
            // Set up directories
            String executionDir = createExecutionDirectory(script.getTicketId(), script.getTestCaseId());
            
            // Execute the script
            List<TestStep> executedSteps = executeScript(script, executionDir);
            
            // Determine overall status
            String overallStatus = determineOverallStatus(executedSteps);
            
            // Update execution result
            executionResult.setExecutionEndTime(LocalDateTime.now());
            executionResult.setExecutionDurationMs(
                java.time.Duration.between(executionResult.getExecutionStartTime(), executionResult.getExecutionEndTime()).toMillis()
            );
            executionResult.setExecutionStatus(overallStatus);
            
            // Collect screenshot paths
            List<String> screenshotPaths = new ArrayList<>();
            List<String> stepResults = new ArrayList<>();
            
            for (TestStep step : executedSteps) {
                if (step.getScreenshotPath() != null) {
                    screenshotPaths.add(step.getScreenshotPath());
                }
                stepResults.add(convertStepToJson(step));
            }
            
            executionResult.setScreenshotPaths(screenshotPaths);
            executionResult.setStepResults(stepResults);
            
            // Update script status
            script.setStatus("EXECUTED");
            automationScriptRepository.save(script);
            
        } catch (Exception e) {
            // Handle execution failure
            executionResult.setExecutionStatus("ERROR");
            executionResult.setExecutionEndTime(LocalDateTime.now());
            executionResult.setErrorMessage(e.getMessage());
            executionResult.setStackTrace(getStackTrace(e));
            
            script.setStatus("FAILED");
            automationScriptRepository.save(script);
            
            throw e;
        }

        // Save execution result
        return testExecutionResultRepository.save(executionResult);
    }

    private List<TestStep> executeScript(AutomationScript script, String executionDir) throws Exception {
        
        List<TestStep> executedSteps = new ArrayList<>();
        WebDriver driver = null;
        
        try {
            // Setup WebDriver
            driver = setupWebDriver(defaultHeadless);
            System.out.println("WebDriver setup completed successfully (headless: " + defaultHeadless + ")");
            
            // For now, we'll simulate script execution with basic web operations
            // In a real implementation, this would involve dynamic script compilation and execution
            executedSteps = simulateScriptExecution(script, driver, executionDir);
            
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
        
        return executedSteps;
    }

    private WebDriver setupWebDriver(boolean headless) {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            options.addArguments("--headless"); // Run in headless mode for server execution
        }
        
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=" + browserWidth + "," + browserHeight);
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        
        // Additional options for better stability
        if (headless) {
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
        }
        
        WebDriver driver = new ChromeDriver(options);
        return driver;
    }

    // Overloaded method for backwards compatibility
    private WebDriver setupWebDriver() {
        return setupWebDriver(defaultHeadless);
    }

    private List<TestStep> simulateScriptExecution(AutomationScript script, WebDriver driver, String executionDir) {
        List<TestStep> steps = new ArrayList<>();
        
        try {
            // Step 1: Navigate to base URL
            TestStep navigationStep = new TestStep(1, "Navigate to application", "Navigate to " + script.getBaseUrl(), "Page should load successfully");
            long startTime = System.currentTimeMillis();
            
            driver.get(script.getBaseUrl() != null ? script.getBaseUrl() : "https://example.com");
            Thread.sleep(2000); // Wait for page load
            
            navigationStep.setExecutionDurationMs(System.currentTimeMillis() - startTime);
            navigationStep.setActualResult("Page loaded with title: " + driver.getTitle());
            navigationStep.setStatus("PASSED");
            
            // Capture screenshot
            String screenshotPath = captureScreenshot(driver, executionDir, "step_1_navigation");
            navigationStep.setScreenshotPath(screenshotPath);
            
            steps.add(navigationStep);
            
            // Step 2: Verify page title (basic verification)
            TestStep verificationStep = new TestStep(2, "Verify page title", "Check if page title is present", "Title should not be empty");
            startTime = System.currentTimeMillis();
            
            String title = driver.getTitle();
            verificationStep.setExecutionDurationMs(System.currentTimeMillis() - startTime);
            verificationStep.setActualResult("Page title: " + title);
            verificationStep.setStatus(title != null && !title.isEmpty() ? "PASSED" : "FAILED");
            
            screenshotPath = captureScreenshot(driver, executionDir, "step_2_verification");
            verificationStep.setScreenshotPath(screenshotPath);
            
            steps.add(verificationStep);
            
            // Additional steps based on script content
            // This is a simplified simulation - in real implementation, 
            // you would parse and execute the actual generated script
            
        } catch (Exception e) {
            TestStep errorStep = new TestStep(steps.size() + 1, "Script execution error", "Execute automation script", "Should complete successfully");
            errorStep.setStatus("FAILED");
            errorStep.setErrorMessage(e.getMessage());
            errorStep.setActualResult("Execution failed: " + e.getMessage());
            
            try {
                String screenshotPath = captureScreenshot(driver, executionDir, "error_screenshot");
                errorStep.setScreenshotPath(screenshotPath);
            } catch (Exception screenshotError) {
                System.err.println("Failed to capture error screenshot: " + screenshotError.getMessage());
            }
            
            steps.add(errorStep);
        } finally {
            // Ensure WebDriver is properly closed
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("WebDriver closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing WebDriver: " + e.getMessage());
                }
            }
        }
        
        return steps;
    }

    private String captureScreenshot(WebDriver driver, String executionDir, String stepName) {
        try {
            if (driver == null) {
                System.err.println("Driver is null, cannot capture screenshot");
                return null;
            }
            
            // Check if driver is still connected
            try {
                driver.getCurrentUrl(); // Test if driver is responsive
            } catch (Exception e) {
                System.err.println("Driver is not responsive, cannot capture screenshot: " + e.getMessage());
                return null;
            }
            
            TakesScreenshot takesScreenshot = (TakesScreenshot) driver;
            File sourceFile = takesScreenshot.getScreenshotAs(OutputType.FILE);
            
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
            String fileName = stepName + "_" + timestamp + ".png";
            String screenshotPath = executionDir + "/" + fileName;
            
            File destFile = new File(screenshotPath);
            // Ensure parent directories exist
            destFile.getParentFile().mkdirs();
            
            FileUtils.copyFile(sourceFile, destFile);
            
            System.out.println("Screenshot captured: " + screenshotPath);
            return screenshotPath;
            
        } catch (Exception e) {
            System.err.println("Failed to capture screenshot for " + stepName + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private String createExecutionDirectory(String ticketId, String testCaseId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String executionDir = SCREENSHOTS_BASE_DIR + "/" + ticketId + "/" + testCaseId + "/" + timestamp;
        
        Path dirPath = Paths.get(executionDir);
        Files.createDirectories(dirPath);
        
        return executionDir;
    }

    private String determineOverallStatus(List<TestStep> steps) {
        boolean hasFailure = steps.stream().anyMatch(step -> "FAILED".equals(step.getStatus()));
        boolean hasError = steps.stream().anyMatch(step -> step.getErrorMessage() != null);
        
        if (hasError) return "ERROR";
        if (hasFailure) return "FAILED";
        return "PASSED";
    }

    private String convertStepToJson(TestStep step) {
        // Simple JSON conversion with truncated content to avoid DB column size issues
        String description = step.getStepDescription() != null ? 
            truncateString(step.getStepDescription().replace("\"", "\\\""), 200) : "";
        String actualResult = step.getActualResult() != null ? 
            truncateString(step.getActualResult().replace("\"", "\\\""), 300) : "";
        String screenshotPath = step.getScreenshotPath() != null ? 
            step.getScreenshotPath() : "";
        
        return String.format(
            "{\"step\":%d,\"desc\":\"%s\",\"status\":\"%s\",\"result\":\"%s\",\"duration\":%d,\"screenshot\":\"%s\"}",
            step.getStepNumber(),
            description,
            step.getStatus(),
            actualResult,
            step.getExecutionDurationMs(),
            screenshotPath
        );
    }

    private String truncateString(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }

    public List<TestExecutionResult> executeAllScriptsForTicket(String ticketId) {
        return executeAllScriptsForTicket(ticketId, null); // Use default headless setting
    }

    public List<TestExecutionResult> executeAllScriptsForTicket(String ticketId, Boolean headless) {
        List<AutomationScript> scripts = automationScriptRepository.findByTicketIdAndStatus(ticketId, "GENERATED");
        List<TestExecutionResult> results = new ArrayList<>();
        
        for (AutomationScript script : scripts) {
            try {
                TestExecutionResult result = executeAutomationScript(script.getId(), headless);
                results.add(result);
            } catch (Exception e) {
                System.err.println("Failed to execute script " + script.getId() + ": " + e.getMessage());
            }
        }
        
        return results;
    }

    public TestExecutionResult executeAutomationScript(Long scriptId, Boolean headless) throws Exception {
        AutomationScript script = automationScriptRepository.findById(scriptId)
            .orElseThrow(() -> new RuntimeException("Automation script not found with id: " + scriptId));

        // Create execution result record
        TestExecutionResult executionResult = new TestExecutionResult(
            script.getTicketId(),
            script.getTestCaseId(),
            scriptId,
            "RUNNING"
        );

        try {
            // Create execution directory for screenshots
            String executionDir = createExecutionDirectory(script.getTicketId(), script.getTestCaseId());
            
            // Execute the script with specified headless mode
            List<TestStep> executedSteps = executeScript(script, executionDir, headless);
            
            // Calculate execution metrics
            executionResult.setExecutionEndTime(LocalDateTime.now());
            executionResult.setExecutionDurationMs(
                Duration.between(executionResult.getExecutionStartTime(), executionResult.getExecutionEndTime()).toMillis()
            );
            executionResult.setExecutionStatus(determineOverallStatus(executedSteps));
            
            // Process screenshots and step results
            List<String> screenshotPaths = new ArrayList<>();
            List<String> stepResults = new ArrayList<>();
            
            for (TestStep step : executedSteps) {
                if (step.getScreenshotPath() != null) {
                    screenshotPaths.add(step.getScreenshotPath());
                }
                stepResults.add(convertStepToJson(step));
            }
            
            executionResult.setScreenshotPaths(screenshotPaths);
            executionResult.setStepResults(stepResults);
            
            // Update script status
            script.setStatus("EXECUTED");
            automationScriptRepository.save(script);
            
        } catch (Exception e) {
            // Handle execution failure
            executionResult.setExecutionStatus("ERROR");
            executionResult.setExecutionEndTime(LocalDateTime.now());
            executionResult.setErrorMessage(e.getMessage());
            executionResult.setStackTrace(getStackTrace(e));
            
            script.setStatus("FAILED");
            automationScriptRepository.save(script);
            
            throw e;
        }

        // Save execution result
        return testExecutionResultRepository.save(executionResult);
    }

    private List<TestStep> executeScript(AutomationScript script, String executionDir, Boolean headless) throws Exception {
        
        List<TestStep> executedSteps = new ArrayList<>();
        WebDriver driver = null;
        
        try {
            // Setup WebDriver with specified headless mode
            boolean useHeadless = headless != null ? headless : defaultHeadless;
            driver = setupWebDriver(useHeadless);
            System.out.println("WebDriver setup completed successfully (headless: " + useHeadless + ")");
            
            // For now, we'll simulate script execution with basic web operations
            List<TestStep> steps = simulateScriptExecution(script, driver, executionDir);
            executedSteps.addAll(steps);
            
        } catch (Exception e) {
            TestStep errorStep = new TestStep(executedSteps.size() + 1, "Script execution error", "Execute automation script", "Should complete successfully");
            errorStep.setStatus("FAILED");
            errorStep.setErrorMessage(e.getMessage());
            errorStep.setActualResult("Execution failed: " + e.getMessage());
            
            try {
                String screenshotPath = captureScreenshot(driver, executionDir, "error_screenshot");
                errorStep.setScreenshotPath(screenshotPath);
            } catch (Exception screenshotError) {
                System.err.println("Failed to capture error screenshot: " + screenshotError.getMessage());
            }
            
            executedSteps.add(errorStep);
        } finally {
            // Ensure WebDriver is properly closed
            if (driver != null) {
                try {
                    driver.quit();
                    System.out.println("WebDriver closed successfully");
                } catch (Exception e) {
                    System.err.println("Error closing WebDriver: " + e.getMessage());
                }
            }
        }
        
        return executedSteps;
    }
} 