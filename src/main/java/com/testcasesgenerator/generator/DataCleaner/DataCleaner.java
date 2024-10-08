package com.testcasesgenerator.generator.DataCleaner;

import org.springframework.stereotype.Service;

@Service
public class DataCleaner {

    public String cleanSensitiveData(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        text = text.replaceAll(emailRegex, "[EMAIL Found]");
        String phoneRegex = "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b";
        text = text.replaceAll(phoneRegex, "[PHONE Found]");
        String apiKeyRegex = "\\b[A-Za-z0-9]{32,}\\b";
        text = text.replaceAll(apiKeyRegex, "[API KEY Found]");
        return text;
    }
    
}
