
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
import com.example.campusbites.FoodItem;
import com.example.campusbites.OrderEntity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<OrderEntity> orders;
    private OnStatusUpdateListener listener;

    public interface OnStatusUpdateListener {
        void onStatusUpdate(OrderEntity order);
    }

    public OrderAdapter(List<OrderEntity> orders, OnStatusUpdateListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        OrderEntity order = orders.get(position);

        holder.tvOrderNumber.setText("Order #" + order.getOrderNumber());
        holder.tvCustomerName.setText("Customer: " + order.getCustomerName());
        holder.tvCustomerPhone.setText("Phone: " + order.getCustomerPhone());
        holder.tvTotalPrice.setText("Total: ₹" +
                String.format("%.2f", order.getTotalPrice()));
        holder.tvItems.setText(formatItems(order.getItemsJson()));

        // Format timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(order.getTimestamp())));

        // Status display
        holder.tvStatus.setText("Status: " + order.getStatus());
        setStatusColor(holder.tvStatus, order.getStatus());

        // Button configuration
        if (order.getStatus().equals("Delivered")) {
            holder.btnUpdateStatus.setVisibility(View.GONE);
        } else {
            holder.btnUpdateStatus.setVisibility(View.VISIBLE);
            String buttonText = getNextStatusText(order.getStatus());
            holder.btnUpdateStatus.setText(buttonText);
            holder.btnUpdateStatus.setOnClickListener(v -> {
                if (listener != null) listener.onStatusUpdate(order);
            });
        }
    }

    private void setStatusColor(TextView textView, String status) {
        switch (status) {
            case "Pending":
                textView.setTextColor(Color.parseColor("#FF9800")); // Orange
                break;
            case "Preparing":
                textView.setTextColor(Color.parseColor("#2196F3")); // Blue
                break;
            case "Ready":
                textView.setTextColor(Color.parseColor("#4CAF50")); // Green
                break;
            case "Delivered":
                textView.setTextColor(Color.parseColor("#9E9E9E")); // Gray
                break;
        }
    }

    private String getNextStatusText(String currentStatus) {
        switch (currentStatus) {
            case "Pending":
                return "Start Preparing";
            case "Preparing":
                return "Mark as Ready";
            case "Ready":
                return "Mark as Delivered";
            default:
                return "Complete";
        }
    }

    private String formatItems(String itemsJson) {
        return itemsJson.replace("[", "").replace("]", "").replace("\"", "");
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView tvOrderNumber, tvCustomerName, tvCustomerPhone;
        TextView tvTotalPrice, tvItems, tvStatus, tvTimestamp;
        Button btnUpdateStatus;

        public OrderViewHolder(View itemView) {
            super(itemView);
            tvOrderNumber = itemView.findViewById(R.id.tvOrderNumber);
            tvCustomerName = itemView.findViewById(R.id.tvCustomerName);
            tvCustomerPhone = itemView.findViewById(R.id.tvCustomerPhone);
            tvTotalPrice = itemView.findViewById(R.id.tvTotalPrice);
            tvItems = itemView.findViewById(R.id.tvItems);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            btnUpdateStatus = itemView.findViewById(R.id.btnUpdateStatus);
        }
    }
}