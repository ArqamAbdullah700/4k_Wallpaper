package com.wallpaper.appdev;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class AddWallaperActivity extends AppCompatActivity {
    TextView addImageTv;
    ImageView imagePreview;
    Button uploadImageBtn;
    String[] cameraPermissions, storagePermissions;
    final int CAMERA_REQUEST_CODE = 100;
    final int STORAGE_REQUEST_CODE = 200;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallaper);
        addImageTv = findViewById(R.id.addImageTv);
        imagePreview = findViewById(R.id.imagePreview);
        uploadImageBtn = findViewById(R.id.uploadImageButton);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};

        addImageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pickImageFromGallery();
            }
        });
        uploadImageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedImageUri != null) {
                    uploadImage(selectedImageUri);
                } else {
                    Toast.makeText(AddWallaperActivity.this, "Please choose an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void pickImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected image URI
            assert data.getData() != null;
            // Set the image to the ImageView
            addImageTv.setVisibility(View.GONE);
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageURI(data.getData());
            selectedImageUri = data.getData();
        }
    }

    private void uploadImage(Uri imageUri) {
        new UploadImageTask().execute(imageUri);
    }

    private class UploadImageTask extends AsyncTask<Uri, Void, Void> {

        @Override
        protected Void doInBackground(Uri... uris) {
            Uri imageUri = uris[0];

            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Retrofit setup
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/") // Change this to the URL of your PHP API
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                // Retrofit service interface
                ApiService apiService = retrofit.create(ApiService.class);
                Log.d("UploadImageTask", "API URL: " + retrofit.baseUrl());

                // Create a request body with the image bytes
                RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("image", "image.jpg", requestFile);

                // Make the API call
                Call<JsonObject> call = apiService.uploadImage(body);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddWallaperActivity.this, "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddWallaperActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(AddWallaperActivity.this, "Error uploading image", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddWallaperActivity.this, MainActivity.class));
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("UploadImageTask", "Failure: " + t.getMessage());
                        Toast.makeText(AddWallaperActivity.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}