package com.example.campusbites;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Lightweight AI chat screen.
 * - Does NOT require an API key to run (uses a safe fallback responder).
 * - Sends a prompt + your current menu context to a pluggable LlmClient.
 */
public class AiChatActivity extends AppCompatActivity {

    private TextView tvMessages;
    private TextView tvAiStatus;
    private EditText etPrompt;
    private Button btnSend;
    private ScrollView svMessages;

    private LlmClient llmClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_chat);

        tvMessages = findViewById(R.id.tvMessages);
        tvAiStatus = findViewById(R.id.tvAiStatus);
        etPrompt = findViewById(R.id.etPrompt);
        btnSend = findViewById(R.id.btnSend);
        svMessages = findViewById(R.id.svMessages);

        llmClient = LlmClients.defaultClient(getApplicationContext());
        updateStatus();

        appendBot("Tell me your budget and preference. Example: 'I have ₹150 and want something spicy'.");

        btnSend.setOnClickListener(v -> sendPrompt());
    }

    private void updateStatus() {
        if (tvAiStatus == null) return;
        tvAiStatus.setText(llmClient.isConfigured() ? "Online" : "Offline");
    }

    private void sendPrompt() {
        if (etPrompt == null) return;
        final String prompt = etPrompt.getText().toString().trim();
        if (TextUtils.isEmpty(prompt)) {
            Toast.makeText(this, "Type a message first", Toast.LENGTH_SHORT).show();
            return;
        }

        etPrompt.setText("");
        appendUser(prompt);

        String menuContextJson = MenuContextProvider.buildMenuContextJson(this);

        setUiLoading(true);
        llmClient.ask(prompt, menuContextJson, new LlmClient.Callback() {
            @Override
            public void onSuccess(String answer) {
                runOnUiThread(() -> {
                    setUiLoading(false);
                    appendBot(answer);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    setUiLoading(false);
                    appendBot("Sorry, I couldn't answer right now. " + (error != null ? error : ""));
                });
            }
        });
    }

    private void setUiLoading(boolean loading) {
        if (btnSend != null) btnSend.setEnabled(!loading);
        if (tvAiStatus != null) tvAiStatus.setText(loading ? "Thinking..." : (llmClient.isConfigured() ? "Online" : "Offline"));
    }

    private void appendUser(String text) {
        appendLine("You: " + text);
    }

    private void appendBot(String text) {
        appendLine("AI: " + text);
    }

    private void appendLine(String line) {
        if (tvMessages == null) return;
        String current = tvMessages.getText() != null ? tvMessages.getText().toString() : "";
        if (current.isEmpty()) {
            tvMessages.setText(line);
        } else {
            tvMessages.setText(current + "\n\n" + line);
        }
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (svMessages == null) return;
        svMessages.post(() -> svMessages.fullScroll(View.FOCUS_DOWN));
    }
}
