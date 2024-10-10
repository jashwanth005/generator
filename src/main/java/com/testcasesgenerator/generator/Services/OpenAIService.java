package com.testcasesgenerator.generator.Services;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class OpenAIService {
private static final Dotenv dotenv = Dotenv.load();
private static final String OPENAI_API_KEY = dotenv.get("OPENAI_API_KEY");
private static final String OPENAI_API_URL = dotenv.get("OPENAI_API_URL");

    public String generateTestCasesWithOpenAI(String title, String description) throws Exception {
        OkHttpClient client = new OkHttpClient();

        JSONObject requestBody = new JSONObject();
        JSONArray messages = new JSONArray();
        
        messages.put(new JSONObject().put("role", "system").put("content", "You are a QA engineer."));
        messages.put(new JSONObject().put("role", "user").put("content", "Based on the following Jira ticket, generate detailed test cases. With covering all positive negative and edge cases.test cases\n\n" +
                "Title: " + title + "\nDescription: " + description + "\n\n" +
                "Provide test cases in this format:\n" +
                "- Test Case ID: TC001\n" +
                "- Scenario: [Test scenario here]\n" +
                "- Steps: \n    1. [Step 1]\n    2. [Step 2]\n" +
                "- Expected Result: [Expected result here]"));

        requestBody.put("model", "gpt-3.5-turbo");
        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.5);

        Request request = new Request.Builder()
                .url(OPENAI_API_URL)
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toString()))
                .build();

       
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        JSONObject jsonResponse = new JSONObject(responseBody);
        return jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content");
    }
}
