package com.testcasesgenerator.generator;


import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

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

    public static void main(String[] args) {
        SpringApplication.run(JiraOpenAIIntegrationApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        final Dotenv dotenv = Dotenv.load();
        String ticketId = dotenv.get("ticketId"); // Fetch from environment

        // Fetch Jira Ticket
        var issue = jiraService.fetchJiraTicket(ticketId);
        String title = issue.getSummary();
        String description = issue.getDescription();
        System.out.println("Title: " + title);
        System.out.println("description: " + description);
        // Generate Test Cases using OpenAI
        String testCases = openAIService.generateTestCasesWithOpenAI(title, description);

        // Save Test Cases to Excel
        excelService.saveTestCasesToExcel(testCases, "test_cases.xlsx");
    }
}

