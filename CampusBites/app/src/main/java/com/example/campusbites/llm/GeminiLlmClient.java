package com.example.campusbites.llm;

import android.content.Context;

import androidx.annotation.Nullable;

import com.example.campusbites.LlmClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Real AI client using Google Gemini REST API.
 *
 * This class fails safely and never crashes the app.
 */
public class GeminiLlmClient implements LlmClient {

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient http = new OkHttpClient();
    private final String apiKey;

    public GeminiLlmClient(@Nullable String apiKey) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
    }

    /**
     * Convenience factory to avoid hard-coding API keys in code.
     * You can pass a key into this method from whatever secure runtime source you choose.
     */
    public static GeminiLlmClient fromApiKey(@Nullable String apiKey) {
        return new GeminiLlmClient(apiKey);
    }

    /**
     * Fallback factory: reads key from a JVM system property (debug/dev only).
     * Property name: GEMINI_API_KEY
     */
    public static GeminiLlmClient fromSystemProperty(Context context) {
        String key = "";
        try {
            key = System.getProperty("GEMINI_API_KEY", "");
        } catch (Exception ignored) {
        }
        return new GeminiLlmClient(key);
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    @Override
    public void ask(String prompt, String menuContextJson, LlmClient.Callback callback) {
        if (callback == null) return;

        if (!isConfigured()) {
            callback.onError("AI key not configured");
            return;
        }

        try {
            String safePrompt = prompt != null ? prompt : "";
            String safeContext = menuContextJson != null ? menuContextJson : "{}";

            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

            JSONObject body = new JSONObject();
            JSONArray contents = new JSONArray();

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            JSONArray parts = new JSONArray();
            parts.put(new JSONObject().put(
                    "text",
                    "You are a smart waiter for a campus food ordering app. " +
                            "Use the provided menu context to recommend items within budget and preferences. " +
                            "Keep answers short and practical.\n\n" +
                            "Menu Context (JSON):\n" + safeContext + "\n\n" +
                            "User: " + safePrompt
            ));
            userMsg.put("parts", parts);
            contents.put(userMsg);

            body.put("contents", contents);

            Request req = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(body.toString(), JSON))
                    .build();

            http.newCall(req).enqueue(new okhttp3.Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e != null ? e.getMessage() : "Network error");
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String raw = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        callback.onError(buildErrorMessage(response.code(), raw));
                        return;
                    }

                    try {
                        String text = extractTextFromGemini(raw);
                        if (text == null || text.trim().isEmpty()) {
                            callback.onError("Empty AI response");
                        } else {
                            callback.onSuccess(text.trim());
                        }
                    } catch (Exception ex) {
                        callback.onError(ex.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private static String buildErrorMessage(int code, String raw) {
        // Try to parse JSON error format: {"error": {"message": "..."}}
        try {
            if (raw != null && !raw.isEmpty()) {
                JSONObject errorJson = new JSONObject(raw);
                JSONObject errorObj = errorJson.optJSONObject("error");
                if (errorObj != null) {
                    String msg = errorObj.optString("message", "");
                    if (!msg.trim().isEmpty()) {
                        return "AI error (" + code + "): " + msg.trim();
                    }
                }
            }
        } catch (Exception ignore) {
            // fall through
        }

        if (raw != null && !raw.trim().isEmpty()) {
            String shortRaw = raw.trim();
            if (shortRaw.length() > 300) shortRaw = shortRaw.substring(0, 300) + "...";
            return "AI error (" + code + "): " + shortRaw;
        }
        return "AI error: " + code;
    }

    private static String extractTextFromGemini(String raw) throws Exception {
        if (raw == null || raw.isEmpty()) return null;

        JSONObject json = new JSONObject(raw);
        JSONArray candidates = json.optJSONArray("candidates");
        if (candidates == null || candidates.length() == 0) return null;

        JSONObject first = candidates.getJSONObject(0);
        JSONObject content = first.optJSONObject("content");
        if (content == null) return null;

        JSONArray parts = content.optJSONArray("parts");
        if (parts == null || parts.length() == 0) return null;

        // Typical response: parts[0].text
        return parts.getJSONObject(0).optString("text", null);
    }
}
