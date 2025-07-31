package com.testcasesgenerator.generator.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.testcasesgenerator.generator.Services.RedisCacheService;

@RestController
@RequestMapping("/cache")
@CrossOrigin(origins = "*")
public class CacheManagementController {

    private final RedisCacheService redisCacheService;

    @Autowired
    public CacheManagementController(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    /**
     * Clear cache for a specific ticket ID
     */
    @GetMapping("/clear")
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

    /**
     * Check if a ticket ID is in cache
     */
    @GetMapping("/check")
    public ObjectNode checkCache(@RequestParam String ticketId) {
        ObjectNode jsonObject = JsonNodeFactory.instance.objectNode();
        
        boolean exists = redisCacheService.hasTestCaseInCache(ticketId);
        
        jsonObject.put("success", "status");
        jsonObject.put("inCache", exists);
        jsonObject.put("ticketId", ticketId);
        
        return jsonObject;
    }
}
