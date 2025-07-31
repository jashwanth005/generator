package com.testcasesgenerator.generator;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

import com.testcasesgenerator.generator.DataCleaner.DataCleaner;
import com.testcasesgenerator.generator.Services.ConfluenceService;
import com.testcasesgenerator.generator.Services.ExcelService;
import com.testcasesgenerator.generator.Services.JiraService;
import com.testcasesgenerator.generator.Services.OpenAIService;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@EnableCaching
public class JiraOpenAIIntegrationApplication implements CommandLineRunner {

    @Autowired
    private JiraService jiraService;
    
    @Autowired
    private OpenAIService openAIService;
    @Autowired
    private ExcelService excelService;
    @Autowired
     private DataCleaner dataCleaner;

     @Autowired
     private ConfluenceService confluenceService;

    public static void main(String[] args) {
        SpringApplication.run(JiraOpenAIIntegrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Dotenv dotenv = Dotenv.load();
        String ticketId = dotenv.get("ticketId"); 

        var issue = jiraService.fetchJiraTicket(ticketId);
        String title =issue.getSummary();
        //issue.getSummary();
        String description =issue.getDescription();
        //  String pageTitle = confluenceService.getPageTitle("2884796417");
        // issue.getDescription();2884796417


         System.out.println("Title: " + title);
         System.out.println("description: " + description);
        //  System.out.println(pageTitle);



        
       String testCases = openAIService.generateTestCasesWithOpenAI(title, description);
       excelService.saveTestCasesToExcel(testCases, "test_cases.xlsx");
    }
}

