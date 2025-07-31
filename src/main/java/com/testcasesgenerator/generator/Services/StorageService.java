package com.testcasesgenerator.generator.Services;

import java.io.InputStream;
import java.util.Optional;

/**
 * Interface for different storage implementations (S3, MySQL, etc.)
 */
public interface StorageService {
    
    /**
     * Store a test case file and its metadata
     * 
     * @param ticketId The Jira ticket ID
     * @param filePath The local file path of the generated Excel file
     * @param title The title of the test case
     * @param description The description of the test case
     * @return The URL to download the file
     */
    String storeTestCase(String ticketId, String filePath, String title, String description);
    
    /**
     * Retrieve a test case by ticket ID
     * 
     * @param ticketId The Jira ticket ID
     * @return Optional containing the download URL if found
     */
    Optional<String> getDownloadUrl(String ticketId);
    
    /**
     * Check if a test case exists for the given ticket ID
     * 
     * @param ticketId The Jira ticket ID
     * @return true if exists, false otherwise
     */
    boolean exists(String ticketId);
    
    /**
     * Delete a test case by ticket ID
     * 
     * @param ticketId The Jira ticket ID
     * @return true if deleted, false otherwise
     */
    boolean delete(String ticketId);
}