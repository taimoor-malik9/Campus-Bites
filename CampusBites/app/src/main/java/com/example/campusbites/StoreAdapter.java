package com.example.campusbites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {

    private List<Store> stores;
    private OnStoreClickListener listener;

    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    public StoreAdapter(List<Store> stores, OnStoreClickListener listener) {
        this.stores = stores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);

        holder.tvStoreName.setText(store.getStoreName());
        holder.tvStoreDesc.setText(store.getStoreDescription());

        if (store.getStoreImageUrl() != null && !store.getStoreImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(store.getStoreImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .circleCrop()
                    .into(holder.ivStoreLogo);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStoreClick(store);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        ImageView ivStoreLogo;
        TextView tvStoreName, tvStoreDesc;

        StoreViewHolder(View itemView) {
            super(itemView);
            ivStoreLogo = itemView.findViewById(R.id.ivStoreLogo);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStoreDesc = itemView.findViewById(R.id.tvStoreDesc);
        }
    }
}