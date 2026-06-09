package com.example.campusbites;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

public class CustomerHomeFragment extends Fragment implements StoreAdapter.OnStoreClickListener {

    private RecyclerView rvStores;
    private ProgressBar progressBar;
    private TextView tvNoStores;
    private TextView tvStoreCount;
    private EditText etSearch;
    private ImageView ivSearchIcon;
    private ImageView ivNotification;
    private Chip chipAll, chipPopular, chipNearby;
    private FloatingActionButton fabAiAgent;
    private StoreAdapter adapter;
    private FirebaseRepository repository;
    private List<Store> storeList = new ArrayList<>();
    private List<Store> filteredStoreList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_home, container, false);

        repository = FirebaseRepository.getInstance();
        initializeViews(view);
        setupRecyclerView();
        setupChipFilters();
        loadStores();

        return view;
    }

    private void initializeViews(View view) {
        rvStores = view.findViewById(R.id.rvStores);
        progressBar = view.findViewById(R.id.progressBar);
        tvNoStores = view.findViewById(R.id.tvNoStores);
        tvStoreCount = view.findViewById(R.id.tvStoreCount);
        etSearch = view.findViewById(R.id.etSearch);
        ivSearchIcon = view.findViewById(R.id.ivSearchIcon);
        ivNotification = view.findViewById(R.id.ivNotification);
        chipAll = view.findViewById(R.id.chipAll);
        chipPopular = view.findViewById(R.id.chipPopular);
        chipNearby = view.findViewById(R.id.chipNearby);
        fabAiAgent = view.findViewById(R.id.fabAiAgent);

        ivNotification.setOnClickListener(v -> openNotifications());

        if (fabAiAgent != null) {
            fabAiAgent.setOnClickListener(v -> openAiAgent());
        }

        // Setup search functionality
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filterStores(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }
    }

    private void openAiAgent() {
        if (!isAdded() || getContext() == null) return;
        startActivity(new Intent(requireContext(), AiAssistantActivity.class));
    }

    private void setupRecyclerView() {
        rvStores.setLayoutManager(new LinearLayoutManager(requireContext()));
        filteredStoreList = new ArrayList<>(storeList);
        adapter = new StoreAdapter(filteredStoreList, this);
        rvStores.setAdapter(adapter);
    }

    private void setupChipFilters() {
        chipAll.setOnClickListener(v -> {
            filterStores(etSearch.getText().toString());
            updateChipSelection(chipAll);
        });

        chipPopular.setOnClickListener(v -> {
            filterStoresByRating();
            updateChipSelection(chipPopular);
        });

        chipNearby.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Location feature coming soon", Toast.LENGTH_SHORT).show();
            updateChipSelection(chipNearby);
        });

        chipAll.setChecked(true);
    }

    private void updateChipSelection(Chip selectedChip) {
        chipAll.setChecked(selectedChip == chipAll);
        chipPopular.setChecked(selectedChip == chipPopular);
        chipNearby.setChecked(selectedChip == chipNearby);
    }

    // Fixed the sorting issue by adding a getRating method to the Store class
    private void filterStoresByRating() {
        filteredStoreList.clear();
        List<Store> sortedStores = new ArrayList<>(storeList);
        sortedStores.sort((s1, s2) -> Float.compare(s2.getRating(), s1.getRating()));
        filteredStoreList.addAll(sortedStores);
        adapter.notifyDataSetChanged();
        updateStoreCount();
    }

    // Updated updateStoreCount to use string resources
    private void updateStoreCount() {
        if (tvStoreCount != null) {
            tvStoreCount.setText(getString(R.string.store_count, filteredStoreList.size()));
        }
    }

    private void openNotifications() {
        Toast.makeText(requireContext(), "Notifications feature coming soon", Toast.LENGTH_SHORT).show();
    }

    // Updated filterStores to use string resources
    private void filterStores(String query) {
        filteredStoreList.clear();
        if (query.isEmpty()) {
            filteredStoreList.addAll(storeList);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Store store : storeList) {
                if (store.getStoreName().toLowerCase().contains(lowerQuery) ||
                    (store.getStoreDescription() != null && store.getStoreDescription().toLowerCase().contains(lowerQuery))) {
                    filteredStoreList.add(store);
                }
            }
        }
        adapter.notifyDataSetChanged();

        if (tvStoreCount != null) {
            tvStoreCount.setText(getString(R.string.store_count, filteredStoreList.size()));
        }

        if (filteredStoreList.isEmpty() && !query.isEmpty()) {
            tvNoStores.setText(getString(R.string.no_stores_matching, query));
            tvNoStores.setVisibility(View.VISIBLE);
            rvStores.setVisibility(View.GONE);
        } else if (filteredStoreList.isEmpty()) {
            tvNoStores.setText(R.string.no_stores_available);
            tvNoStores.setVisibility(View.VISIBLE);
            rvStores.setVisibility(View.GONE);
        } else {
            tvNoStores.setVisibility(View.GONE);
            rvStores.setVisibility(View.VISIBLE);
        }
    }

    // Updated loadStores to use string resources
    private void loadStores() {
        showLoading(true);

        repository.getAllStores(new FirebaseRepository.StoreListCallback() {
            @Override
            public void onSuccess(List<Store> stores) {
                showLoading(false);
                storeList.clear();
                storeList.addAll(stores);
                filteredStoreList.clear();
                filteredStoreList.addAll(stores);

                if (adapter == null) {
                    setupRecyclerView();
                } else {
                    adapter.notifyDataSetChanged();
                }

                if (tvStoreCount != null) {
                    tvStoreCount.setText(getString(R.string.store_count, stores.size()));
                }

                if (stores.isEmpty()) {
                    tvNoStores.setText(R.string.no_stores_available);
                    tvNoStores.setVisibility(View.VISIBLE);
                    rvStores.setVisibility(View.GONE);
                } else {
                    tvNoStores.setVisibility(View.GONE);
                    rvStores.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(requireContext(),
                        getString(R.string.failed_to_load_stores, error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStoreClick(Store store) {
        Intent intent = new Intent(requireContext(), StoreDetailActivity.class);
        intent.putExtra("STORE_ID", store.getId());
        intent.putExtra("STORE_NAME", store.getStoreName());
        startActivity(intent);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStores();
    }
}