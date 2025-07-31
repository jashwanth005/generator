package com.testcasesgenerator.generator.Services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.testcasesgenerator.generator.Model.AutomationScript;
import com.testcasesgenerator.generator.Repository.AutomationScriptRepository;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import io.github.cdimascio.dotenv.Dotenv;

@Service
public class AutomationScriptGeneratorService {

    @Autowired
    private AutomationScriptRepository automationScriptRepository;

    @Autowired
    private ToqanAiService toqanAiService;

    @Autowired
    private OpenAIService openAIService;

    private static final Dotenv dotenv = Dotenv.load();

    public List<AutomationScript> generateAutomationScripts(String ticketId, String testCasesContent, 
                                                           String baseUrl, String scriptLanguage) throws Exception {
        
        List<AutomationScript> generatedScripts = new ArrayList<>();
        
        // Parse test cases from the content
        List<TestCaseInfo> testCases = parseTestCases(testCasesContent);
        
        for (TestCaseInfo testCase : testCases) {
            // Check if script already exists
            if (!automationScriptRepository.existsByTicketIdAndTestCaseId(ticketId, testCase.getId())) {
                
                // Generate automation script using AI
                String scriptContent = generateScriptWithAI(testCase, baseUrl, scriptLanguage);
                
                // Create automation script entity
                AutomationScript automationScript = new AutomationScript(
                    ticketId,
                    testCase.getId(),
                    scriptContent,
                    scriptLanguage,
                    "WEB", // Default to web automation
                    testCase.getScenario()
                );
                
                automationScript.setBaseUrl(baseUrl);
                
                // Save to database
                AutomationScript savedScript = automationScriptRepository.save(automationScript);
                generatedScripts.add(savedScript);
                
                System.out.println("Generated automation script for test case: " + testCase.getId());
            }
        }
        
        return generatedScripts;
    }

    private String generateScriptWithAI(TestCaseInfo testCase, String baseUrl, String scriptLanguage) throws IOException {
        
        String prompt = buildAutomationScriptPrompt(testCase, baseUrl, scriptLanguage);
        
        // Use Toqua AI for script generation
        try {
            return toqanAiService.generateTestCasesWithToqanAi("Automation Script Generation", prompt);
        } catch (Exception e) {
            // Fallback to OpenAI if Toqua AI fails
            System.out.println("Toqua AI failed, falling back to OpenAI: " + e.getMessage());
            return generateWithOpenAI(prompt);
        }
    }

    private String generateWithOpenAI(String prompt) throws IOException {
        OkHttpClient client = new OkHttpClient();
        
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", new org.json.JSONArray()
            .put(new JSONObject().put("role", "system").put("content", "You are an expert test automation engineer."))
            .put(new JSONObject().put("role", "user").put("content", prompt)));
        requestBody.put("max_tokens", 2000);
        requestBody.put("temperature", 0.3);

        Request request = new Request.Builder()
                .url(dotenv.get("OPENAI_API_URL"))
                .header("Authorization", "Bearer " + dotenv.get("OPENAI_API_KEY"))
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);
        
        return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }

    private String buildAutomationScriptPrompt(TestCaseInfo testCase, String baseUrl, String scriptLanguage) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("Generate a complete ").append(scriptLanguage).append(" automation script for the following test case:\n\n");
        prompt.append("Test Case ID: ").append(testCase.getId()).append("\n");
        prompt.append("Scenario: ").append(testCase.getScenario()).append("\n");
        prompt.append("Base URL: ").append(baseUrl).append("\n\n");
        
        prompt.append("Test Steps:\n");
        for (int i = 0; i < testCase.getSteps().size(); i++) {
            prompt.append((i + 1)).append(". ").append(testCase.getSteps().get(i)).append("\n");
        }
        
        prompt.append("\nExpected Result: ").append(testCase.getExpectedResult()).append("\n\n");
        
        if (scriptLanguage.equalsIgnoreCase("JAVA_SELENIUM")) {
            prompt.append(getJavaSeleniumTemplate());
        } else if (scriptLanguage.equalsIgnoreCase("PYTHON_SELENIUM")) {
            prompt.append(getPythonSeleniumTemplate());
        } else {
            prompt.append("Generate the script using best practices for ").append(scriptLanguage).append(" automation framework.");
        }
        
        prompt.append("\n\nRequirements:");
        prompt.append("\n- Include proper error handling");
        prompt.append("\n- Add explicit waits for better stability");
        prompt.append("\n- Include assertions to verify expected results");
        prompt.append("\n- Add comments explaining each step");
        prompt.append("\n- Include screenshot capture on failure");
        prompt.append("\n- Make the script ready to execute");
        
        return prompt.toString();
    }

    private String getJavaSeleniumTemplate() {
        return "\n\nPlease generate a Java Selenium script following this template structure:\n\n" +
                "```java\n" +
                "import org.openqa.selenium.WebDriver;\n" +
                "import org.openqa.selenium.chrome.ChromeDriver;\n" +
                "import org.openqa.selenium.By;\n" +
                "import org.openqa.selenium.WebElement;\n" +
                "import org.openqa.selenium.support.ui.WebDriverWait;\n" +
                "import org.openqa.selenium.support.ui.ExpectedConditions;\n" +
                "import org.testng.Assert;\n" +
                "import org.testng.annotations.*;\n" +
                "import io.github.bonigarcia.wdm.WebDriverManager;\n" +
                "import java.time.Duration;\n\n" +
                "public class [TestCaseClassName] {\n" +
                "    private WebDriver driver;\n" +
                "    private WebDriverWait wait;\n\n" +
                "    @BeforeMethod\n" +
                "    public void setUp() {\n" +
                "        WebDriverManager.chromedriver().setup();\n" +
                "        driver = new ChromeDriver();\n" +
                "        wait = new WebDriverWait(driver, Duration.ofSeconds(10));\n" +
                "        driver.manage().window().maximize();\n" +
                "    }\n\n" +
                "    @Test\n" +
                "    public void [testMethodName]() {\n" +
                "        // Test implementation here\n" +
                "    }\n\n" +
                "    @AfterMethod\n" +
                "    public void tearDown() {\n" +
                "        if (driver != null) {\n" +
                "            driver.quit();\n" +
                "        }\n" +
                "    }\n" +
                "}\n" +
                "```";
    }

    private String getPythonSeleniumTemplate() {
        return "\n\nPlease generate a Python Selenium script following this template structure:\n\n" +
                "```python\n" +
                "import pytest\n" +
                "from selenium import webdriver\n" +
                "from selenium.webdriver.common.by import By\n" +
                "from selenium.webdriver.support.ui import WebDriverWait\n" +
                "from selenium.webdriver.support import expected_conditions as EC\n" +
                "from selenium.webdriver.chrome.service import Service\n" +
                "from webdriver_manager.chrome import ChromeDriverManager\n\n" +
                "class Test[TestCaseClassName]:\n\n" +
                "    def setup_method(self):\n" +
                "        service = Service(ChromeDriverManager().install())\n" +
                "        self.driver = webdriver.Chrome(service=service)\n" +
                "        self.wait = WebDriverWait(self.driver, 10)\n" +
                "        self.driver.maximize_window()\n\n" +
                "    def test_[test_method_name](self):\n" +
                "        # Test implementation here\n" +
                "        pass\n\n" +
                "    def teardown_method(self):\n" +
                "        if self.driver:\n" +
                "            self.driver.quit()\n" +
                "```";
    }

    private List<TestCaseInfo> parseTestCases(String testCasesContent) {
        List<TestCaseInfo> testCases = new ArrayList<>();
        
        // Parse test cases using regex patterns
        Pattern testCasePattern = Pattern.compile("- Test Case ID: (TC\\d+)\\s*- Scenario: ([^\\n]+)\\s*- Steps:\\s*([^-]*?)- Expected Result: ([^\\n-]+)", 
                                                Pattern.DOTALL | Pattern.MULTILINE);
        
        Matcher matcher = testCasePattern.matcher(testCasesContent);
        
        while (matcher.find()) {
            String id = matcher.group(1).trim();
            String scenario = matcher.group(2).trim();
            String stepsText = matcher.group(3).trim();
            String expectedResult = matcher.group(4).trim();
            
            // Parse individual steps
            List<String> steps = new ArrayList<>();
            Pattern stepPattern = Pattern.compile("\\d+\\. ([^\\n]+)");
            Matcher stepMatcher = stepPattern.matcher(stepsText);
            
            while (stepMatcher.find()) {
                steps.add(stepMatcher.group(1).trim());
            }
            
            TestCaseInfo testCase = new TestCaseInfo(id, scenario, steps, expectedResult);
            testCases.add(testCase);
        }
        
        return testCases;
    }

    // Inner class for test case information
    private static class TestCaseInfo {
        private String id;
        private String scenario;
        private List<String> steps;
        private String expectedResult;
        
        public TestCaseInfo(String id, String scenario, List<String> steps, String expectedResult) {
            this.id = id;
            this.scenario = scenario;
            this.steps = steps;
            this.expectedResult = expectedResult;
        }
        
        // Getters
        public String getId() { return id; }
        public String getScenario() { return scenario; }
        public List<String> getSteps() { return steps; }
        public String getExpectedResult() { return expectedResult; }
    }
} 