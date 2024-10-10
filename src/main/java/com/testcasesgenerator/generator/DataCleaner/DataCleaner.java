package com.testcasesgenerator.generator.DataCleaner;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataCleaner {

    private List<String> sensitiveKeywords;

    public DataCleaner() {
        this.sensitiveKeywords = loadSensitiveKeywords("src/main/resources/sensitive_keywords.txt");
    }

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

        for (String keyword : sensitiveKeywords) {
            if (keyword != null && !keyword.isEmpty()) {
                text = text.replaceAll("(?i)" + keyword, "Replaced");
            }
        }

        return text;
    }

    private List<String> loadSensitiveKeywords(String filePath) {
        List<String> keywords = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                keywords.add(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return keywords;
    }
}
