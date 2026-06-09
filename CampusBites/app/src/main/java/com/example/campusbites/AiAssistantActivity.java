package com.example.campusbites;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusbites.ai.ChatAdapter;
import com.example.campusbites.ai.ChatMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * Phase-2 AI Assistant UI:
 * - RecyclerView chat bubbles
 * - Builds prompt using FoodItem list from Firestore
 * - Sends to LLM (Gemini) via LlmClient abstraction
 */
public class AiAssistantActivity extends AppCompatActivity {

    private RecyclerView rvChat;
    private EditText etMessage;
    private Button btnSend;
    private TextView tvStatus;

    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    private FirebaseRepository repository;
    private LlmClient llmClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_assistant);

        repository = FirebaseRepository.getInstance();
        llmClient = LlmClients.defaultClient(getApplicationContext());

        rvChat = findViewById(R.id.rvChat);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        tvStatus = findViewById(R.id.tvStatus);

        adapter = new ChatAdapter();
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        updateStatus(false);

        addAi("Hi! I'm your smart waiter. Ask me things like: 'What is cheap and filling?' or 'I have ₹150 and want something spicy.'");

        btnSend.setOnClickListener(v -> onSend());
    }

    private void updateStatus(boolean thinking) {
        if (tvStatus == null) return;
        if (thinking) {
            tvStatus.setText("Thinking...");
        } else {
            tvStatus.setText(llmClient != null && llmClient.isConfigured() ? "Online" : "Offline");
        }
    }

    private void onSend() {
        if (etMessage == null) return;
        String userText = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(userText)) {
            Toast.makeText(this, "Type a message first", Toast.LENGTH_SHORT).show();
            return;
        }
        etMessage.setText("");

        addUser(userText);

        setUiEnabled(false);
        updateStatus(true);

        // Build prompt with menu data from Firestore (foodItems collection)
        loadMenuAndAskAi(userText);
    }

    private void loadMenuAndAskAi(String userQuestion) {
        // We don't have a "getAllFoodItems" method, so we query through repository's Firestore directly.
        // Minimal change: use existing repository db access by adding a new method would be cleaner, but we keep it local here.

        // Reuse existing data model: FoodItem in "foodItems" collection.
        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("foodItems")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<FoodItem> items = new ArrayList<>();
                    for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        FoodItem item = doc.toObject(FoodItem.class);
                        if (item != null) {
                            item.setId(doc.getId());
                            items.add(item);
                        }
                    }
                    askAiWithMenu(userQuestion, items);
                })
                .addOnFailureListener(e -> {
                    askAiWithMenu(userQuestion, new ArrayList<>());
                });
    }

    private void askAiWithMenu(String userQuestion, List<FoodItem> items) {
        String menuText = formatMenu(items);
        String prompt = "You are a canteen waiter. Here is the menu (name, description, prices):\n" +
                menuText +
                "\n\nThe user asked: '" + userQuestion + "'. Recommend 2 items and explain why briefly. If the menu doesn't have enough data, ask 1 clarifying question.";

        String contextJson = MenuContextProvider.buildMenuContextJson(this);

        if (llmClient == null) {
            addAi("AI is unavailable right now.");
            setUiEnabled(true);
            updateStatus(false);
            return;
        }

        llmClient.ask(prompt, contextJson, new LlmClient.Callback() {
            @Override
            public void onSuccess(String answer) {
                runOnUiThread(() -> {
                    addAi(answer);
                    setUiEnabled(true);
                    updateStatus(false);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    addAi("AI error: " + (error != null ? error : "Unknown") + "\n\nTip: Configure your GEMINI_API_KEY.");
                    setUiEnabled(true);
                    updateStatus(false);
                });
            }
        });
    }

    private String formatMenu(List<FoodItem> items) {
        if (items == null || items.isEmpty()) {
            return "(No menu items found)";
        }

        StringBuilder sb = new StringBuilder();
        int limit = Math.min(items.size(), 80); // safety
        for (int i = 0; i < limit; i++) {
            FoodItem it = items.get(i);
            sb.append("- ")
                    .append(safe(it.getName()))
                    .append(" | ")
                    .append(safe(it.getDescription()))
                    .append(" | small: ")
                    .append(it.getPriceSmall())
                    .append(", medium: ")
                    .append(it.getPriceMedium())
                    .append(", large: ")
                    .append(it.getPriceLarge())
                    .append("\n");
        }
        if (items.size() > limit) sb.append("...and more\n");
        return sb.toString();
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private void addUser(String text) {
        ChatMessage m = new ChatMessage(ChatMessage.Role.USER, text);
        messages.add(m);
        adapter.add(m);
        scrollToBottom();
    }

    private void addAi(String text) {
        ChatMessage m = new ChatMessage(ChatMessage.Role.AI, text);
        messages.add(m);
        adapter.add(m);
        scrollToBottom();
    }

    private void scrollToBottom() {
        if (rvChat == null) return;
        rvChat.post(() -> rvChat.scrollToPosition(Math.max(0, adapter.getItemCount() - 1)));
    }

    private void setUiEnabled(boolean enabled) {
        if (btnSend != null) btnSend.setEnabled(enabled);
        if (etMessage != null) etMessage.setEnabled(enabled);
    }
}
