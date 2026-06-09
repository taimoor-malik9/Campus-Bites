package com.example.campusbites;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CustomerOrderAdapter
        extends RecyclerView.Adapter<CustomerOrderAdapter.OrderViewHolder> {

    private List<OrderEntity> orders;

    public CustomerOrderAdapter(List<OrderEntity> orders) {
        this.orders = orders;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_customer_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderEntity order = orders.get(position);

        holder.tvOrderNumber.setText("Order #" + order.getOrderNumber());
        holder.tvStoreName.setText(order.getStoreName());
        holder.tvTotalPrice.setText("Rs " + String.format("%.0f", order.getTotalPrice()));

        // Format status
        holder.tvStatus.setText(order.getStatus());
        setStatusColor(holder.tvStatus, order.getStatus());

        // Format date
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvDate.setText(sdf.format(new Date(order.getTimestamp())));

        // Format items
        holder.tvItems.setText(formatItems(order.getItemsJson()));
    }

    private void setStatusColor(TextView textView, String status) {
        switch (status) {
            case "Pending":
                textView.setTextColor(Color.parseColor("#FF9800"));
                textView.setBackgroundResource(R.drawable.status_pending_bg);
                break;
            case "Preparing":
                textView.setTextColor(Color.parseColor("#2196F3"));
                textView.setBackgroundResource(R.drawable.status_preparing_bg);
                break;
            case "Ready":
                textView.setTextColor(Color.parseColor("#4CAF50"));
                textView.setBackgroundResource(R.drawable.status_ready_bg);
                break;
            case "Delivered":
                textView.setTextColor(Color.parseColor("#9E9E9E"));
                textView.setBackgroundResource(R.drawable.status_delivered_bg);
                break;
        }
    }

    private String formatItems(String itemsJson) {
        // Simple format - you can improve this
        return itemsJson.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace("{", "")
                .replace("}", "");
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvStoreName, tvStatus, tvTotalPrice, tvDate, tvItems;

        OrderViewHolder(View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvStoreName = itemView.findViewById(R.id.tvStoreName);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvItems = itemView.findViewById(R.id.tvItems);
        }
    }
}