package com.example.campusbites;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class CustomerCartFragment extends Fragment implements CartAdapter.CartItemListener {

    private RecyclerView rvCartItems;
    private LinearLayout layoutEmptyCart;
    private TextView tvTotalPrice;
    private Button btnCheckout;

    private CartManager cartManager;
    private CartAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_cart, container, false);

        cartManager = CartManager.getInstance(requireContext());
        initializeViews(view);
        setupRecyclerView();

        return view;
    }

    private void initializeViews(View view) {
        rvCartItems = view.findViewById(R.id.rvCartItems);
        layoutEmptyCart = view.findViewById(R.id.layoutEmptyCart);
        tvTotalPrice = view.findViewById(R.id.tvTotalPrice);
        btnCheckout = view.findViewById(R.id.btnCheckout);

        btnCheckout.setOnClickListener(v -> {
            if (cartManager.hasItems()) {
                startActivity(new Intent(requireContext(), CheckoutActivity.class));
            }
        });
    }

    private void setupRecyclerView() {
        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartAdapter(cartManager.getCartItems(), this);
        rvCartItems.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCart();
    }

    private void refreshCart() {
        List<CartItem> items = cartManager.getCartItems();
        adapter.updateItems(items);

        if (items.isEmpty()) {
            layoutEmptyCart.setVisibility(View.VISIBLE);
            rvCartItems.setVisibility(View.GONE);
            btnCheckout.setEnabled(false);
            tvTotalPrice.setText("Total: Rs 0");
        } else {
            layoutEmptyCart.setVisibility(View.GONE);
            rvCartItems.setVisibility(View.VISIBLE);
            btnCheckout.setEnabled(true);
            tvTotalPrice.setText("Total: Rs " +
                    String.format("%.0f", cartManager.getTotalPrice()));
        }

        // Update badge in activity
        if (getActivity() instanceof CustomerMainActivity) {
            ((CustomerMainActivity) getActivity()).updateCartBadge();
        }
    }

    @Override
    public void onQuantityChanged(CartItem item) {
        cartManager.updateItem(item);
        refreshCart();
    }

    @Override
    public void onItemRemoved(CartItem item) {
        cartManager.removeItem(item.getId());
        refreshCart();
    }
}