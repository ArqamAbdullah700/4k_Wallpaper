package com.wallpaper.appdev;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class ImageViewActivity extends AppCompatActivity {
    ImageView imageView, backImageBtn;
    ProgressBar progressBar;
    FloatingActionButton delete, share, fabFullScreen;
    CardView setAsWall;
    String imageUrl, imageThumbUrl;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_view);
        imageView = findViewById(R.id.imageViewDetail);
        backImageBtn = findViewById(R.id.backImageBtn);
        progressBar = findViewById(R.id.progressBar);
        delete = findViewById(R.id.fabDelete);
        share = findViewById(R.id.fabShare);
        fabFullScreen = findViewById(R.id.fabFullScreen);
        setAsWall = findViewById(R.id.setAsWallpaper);
        imageUrl = getIntent().getStringExtra("imageUrlOriginal");
        imageThumbUrl = getIntent().getStringExtra("imageUrlThumb");
        FirebaseStorage storage = FirebaseStorage.getInstance();

        backImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ImageViewActivity.this, MainActivity.class));
                finish();
            }
        });
        String imageName = extractImageName(imageUrl);
        Log.e("Arqam", imageName);
        storageReference = storage.getReference().child("4kWallpapers").child("Images").child(imageName);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //shareImage();
                shareUnsplashImage();
            }
        });

        setAsWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setWallpaper();
            }
        });
        fabFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ImageViewActivity.this, FullImageViewActivity.class);
                intent.putExtra("imageResId", imageUrl);
                startActivity(intent);
            }
        });

        Picasso.get()
                .load(imageUrl)
                .into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        progressBar.setVisibility(View.GONE);
                    }
                });

    }

    private class DeleteImageTask extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            overridePendingTransition(0, 0);
            // startActivity(getIntent());
            Intent intent = new Intent(ImageViewActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

        @Override
        protected Void doInBackground(String... params) {
            String apiEndpoint = params[0];
            String srNo = params[1];
            String imageUrl = params[2];

            Log.d("Test", "API END Point:" + apiEndpoint);
            Log.d("Test", "SR no: " + srNo);
            Log.d("Test", "Image Url: " + imageUrl);

            try {
                // Create URL
                URL url = new URL("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/deleteWallpaper.php?sr_no=" + srNo + "&image_url=" + apiEndpoint);

                // Create connection
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Create request parameters
                Map<String, String> parameters = new HashMap<>();
                parameters.put("sr_no", srNo);
                parameters.put("image_url", apiEndpoint);

                // Write parameters to the connection
                OutputStream os = connection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(parameters));
                writer.flush();
                writer.close();
                os.close();

                int responseCode = connection.getResponseCode();
                Log.d("Test", "Response Code: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream is = connection.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                    StringBuilder responseStringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseStringBuilder.append(line);
                    }
                    JSONObject jsonObject = new JSONObject(responseStringBuilder.toString());
                    String response = jsonObject.getString("status");

                    if ("success".equals(response)) {
                        runOnUiThread(() -> Toast.makeText(ImageViewActivity.this, "Image deleted successfully", Toast.LENGTH_SHORT).show());
                    } else {
                        runOnUiThread(() -> Toast.makeText(ImageViewActivity.this, "Failed to delete image", Toast.LENGTH_SHORT).show());
                    }
                }

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


    }

    private String getPostDataString(Map<String, String> params) throws UnsupportedEncodingException {
        Uri.Builder builder = new Uri.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.appendQueryParameter(entry.getKey(), entry.getValue());
        }
        return builder.build().getEncodedQuery();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this image?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new DeleteImageTask().execute(imageUrl, imageThumbUrl.replace("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/", ""), imageUrl.replace("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/", ""));
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private String extractImageName(String url) {
        String imageName = getImageNameFromUrl(url);
        return imageName;
    }

    private void setWallpaper() {
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);

        // Get the Drawable from the ImageView
        imageView.setDrawingCacheEnabled(true);
        Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();

        try {
            wallpaperManager.setBitmap(bitmap);
            showToast("Wallpaper set successfully");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Failed to set wallpaper");
        }
    }


    private void shareUnsplashImage() {
        try {
            imageView.setDrawingCacheEnabled(true);

            Bitmap bitmap = imageView.getDrawingCache();

            // Create an Intent with ACTION_SEND
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/jpeg");

            // Add the image as an extra to the intent
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "4K_xWallpaper" + extractImageName(imageUrl), null);
            Uri imageUri = Uri.parse(path);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);

            // Add any text you want to share with the image
            shareIntent.putExtra(Intent.EXTRA_TEXT, "Please rate us on google play store.");

            // Launch the share activity
            startActivity(Intent.createChooser(shareIntent, "Share Image"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static String getImageNameFromUrl(String imageUrl) {
        Uri uri = Uri.parse(imageUrl);
        String path = uri.getPath();

        if (path != null) {
            int lastSlashIndex = path.lastIndexOf('/');
            if (lastSlashIndex != -1 && lastSlashIndex < path.length() - 1) {
                return path.substring(lastSlashIndex + 1);
            }
        }
        return null;
    }


}