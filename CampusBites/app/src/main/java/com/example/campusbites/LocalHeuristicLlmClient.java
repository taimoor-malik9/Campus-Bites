package com.example.campusbites;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Offline fallback "AI".
 * This keeps the app 100% functional without any external API keys.
 * Later you can replace it with a real OpenAI/Gemini client behind LlmClient.
 */
public class LocalHeuristicLlmClient implements LlmClient {

    private static final Pattern MONEY_PATTERN = Pattern.compile("(₹|rs\\.?|inr\\s*)(\\d+)", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public void ask(String prompt, String menuContextJson, Callback callback) {
        // Simple heuristic: detect budget + preferences and give a helpful response.
        try {
            String lower = prompt == null ? "" : prompt.toLowerCase(Locale.US);
            Integer budget = extractBudget(prompt);

            StringBuilder sb = new StringBuilder();
            sb.append("I can help you decide. ");

            if (budget != null) {
                sb.append("Your budget looks like ₹").append(budget).append(". ");
            }

            if (lower.contains("spicy")) {
                sb.append("You want something spicy. ");
            }
            if (lower.contains("sweet")) {
                sb.append("You want something sweet. ");
            }
            if (lower.contains("veg") || lower.contains("vegetarian")) {
                sb.append("You prefer vegetarian items. ");
            }

            sb.append("\n\nRight now AI suggestions are running in offline mode in this build. " +
                    "To enable real AI, we can plug in a Gemini/OpenAI API client later.\n\n");

            sb.append("Tip: Ask like this: 'I have ₹150 and want something spicy. Suggest 2 items under budget.'");

            callback.onSuccess(sb.toString());
        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private Integer extractBudget(String prompt) {
        if (prompt == null) return null;
        Matcher m = MONEY_PATTERN.matcher(prompt);
        if (m.find()) {
            try {
                return Integer.parseInt(m.group(2));
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
