package com.example.campusbites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private CartItemListener listener;

    public interface CartItemListener {
        void onQuantityChanged(CartItem item);
        void onItemRemoved(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, CartItemListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    public void updateItems(List<CartItem> items) {
        this.cartItems = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.tvName.setText(item.getFoodName());
        holder.tvSize.setText("Size: " + item.getSize());
        holder.tvPrice.setText("Rs " + String.format("%.0f", item.getTotalPrice()));
        holder.tvQuantity.setText(String.valueOf(item.getQuantity()));

        if (item.getFoodImageUrl() != null && !item.getFoodImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getFoodImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.ivImage);
        }

        holder.btnMinus.setOnClickListener(v -> {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
                holder.tvPrice.setText("Rs " + String.format("%.0f", item.getTotalPrice()));
                if (listener != null) {
                    listener.onQuantityChanged(item);
                }
            }
        });

        holder.btnPlus.setOnClickListener(v -> {
            item.setQuantity(item.getQuantity() + 1);
            holder.tvQuantity.setText(String.valueOf(item.getQuantity()));
            holder.tvPrice.setText("Rs " + String.format("%.0f", item.getTotalPrice()));
            if (listener != null) {
                listener.onQuantityChanged(item);
            }
        });

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemRemoved(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvSize, tvPrice, tvQuantity;
        ImageButton btnMinus, btnPlus, btnRemove;

        CartViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivFoodImage);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvSize = itemView.findViewById(R.id.tvSize);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}