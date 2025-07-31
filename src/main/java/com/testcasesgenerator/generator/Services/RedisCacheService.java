package com.testcasesgenerator.generator.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // Prefix for test case keys in Redis
    private static final String TEST_CASE_KEY_PREFIX = "testcase:";
    
    // Default expiration time in hours
    private static final long DEFAULT_EXPIRATION_HOURS = 24;

    @Autowired
    public RedisCacheService(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Store the test case response in Redis cache
     * 
     * @param ticketId The Jira ticket ID
     * @param response The response to store
     */
    public void cacheTestCaseResponse(String ticketId, ObjectNode response) {
        String key = generateKey(ticketId);
        redisTemplate.opsForValue().set(key, response, DEFAULT_EXPIRATION_HOURS, TimeUnit.HOURS);
    }

    /**
     * Get the cached test case response for a ticket ID
     * 
     * @param ticketId The Jira ticket ID
     * @return The cached response or null if not found
     */
    public ObjectNode getCachedTestCaseResponse(String ticketId) {
        String key = generateKey(ticketId);
        Object cachedValue = redisTemplate.opsForValue().get(key);
        
        if (cachedValue instanceof ObjectNode) {
            return (ObjectNode) cachedValue;
        }
        
        return null;
    }

    /**
     * Check if a test case exists in cache
     * 
     * @param ticketId The Jira ticket ID
     * @return true if cached, false otherwise
     */
    public boolean hasTestCaseInCache(String ticketId) {
        String key = generateKey(ticketId);
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Delete a test case from cache
     * 
     * @param ticketId The Jira ticket ID
     */
    public void deleteTestCaseFromCache(String ticketId) {
        String key = generateKey(ticketId);
        redisTemplate.delete(key);
    }

    /**
     * Generate a Redis key for a ticket ID
     * 
     * @param ticketId The Jira ticket ID
     * @return The Redis key
     */
    private String generateKey(String ticketId) {
        return TEST_CASE_KEY_PREFIX + ticketId;
    }
}
