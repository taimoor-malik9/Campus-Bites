package com.example.campusbites;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import com.example.campusbites.Store;
import com.example.campusbites.FirebaseRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.bumptech.glide.Glide; // Make sure Glide is imported

public class ProfileFragment extends Fragment {

    private EditText etStoreName, etOwnerName, etEmail;
    // Added etDescription if you have it in XML, otherwise remove it
    private EditText etDescription;

    private Button btnUpdate, btnDelete, btnLogout, btnChangeLogo;
    private ImageView ivProfileLogo, ivProfileHeaderLogo;
    private TextView tvProfileHeaderName;
    private ProgressBar progressBar;

    private FirebaseRepository repository;
    private String currentStoreId;
    private Uri selectedLogoUri;
    private String currentLogoUrl = "";
    private Store currentStore;
    private ActivityResultLauncher<Intent> logoLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        try {
            repository = FirebaseRepository.getInstance();
            if (repository == null) {
                return view;
            }

            // --- SAFE STORE ID LOGIC ---
            if (getContext() == null) {
                return view;
            }

            SharedPreferences prefs = requireContext().getSharedPreferences(
                    "SmartCanteenPrefs", Context.MODE_PRIVATE);

            try {
                currentStoreId = prefs.getString("STORE_ID", null);
            } catch (ClassCastException e) {
                int idAsInt = prefs.getInt("STORE_ID", -1);
                if (idAsInt != -1) currentStoreId = String.valueOf(idAsInt);
            }

            if (currentStoreId == null || currentStoreId.isEmpty()) {
                // Don't call logout here - just return and let MainActivity handle it
                return view;
            }

            // Initialize views FIRST
            initializeViews(view);
            
            // Register ActivityResultLauncher AFTER views are initialized
            try {
                logoLauncher = registerForActivityResult(
                        new ActivityResultContracts.StartActivityForResult(),
                        result -> {
                            if (result.getResultCode() == android.app.Activity.RESULT_OK
                                    && result.getData() != null) {
                                selectedLogoUri = result.getData().getData();
                                if (selectedLogoUri != null && ivProfileLogo != null) {
                                    try {
                                        ivProfileLogo.setImageURI(selectedLogoUri);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Only proceed if views were initialized successfully
            if (etStoreName != null && etOwnerName != null && etEmail != null) {
                loadStoreData();
                setupListeners();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // If anything fails, return empty view to prevent crash
        }

        return view;
    }

    private void initializeViews(View view) {
        try {
            etStoreName = view.findViewById(R.id.etProfileStoreName);
            etOwnerName = view.findViewById(R.id.etProfileOwnerName);
            etEmail = view.findViewById(R.id.etProfileEmail);
            // Assuming you have description in XML, if not remove this line
            // etDescription = view.findViewById(R.id.etProfileDescription);

            ivProfileLogo = view.findViewById(R.id.ivProfileLogo);
            ivProfileHeaderLogo = view.findViewById(R.id.ivProfileHeaderLogo);
            tvProfileHeaderName = view.findViewById(R.id.tvProfileHeaderName);
            btnChangeLogo = view.findViewById(R.id.btnChangeLogo);
            btnUpdate = view.findViewById(R.id.btnUpdateProfile);
            btnDelete = view.findViewById(R.id.btnDeleteAccount);
            btnLogout = view.findViewById(R.id.btnLogout);
            progressBar = view.findViewById(R.id.progressBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupListeners() {
        try {
            if (btnChangeLogo != null && logoLauncher != null) {
                btnChangeLogo.setOnClickListener(v -> {
                    try {
                        if (getContext() != null && isAdded()) {
                            Intent intent = new Intent(Intent.ACTION_PICK);
                            intent.setType("image/*");
                            logoLauncher.launch(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            if (btnUpdate != null) {
                btnUpdate.setOnClickListener(v -> {
                    try {
                        updateProfile();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            if (btnDelete != null) {
                btnDelete.setOnClickListener(v -> {
                    try {
                        confirmDeleteAccount();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadStoreData() {
        if (currentStoreId == null || currentStoreId.isEmpty() || repository == null) {
            return;
        }

        try {
            showLoading(true);

            repository.getStoreById(currentStoreId, new FirebaseRepository.StoreCallback() {
                @Override
                public void onSuccess(Store store) {
                    if (!isAdded() || getContext() == null) return; // Safety check

                    try {
                        currentStore = store;
                        if (store == null) {
                            showLoading(false);
                            return;
                        }

                        currentLogoUrl = store.getStoreImageUrl();

                        if (etStoreName != null && store.getStoreName() != null) {
                            etStoreName.setText(store.getStoreName());
                        }
                        if (etOwnerName != null && store.getOwnerName() != null) {
                            etOwnerName.setText(store.getOwnerName());
                        }
                        if (etEmail != null && store.getOwnerEmail() != null) {
                            etEmail.setText(store.getOwnerEmail());
                        }
                        // if (etDescription != null) etDescription.setText(store.getStoreDescription());

                        // Update header
                        if (tvProfileHeaderName != null && store.getStoreName() != null) {
                            tvProfileHeaderName.setText(store.getStoreName());
                        }

                        // --- IMAGE LOADING WITH GLIDE ---
                        if (currentLogoUrl != null && !currentLogoUrl.isEmpty()) {
                            if (currentLogoUrl.startsWith("http://")) {
                                currentLogoUrl = currentLogoUrl.replace("http://", "https://");
                            }

                            try {
                                // Load in profile logo
                                if (ivProfileLogo != null) {
                                    Glide.with(requireContext())
                                            .load(currentLogoUrl)
                                            .placeholder(R.mipmap.ic_launcher)
                                            .error(R.mipmap.ic_launcher)
                                            .circleCrop()
                                            .into(ivProfileLogo);
                                }
                                // Load in header logo
                                if (ivProfileHeaderLogo != null) {
                                    Glide.with(requireContext())
                                            .load(currentLogoUrl)
                                            .placeholder(R.mipmap.ic_launcher)
                                            .error(R.mipmap.ic_launcher)
                                            .circleCrop()
                                            .into(ivProfileHeaderLogo);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        showLoading(false);
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
                                "Failed to load profile: " + error, Toast.LENGTH_SHORT).show();
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

        if (currentStore == null) {
            Toast.makeText(requireContext(), "Profile data not loaded yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etStoreName == null || etOwnerName == null || etEmail == null) {
            Toast.makeText(requireContext(), "Fields not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etStoreName.getText().toString().trim();
        String newOwner = etOwnerName.getText().toString().trim();
        String newEmail = etEmail.getText().toString().trim();

        if (newName.isEmpty() || newOwner.isEmpty()) {
            Toast.makeText(requireContext(), "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        String currentEmail = currentStore.getOwnerEmail();
        // Handle null email just in case
        if (currentEmail == null) currentEmail = "";

        if (!newEmail.equals(currentEmail)) {
            updateEmailAndProfile(newEmail, newName, newOwner);
        } else {
            if (selectedLogoUri != null) {
                uploadLogoAndUpdate(newName, newOwner, newEmail);
            } else {
                updateStoreProfile(newName, newOwner, newEmail, currentLogoUrl);
            }
        }
    }

    private void updateEmailAndProfile(String newEmail, String newName, String newOwner) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.updateEmail(newEmail)
                    .addOnSuccessListener(aVoid -> {
                        if (selectedLogoUri != null) {
                            uploadLogoAndUpdate(newName, newOwner, newEmail);
                        } else {
                            updateStoreProfile(newName, newOwner, newEmail, currentLogoUrl);
                        }
                    })
                    .addOnFailureListener(e -> {
                        if (isAdded()) {
                            showLoading(false);
                            Toast.makeText(requireContext(),
                                    "Failed to update email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    private void uploadLogoAndUpdate(String newName, String newOwner, String newEmail) {
        // Updated to use your new "logo" type logic
        repository.uploadImage(requireContext(), selectedLogoUri, "logo",
                new FirebaseRepository.ImageUploadCallback() {
                    @Override
                    public void onSuccess(String downloadUrl) {
                        updateStoreProfile(newName, newOwner, newEmail, downloadUrl);
                    }

                    @Override
                    public void onProgress(int progress) {
                        // Optional: Update progress bar
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isAdded()) {
                            showLoading(false);
                            Toast.makeText(requireContext(),
                                    "Image upload failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateStoreProfile(String newName, String newOwner,
                                    String newEmail, String logoUrl) {
        // Update local object
        currentStore.setStoreName(newName);
        currentStore.setOwnerName(newOwner);
        currentStore.setOwnerEmail(newEmail);
        currentStore.setStoreImageUrl(logoUrl);

        repository.updateStore(currentStoreId, currentStore,
                new FirebaseRepository.GenericCallback() {
                    @Override
                    public void onSuccess() {
                        if (!isAdded()) return;

                        showLoading(false);

                        SharedPreferences prefs = requireContext().getSharedPreferences(
                                "SmartCanteenPrefs", Context.MODE_PRIVATE);
                        prefs.edit().putString("STORE_NAME", newName).apply();

                        Toast.makeText(requireContext(),
                                "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                        selectedLogoUri = null;

                        // Force refresh image if updated
                        if (logoUrl != null && !logoUrl.isEmpty()) {
                            Glide.with(requireContext())
                                    .load(logoUrl)
                                    .circleCrop()
                                    .into(ivProfileLogo);
                        }
                    }

                    @Override
                    public void onFailure(String error) {
                        if (isAdded()) {
                            showLoading(false);
                            Toast.makeText(requireContext(),
                                    "Update failed: " + error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void confirmDeleteAccount() {
        if (!isAdded() || getContext() == null) {
            return;
        }
        try {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure? This will permanently delete your store.")
                    .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteAccount() {
        showLoading(true);

        repository.deleteStore(currentStoreId, new FirebaseRepository.GenericCallback() {
            @Override
            public void onSuccess() {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    user.delete()
                            .addOnSuccessListener(aVoid -> {
                                if (isAdded()) {
                                    showLoading(false);
                                    Toast.makeText(requireContext(), "Account deleted", Toast.LENGTH_SHORT).show();
                                    logout();
                                }
                            })
                            .addOnFailureListener(e -> {
                                if (isAdded()) {
                                    showLoading(false);
                                    logout(); // Log out anyway
                                }
                            });
                } else {
                    logout();
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    showLoading(false);
                    Toast.makeText(requireContext(), "Failed to delete: " + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void logout() {
        try {
            if (repository != null) {
                repository.logout();
            }

            if (getContext() != null && isAdded()) {
                // Clear all preferences
                SharedPreferences prefs = requireContext().getSharedPreferences(
                        "SmartCanteenPrefs", Context.MODE_PRIVATE);
                prefs.edit().clear().apply();
                
                // Also clear CampusBitesPrefs for proper navigation
                SharedPreferences campusBitesPrefs = requireContext().getSharedPreferences(
                        "CampusBitesPrefs", Context.MODE_PRIVATE);
                campusBitesPrefs.edit().clear().apply();

                // Redirect to RoleSelectionActivity so user can choose customer or seller
                Intent intent = new Intent(requireContext(), RoleSelectionActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    requireActivity().finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback: try to navigate even if there's an error
            try {
                if (getContext() != null) {
                    Intent intent = new Intent(getContext(), RoleSelectionActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if (getActivity() != null) {
                        getActivity().finish();
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showLoading(boolean show) {
        if (progressBar != null) progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (btnUpdate != null) btnUpdate.setEnabled(!show);
        if (btnDelete != null) btnDelete.setEnabled(!show);
    }
}