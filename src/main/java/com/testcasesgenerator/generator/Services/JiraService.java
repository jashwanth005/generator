package com.testcasesgenerator.generator.Services;
import com.atlassian.jira.rest.client.api.*;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.stereotype.Service;
import java.net.URI;


@Service
public class JiraService {
private static final Dotenv dotenv = Dotenv.load();
private static final String JIRA_URL = dotenv.get("JIRA_URL");
private static final String JIRA_USERNAME = dotenv.get("JIRA_USERNAME");
private static final String JIRA_API_TOKEN = dotenv.get("JIRA_API_TOKEN");

    public Issue fetchJiraTicket(String ticketId) throws Exception {
        URI jiraServerUri = new URI(JIRA_URL);
        if (JIRA_URL == null || JIRA_URL.isEmpty()) {
            throw new IllegalArgumentException("JIRA_URL is not set correctly in the environment variables");
        }
        AsynchronousJiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        JiraRestClient jiraRestClient = factory.createWithBasicHttpAuthentication(jiraServerUri, JIRA_USERNAME, JIRA_API_TOKEN);
        Issue issue = jiraRestClient.getIssueClient().getIssue(ticketId).claim();
        jiraRestClient.close();
        return issue;
    }
}

