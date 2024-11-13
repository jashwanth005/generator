package com.testcasesgenerator.generator.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.atlassian.jira.rest.client.api.domain.Issue;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testcasesgenerator.generator.DataCleaner.DataCleaner;
import com.testcasesgenerator.generator.Services.ExcelService;
import com.testcasesgenerator.generator.Services.JiraService;
import com.testcasesgenerator.generator.Services.OpenAIService;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
public class GetTestCasesController {


 
    private  JiraService jiraService;
  
    private  OpenAIService openAIService;
    
    private ExcelService excelService;

    private DataCleaner dataCleaner;

    public GetTestCasesController(JiraService jiraService, OpenAIService openAIService, ExcelService excelService, DataCleaner dataCleaner) {
        this.jiraService = jiraService;
        this.openAIService = openAIService;
        this.excelService = excelService;
        this.dataCleaner = dataCleaner;
    }
     ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
@GetMapping("/getTestCases")
public ObjectNode getMethodName(@RequestParam String ticketId) {
try {
    
// final Dotenv dotenv = Dotenv.load();
//        String ticketId = dotenv.get("ticketId"); 
        var issue = jiraService.fetchJiraTicket(ticketId);
        String title =dataCleaner.cleanSensitiveData(issue.getSummary());
        //issue.getSummary();
        String description =dataCleaner.cleanSensitiveData(issue.getDescription());
        // if(description == null){
        //     jsonObject.put("error", "status");
        //     jsonObject.put("message", "Please add a description for the test case");
        //     return jsonObject;
        // }
        // issue.getDescription();
        System.out.println("Title: " + title);
        System.out.println("description: " + description);
       String testCases = openAIService.generateTestCasesWithOpenAI(title, description);
       excelService.saveTestCasesToExcel(testCases, "test_cases_" + ticketId + ".xlsx");
       String filePath = "test_cases_" + ticketId + ".xlsx";
       jsonObject.put("sucess", "status");
            jsonObject.put("message", "Test cases generated and saved to: "+ filePath+"");
            return jsonObject;
    } catch (Exception e) {
        jsonObject.put("error", "status");
            jsonObject.put("message", "some erroe occoured: " + e.getMessage()+"");
            return jsonObject;
    }
}
}
