package com.example.campusbites;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusbites.OrderEntity;
import com.example.campusbites.Store;
import com.example.campusbites.FirebaseRepository;
import java.util.ArrayList;
import java.util.List;

public class OrdersFragment extends Fragment {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private FirebaseRepository repository;
    private String currentStoreId;
    private LinearLayout layoutNoOrders;
    private TextView tvStoreName;
    private ImageView ivStoreLogo, ivRefreshOrders;
    private ProgressBar progressBar;
    private List<OrderEntity> orderList = new ArrayList<>();

    // Filter chips
    private TextView chipAll, chipPending, chipPreparing, chipReady, chipDelivered;
    private String currentFilter = "All";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orders, container, false);

        try {
            repository = FirebaseRepository.getInstance();
            if (repository == null || getContext() == null) {
                return view;
            }

            SharedPreferences prefs = requireContext().getSharedPreferences(
                    "SmartCanteenPrefs", requireContext().MODE_PRIVATE);
            
            try {
                currentStoreId = prefs.getString("STORE_ID", null);
            } catch (ClassCastException e) {
                try {
                    int idAsInt = prefs.getInt("STORE_ID", -1);
                    if (idAsInt != -1) {
                        currentStoreId = String.valueOf(idAsInt);
                        prefs.edit().putString("STORE_ID", currentStoreId).apply();
                    }
                } catch (Exception ex) {
                    currentStoreId = null;
                }
            }

            if (currentStoreId == null || currentStoreId.isEmpty()) {
                return view;
            }

            initializeViews(view);
            setupRecyclerView();
            setupFilterChips();
            loadStoreInfo();
            loadOrders();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initializeViews(View view) {
        try {
            tvStoreName = view.findViewById(R.id.tvStoreName);
            ivStoreLogo = view.findViewById(R.id.ivStoreLogo);
            ivRefreshOrders = view.findViewById(R.id.ivRefreshOrders);
            layoutNoOrders = view.findViewById(R.id.layoutNoOrders);
            recyclerView = view.findViewById(R.id.rvOrders);
            progressBar = view.findViewById(R.id.progressBar);

            chipAll = view.findViewById(R.id.chipAll);
            chipPending = view.findViewById(R.id.chipPending);
            chipPreparing = view.findViewById(R.id.chipPreparing);
            chipReady = view.findViewById(R.id.chipReady);
            chipDelivered = view.findViewById(R.id.chipDelivered);

            if (ivRefreshOrders != null) {
                ivRefreshOrders.setOnClickListener(v -> loadOrders());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        if (recyclerView == null || getContext() == null) {
            return;
        }
        try {
            recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new OrderAdapter(orderList, order -> {
                if (order != null) {
                    updateOrderStatus(order);
                }
            });
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupFilterChips() {
        try {
            if (chipAll != null) chipAll.setOnClickListener(v -> filterOrders("All"));
            if (chipPending != null) chipPending.setOnClickListener(v -> filterOrders("Pending"));
            if (chipPreparing != null) chipPreparing.setOnClickListener(v -> filterOrders("Preparing"));
            if (chipReady != null) chipReady.setOnClickListener(v -> filterOrders("Ready"));
            if (chipDelivered != null) chipDelivered.setOnClickListener(v -> filterOrders("Delivered"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStoreInfo();
        loadOrders();
    }

    private void loadStoreInfo() {
        if (currentStoreId == null || currentStoreId.isEmpty() || repository == null) {
            return;
        }

        try {
            repository.getStoreById(currentStoreId, new FirebaseRepository.StoreCallback() {
                @Override
                public void onSuccess(Store store) {
                    if (!isAdded() || getContext() == null || store == null) return;
                    try {
                        if (tvStoreName != null && store.getStoreName() != null) {
                            tvStoreName.setText(store.getStoreName());
                        }
                        // Load store logo if needed
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(String error) {
                    // Handle error silently or show toast
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filterOrders(String filter) {
        currentFilter = filter;
        updateChipSelection();
        displayFilteredOrders();
    }

    private void updateChipSelection() {
        setChipUnselected(chipAll);
        setChipUnselected(chipPending);
        setChipUnselected(chipPreparing);
        setChipUnselected(chipReady);
        setChipUnselected(chipDelivered);

        switch (currentFilter) {
            case "All":
                setChipSelected(chipAll);
                break;
            case "Pending":
                setChipSelected(chipPending);
                break;
            case "Preparing":
                setChipSelected(chipPreparing);
                break;
            case "Ready":
                setChipSelected(chipReady);
                break;
            case "Delivered":
                setChipSelected(chipDelivered);
                break;
        }
    }

    private void setChipSelected(TextView chip) {
        if (chip == null || getContext() == null) {
            return;
        }
        try {
            chip.setBackgroundResource(R.drawable.chip_selected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setChipUnselected(TextView chip) {
        if (chip == null || getContext() == null) {
            return;
        }
        try {
            chip.setBackgroundResource(R.drawable.chip_unselected);
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_orange));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadOrders() {
        if (currentStoreId == null || currentStoreId.isEmpty() || repository == null) {
            return;
        }

        try {
            showLoading(true);

            repository.getOrdersByStoreId(currentStoreId,
                    new FirebaseRepository.OrderListCallback() {
                        @Override
                        public void onSuccess(List<OrderEntity> orders) {
                            if (!isAdded() || getContext() == null) return;
                            try {
                                showLoading(false);
                                if (orders != null) {
                                    orderList.clear();
                                    orderList.addAll(orders);
                                    displayFilteredOrders();
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

    private void displayFilteredOrders() {
        if (recyclerView == null || layoutNoOrders == null) {
            return;
        }

        try {
            List<OrderEntity> filteredOrders = new ArrayList<>();

            if (currentFilter != null && currentFilter.equals("All")) {
                filteredOrders = new ArrayList<>(orderList);
            } else {
                for (OrderEntity order : orderList) {
                    if (order != null && order.getStatus() != null && order.getStatus().equals(currentFilter)) {
                        filteredOrders.add(order);
                    }
                }
            }

            if (filteredOrders.isEmpty()) {
                layoutNoOrders.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                layoutNoOrders.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);

                adapter = new OrderAdapter(filteredOrders, order -> {
                    if (order != null) {
                        updateOrderStatus(order);
                    }
                });
                recyclerView.setAdapter(adapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateOrderStatus(OrderEntity order) {
        if (order == null || repository == null || getContext() == null) {
            return;
        }

        try {
            String currentStatus = order.getStatus();
            if (currentStatus == null) {
                return;
            }

            String newStatus;
            switch (currentStatus) {
                case "Pending":
                    newStatus = "Preparing";
                    break;
                case "Preparing":
                    newStatus = "Ready";
                    break;
                case "Ready":
                    newStatus = "Delivered";
                    break;
                default:
                    return;
            }

            order.setStatus(newStatus);

            repository.updateOrder(order.getId(), order, new FirebaseRepository.GenericCallback() {
                @Override
                public void onSuccess() {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(),
                                "Order status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        loadOrders();
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (isAdded() && getContext() != null) {
                        Toast.makeText(requireContext(),
                                "Failed to update status: " + (error != null ? error : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}