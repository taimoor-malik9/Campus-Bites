package com.example.campusbites.ai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.campusbites.R;

import java.util.ArrayList;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<ChatMessage> items = new ArrayList<>();

    public void submit(List<ChatMessage> messages) {
        items.clear();
        if (messages != null) items.addAll(messages);
        notifyDataSetChanged();
    }

    public void add(ChatMessage message) {
        items.add(message);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage m = items.get(position);
        return m.getRole() == ChatMessage.Role.USER ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = (viewType == TYPE_USER) ? R.layout.item_chat_message_user : R.layout.item_chat_message_ai;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.tvMessage.setText(items.get(position).getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView tvMessage;

        VH(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tvMessage);
        }
    }
}
