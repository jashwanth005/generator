package com.testcasesgenerator.generator.Services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebElement;

import java.time.Duration;
import java.util.*;

@Service
public class WebsiteAnalyzerService {
    
    private static final int WAIT_TIMEOUT_SECONDS = 10;

    public static class WebPageElement {
        private String elementType;
        private String identifier;
        private String text;
        private Map<String, String> attributes;
        private String xpath;

        public WebPageElement(String elementType, String identifier, String text, Map<String, String> attributes, String xpath) {
            this.elementType = elementType;
            this.identifier = identifier;
            this.text = text;
            this.attributes = attributes;
            this.xpath = xpath;
        }

        // Getters
        public String getElementType() { return elementType; }
        public String getIdentifier() { return identifier; }
        public String getText() { return text; }
        public Map<String, String> getAttributes() { return attributes; }
        public String getXpath() { return xpath; }
    }

    public List<WebPageElement> analyzeWebsite(String url) {
        WebDriver driver = null;
        List<WebPageElement> elements = new ArrayList<>();
        
        try {
            driver = setupWebDriver();
            driver.get(url);
            
            // Wait for page to load
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(WAIT_TIMEOUT_SECONDS));
            wait.until(webDriver -> ((org.openqa.selenium.JavascriptExecutor) webDriver)
                .executeScript("return document.readyState").equals("complete"));

            // Find interactive elements
            elements.addAll(findInteractiveElements(driver));
            
            // Find form elements
            elements.addAll(findFormElements(driver));
            
            // Find navigation elements
            elements.addAll(findNavigationElements(driver));

            return elements;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    private WebDriver setupWebDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        return new ChromeDriver(options);
    }

    private List<WebPageElement> findInteractiveElements(WebDriver driver) {
        List<WebPageElement> elements = new ArrayList<>();
        
        // Find buttons - including those without explicit type
        List<WebElement> buttons = driver.findElements(By.tagName("button"));
        buttons.addAll(driver.findElements(By.cssSelector("input[type='button']")));
        buttons.addAll(driver.findElements(By.cssSelector("input[type='submit']")));
        buttons.addAll(driver.findElements(By.cssSelector("input[type='image']")));
        
        for (WebElement button : buttons) {
            elements.add(createWebPageElement("button", button));
        }
        
        // Find links
        List<WebElement> links = driver.findElements(By.tagName("a"));
        for (WebElement link : links) {
            elements.add(createWebPageElement("link", link));
        }
        
        // Find divs and spans that might act as buttons (common in modern web apps)
        List<WebElement> clickableDivs = driver.findElements(By.cssSelector("div[role='button'], div[onclick], div[class*='btn'], div[class*='button']"));
        for (WebElement div : clickableDivs) {
            elements.add(createWebPageElement("button", div));
        }
        
        List<WebElement> clickableSpans = driver.findElements(By.cssSelector("span[role='button'], span[onclick], span[class*='btn'], span[class*='button']"));
        for (WebElement span : clickableSpans) {
            elements.add(createWebPageElement("button", span));
        }
        
        return elements;
    }

    private List<WebPageElement> findFormElements(WebDriver driver) {
        List<WebPageElement> elements = new ArrayList<>();
        
        // Find all forms first
        List<WebElement> forms = driver.findElements(By.tagName("form"));
        for (WebElement form : forms) {
            // Find input fields within the form
            List<WebElement> inputs = form.findElements(By.cssSelector("input:not([type='hidden']):not([type='submit']):not([type='button'])"));
            for (WebElement input : inputs) {
                elements.add(createWebPageElement("input", input, form));
            }
            
            // Find select dropdowns within the form
            List<WebElement> selects = form.findElements(By.tagName("select"));
            for (WebElement select : selects) {
                elements.add(createWebPageElement("select", select, form));
            }
            
            // Find textareas within the form
            List<WebElement> textareas = form.findElements(By.tagName("textarea"));
            for (WebElement textarea : textareas) {
                elements.add(createWebPageElement("textarea", textarea, form));
            }
            
            // Find all buttons within the form (including those without explicit type)
            List<WebElement> buttons = form.findElements(By.tagName("button"));
            buttons.addAll(form.findElements(By.cssSelector("input[type='submit']")));
            buttons.addAll(form.findElements(By.cssSelector("input[type='button']")));
            
            for (WebElement button : buttons) {
                elements.add(createWebPageElement("button", button, form));
            }
            
            // Find links within forms that might act as submit buttons
            List<WebElement> formLinks = form.findElements(By.tagName("a"));
            for (WebElement link : formLinks) {
                elements.add(createWebPageElement("link", link, form));
            }
        }
        
        // Also look for elements outside of forms
        // Find all input fields that accept text
        List<WebElement> standaloneInputs = driver.findElements(By.cssSelector(
            "input:not(form input):not([type='hidden']):not([type='submit']):not([type='button']):not([type='checkbox']):not([type='radio'])" +
            ", input[type='text']:not(form input)" +
            ", input[type='email']:not(form input)" +
            ", input[type='password']:not(form input)" +
            ", input[type='search']:not(form input)" +
            ", input[type='tel']:not(form input)" +
            ", input[type='url']:not(form input)" +
            ", input[type='number']:not(form input)"
        ));
        for (WebElement input : standaloneInputs) {
            elements.add(createWebPageElement("input", input, null));
        }
        
        // Find all textareas
        List<WebElement> standaloneTextareas = driver.findElements(By.cssSelector("textarea:not(form textarea)"));
        for (WebElement textarea : standaloneTextareas) {
            elements.add(createWebPageElement("textarea", textarea, null));
        }
        
        // Find all select dropdowns
        List<WebElement> standaloneSelects = driver.findElements(By.cssSelector("select:not(form select)"));
        for (WebElement select : standaloneSelects) {
            elements.add(createWebPageElement("select", select, null));
        }
        
        // Find all buttons outside forms
        List<WebElement> standaloneButtons = driver.findElements(By.cssSelector("button:not(form button)"));
        standaloneButtons.addAll(driver.findElements(By.cssSelector("input[type='submit']:not(form input)")));
        standaloneButtons.addAll(driver.findElements(By.cssSelector("input[type='button']:not(form input)")));
        
        for (WebElement button : standaloneButtons) {
            elements.add(createWebPageElement("button", button, null));
        }
        
        // Find all contenteditable elements
        List<WebElement> editableElements = driver.findElements(By.cssSelector("[contenteditable='true']"));
        for (WebElement editable : editableElements) {
            elements.add(createWebPageElement("contenteditable", editable, null));
        }
        
        return elements;
    }

    private List<WebPageElement> findNavigationElements(WebDriver driver) {
        List<WebPageElement> elements = new ArrayList<>();
        
        // Find navigation menus
        List<WebElement> navElements = driver.findElements(By.tagName("nav"));
        navElements.addAll(driver.findElements(By.className("navigation")));
        navElements.addAll(driver.findElements(By.className("menu")));
        
        for (WebElement nav : navElements) {
            elements.add(createWebPageElement("navigation", nav));
        }
        
        return elements;
    }

    private WebPageElement createWebPageElement(String type, WebElement element, WebElement form) {
        String identifier = generateIdentifier(element, form);
        String text = element.getText();
        Map<String, String> attributes = extractAttributes(element);
        String xpath = generateXPath(element);
        
        // Add form-related attributes
        if (form != null) {
            attributes.put("form", form.getAttribute("id"));
            attributes.put("form-name", form.getAttribute("name"));
        }
        
        return new WebPageElement(type, identifier, text, attributes, xpath);
    }

    private WebPageElement createWebPageElement(String type, WebElement element) {
        String identifier = generateIdentifier(element, null);
        String text = element.getText();
        Map<String, String> attributes = extractAttributes(element);
        String xpath = generateXPath(element);
        
        return new WebPageElement(type, identifier, text, attributes, xpath);
    }

    private String generateIdentifier(WebElement element, WebElement form) {
        // Try to get the most reliable identifier
        String id = element.getAttribute("id");
        if (id != null && !id.isEmpty()) {
            return "id:" + id;
        }
        
        String name = element.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            return "name:" + name;
        }
        
        // Try CSS selector with multiple attributes
        String type = element.getAttribute("type");
        String placeholder = element.getAttribute("placeholder");
        String ariaLabel = element.getAttribute("aria-label");
        String role = element.getAttribute("role");
        String dataTestId = element.getAttribute("data-testid");
        
        // Build a unique CSS selector based on available attributes
        StringBuilder cssSelector = new StringBuilder();
        
        if (form != null) {
            String formId = form.getAttribute("id");
            String formName = form.getAttribute("name");
            if (formId != null && !formId.isEmpty()) {
                cssSelector.append("#").append(formId).append(" ");
            } else if (formName != null && !formName.isEmpty()) {
                cssSelector.append("form[name='").append(formName).append("'] ");
            }
        }
        
        cssSelector.append(element.getTagName());
        
        if (type != null && !type.isEmpty()) {
            cssSelector.append("[type='").append(type).append("']");
        }
        if (placeholder != null && !placeholder.isEmpty()) {
            cssSelector.append("[placeholder='").append(placeholder).append("']");
        }
        if (ariaLabel != null && !ariaLabel.isEmpty()) {
            cssSelector.append("[aria-label='").append(ariaLabel).append("']");
        }
        if (role != null && !role.isEmpty()) {
            cssSelector.append("[role='").append(role).append("']");
        }
        if (dataTestId != null && !dataTestId.isEmpty()) {
            cssSelector.append("[data-testid='").append(dataTestId).append("']");
        }
        
        if (cssSelector.length() > element.getTagName().length()) {
            return "css:" + cssSelector.toString();
        }
        
        // Try to find a unique class
        String className = element.getAttribute("class");
        if (className != null && !className.isEmpty()) {
            String[] classes = className.split("\\s+");
            // Look for classes that might be unique identifiers
            for (String cls : classes) {
                if (cls.contains("input") || cls.contains("field") || cls.contains("control")) {
                    return "css:." + cls;
                }
            }
            // Fallback to first class
            return "css:." + classes[0];
        }
        
        // Fallback to XPath with index
        return "xpath:" + generateXPath(element);
    }

    private String generateIdentifier(WebElement element) {
        // Try to get the most reliable identifier
        String id = element.getAttribute("id");
        if (id != null && !id.isEmpty()) {
            return "id:" + id;
        }
        
        String name = element.getAttribute("name");
        if (name != null && !name.isEmpty()) {
            return "name:" + name;
        }
        
        String className = element.getAttribute("class");
        if (className != null && !className.isEmpty()) {
            return "class:" + className;
        }
        
        return "xpath:" + generateXPath(element);
    }

    private Map<String, String> extractAttributes(WebElement element) {
        Map<String, String> attributes = new HashMap<>();
        
        // Common attributes to extract
        String[] attributesToExtract = {
            "id", "name", "class", "type", "value", "href", "placeholder",
            "aria-label", "title", "role", "data-testid", "required",
            "pattern", "minlength", "maxlength", "min", "max",
            "autocomplete", "form", "formaction", "formmethod",
            "contenteditable", "aria-required", "aria-invalid",
            "aria-describedby", "aria-labelledby", "tabindex",
            "data-*"  // Will be handled specially
        };
        
        for (String attr : attributesToExtract) {
            if (attr.equals("data-*")) {
                // Extract all data-* attributes
                String script = "var items = {}; for (var i = 0; i < arguments[0].attributes.length; i++) { " +
                              "var attr = arguments[0].attributes[i]; " +
                              "if (attr.name.startsWith('data-')) { items[attr.name] = attr.value; } }; " +
                              "return items;";
                try {
                    Map<String, String> dataAttrs = (Map<String, String>) ((JavascriptExecutor) ((RemoteWebElement) element).getWrappedDriver())
                        .executeScript(script, element);
                    attributes.putAll(dataAttrs);
                } catch (Exception e) {
                    System.err.println("Failed to extract data-* attributes: " + e.getMessage());
                }
            } else {
                String value = element.getAttribute(attr);
                if (value != null && !value.isEmpty()) {
                    attributes.put(attr, value);
                }
            }
        }
        
        // Check if element is required
        if (element.getAttribute("required") != null || 
            "true".equals(element.getAttribute("aria-required"))) {
            attributes.put("required", "true");
        }
        
        // Check if element is disabled
        if (element.getAttribute("disabled") != null || 
            "true".equals(element.getAttribute("aria-disabled"))) {
            attributes.put("disabled", "true");
        }
        
        // Check if element is readonly
        if (element.getAttribute("readonly") != null || 
            "true".equals(element.getAttribute("aria-readonly"))) {
            attributes.put("readonly", "true");
        }
        
        // Get computed styles that might be relevant
        String[] stylesToExtract = {"display", "visibility", "opacity"};
        try {
            String script = "var styles = window.getComputedStyle(arguments[0]); " +
                          "var items = {}; " +
                          "for (var i = 0; i < arguments[1].length; i++) { " +
                          "var style = arguments[1][i]; " +
                          "items[style] = styles.getPropertyValue(style); }; " +
                          "return items;";
            Map<String, String> computedStyles = (Map<String, String>) ((JavascriptExecutor) ((RemoteWebElement) element).getWrappedDriver())
                .executeScript(script, element, stylesToExtract);
            for (Map.Entry<String, String> style : computedStyles.entrySet()) {
                attributes.put("style-" + style.getKey(), style.getValue());
            }
        } catch (Exception e) {
            System.err.println("Failed to extract computed styles: " + e.getMessage());
        }
        
        return attributes;
    }

    private String generateXPath(WebElement element) {
        // This is a simplified XPath generation
        try {
            return (String) ((JavascriptExecutor) ((RemoteWebElement) element).getWrappedDriver()).executeScript(
                "function getXPath(element) {" +
                "   if (element.id !== '') return `//*[@id=\"${element.id}\"]`;" +
                "   if (element === document.body) return '/html/body';" +
                "   let ix = 1, siblings = element.parentNode.childNodes;" +
                "   for (let sibling of siblings) {" +
                "       if (sibling === element) return getXPath(element.parentNode) + '/' + element.tagName.toLowerCase() + '[' + ix + ']';" +
                "       if (sibling.nodeType === 1 && sibling.tagName === element.tagName) ix++;" +
                "   }" +
                "}" +
                "return getXPath(arguments[0]);", element
            );
        } catch (Exception e) {
            // Fallback to a simpler XPath generation
            try {
                String id = element.getAttribute("id");
                if (id != null && !id.isEmpty()) {
                    return "//*[@id='" + id + "']";
                }
                
                String name = element.getAttribute("name");
                if (name != null && !name.isEmpty()) {
                    return "//*[@name='" + name + "']";
                }
                
                // Get tag name and position
                String tagName = element.getTagName();
                List<WebElement> siblings = element.findElements(By.xpath("../" + tagName));
                int position = 1;
                for (WebElement sibling : siblings) {
                    if (sibling.equals(element)) {
                        break;
                    }
                    position++;
                }
                return "//" + tagName + "[" + position + "]";
            } catch (Exception fallbackError) {
                return "";
            }
        }
    }
} 