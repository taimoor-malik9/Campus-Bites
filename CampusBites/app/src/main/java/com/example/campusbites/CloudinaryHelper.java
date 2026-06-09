package com.example.campusbites;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {

    private static final String TAG = "CloudinaryHelper";
    private static CloudinaryHelper instance;
    private static boolean isInitialized = false;

    // IMPORTANT: Replace with YOUR Cloudinary credentials
    private static final String CLOUD_NAME = "dmtxmhgh8";
    private static final String API_KEY = "759911352253458";
    private static final String API_SECRET = "0ZcKHDXOxCLeCXxLjhmVmiuV-OY";

    private CloudinaryHelper() {}

    public static synchronized CloudinaryHelper getInstance() {
        if (instance == null) {
            instance = new CloudinaryHelper();
        }
        return instance;
    }

    /**
     * Initialize Cloudinary - Call this in Application class or before first upload
     */
    public void initialize(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", CLOUD_NAME);
            config.put("api_key", API_KEY);
            config.put("api_secret", API_SECRET);
            config.put("secure", "true");

            MediaManager.init(context, config);
            isInitialized = true;
            Log.d(TAG, "Cloudinary initialized");
        }
    }

    /**
     * Upload image to Cloudinary
     * @param context Application context
     * @param imageUri URI of the image to upload
     * @param folder Folder name in Cloudinary (e.g., "store_logos", "food_items")
     * @param callback Callback for upload result
     */
    @SuppressWarnings("unchecked")
    public void uploadImage(Context context, Uri imageUri, String folder,
                            CloudinaryUploadCallback callback) {
        if (!isInitialized) {
            initialize(context);
        }

        // Upload options
        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("resource_type", "image");
        options.put("quality", "auto");
        options.put("fetch_format", "auto");

        // Generate unique filename
        String publicId = folder + "/" + System.currentTimeMillis();
        options.put("public_id", publicId);

        MediaManager.get()
                .upload(imageUri)
                .options(options)
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                        callback.onUploadStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        int progress = (int) ((bytes * 100) / totalBytes);
                        Log.d(TAG, "Upload progress: " + progress + "%");
                        callback.onProgress(progress);
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload successful: " + imageUrl);
                        callback.onSuccess(imageUrl);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload failed: " + error.getDescription());
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.w(TAG, "Upload rescheduled: " + error.getDescription());
                    }
                })
                .dispatch();
    }

    /**
     * Get optimized image URL with transformations
     * @param imageUrl Original Cloudinary URL
     * @param width Desired width
     * @param height Desired height
     * @return Transformed URL
     */
    public String getOptimizedImageUrl(String imageUrl, int width, int height) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return "";
        }

        // Example: Convert URL to optimized version
        // Original: https://res.cloudinary.com/demo/image/upload/v1234/sample.jpg
        // Optimized: https://res.cloudinary.com/demo/image/upload/w_300,h_300,c_fill/v1234/sample.jpg

        String transformation = "w_" + width + ",h_" + height + ",c_fill,q_auto,f_auto";
        return imageUrl.replace("/upload/", "/upload/" + transformation + "/");
    }

    /**
     * Delete image from Cloudinary
     * @param publicId Public ID of the image (from upload)
     * @param callback Callback for delete result
     */
    public void deleteImage(String publicId, CloudinaryDeleteCallback callback) {
        // Note: Deletion requires server-side implementation for security
        // You'll need to create a backend API to handle deletions
        // For now, just log
        Log.d(TAG, "Delete requested for: " + publicId);
        callback.onDeleteComplete();
    }

    // Callback interfaces
    public interface CloudinaryUploadCallback {
        void onUploadStart();
        void onProgress(int progress);
        void onSuccess(String imageUrl);
        void onError(String error);
    }

    public interface CloudinaryDeleteCallback {
        void onDeleteComplete();
    }
}