package com.testcasesgenerator.generator.Controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testcasesgenerator.generator.DataCleaner.DataCleaner;
import com.testcasesgenerator.generator.Services.ConfluenceService;
import com.testcasesgenerator.generator.Services.ExcelService;
import com.testcasesgenerator.generator.Services.JiraService;
import com.testcasesgenerator.generator.Services.OpenAIService;
import com.testcasesgenerator.generator.Services.RedisCacheService;
import com.testcasesgenerator.generator.Services.StorageService;
import com.testcasesgenerator.generator.Services.ToqanAiService;
import com.testcasesgenerator.generator.factory.StorageFactory;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class GetTestCasesController {

    private JiraService jiraService;
    private OpenAIService openAIService;
    private ExcelService excelService;
    private DataCleaner dataCleaner;
    private ToqanAiService toqanAiService;
    private ConfluenceService confluenceService;
    private RedisCacheService redisCacheService;
    private StorageFactory storageFactory;

    public GetTestCasesController(
        JiraService jiraService, 
        OpenAIService openAIService, 
        ExcelService excelService, 
        DataCleaner dataCleaner, 
        ToqanAiService toqanAiService, 
        ConfluenceService confluenceService,
        RedisCacheService redisCacheService,
        StorageFactory storageFactory
    ) {
        this.jiraService = jiraService;
        this.openAIService = openAIService;
        this.excelService = excelService;
        this.dataCleaner = dataCleaner;
        this.toqanAiService = toqanAiService;
        this.confluenceService = confluenceService;
        this.redisCacheService = redisCacheService;
        this.storageFactory = storageFactory;
    }
    
    @GetMapping("/getTestCases")
    public ObjectNode getMethodName(@RequestParam String ticketId) {
        // Get the configured storage service
        StorageService storageService = storageFactory.getStorageService();
        
        // Check Redis cache first
        if (redisCacheService.hasTestCaseInCache(ticketId)) {
            System.out.println("Cache hit for ticket ID: " + ticketId);
            ObjectNode cachedResponse = redisCacheService.getCachedTestCaseResponse(ticketId);
            if (cachedResponse != null) {
                return cachedResponse;
            }
        }
        
        // Check storage if not in cache
        if (storageService.exists(ticketId)) {
            System.out.println("Found in storage for ticket ID: " + ticketId);
            
            Optional<String> downloadUrlOpt = storageService.getDownloadUrl(ticketId);
            if (downloadUrlOpt.isPresent()) {
                String downloadUrl = downloadUrlOpt.get();
                
                // Create response
                ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
                jsonObject.put("success", "status");
                jsonObject.put("message", "Test cases retrieved from storage for: " + ticketId);
                jsonObject.put("downloadUrl", downloadUrl);
                
                // Cache the response
                redisCacheService.cacheTestCaseResponse(ticketId, jsonObject);
                
                return jsonObject;
            }
        }
        
        // If not in cache or storage, generate new test cases
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
        try {
            var issue = jiraService.fetchJiraTicket(ticketId);
            String title = dataCleaner.cleanSensitiveData(issue.getSummary());
            String description = dataCleaner.cleanSensitiveData(issue.getDescription());
            
            System.out.println("Title: " + title);
            System.out.println("Description: " + description);
            
            String testCases = toqanAiService.generateTestCasesWithToqanAi(title, description);
            System.out.println("Generated test cases");
            
             String filePath = "test_cases_" + ticketId + ".xlsx";
            // excelService.saveTestCasesToExcel(testCases, filePath);
            
            // Store in the configured storage (S3 or MySQL)
            String downloadUrl = storageService.storeTestCase(ticketId, filePath, title, description);
            
            jsonObject.put("success", "status");
            jsonObject.put("message", "Test cases generated and saved to: " + filePath);
            jsonObject.put("downloadUrl", downloadUrl);
            
            // Cache the response
            redisCacheService.cacheTestCaseResponse(ticketId, jsonObject);
            
            return jsonObject;
        } catch (Exception e) {
            jsonObject.put("error", "status");
            jsonObject.put("message", "An error occurred: " + e.getMessage());
            return jsonObject;
        }
    }
    
    @GetMapping("/clearCache")
    public ObjectNode clearCache(@RequestParam String ticketId) {
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
        
        try {
            redisCacheService.deleteTestCaseFromCache(ticketId);
            jsonObject.put("success", "status");
            jsonObject.put("message", "Cache cleared for ticket ID: " + ticketId);
        } catch (Exception e) {
            jsonObject.put("error", "status");
            jsonObject.put("message", "Failed to clear cache: " + e.getMessage());
        }
        
        return jsonObject;
    }
}