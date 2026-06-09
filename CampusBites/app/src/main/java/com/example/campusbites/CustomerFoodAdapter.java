package com.example.campusbites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;

public class CustomerFoodAdapter extends RecyclerView.Adapter<CustomerFoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodItems;
    private OnFoodClickListener listener;

    public interface OnFoodClickListener {
        void onFoodClick(FoodItem item);
        void onAddToCart(FoodItem item, String size, double price);
    }

    public CustomerFoodAdapter(List<FoodItem> foodItems, OnFoodClickListener listener) {
        this.foodItems = foodItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodItems.get(position);

        holder.tvName.setText(item.getName());
        holder.tvDesc.setText(item.getDescription());
        holder.tvPrice.setText("From Rs " + String.format("%.0f", item.getPriceSmall()));

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(holder.ivImage);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFoodClick(item);
            }
        });

        holder.btnQuickAdd.setOnClickListener(v -> {
            if (listener != null) {
                // Quick add with medium size
                listener.onAddToCart(item, "Medium", item.getPriceMedium());
            }
        });
    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }

    static class FoodViewHolder extends RecyclerView.ViewHolder {
        ImageView ivImage;
        TextView tvName, tvDesc, tvPrice;
        Button btnQuickAdd;

        FoodViewHolder(View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivFoodImage);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvPrice = itemView.findViewById(R.id.tvFoodPrice);
            btnQuickAdd = itemView.findViewById(R.id.btnQuickAdd);
        }
    }
}