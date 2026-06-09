package com.example.campusbites;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class CustomerOrdersFragment extends Fragment {

    private RecyclerView rvOrders;
    private LinearLayout layoutNoOrders;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private CustomerOrderAdapter adapter;
    private List<OrderEntity> orderList = new ArrayList<>();
    private String customerId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_orders, container, false);

        try {
            repository = FirebaseRepository.getInstance();
            if (repository == null || getContext() == null) {
                return view;
            }

            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("CampusBitesPrefs", requireContext().MODE_PRIVATE);
            customerId = prefs.getString("USER_ID", null);

            if (customerId == null || customerId.isEmpty()) {
                return view;
            }

            initializeViews(view);
            setupRecyclerView();
            loadOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initializeViews(View view) {
        try {
            rvOrders = view.findViewById(R.id.rvOrders);
            layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
            progressBar = view.findViewById(R.id.progressBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        if (rvOrders == null || getContext() == null) {
            return;
        }
        try {
            rvOrders.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new CustomerOrderAdapter(orderList);
            rvOrders.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrders() {
        if (customerId == null || customerId.isEmpty() || repository == null) {
            return;
        }

        try {
            showLoading(true);

            repository.getOrdersByCustomerId(customerId, new FirebaseRepository.OrderListCallback() {
                @Override
                public void onSuccess(List<OrderEntity> orders) {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }
                    try {
                        showLoading(false);
                        if (orders != null) {
                            orderList.clear();
                            orderList.addAll(orders);
                            if (adapter != null) {
                                adapter.notifyDataSetChanged();
                            }

                            if (orders.isEmpty()) {
                                if (layoutNoOrders != null) {
                                    layoutNoOrders.setVisibility(View.VISIBLE);
                                }
                                if (rvOrders != null) {
                                    rvOrders.setVisibility(View.GONE);
                                }
                            } else {
                                if (layoutNoOrders != null) {
                                    layoutNoOrders.setVisibility(View.GONE);
                                }
                                if (rvOrders != null) {
                                    rvOrders.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (isAdded() && getContext() != null) {
                        showLoading(false);
                        Toast.makeText(requireContext(),
                                "Failed to load orders: " + (error != null ? error : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadOrders();
    }
}