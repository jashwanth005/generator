package com.testcasesgenerator.generator.Services;

import com.testcasesgenerator.generator.Model.AutomationScript;
import com.testcasesgenerator.generator.Model.TestExecutionResult;
import com.testcasesgenerator.generator.Model.TestStep;
import com.testcasesgenerator.generator.Repository.AutomationScriptRepository;
import com.testcasesgenerator.generator.Repository.TestExecutionResultRepository;
import com.testcasesgenerator.generator.Services.SmartTestCaseGeneratorService.TestCase;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import java.util.logging.Level;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

@Service
public class TestExecutionService {

    @Autowired
    private TestExecutionResultRepository testExecutionResultRepository;

    @Autowired
    private AutomationScriptRepository automationScriptRepository;

    @Autowired
    private ScreenshotService screenshotService;

    @Autowired
    private SmartTestCaseGeneratorService smartTestCaseGeneratorService;

    @Value("${browser.headless:true}")
    private boolean defaultHeadless;

    @Value("${browser.window.width:1920}")
    private int browserWidth;

    @Value("${browser.window.height:1080}")
    private int browserHeight;

    private static final String SCREENSHOTS_BASE_DIR = "test-reports/screenshots";
    private static final int WAIT_TIMEOUT_SECONDS = 10;

    public TestExecutionResult executeSmartTestCase(TestCase testCase, String baseUrl, String executionDir) {
        WebDriver driver = null;
        TestExecutionResult result = new TestExecutionResult();
        result.setTicketId(testCase.getId().split("_")[0]); // Extract ticket ID from test case ID
        result.setTestCaseId(testCase.getId());
        result.setExecutionStatus("RUNNING");
        result.setStartTime(LocalDateTime.now());
        
        List<TestStep> executedSteps = new ArrayList<>();
        
        try {
            driver = setupWebDriver(defaultHeadless);
            driver.get(baseUrl);
            
            // Wait for initial page load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Execute each test step
            for (com.testcasesgenerator.generator.Services.SmartTestCaseGeneratorService.TestStep step : testCase.getSteps()) {
                TestStep executedStep = executeTestStep(driver, step, executionDir, wait);
                executedStep.setTestExecutionResult(result);
                executedSteps.add(executedStep);
                
                // If a step fails, stop execution
                if (executedStep.getStatus().equals("FAILED")) {
                    result.setExecutionStatus("FAILED");
                    break;
                }
            }
            
            // If all steps passed, mark test as passed
            if (result.getExecutionStatus().equals("RUNNING")) {
                result.setExecutionStatus("PASSED");
            }
            
        } catch (Exception e) {
            result.setExecutionStatus("ERROR");
            result.setErrorMessage(e.getMessage());
            
            // Create error step
            TestStep errorStep = new TestStep();
            errorStep.setTestExecutionResult(result);
            errorStep.setStepNumber(executedSteps.size() + 1);
            errorStep.setDescription("Unexpected error during test execution");
            errorStep.setExpectedResult("Test should execute without errors");
            errorStep.setActualResult("Error: " + e.getMessage());
            errorStep.setStatus("ERROR");
            
            // Capture error screenshot if possible
            try {
                if (driver != null) {
                    String screenshotPath = screenshotService.captureScreenshot(driver, executionDir, "error_screenshot");
                    errorStep.setScreenshotPath(screenshotPath);
                }
            } catch (Exception screenshotError) {
                System.err.println("Failed to capture error screenshot: " + screenshotError.getMessage());
            }
            
            executedSteps.add(errorStep);
        } finally {
            cleanupWebDriver(driver);
            result.setEndTime(LocalDateTime.now());
        }
        
        result.setExecutedSteps(executedSteps);
        return testExecutionResultRepository.save(result);
    }

    private WebElement findElement(WebDriver driver, String identifier) {
        String[] parts = identifier.split(":", 2);
        if (parts.length != 2) {
            throw new org.openqa.selenium.NoSuchElementException("Invalid identifier format: " + identifier);
        }
        
        String type = parts[0];
        String value = parts[1];
        
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
        
        try {
            switch (type.toLowerCase()) {
                case "id":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.id(value)));
                case "name":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.name(value)));
                case "class":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.className(value)));
                case "xpath":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.xpath(value)));
                case "css":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(value)));
                case "linktext":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.linkText(value)));
                case "partiallinktext":
                    return wait.until(ExpectedConditions.elementToBeClickable(By.partialLinkText(value)));
                default:
                    throw new org.openqa.selenium.NoSuchElementException("Unsupported identifier type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Failed to find element: " + identifier);
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
    }

    private WebDriver setupWebDriver(boolean headless) {
        // Setup ChromeDriver with specific version
        WebDriverManager.chromedriver()
            .browserVersion("127")  // Match your Chrome browser version
            .setup();

        ChromeOptions options = new ChromeOptions();
        
        if (headless) {
            options.addArguments("--headless=new");  // Use new headless mode
        }
        
        // Add required options for stability
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=" + browserWidth + "," + browserHeight);
        options.addArguments("--disable-gpu");
        options.addArguments("--disable-extensions");
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--ignore-certificate-errors");
        
        // Add options for better stability in headless mode
        if (headless) {
            options.addArguments("--disable-background-timer-throttling");
            options.addArguments("--disable-backgrounding-occluded-windows");
            options.addArguments("--disable-renderer-backgrounding");
        }

        // Add CDP options
        options.setExperimentalOption("w3c", true);
        
        // Add performance logging preferences
        LoggingPreferences logPrefs = new LoggingPreferences();
        logPrefs.enable(LogType.BROWSER, Level.ALL);
        logPrefs.enable(LogType.PERFORMANCE, Level.ALL);
        options.setCapability("goog:loggingPrefs", logPrefs);
        
        try {
            WebDriver driver = new ChromeDriver(options);
            
            // Set timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            
            return driver;
        } catch (Exception e) {
            System.err.println("Failed to create ChromeDriver: " + e.getMessage());
            // Try with default configuration
            ChromeOptions fallbackOptions = new ChromeOptions();
            fallbackOptions.addArguments("--headless=new");
            fallbackOptions.addArguments("--no-sandbox");
            fallbackOptions.addArguments("--disable-dev-shm-usage");
            return new ChromeDriver(fallbackOptions);
        }
    }

    private void cleanupWebDriver(WebDriver driver) {
        if (driver != null) {
            try {
                // Get all window handles
                Set<String> windowHandles = driver.getWindowHandles();
                for (String handle : windowHandles) {
                    try {
                        driver.switchTo().window(handle);
                        driver.close();
                    } catch (Exception e) {
                        System.err.println("Error closing window: " + e.getMessage());
                    }
                }
                
                // Quit the driver
                driver.quit();
                
                // Wait a bit to ensure cleanup
                Thread.sleep(1000);
            } catch (Exception e) {
                System.err.println("Error closing WebDriver: " + e.getMessage());
                try {
                    // Force quit as last resort
                    driver.quit();
                } catch (Exception ignored) {}
            }
        }
    }

    private boolean waitForElement(WebDriver driver, WebElement element, Duration timeout) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, timeout);
            wait.until(ExpectedConditions.and(
                ExpectedConditions.visibilityOf(element),
                ExpectedConditions.elementToBeClickable(element)
            ));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean waitForPageLoad(WebDriver driver) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            wait.until(webDriver -> {
                String readyState = ((JavascriptExecutor) webDriver)
                    .executeScript("return document.readyState").toString();
                return readyState.equals("complete");
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void scrollToElement(WebDriver driver, WebElement element) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", 
                element
            );
            Thread.sleep(500);
        } catch (Exception e) {
            System.err.println("Error scrolling to element: " + e.getMessage());
        }
    }

    private TestStep executeTestStep(WebDriver driver, SmartTestCaseGeneratorService.TestStep step, String executionDir, WebDriverWait wait) {
        TestStep executedStep = new TestStep();
        executedStep.setStepNumber(step.getStepNumber());
        executedStep.setDescription(step.getAction());
        executedStep.setExpectedResult(step.getExpectedResult());
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Find and prepare the element
            WebElement element = findElement(driver, step.getElement());
            if (!waitForElement(driver, element, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS))) {
                throw new TimeoutException("Element not ready for interaction");
            }
            scrollToElement(driver, element);
            
            // Perform the action based on element type and action
            switch (step.getAction().toLowerCase()) {
                case "enter text":
                case "enter valid data":
                case "enter invalid data":
                case "enter invalid email":
                case "enter weak password":
                case "enter invalid number":
                case "enter invalid phone number":
                    handleTextInput(driver, element, step.getData().get("value"));
                    break;

                case "select dropdown":
                case "select option":
                    handleDropdownSelection(driver, element, step.getData().get("value"));
                    break;

                case "select radio":
                case "select radio button":
                    handleRadioSelection(driver, element);
                    break;

                case "check checkbox":
                case "uncheck checkbox":
                    handleCheckbox(driver, element, step.getAction().startsWith("check"));
                    break;

                case "select date":
                    handleDateSelection(driver, element, step.getData().get("value"));
                    break;

                case "upload file":
                    handleFileUpload(driver, element, step.getData().get("filepath"));
                    break;

                case "drag and drop":
                    handleDragAndDrop(driver, element, step.getData().get("target"));
                    break;

                case "hover":
                case "mouse over":
                    handleHover(driver, element);
                    break;

                case "scroll to":
                    handleScroll(driver, element);
                    break;

                case "submit form":
                    // Use the new flexible form submission method
                    if ("form-submission".equals(step.getElement())) {
                        // Generic form submission - find any form on the page
                        handleFormSubmission(driver, "any-form");
                    } else {
                        handleFormSubmission(driver, step.getElement());
                    }
                    break;

                case "click navigation element":
                case "click":
                case "click button":
                    handleClick(driver, element);
                    break;
                    
                default:
                    throw new UnsupportedOperationException("Unsupported action: " + step.getAction());
            }
            
            // Wait for any potential page load or AJAX requests
            waitForPageLoadAndAjax(driver);
            
            // Verify the expected result
            boolean stepPassed = verifyStepResult(driver, step);
            executedStep.setStatus(stepPassed ? "PASSED" : "FAILED");
            executedStep.setActualResult(stepPassed ? "Action completed successfully" : "Verification failed");
            
        } catch (Exception e) {
            executedStep.setStatus("FAILED");
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.length() > 1000) {
                errorMessage = errorMessage.substring(0, 997) + "...";
            }
            executedStep.setActualResult("Error: " + errorMessage);
            executedStep.setErrorMessage(errorMessage);
            
            // Take screenshot on failure
            captureErrorScreenshot(driver, executionDir, step, executedStep);
        }
        
        executedStep.setExecutionDurationMs(System.currentTimeMillis() - startTime);
        captureStepScreenshot(driver, executionDir, step, executedStep);
        
        return executedStep;
    }

    private void handleTextInput(WebDriver driver, WebElement element, String value) {
        // Clear using different methods to ensure field is empty
        element.clear();
        element.sendKeys(Keys.CONTROL + "a" + Keys.DELETE);
        try { Thread.sleep(300); } catch (InterruptedException e) {} // Small pause after clearing
        
        // Handle special input types
        String inputType = element.getAttribute("type");
        if (inputType != null) {
            switch (inputType.toLowerCase()) {
                case "date":
                    // Use JavaScript to set date value directly
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = arguments[1]", element, value);
                    return;
                case "range":
                    // Use JavaScript to set range value
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = arguments[1]", element, value);
                    return;
                case "color":
                    // Use JavaScript to set color value
                    ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].value = arguments[1]", element, value);
                    return;
            }
        }
        
        // Type the value character by character for normal inputs
        for (char c : value.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try { Thread.sleep(50); } catch (InterruptedException e) {} // Small delay between characters
        }
    }

    private void handleDropdownSelection(WebDriver driver, WebElement element, String value) {
        if (element.getTagName().equalsIgnoreCase("select")) {
            // Standard select element
            Select select = new Select(element);
            try {
                select.selectByVisibleText(value);
            } catch (org.openqa.selenium.NoSuchElementException e) {
                try {
                    select.selectByValue(value);
                } catch (org.openqa.selenium.NoSuchElementException e2) {
                    select.selectByIndex(Integer.parseInt(value));
                }
            }
        } else {
            // Custom dropdown (might be div/span based)
            element.click();
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            
            // Try different strategies to find and click the option
            List<WebElement> options = driver.findElements(By.cssSelector(
                "[role='option'], [role='listbox'] li, .dropdown-item, .select-option"));
            
            for (WebElement option : options) {
                if (option.getText().trim().equals(value) || 
                    option.getAttribute("value").equals(value)) {
                    scrollToElement(driver, option);
                    option.click();
                    return;
                }
            }
            throw new org.openqa.selenium.NoSuchElementException("Dropdown option not found: " + value);
        }
    }

    private void handleRadioSelection(WebDriver driver, WebElement element) {
        if (!element.isSelected()) {
            try {
                element.click();
            } catch (ElementClickInterceptedException e) {
                // Try clicking the label if the input is hidden
                WebElement label = driver.findElement(By.cssSelector("label[for='" + element.getAttribute("id") + "']"));
                label.click();
            }
        }
    }

    private void handleCheckbox(WebDriver driver, WebElement element, boolean check) {
        if (element.isSelected() != check) {
            try {
                element.click();
            } catch (ElementClickInterceptedException e) {
                // Try clicking the label if the input is hidden
                WebElement label = driver.findElement(By.cssSelector("label[for='" + element.getAttribute("id") + "']"));
                label.click();
            }
        }
    }

    private void handleDateSelection(WebDriver driver, WebElement element, String date) {
        String inputType = element.getAttribute("type");
        
        if ("date".equals(inputType)) {
            // Native date input
            ((JavascriptExecutor) driver).executeScript(
                "arguments[0].value = arguments[1]", element, date);
        } else {
            // Custom datepicker
            element.click();
            try { Thread.sleep(500); } catch (InterruptedException e) {}
            
            // Try to find and click the date
            // This is a basic implementation - you might need to customize based on your datepicker
            WebElement dateElement = driver.findElement(By.cssSelector(
                "[data-date='" + date + "'], [aria-label*='" + date + "']"));
            dateElement.click();
        }
    }

    private void handleFileUpload(WebDriver driver, WebElement element, String filepath) {
        if (element.getAttribute("type").equals("file")) {
            element.sendKeys(filepath);
        } else {
            throw new UnsupportedOperationException("Element is not a file input");
        }
    }

    private void handleDragAndDrop(WebDriver driver, WebElement source, String targetLocator) {
        WebElement target = findElement(driver, targetLocator);
        
        try {
            // Try using Actions
            Actions actions = new Actions(driver);
            actions.dragAndDrop(source, target).perform();
        } catch (Exception e) {
            // Fallback to JavaScript
            String script =
                "function createEvent(typeOfEvent) {\n" +
                "  var event = document.createEvent(\"CustomEvent\");\n" +
                "  event.initCustomEvent(typeOfEvent, true, true, null);\n" +
                "  event.dataTransfer = {\n" +
                "    data: {},\n" +
                "    setData: function(key, value) { this.data[key] = value; },\n" +
                "    getData: function(key) { return this.data[key]; }\n" +
                "  };\n" +
                "  return event;\n" +
                "}\n" +
                "\n" +
                "function dispatchEvent(element, event, transferData) {\n" +
                "  if (transferData !== undefined) {\n" +
                "    event.dataTransfer = transferData;\n" +
                "  }\n" +
                "  if (element.dispatchEvent) {\n" +
                "    element.dispatchEvent(event);\n" +
                "  } else if (element.fireEvent) {\n" +
                "    element.fireEvent(\"on\" + event.type, event);\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "function simulateDragDrop(sourceElement, targetElement) {\n" +
                "  var dragStartEvent = createEvent('dragstart');\n" +
                "  dispatchEvent(sourceElement, dragStartEvent);\n" +
                "  var dropEvent = createEvent('drop');\n" +
                "  dispatchEvent(targetElement, dropEvent, dragStartEvent.dataTransfer);\n" +
                "  var dragEndEvent = createEvent('dragend');\n" +
                "  dispatchEvent(sourceElement, dragEndEvent, dropEvent.dataTransfer);\n" +
                "}\n" +
                "\n" +
                "simulateDragDrop(arguments[0], arguments[1]);";
            
            ((JavascriptExecutor) driver).executeScript(script, source, target);
        }
    }

    private void handleHover(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element).perform();
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    private void handleScroll(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", 
            element);
        try { Thread.sleep(500); } catch (InterruptedException e) {}
    }

    private void handleClick(WebDriver driver, WebElement element) {
        try {
            // Try regular click first
            element.click();
        } catch (org.openqa.selenium.ElementClickInterceptedException e) {
            // If click is intercepted, try JavaScript click
            try {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            } catch (Exception jsException) {
                throw new RuntimeException("Failed to click element: " + jsException.getMessage());
            }
        }
    }

    private void handleFormSubmission(WebDriver driver, String formIdentifier) {
        try {
            // First, try to find a submit button
            WebElement submitButton = findSubmitButton(driver, formIdentifier);
            if (submitButton != null) {
                handleClick(driver, submitButton);
                return;
            }
            
            // If no submit button found, try pressing Enter on the last input field
            List<WebElement> inputs = driver.findElements(By.cssSelector("input:not([type='hidden']):not([type='submit']):not([type='button'])"));
            if (!inputs.isEmpty()) {
                WebElement lastInput = inputs.get(inputs.size() - 1);
                lastInput.sendKeys(Keys.RETURN);
                return;
            }
            
            // Try to find any clickable element that might submit the form
            List<WebElement> clickableElements = driver.findElements(By.cssSelector("button, input[type='submit'], input[type='button'], a.btn, .btn"));
            for (WebElement element : clickableElements) {
                if (element.isDisplayed() && element.isEnabled()) {
                    handleClick(driver, element);
                    return;
                }
            }
            
            // Last resort: try to submit the form using JavaScript
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            if (!forms.isEmpty()) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].submit();", forms.get(0));
                return;
            }
            
            throw new RuntimeException("No form submission method found");
            
        } catch (Exception e) {
            throw new RuntimeException("Form submission failed: " + e.getMessage());
        }
    }

    private WebElement findSubmitButton(WebDriver driver, String formIdentifier) {
        // Look for submit buttons in various ways
        List<WebElement> submitButtons = driver.findElements(By.cssSelector("button[type='submit'], input[type='submit']"));
        for (WebElement button : submitButtons) {
            if (button.isDisplayed() && button.isEnabled()) {
                return button;
            }
        }
        
        // Look for buttons with submit-related text or classes
        List<WebElement> allButtons = driver.findElements(By.tagName("button"));
        for (WebElement button : allButtons) {
            if (button.isDisplayed() && button.isEnabled()) {
                String text = button.getText().toLowerCase();
                String className = button.getAttribute("class");
                if (text.contains("submit") || text.contains("save") || text.contains("send") ||
                    (className != null && (className.contains("submit") || className.contains("btn-primary")))) {
                    return button;
                }
            }
        }
        
        return null;
    }

    private void waitForPageLoadAndAjax(WebDriver driver) {
        // Wait for page load
        new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS))
            .until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
        
        // Wait for jQuery if present
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> (Boolean) ((JavascriptExecutor) webDriver)
                    .executeScript("return jQuery.active == 0"));
        } catch (Exception ignored) {}
        
        // Wait for Angular if present
        try {
            new WebDriverWait(driver, Duration.ofSeconds(5))
                .until(webDriver -> (Boolean) ((JavascriptExecutor) webDriver)
                    .executeScript("return window.getAllAngularTestabilities().findIndex(x => !x.isStable()) === -1"));
        } catch (Exception ignored) {}
        
        try { Thread.sleep(1000); } catch (InterruptedException e) {} // Additional wait after page load
    }

    private void captureErrorScreenshot(WebDriver driver, String executionDir, SmartTestCaseGeneratorService.TestStep step, TestStep executedStep) {
        try {
            String screenshotPath = screenshotService.captureScreenshot(
                driver,
                executionDir,
                "error_" + step.getStepNumber() + "_" + step.getAction().toLowerCase().replace(" ", "_")
            );
            executedStep.setScreenshotPath(screenshotPath);
        } catch (Exception screenshotError) {
            System.err.println("Failed to capture error screenshot: " + screenshotError.getMessage());
        }
    }

    private void captureStepScreenshot(WebDriver driver, String executionDir, SmartTestCaseGeneratorService.TestStep step, TestStep executedStep) {
        try {
            String screenshotPath = screenshotService.captureScreenshot(
                driver,
                executionDir,
                "step_" + step.getStepNumber() + "_" + step.getAction().toLowerCase().replace(" ", "_")
            );
            executedStep.setScreenshotPath(screenshotPath);
        } catch (Exception e) {
            System.err.println("Failed to capture step screenshot: " + e.getMessage());
        }
    }

    private boolean verifyStepResult(WebDriver driver, SmartTestCaseGeneratorService.TestStep step) {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            
            switch (step.getAction().toLowerCase()) {
                case "enter valid data":
                    // For valid data, verify the element contains the entered value
                    WebElement element = findElement(driver, step.getElement());
                    String actualValue = element.getAttribute("value");
                    if (actualValue == null) {
                        actualValue = element.getText();
                    }
                    return actualValue != null && actualValue.equals(step.getData().get("value"));
                    
                case "enter invalid data":
                case "enter invalid email":
                case "enter weak password":
                case "enter invalid number":
                case "enter invalid phone number":
                    // For invalid data, verify validation error is shown
                    return wait.until(d -> {
                        // Check for various error indicators
                        boolean hasError = !d.findElements(By.cssSelector(".error-message, .invalid-feedback, [aria-invalid='true']")).isEmpty();
                        hasError |= !d.findElements(By.xpath("//*[contains(@class, 'error') or contains(@class, 'invalid')]")).isEmpty();
                        hasError |= !d.findElements(By.xpath("//*[contains(text(), 'error') or contains(text(), 'invalid')]")).isEmpty();
                        return hasError;
                    });
                    
                case "submit form":
                    if (step.getExpectedResult().contains("fail")) {
                        // For negative test cases, verify form was not submitted
                        return wait.until(d -> {
                            boolean hasError = !d.findElements(By.cssSelector(".error-message, .invalid-feedback, [aria-invalid='true']")).isEmpty();
                            hasError |= !d.findElements(By.xpath("//*[contains(@class, 'error') or contains(@class, 'invalid')]")).isEmpty();
                            return hasError;
                        });
                    } else {
                        // For positive test cases, verify successful submission
                        return wait.until(d -> {
                            boolean hasSuccess = !d.findElements(By.cssSelector(".success-message, .alert-success")).isEmpty();
                            hasSuccess |= !d.findElements(By.xpath("//*[contains(@class, 'success')]")).isEmpty();
                            hasSuccess |= !d.findElements(By.xpath("//*[contains(text(), 'success') or contains(text(), 'thank')]")).isEmpty();
                            return hasSuccess;
                        });
                    }
                    
                case "click navigation element":
                case "click":
                    // Verify navigation occurred or element state changed
                    String expectedUrl = step.getData().get("href");
                    if (expectedUrl != null) {
                        return wait.until(d -> d.getCurrentUrl().contains(expectedUrl));
                    }
                    // If no URL specified, check for any page change
                    return wait.until(d -> ((JavascriptExecutor) d)
                        .executeScript("return document.readyState").equals("complete"));
                    
                default:
                    throw new UnsupportedOperationException("Verification not implemented for action: " + step.getAction());
            }
        } catch (Exception e) {
            System.err.println("Verification failed: " + e.getMessage());
            return false;
        }
    }

    private String createExecutionDirectory(String baseDir, String ticketId) throws IOException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String dirPath = baseDir + "/" + ticketId + "/" + timestamp;
        Files.createDirectories(Paths.get(dirPath));
        return dirPath;
    }

    // Existing methods for backward compatibility
    public TestExecutionResult executeAutomationScript(Long scriptId) throws Exception {
        AutomationScript script = automationScriptRepository.findById(scriptId)
            .orElseThrow(() -> new RuntimeException("Automation script not found with ID: " + scriptId));
        
        TestExecutionResult result = new TestExecutionResult(
            script.getTicketId(),
            script.getTestCaseId(),
            "RUNNING"
        );
        
        try {
            String executionDir = createExecutionDirectory(SCREENSHOTS_BASE_DIR, script.getTicketId());
            List<TestStep> steps = executeScript(script, executionDir);
            result.setExecutedSteps(steps);
            result.setExecutionStatus(determineOverallStatus(steps));
        } catch (Exception e) {
            result.setExecutionStatus("ERROR");
            result.setErrorMessage(e.getMessage());
        }
        
        return testExecutionResultRepository.save(result);
    }

    public List<TestExecutionResult> executeAllScriptsForTicket(String ticketId, Boolean headless) throws Exception {
        List<AutomationScript> scripts = automationScriptRepository.findByTicketId(ticketId);
        List<TestExecutionResult> results = new ArrayList<>();
        
        for (AutomationScript script : scripts) {
            TestExecutionResult result = executeAutomationScript(script.getId());
            results.add(result);
        }
        
        return results;
    }

    private String determineOverallStatus(List<TestStep> steps) {
        if (steps.stream().anyMatch(s -> "ERROR".equals(s.getStatus()))) {
            return "ERROR";
        } else if (steps.stream().anyMatch(s -> "FAILED".equals(s.getStatus()))) {
            return "FAILED";
        } else {
            return "PASSED";
        }
    }

    private List<TestStep> executeScript(AutomationScript script, String executionDir) throws Exception {
        List<TestStep> executedSteps = new ArrayList<>();
        WebDriver driver = null;
        
        TestExecutionResult result = new TestExecutionResult(
            script.getTicketId(),
            script.getTestCaseId(),
            "RUNNING"
        );
        result.setStartTime(LocalDateTime.now());
        
        try {
            driver = setupWebDriver(defaultHeadless);
            driver.get(script.getBaseUrl());
            
            // Wait for page load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            wait.until(webDriver -> ((JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));
            
            // Add navigation step
            TestStep navigationStep = new TestStep();
            navigationStep.setTestExecutionResult(result);
            navigationStep.setStepNumber(1);
            navigationStep.setDescription("Navigate to application");
            navigationStep.setExpectedResult("Page should load successfully");
            navigationStep.setActualResult("Page loaded with title: " + driver.getTitle());
            navigationStep.setStatus("PASSED");
            
            String screenshotPath = screenshotService.captureScreenshot(driver, executionDir, "step_1_navigation");
            navigationStep.setScreenshotPath(screenshotPath);
            
            executedSteps.add(navigationStep);
            
        } catch (Exception e) {
            TestStep errorStep = new TestStep();
            errorStep.setTestExecutionResult(result);
            errorStep.setStepNumber(executedSteps.size() + 1);
            errorStep.setDescription("Script execution error");
            errorStep.setExpectedResult("Should complete successfully");
            errorStep.setStatus("ERROR");
            errorStep.setErrorMessage(e.getMessage());
            errorStep.setActualResult("Execution failed: " + e.getMessage());
            
            try {
                String screenshotPath = screenshotService.captureScreenshot(driver, executionDir, "error_screenshot");
                errorStep.setScreenshotPath(screenshotPath);
            } catch (Exception screenshotError) {
                System.err.println("Failed to capture error screenshot: " + screenshotError.getMessage());
            }
            
            executedSteps.add(errorStep);
        } finally {
            cleanupWebDriver(driver);
            result.setEndTime(LocalDateTime.now());
        }
        
        return executedSteps;
    }
} 