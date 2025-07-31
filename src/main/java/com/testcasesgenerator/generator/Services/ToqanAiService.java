package com.testcasesgenerator.generator.Services;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.stereotype.Service;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ToqanAiService {

    private static final Dotenv dotenv = Dotenv.load();
    private static final String TOQAN_API_URL = dotenv.get("TOQAN_API_URL");
    private static final String TOQAN_API_KEY = dotenv.get("TOQAN_API_KEY");
 


    public String generateTestCasesWithToqanAi(String title, String description) throws IOException {

    
        OkHttpClient client = new OkHttpClient();
        JSONObject jsonRequest = new JSONObject();
        jsonRequest.put("user_message", 
            "Based on the following Jira ticket, generate detailed test cases. With covering all positive negative and edge cases dont add star symbole this instand add dash dont add this lines give ### **Test Cases** and this also Let me know if you require further adjustments or additional test cases!  \n" + //
                                "\n" + //
                                "\n" + //
                                "--- \n\n" +
                "Title: " + title + "\nDescription: " + description + "\n\n" +
                "Provide test cases in this format:\n" +
                "- Test Case ID: TC001\n" +
                "- Scenario: [Test scenario here]\n" +
                "- Steps: \n    1. [Step 1]\n    2. [Step 2]\n" +
                "- Expected Result: [Expected result here]"
        );
       
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, jsonRequest.toString());
        
        Request request = new Request.Builder()
          .url("https://api.coco.prod.toqan.ai/api/create_conversation")
          .post(body)
          .addHeader("accept", "*/*")
          .addHeader("content-type", "application/json")
          .addHeader("X-Api-Key", TOQAN_API_KEY)
          .build();
        
        Response response = client.newCall(request).execute();
        
        String responseBody = response.body().string();
        
        System.out.println("Status Code: " + response.code());
        System.out.println("Response Body: " + responseBody);
        
        if (!response.isSuccessful()) {
            throw new IOException("API call failed: " + response.code() + " - " + responseBody);
        }
        
        JSONObject jsonResponse = new JSONObject(responseBody);
        String conversationId = jsonResponse.getString("conversation_id");
        String requestId = jsonResponse.getString("request_id");
        
        System.out.println("Conversation ID: " + conversationId);
        System.out.println("Request ID: " + requestId);
        
        try {
            Thread.sleep(50000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return getAnswerFromToqanAi(conversationId, requestId);
    }

    private String getAnswerFromToqanAi(String conversationId, String requestId) throws IOException {
        OkHttpClient client = new OkHttpClient();
    
        Request request = new Request.Builder()
          .url("https://api.coco.prod.toqan.ai/api/get_answer?conversation_id=" + conversationId + "&request_id=" + requestId)
          .get()
          .addHeader("accept", "*/*")
          .addHeader("X-Api-Key", TOQAN_API_KEY)
          .build();
        
        Response response = client.newCall(request).execute();
        String responseBody = response.body().string();
        System.out.println("Answer API Response: " + responseBody);
        
        if (!response.isSuccessful()) {
            throw new IOException("Get answer API call failed: " + response.code() + " - " + responseBody);
        }
        
        JSONObject jsonResponse = new JSONObject(responseBody);
        
        try {
            return jsonResponse.getString("answer");
        } catch (Exception e) {
            System.out.println("Error parsing response: " + e.getMessage());
            System.out.println("Full response: " + responseBody);
            
            return responseBody;
        }
    }
    
    
}
