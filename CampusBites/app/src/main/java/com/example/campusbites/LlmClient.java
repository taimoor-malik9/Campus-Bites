package com.example.campusbites;

/**
 * Tiny abstraction for an LLM / AI provider.
 * Kept simple on purpose so the app stays stable even if AI isn't configured.
 */
public interface LlmClient {

    interface Callback {
        void onSuccess(String answer);
        void onError(String error);
    }

    boolean isConfigured();

    /**
     * @param prompt User text
     * @param menuContextJson Menu context so AI can suggest items
     */
    void ask(String prompt, String menuContextJson, Callback callback);
}
