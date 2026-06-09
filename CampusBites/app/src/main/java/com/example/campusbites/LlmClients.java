package com.example.campusbites;

import android.content.Context;

import com.example.campusbites.llm.GeminiLlmClient;

public final class LlmClients {
    private LlmClients() {}

    public static LlmClient defaultClient(Context context) {
        // Real AI if a key is available (dev-friendly): set JVM property GEMINI_API_KEY.
        // Example (Android Studio Run/Debug VM options): -DGEMINI_API_KEY=xxxxx
        GeminiLlmClient gemini = GeminiLlmClient.fromSystemProperty(context);
        if (gemini.isConfigured()) {
            return gemini;
        }
        // Safe default: app never crashes if no AI key is configured.
        return new LocalHeuristicLlmClient();
    }
}
