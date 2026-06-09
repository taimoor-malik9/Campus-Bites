package com.example.campusbites;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CheckoutSummaryAdapter
        extends RecyclerView.Adapter<CheckoutSummaryAdapter.SummaryViewHolder> {

    private List<CartItem> items;

    public CheckoutSummaryAdapter(List<CartItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public SummaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_summary, parent, false);
        return new SummaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SummaryViewHolder holder, int position) {
        CartItem item = items.get(position);

        holder.tvName.setText(item.getFoodName() + " (" + item.getSize() + ")");
        holder.tvQuantity.setText("x" + item.getQuantity());
        holder.tvPrice.setText("Rs " + String.format("%.0f", item.getTotalPrice()));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SummaryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvPrice;

        SummaryViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvItemName);
            tvQuantity = itemView.findViewById(R.id.tvQuantity);
            tvPrice = itemView.findViewById(R.id.tvPrice);
        }
    }
}