package com.wallpaper.appdev;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import android.app.AlertDialog;
import android.app.WallpaperManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;


import java.io.IOException;


public class ImageViewActivity extends AppCompatActivity {
    ImageView imageView, backImageBtn;
    ProgressBar progressBar;
    FloatingActionButton delete,share,fabFullScreen;
    CardView setAsWall;
    String imageUrl;
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
        imageUrl = getIntent().getStringExtra("imageUrl");
        FirebaseStorage storage = FirebaseStorage.getInstance();

        backImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ImageViewActivity.this, MainActivity.class));
            }
        });
        String imageName = extractImageName(imageUrl);
        Log.e("Arqam",imageName);
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
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Delete");
        builder.setMessage("Are you sure you want to delete this image?");

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteImage();
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
    private void deleteImage() {
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        showToast("Image deleted successfully");
                        finish();
                        startActivity(new Intent(ImageViewActivity.this, MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        showToast("Error deleting image: " + exception.getMessage());
                    }
                });
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
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "IslamicWallpapers_"+extractImageName(imageUrl), null);
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