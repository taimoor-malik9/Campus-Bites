package com.example.campusbites;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.campusbites.FoodItem;
import com.example.campusbites.Store;
import com.example.campusbites.FirebaseRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private FoodAdapter adapter;
    private FirebaseRepository repository;
    private String currentStoreId;
    private TextView tvStoreName;
    private ImageView ivStoreLogo;
    private ProgressBar progressBar;
    private List<FoodItem> foodList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        try {
            repository = FirebaseRepository.getInstance();
            if (repository == null || getContext() == null) {
                return view;
            }

            SharedPreferences prefs = requireContext().getSharedPreferences(
                    "SmartCanteenPrefs", requireContext().MODE_PRIVATE);

            try {
                // Try to get it as a String (Normal case)
                currentStoreId = prefs.getString("STORE_ID", null);
            } catch (ClassCastException e) {
                // If it crashes, it means it's an Integer. Get it as int and convert to String.
                try {
                    int idAsInt = prefs.getInt("STORE_ID", -1);
                    if (idAsInt != -1) {
                        currentStoreId = String.valueOf(idAsInt);
                        // AUTO-FIX: Overwrite the bad Integer with the correct String so it doesn't happen again
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
            loadStoreData();
            loadMenuItems();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initializeViews(View view) {
        try {
            tvStoreName = view.findViewById(R.id.tvStoreName);
            ivStoreLogo = view.findViewById(R.id.ivStoreLogo);
            recyclerView = view.findViewById(R.id.rvMenuItems);
            progressBar = view.findViewById(R.id.progressBar);

            FloatingActionButton fabAdd = view.findViewById(R.id.fabAddItem);
            if (fabAdd != null && getContext() != null) {
                fabAdd.setOnClickListener(v -> {
                    try {
                        Intent intent = new Intent(requireContext(), AddEditItemActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
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
            adapter = new FoodAdapter(foodList,
                    item -> {
                        // Edit click
                        try {
                            if (getContext() != null && item != null) {
                                Intent intent = new Intent(requireContext(), AddEditItemActivity.class);
                                intent.putExtra("ITEM_ID", item.getId());
                                startActivity(intent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    },
                    item -> {
                        // Delete click
                        if (item != null) {
                            deleteFoodItem(item);
                        }
                    });
            recyclerView.setAdapter(adapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStoreData();
        loadMenuItems();
    }

    private void loadStoreData() {
        if (getContext() == null || repository == null) {
            return;
        }

        try {
            // 1. Get the Store ID from SharedPreferences (Handling the String/Int issue safely)
            SharedPreferences prefs = requireContext().getSharedPreferences("SmartCanteenPrefs", requireContext().MODE_PRIVATE);
            String safeStoreId = prefs.getString("STORE_ID", null);

            // If it was null, try the backup int method (Just to be safe based on previous crashes)
            if (safeStoreId == null) {
                int idAsInt = prefs.getInt("STORE_ID", -1);
                if (idAsInt != -1) safeStoreId = String.valueOf(idAsInt);
            }

            if (safeStoreId == null || safeStoreId.isEmpty()) {
                return;
            }

            // 2. Fetch Data from Firebase
            repository.getStoreById(safeStoreId, new FirebaseRepository.StoreCallback() {
                @Override
                public void onSuccess(Store store) {
                    if (!isAdded() || getContext() == null || store == null) {
                        return;
                    }
                    try {
                        // Set Name
                        if (tvStoreName != null && store.getStoreName() != null) {
                            tvStoreName.setText(store.getStoreName());
                        }

                        // --- THIS IS THE NEW CODE TO DISPLAY THE LOGO ---
                        String logoUrl = store.getStoreImageUrl();

                        if (logoUrl != null && !logoUrl.isEmpty() && ivStoreLogo != null) {
                            // Android requires HTTPS
                            if (logoUrl.startsWith("http://")) {
                                logoUrl = logoUrl.replace("http://", "https://");
                            }

                            // Load with Glide
                            if (isAdded() && getContext() != null) { // Check if fragment is attached
                                try {
                                    com.bumptech.glide.Glide.with(requireContext())
                                            .load(logoUrl)
                                            .placeholder(R.drawable.outline_garage_home_24) // Show default while loading
                                            .error(R.drawable.outline_garage_home_24)       // Show default if error
                                            .circleCrop()                      // Optional: Makes it round
                                            .into(ivStoreLogo);
                                } catch (Exception e) {
                                    e.printStackTrace();
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
                        Toast.makeText(requireContext(), "Failed to load store: " + (error != null ? error : "Unknown error"), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadMenuItems() {
        if (currentStoreId == null || currentStoreId.isEmpty() || repository == null) {
            return;
        }

        try {
            showLoading(true);

            repository.getFoodItemsByStoreId(currentStoreId,
                    new FirebaseRepository.FoodListCallback() {
                        @Override
                        public void onSuccess(List<FoodItem> items) {
                            if (!isAdded() || getContext() == null) return;
                            try {
                                showLoading(false);
                                if (items != null) {
                                    foodList.clear();
                                    foodList.addAll(items);
                                    if (adapter != null) {
                                        adapter.notifyDataSetChanged();
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
                                        "Failed to load menu items: " + (error != null ? error : "Unknown error"), 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
        }
    }

    private void deleteFoodItem(FoodItem item) {
        repository.deleteFoodItem(item.getId(), new FirebaseRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(requireContext(),
                        "Item deleted", Toast.LENGTH_SHORT).show();
                loadMenuItems();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(),
                        "Failed to delete: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}