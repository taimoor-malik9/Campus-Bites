package com.example.campusbites;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.widget.ImageView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class ImageUtils {

    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            String filename = "img_" + System.currentTimeMillis() + ".jpg";
            File directory = context.getFilesDir();
            File file = new File(directory, filename);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
            inputStream.close();

            return file.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadCustomImage(Context context, ImageView imageView, String source) {
        if (source == null || source.isEmpty()) return;

        try {
            if (source.startsWith("/")) {
                // Real File Path
                File imgFile = new File(source);
                if (imgFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }
            } else if (source.startsWith("mipmap") || source.startsWith("drawable")) {
                // App Resource
                String[] parts = source.split("/");
                String name = parts[parts.length - 1];
                String folder = parts[0].equals("mipmap") ? "mipmap" : "drawable";
                int resId = context.getResources().getIdentifier(name, folder, context.getPackageName());
                if (resId != 0) imageView.setImageResource(resId);
            } else if (source.startsWith("android")) {
                // System Resource
                String[] parts = source.split("/");
                if (parts.length > 2) {
                    int resId = context.getResources().getIdentifier(parts[2], "drawable", "android");
                    if (resId != 0) imageView.setImageResource(resId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}