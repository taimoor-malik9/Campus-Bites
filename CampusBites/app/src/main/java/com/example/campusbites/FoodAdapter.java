
package com.example.campusbites;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.campusbites.FoodItem;
import com.example.campusbites.OrderEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoodAdapter extends RecyclerView.Adapter<FoodAdapter.FoodViewHolder> {

    private List<FoodItem> foodList;
    private OnItemClickListener listener;
    private OnDeleteListener deleteListener;

    public interface OnItemClickListener {
        void onEditClick(FoodItem item);
    }

    public interface OnDeleteListener {
        void onDeleteClick(FoodItem item);
    }

    public FoodAdapter(List<FoodItem> foodList, OnItemClickListener listener,
                       OnDeleteListener deleteListener) {
        this.foodList = foodList;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_food, parent, false);
        return new FoodViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodViewHolder holder, int position) {
        FoodItem item = foodList.get(position);

        holder.tvName.setText(item.getName());
        holder.tvDesc.setText(item.getDescription());

        holder.tvPriceSmall.setText("Small: Rs" +
                String.format("%.2f", item.getPriceSmall()));
        holder.tvPriceMedium.setText("Medium: Rs" +
                String.format("%.2f", item.getPriceMedium()));
        holder.tvPriceLarge.setText("Large: Rs" +
                String.format("%.2f", item.getPriceLarge()));

        // Load image from URL using Glide or Picasso
        // If using Glide:
        Glide.with(holder.itemView.getContext())
            .load(item.getImageUrl())
            .placeholder(R.drawable.placeholder)
           .into(holder.ivImage);

        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(item);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (deleteListener != null) deleteListener.onDeleteClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return foodList.size();
    }

    public static class FoodViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvDesc, tvPriceSmall, tvPriceMedium, tvPriceLarge;
        ImageView ivImage;
        Button btnEdit, btnDelete;

        public FoodViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvFoodName);
            tvDesc = itemView.findViewById(R.id.tvFoodDesc);
            tvPriceSmall = itemView.findViewById(R.id.tvFoodPriceSmall);
            tvPriceMedium = itemView.findViewById(R.id.tvFoodPriceMedium);
            tvPriceLarge = itemView.findViewById(R.id.tvFoodPriceLarge);
            ivImage = itemView.findViewById(R.id.ivFoodImage);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
