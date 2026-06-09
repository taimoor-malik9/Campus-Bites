package com.example.campusbites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CustomerProfileFragment extends Fragment {

    private EditText etName, etEmail, etPhone, etAddress;
    private Button btnUpdate, btnLogout, btnDeleteAccount;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private String customerId;
    private Customer currentCustomer;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_customer_profile, container, false);

        try {
            repository = FirebaseRepository.getInstance();
            if (repository == null || getContext() == null) {
                return view;
            }

            SharedPreferences prefs = requireContext()
                    .getSharedPreferences("CampusBitesPrefs", Context.MODE_PRIVATE);
            customerId = prefs.getString("USER_ID", null);

            if (customerId == null || customerId.isEmpty()) {
                return view;
            }

            initializeViews(view);
            loadCustomerData();
            setupListeners();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    private void initializeViews(View view) {
        try {
            etName = view.findViewById(R.id.etName);
            etEmail = view.findViewById(R.id.etEmail);
            etPhone = view.findViewById(R.id.etPhone);
            etAddress = view.findViewById(R.id.etAddress);
            btnUpdate = view.findViewById(R.id.btnUpdate);
            btnLogout = view.findViewById(R.id.btnLogout);
            btnDeleteAccount = view.findViewById(R.id.btnDeleteAccount);
            progressBar = view.findViewById(R.id.progressBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            if (btnUpdate != null) {
                btnUpdate.setOnClickListener(v -> {
                    try {
                        updateProfile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (btnLogout != null) {
                btnLogout.setOnClickListener(v -> {
                    try {
                        logout();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (btnDeleteAccount != null) {
                btnDeleteAccount.setOnClickListener(v -> {
                    try {
                        confirmDeleteAccount();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCustomerData() {
        if (customerId == null || customerId.isEmpty() || repository == null) {
            return;
        }

        try {
            showLoading(true);

            repository.getCustomerById(customerId, new FirebaseRepository.CustomerCallback() {
                @Override
                public void onSuccess(Customer customer) {
                    if (!isAdded() || getContext() == null) {
                        return;
                    }
                    try {
                        showLoading(false);
                        currentCustomer = customer;

                        if (customer != null) {
                            if (etName != null && customer.getFullName() != null) {
                                etName.setText(customer.getFullName());
                            }
                            if (etEmail != null && customer.getEmail() != null) {
                                etEmail.setText(customer.getEmail());
                            }
                            if (etPhone != null && customer.getPhone() != null) {
                                etPhone.setText(customer.getPhone());
                            }
                            if (etAddress != null && customer.getAddress() != null) {
                                etAddress.setText(customer.getAddress());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showLoading(false);
                    }
                }

                @Override
                public void onFailure(String error) {
                    if (isAdded() && getContext() != null) {
                        showLoading(false);
                        Toast.makeText(requireContext(),
                                "Failed to load profile: " + (error != null ? error : "Unknown error"), 
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
        }
    }

    private void updateProfile() {
        if (!isAdded() || getContext() == null) {
            return;
        }

        if (currentCustomer == null || repository == null || customerId == null || customerId.isEmpty()) {
            Toast.makeText(requireContext(), "Profile data not loaded yet",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (etName == null || etPhone == null) {
            Toast.makeText(requireContext(), "Fields not initialized",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress != null ? etAddress.getText().toString().trim() : "";

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(requireContext(), "Name and phone are required",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            showLoading(true);

            currentCustomer.setFullName(name);
            currentCustomer.setPhone(phone);
            if (address != null) {
                currentCustomer.setAddress(address);
            }

            repository.updateCustomer(customerId, currentCustomer,
                    new FirebaseRepository.GenericCallback() {
                        @Override
                        public void onSuccess() {
                            if (!isAdded() || getContext() == null) {
                                return;
                            }
                            try {
                                showLoading(false);

                                // Update SharedPreferences
                                SharedPreferences prefs = requireContext()
                                        .getSharedPreferences("CampusBitesPrefs", Context.MODE_PRIVATE);
                                prefs.edit()
                                        .putString("CUSTOMER_NAME", name)
                                        .putString("CUSTOMER_PHONE", phone)
                                        .apply();

                                Toast.makeText(requireContext(),
                                        "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                e.printStackTrace();
                                showLoading(false);
                            }
                        }

                        @Override
                        public void onFailure(String error) {
                            if (isAdded() && getContext() != null) {
                                showLoading(false);
                                Toast.makeText(requireContext(),
                                        "Update failed: " + (error != null ? error : "Unknown error"), 
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
            showLoading(false);
            Toast.makeText(requireContext(),
                    "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmDeleteAccount() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Account")
                .setMessage("Are you sure? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteAccount() {
        showLoading(true);

        repository.deleteCustomer(customerId, new FirebaseRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.delete()
                            .addOnCompleteListener(task -> {
                                showLoading(false);
                                logout();
                            });
                } else {
                    logout();
                }
            }

            @Override
            public void onFailure(String error) {
                showLoading(false);
                Toast.makeText(requireContext(),
                        "Failed to delete account: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        repository.logout();

        SharedPreferences prefs = requireContext()
                .getSharedPreferences("CampusBitesPrefs", Context.MODE_PRIVATE);
        prefs.edit().clear().apply();

        // Clear cart
        CartManager.getInstance(requireContext()).clearCart();

        Intent intent = new Intent(requireContext(), RoleSelectionActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (btnUpdate != null) {
                btnUpdate.setEnabled(!show);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}