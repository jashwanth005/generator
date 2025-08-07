package com.testcasesgenerator.generator.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.testcasesgenerator.generator.Services.WebsiteAnalyzerService.WebPageElement;
import java.util.*;
import java.util.Optional;

@Service
public class SmartTestCaseGeneratorService {

    @Autowired
    private WebsiteAnalyzerService websiteAnalyzerService;

    @Autowired
    private ToqanAiService toqanAiService;

    public static class TestCase {
        private String id;
        private String title;
        private String description;
        private List<TestStep> steps;
        private Map<String, String> testData;

        public TestCase(String id, String title, String description, List<TestStep> steps, Map<String, String> testData) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.steps = steps;
            this.testData = testData;
        }

        // Getters
        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public List<TestStep> getSteps() { return steps; }
        public Map<String, String> getTestData() { return testData; }
    }

    public static class TestStep {
        private int stepNumber;
        private String action;
        private String element;
        private String expectedResult;
        private Map<String, String> data;

        public TestStep(int stepNumber, String action, String element, String expectedResult, Map<String, String> data) {
            this.stepNumber = stepNumber;
            this.action = action;
            this.element = element;
            this.expectedResult = expectedResult;
            this.data = data;
        }

        // Getters
        public int getStepNumber() { return stepNumber; }
        public String getAction() { return action; }
        public String getElement() { return element; }
        public String getExpectedResult() { return expectedResult; }
        public Map<String, String> getData() { return data; }
    }

    public List<TestCase> generateSmartTestCases(String url, String ticketId) {
        // First analyze the website
        List<WebPageElement> elements = websiteAnalyzerService.analyzeWebsite(url);
        
        // Generate test cases based on the elements found
        List<TestCase> testCases = new ArrayList<>();
        
        // Generate form submission test cases
        testCases.addAll(generateFormTestCases(elements, ticketId));
        
        // Generate navigation test cases
        testCases.addAll(generateNavigationTestCases(elements, ticketId));
        
        // Generate validation test cases
        testCases.addAll(generateValidationTestCases(elements, ticketId));
        
        return testCases;
    }

    private List<TestCase> generateFormTestCases(List<WebPageElement> elements, String ticketId) {
        List<TestCase> testCases = new ArrayList<>();
        Map<String, List<WebPageElement>> forms = groupFormElements(elements);
        
        for (Map.Entry<String, List<WebPageElement>> form : forms.entrySet()) {
            // Generate positive test case
            testCases.add(generatePositiveFormTestCase(form.getValue(), ticketId));
            
            // Generate negative test cases
            testCases.addAll(generateNegativeFormTestCases(form.getValue(), ticketId));
        }
        
        return testCases;
    }

    private Map<String, List<WebPageElement>> groupFormElements(List<WebPageElement> elements) {
        Map<String, List<WebPageElement>> forms = new HashMap<>();
        
        // Group form elements by their closest form ancestor or create virtual forms based on proximity
        for (WebPageElement element : elements) {
            if (isFormElement(element)) {
                String formId = element.getAttributes().getOrDefault("form", "default_form");
                forms.computeIfAbsent(formId, k -> new ArrayList<>()).add(element);
            }
        }
        
        return forms;
    }

    private boolean isFormElement(WebPageElement element) {
        return element.getElementType().equals("input") ||
               element.getElementType().equals("select") ||
               element.getElementType().equals("textarea") ||
               (element.getElementType().equals("button") && 
                element.getAttributes().getOrDefault("type", "").equals("submit"));
    }

    private TestCase generatePositiveFormTestCase(List<WebPageElement> formElements, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "positive_form");
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        int stepNumber = 1;
        
        // Fill all form fields with valid data
        for (WebPageElement element : formElements) {
            if (!element.getElementType().equals("button")) {
                String value = generateTestDataForElement(element);
                testData.put(element.getIdentifier(), value);
                
                steps.add(new TestStep(
                    stepNumber++,
                    "Enter valid data",
                    element.getIdentifier(),
                    "Data should be entered successfully",
                    Collections.singletonMap("value", value)
                ));
            }
        }
        
        // Add form submission step - handle cases where no submit button is found
        try {
            WebPageElement submitButton = findSubmitButton(formElements);
            steps.add(new TestStep(
                stepNumber,
                "Submit form",
                submitButton.getIdentifier(),
                "Form should be submitted successfully",
                new HashMap<>()
            ));
        } catch (RuntimeException e) {
            // If no submit button found, create a generic form submission step
            steps.add(new TestStep(
                stepNumber,
                "Submit form",
                "form-submission", // Generic identifier for form submission
                "Form should be submitted successfully using available submission method",
                new HashMap<>()
            ));
        }
        
        return new TestCase(
            testCaseId,
            "Positive Form Submission Test",
            "Verify form submission with valid data",
            steps,
            testData
        );
    }

    private List<TestCase> generateNegativeFormTestCases(List<WebPageElement> formElements, String ticketId) {
        List<TestCase> testCases = new ArrayList<>();
        
        // Generate a test case for each required field being empty
        for (WebPageElement element : formElements) {
            if (isRequiredField(element)) {
                testCases.add(generateEmptyFieldTestCase(element, formElements, ticketId));
            }
        }
        
        // Generate test cases for invalid data formats
        testCases.addAll(generateInvalidFormatTestCases(formElements, ticketId));
        
        return testCases;
    }

    private boolean isRequiredField(WebPageElement element) {
        return element.getAttributes().containsKey("required") ||
               element.getAttributes().getOrDefault("aria-required", "false").equals("true");
    }

    private TestCase generateEmptyFieldTestCase(WebPageElement targetElement, List<WebPageElement> formElements, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "empty_" + targetElement.getElementType());
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        int stepNumber = 1;
        
        // Fill all fields except the target field
        for (WebPageElement element : formElements) {
            if (!element.equals(targetElement) && !element.getElementType().equals("button")) {
                String value = generateTestDataForElement(element);
                testData.put(element.getIdentifier(), value);
                
                steps.add(new TestStep(
                    stepNumber++,
                    "Enter valid data",
                    element.getIdentifier(),
                    "Data should be entered successfully",
                    Collections.singletonMap("value", value)
                ));
            }
        }
        
        // Add form submission step
        try {
            WebPageElement submitButton = findSubmitButton(formElements);
            steps.add(new TestStep(
                stepNumber,
                "Submit form",
                submitButton.getIdentifier(),
                "Form submission should fail with validation error",
                new HashMap<>()
            ));
        } catch (RuntimeException e) {
            // If no submit button found, create a generic form submission step
            steps.add(new TestStep(
                stepNumber,
                "Submit form",
                "form-submission", // Generic identifier for form submission
                "Form submission should fail with validation error",
                new HashMap<>()
            ));
        }
        
        return new TestCase(
            testCaseId,
            "Empty Required Field Test - " + targetElement.getElementType(),
            "Verify form validation when required field is empty",
            steps,
            testData
        );
    }

    private List<TestCase> generateNavigationTestCases(List<WebPageElement> elements, String ticketId) {
        List<TestCase> testCases = new ArrayList<>();
        
        // Find all navigation elements
        List<WebPageElement> navElements = elements.stream()
            .filter(e -> e.getElementType().equals("navigation") || e.getElementType().equals("link"))
            .toList();
        
        // Generate test case for each navigation path
        for (WebPageElement navElement : navElements) {
            testCases.add(generateNavigationTestCase(navElement, ticketId));
        }
        
        return testCases;
    }

    private List<TestCase> generateValidationTestCases(List<WebPageElement> elements, String ticketId) {
        List<TestCase> testCases = new ArrayList<>();
        
        // Find all input elements that might need validation
        List<WebPageElement> inputElements = elements.stream()
            .filter(e -> e.getElementType().equals("input") || e.getElementType().equals("textarea"))
            .toList();
        
        // Generate validation test cases for each input
        for (WebPageElement input : inputElements) {
            testCases.addAll(generateInputValidationTestCases(input, ticketId));
        }
        
        return testCases;
    }

    private String generateTestCaseId(String ticketId, String suffix) {
        return ticketId + "_" + suffix + "_" + UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateTestDataForElement(WebPageElement element) {
        String type = element.getAttributes().getOrDefault("type", "text");
        
        switch (type.toLowerCase()) {
            case "email":
                return "test@example.com";
            case "password":
                return "Test@123";
            case "number":
                return "12345";
            case "tel":
                return "+1234567890";
            case "date":
                return "2024-01-01";
            default:
                return "Test Input";
        }
    }

    private WebPageElement findSubmitButton(List<WebPageElement> formElements) {
        // First, try to find explicit submit buttons
        Optional<WebPageElement> submitButton = formElements.stream()
            .filter(e -> e.getElementType().equals("button") &&
                        e.getAttributes().getOrDefault("type", "").equals("submit"))
            .findFirst();
        
        if (submitButton.isPresent()) {
            return submitButton.get();
        }
        
        // Look for buttons that might act as submit buttons (common patterns)
        Optional<WebPageElement> potentialSubmitButton = formElements.stream()
            .filter(e -> e.getElementType().equals("button"))
            .filter(e -> {
                String text = e.getText().toLowerCase();
                String className = e.getAttributes().getOrDefault("class", "").toLowerCase();
                String id = e.getAttributes().getOrDefault("id", "").toLowerCase();
                String name = e.getAttributes().getOrDefault("name", "").toLowerCase();
                
                // Common submit button patterns
                return text.contains("submit") || text.contains("save") || text.contains("send") ||
                       text.contains("login") || text.contains("sign") || text.contains("pay") ||
                       text.contains("continue") || text.contains("next") || text.contains("proceed") ||
                       className.contains("submit") || className.contains("btn-primary") ||
                       className.contains("btn-success") || className.contains("btn-submit") ||
                       id.contains("submit") || name.contains("submit");
            })
            .findFirst();
        
        if (potentialSubmitButton.isPresent()) {
            return potentialSubmitButton.get();
        }
        
        // Look for any button in the form (fallback)
        Optional<WebPageElement> anyButton = formElements.stream()
            .filter(e -> e.getElementType().equals("button"))
            .findFirst();
        
        if (anyButton.isPresent()) {
            return anyButton.get();
        }
        
        // Look for links that might act as submit buttons
        Optional<WebPageElement> submitLink = formElements.stream()
            .filter(e -> e.getElementType().equals("link"))
            .filter(e -> {
                String text = e.getText().toLowerCase();
                String className = e.getAttributes().getOrDefault("class", "").toLowerCase();
                return text.contains("submit") || text.contains("save") || text.contains("send") ||
                       text.contains("login") || text.contains("sign") || text.contains("pay") ||
                       className.contains("submit") || className.contains("btn-primary");
            })
            .findFirst();
        
        if (submitLink.isPresent()) {
            return submitLink.get();
        }
        
        // Look for any clickable element that might submit the form
        Optional<WebPageElement> anyClickable = formElements.stream()
            .filter(e -> e.getElementType().equals("link") || e.getElementType().equals("button"))
            .findFirst();
        
        if (anyClickable.isPresent()) {
            return anyClickable.get();
        }
        
        // If no submit button is found, create a generic form submission step
        // This will be handled by the test execution service to find any clickable element
        throw new RuntimeException("No submit button found in form - form may use JavaScript submission or different patterns");
    }

    private List<TestCase> generateInputValidationTestCases(WebPageElement input, String ticketId) {
        List<TestCase> testCases = new ArrayList<>();
        String type = input.getAttributes().getOrDefault("type", "text");
        
        switch (type.toLowerCase()) {
            case "email":
                testCases.add(generateInvalidEmailTestCase(input, ticketId));
                break;
            case "password":
                testCases.add(generateWeakPasswordTestCase(input, ticketId));
                break;
            case "number":
                testCases.add(generateInvalidNumberTestCase(input, ticketId));
                break;
            case "tel":
                testCases.add(generateInvalidPhoneTestCase(input, ticketId));
                break;
        }
        
        return testCases;
    }

    private TestCase generateInvalidEmailTestCase(WebPageElement input, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "invalid_email");
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        
        steps.add(new TestStep(
            1,
            "Enter invalid email",
            input.getIdentifier(),
            "Validation error should be shown",
            Collections.singletonMap("value", "invalid.email")
        ));
        
        return new TestCase(
            testCaseId,
            "Invalid Email Format Test",
            "Verify email validation",
            steps,
            testData
        );
    }

    private TestCase generateWeakPasswordTestCase(WebPageElement input, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "weak_password");
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        
        steps.add(new TestStep(
            1,
            "Enter weak password",
            input.getIdentifier(),
            "Password strength validation error should be shown",
            Collections.singletonMap("value", "weak")
        ));
        
        return new TestCase(
            testCaseId,
            "Weak Password Test",
            "Verify password strength validation",
            steps,
            testData
        );
    }

    private TestCase generateInvalidNumberTestCase(WebPageElement input, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "invalid_number");
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        
        steps.add(new TestStep(
            1,
            "Enter invalid number",
            input.getIdentifier(),
            "Number validation error should be shown",
            Collections.singletonMap("value", "abc")
        ));
        
        return new TestCase(
            testCaseId,
            "Invalid Number Test",
            "Verify number validation",
            steps,
            testData
        );
    }

    private TestCase generateInvalidPhoneTestCase(WebPageElement input, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "invalid_phone");
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        
        steps.add(new TestStep(
            1,
            "Enter invalid phone number",
            input.getIdentifier(),
            "Phone number validation error should be shown",
            Collections.singletonMap("value", "invalid-phone")
        ));
        
        return new TestCase(
            testCaseId,
            "Invalid Phone Number Test",
            "Verify phone number validation",
            steps,
            testData
        );
    }

    private TestCase generateNavigationTestCase(WebPageElement navElement, String ticketId) {
        String testCaseId = generateTestCaseId(ticketId, "navigation");
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        
        steps.add(new TestStep(
            1,
            "Click navigation element",
            navElement.getIdentifier(),
            "Page should navigate successfully",
            Collections.singletonMap("href", navElement.getAttributes().getOrDefault("href", ""))
        ));
        
        return new TestCase(
            testCaseId,
            "Navigation Test - " + navElement.getText(),
            "Verify navigation functionality",
            steps,
            testData
        );
    }

    private List<TestCase> generateInvalidFormatTestCases(List<WebPageElement> formElements, String ticketId) {
        List<TestCase> testCases = new ArrayList<>();
        
        for (WebPageElement element : formElements) {
            String type = element.getAttributes().getOrDefault("type", "text");
            if (type.equals("email") || type.equals("number") || type.equals("tel")) {
                testCases.add(generateInvalidFormatTestCase(element, formElements, ticketId));
            }
        }
        
        return testCases;
    }

    private TestCase generateInvalidFormatTestCase(WebPageElement element, List<WebPageElement> formElements, String ticketId) {
        String type = element.getAttributes().getOrDefault("type", "text");
        String testCaseId = generateTestCaseId(ticketId, "invalid_format_" + type);
        List<TestStep> steps = new ArrayList<>();
        Map<String, String> testData = new HashMap<>();
        int stepNumber = 1;
        
        // Fill other fields with valid data
        for (WebPageElement formElement : formElements) {
            if (!formElement.equals(element) && !formElement.getElementType().equals("button")) {
                String value = generateTestDataForElement(formElement);
                testData.put(formElement.getIdentifier(), value);
                
                steps.add(new TestStep(
                    stepNumber++,
                    "Enter valid data",
                    formElement.getIdentifier(),
                    "Data should be entered successfully",
                    Collections.singletonMap("value", value)
                ));
            }
        }
        
        // Add invalid data for the target field
        String invalidValue;
        switch (type) {
            case "email":
                invalidValue = "invalid.email";
                break;
            case "number":
                invalidValue = "abc";
                break;
            case "tel":
                invalidValue = "invalid-phone";
                break;
            default:
                invalidValue = "invalid-input";
                break;
        }
        
        steps.add(new TestStep(
            stepNumber++,
            "Enter invalid " + type,
            element.getIdentifier(),
            type + " format validation error should be shown",
            Collections.singletonMap("value", invalidValue)
        ));
        
        // Add form submission step
        steps.add(new TestStep(
            stepNumber,
            "Submit form",
            findSubmitButton(formElements).getIdentifier(),
            "Form submission should fail with validation error",
            new HashMap<>()
        ));
        
        return new TestCase(
            testCaseId,
            "Invalid " + type + " Format Test",
            "Verify " + type + " format validation",
            steps,
            testData
        );
    }
} 