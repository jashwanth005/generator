package com.testcasesgenerator.generator.Services;

import com.atlassian.sal.api.net.Response;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.stereotype.Service;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
public class ConfluenceService {
    private static final Dotenv dotenv = Dotenv.load();
    private static final String CONFLUENCE_URL = dotenv.get("CONFLUENCE_URL");
    private static final String CONFLUENCE_USERNAME = dotenv.get("CONFLUENCE_USERNAME");
    private static final String CONFLUENCE_API_TOKEN = dotenv.get("CONFLUENCE_API_TOKEN");
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
 * Searches for pages by title in a specific space
 * @param spaceKey The key of the space to search in
 * @param title The title to search for
 * @return A list of page IDs matching the search criteria
 * @throws Exception If there's an error during the search
 */
public List<String> findPageIdsByTitle(String spaceKey, String title) throws Exception {
    String apiUrl = String.format("%s/rest/api/content?spaceKey=%s&title=%s&expand=version",
            CONFLUENCE_URL.endsWith("/") ? CONFLUENCE_URL.substring(0, CONFLUENCE_URL.length() - 1) : CONFLUENCE_URL,
            spaceKey, 
            java.net.URLEncoder.encode(title, "UTF-8"));
    
    CredentialsProvider provider = new BasicCredentialsProvider();
    provider.setCredentials(
            AuthScope.ANY,
            new UsernamePasswordCredentials(CONFLUENCE_USERNAME, CONFLUENCE_API_TOKEN));
    
    HttpClient httpClient = HttpClientBuilder.create()
            .setDefaultCredentialsProvider(provider)
            .build();
    
    HttpGet request = new HttpGet(apiUrl);
    request.addHeader("Accept", "application/json");
    
    HttpResponse response = httpClient.execute(request);
    HttpEntity entity = response.getEntity();
    
    if (response.getStatusLine().getStatusCode() != 200) {
        throw new IOException("Failed to search Confluence. Status: " + 
                response.getStatusLine().getStatusCode());
    }
    
    String jsonResponse = EntityUtils.toString(entity);
    JsonNode root = objectMapper.readTree(jsonResponse);
    
    List<String> pageIds = new ArrayList<>();
    JsonNode results = root.path("results");
    for (JsonNode result : results) {
        pageIds.add(result.path("id").asText());
    }
    
    return pageIds;
}


    /**
     * Fetches a Confluence page by its ID
     * @param pageId The ID of the Confluence page to fetch
     * @return A JsonNode containing the page content and metadata
     * @throws Exception If there's an error fetching the page
     */
    public JsonNode fetchConfluencePage(String pageId) throws Exception {
        if (CONFLUENCE_URL == null || CONFLUENCE_URL.isEmpty()) {
            throw new IllegalArgumentException("CONFLUENCE_URL is not set correctly in the environment variables");
        }
        
        if (CONFLUENCE_USERNAME == null || CONFLUENCE_USERNAME.isEmpty() || 
            CONFLUENCE_API_TOKEN == null || CONFLUENCE_API_TOKEN.isEmpty()) {
            throw new IllegalArgumentException("Confluence credentials are not set correctly in the environment variables");
        }
        
        // Create HTTP client with basic auth
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(CONFLUENCE_USERNAME, CONFLUENCE_API_TOKEN));
        
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCredentialsProvider(provider)
                .build();
        
        // Build the request URL
        String apiUrl = String.format("%s/rest/api/content/%s?expand=body.storage,version,space", 
                CONFLUENCE_URL.endsWith("/") ? CONFLUENCE_URL.substring(0, CONFLUENCE_URL.length() - 1) : CONFLUENCE_URL, 
                pageId);
        
        HttpGet request = new HttpGet(apiUrl);
        request.addHeader("Accept", "application/json");
        
        // Execute the request
        HttpResponse response = httpClient.execute(request);
        HttpEntity entity = response.getEntity();
        
        if (response.getStatusLine().getStatusCode() != 200) {
            throw new IOException("Failed to fetch Confluence page. Status: " + 
                    response.getStatusLine().getStatusCode() + ", Response: " + 
                    EntityUtils.toString(entity));
        }
        
        // Parse the JSON response
        String jsonResponse = EntityUtils.toString(entity);
        return objectMapper.readTree(jsonResponse);
        
        // If you need to clean sensitive data like in your JiraService:
        // String cleanedContent = dataCleaner.cleanSensitiveData(jsonNode.path("body").path("storage").path("value").asText());
    }
    
    /**
     * Gets the raw HTML content of a Confluence page
     * @param pageId The ID of the Confluence page
     * @return The HTML content of the page
     * @throws Exception If there's an error fetching the page
     */
    public String getPageContent(String pageId) throws Exception {
        JsonNode pageData = fetchConfluencePage(pageId);
        return pageData.path("body").path("storage").path("value").asText();
    }
    
    /**
     * Gets the title of a Confluence page
     * @param pageId The ID of the Confluence page
     * @return The title of the page
     * @throws Exception If there's an error fetching the page
     */
    public String getPageTitle(String pageId) throws Exception {
        JsonNode pageData = fetchConfluencePage(pageId);
        return pageData.path("title").asText();
    }
    
    /**
     * Gets the space key of a Confluence page
     * @param pageId The ID of the Confluence page
     * @return The space key of the page
     * @throws Exception If there's an error fetching the page
     */
    public String getPageSpace(String pageId) throws Exception {
        JsonNode pageData = fetchConfluencePage(pageId);
        return pageData.path("space").path("key").asText();
    }
}
