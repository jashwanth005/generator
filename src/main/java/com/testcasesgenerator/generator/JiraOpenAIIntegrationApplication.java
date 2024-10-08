package com.testcasesgenerator.generator;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.testcasesgenerator.generator.DataCleaner.DataCleaner;
import com.testcasesgenerator.generator.Services.ExcelService;
import com.testcasesgenerator.generator.Services.JiraService;
import com.testcasesgenerator.generator.Services.OpenAIService;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
public class JiraOpenAIIntegrationApplication implements CommandLineRunner {

    @Autowired
    private JiraService jiraService;
    
    @Autowired
    private OpenAIService openAIService;
    
    @Autowired
    private ExcelService excelService;
    @Autowired
     private DataCleaner dataCleaner;

    public static void main(String[] args) {
        SpringApplication.run(JiraOpenAIIntegrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Dotenv dotenv = Dotenv.load();
        String ticketId = dotenv.get("ticketId"); 

        var issue = jiraService.fetchJiraTicket(ticketId);
        String title =dataCleaner.cleanSensitiveData(issue.getSummary());
        //issue.getSummary();
        String description =dataCleaner.cleanSensitiveData(issue.getDescription());
        // issue.getDescription();

        System.out.println("Title: " + title);
        System.out.println("description: " + description);
       String testCases = openAIService.generateTestCasesWithOpenAI(title, description);
       excelService.saveTestCasesToExcel(testCases, "test_cases.xlsx");
    }
}

