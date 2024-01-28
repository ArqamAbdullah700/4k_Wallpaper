package com.wallpaper.appdev;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.chip.Chip;
import com.google.gson.JsonObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
    String category;
    String[] cameraPermissions, storagePermissions;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri, originalImageUri;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_wallaper);
        findViews();
        Spinner spinner = findViewById(R.id.spinner);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.Categories,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        // Set an item selection listener to handle selections
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Handle the selected item
                category = (String) parentView.getItemAtPosition(position);
                Toast.makeText(AddWallaperActivity.this, "Selected: " + category, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing here if nothing is selected
            }
        });

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
                    uploadImage(selectedImageUri, originalImageUri);
                } else {
                    Toast.makeText(AddWallaperActivity.this, "Please choose an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void findViews() {
        addImageTv = findViewById(R.id.addImageTv);
        imagePreview = findViewById(R.id.imagePreview);
        uploadImageBtn = findViewById(R.id.uploadImageButton);
        cameraPermissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};



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
            // original uri
            //  selectedImageUri = data.getData();
            originalImageUri = data.getData();
            InputStream inputStream = null;
            try {
                inputStream = AddWallaperActivity.this.getContentResolver().openInputStream(data.getData());
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            // Decode the input stream into a Bitmap
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            Bitmap thumbnail = createThumbnail(bitmap);
            //thumbnail uri
            selectedImageUri = bitmapToUri(AddWallaperActivity.this, thumbnail);


        }
    }

    public static Uri bitmapToUri(Context context, Bitmap bitmap) {
        // Get the application's cache directory
        File cacheDir = context.getExternalCacheDir();
        File file = new File(cacheDir, "image.jpg");

        try {
            // Write the Bitmap to the file
            FileOutputStream outputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();

            // Return the Uri for the file
            return Uri.fromFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private Bitmap createThumbnail(Bitmap originalBitmap) {
        int thumbnailSize = 500; // Adjust as needed
        return Bitmap.createScaledBitmap(originalBitmap, thumbnailSize, thumbnailSize, true);
    }

    private void uploadImage(Uri imageUri, Uri originalImageUri) {
        Uri[] uris = new Uri[2];
        uris[0] = imageUri;
        uris[1] = originalImageUri;
        new UploadImageTask().execute(uris);
    }

    private class UploadImageTask extends AsyncTask<Uri[], Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AddWallaperActivity.this);
            progressDialog.setMessage("Uploading Image...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progressDialog.dismiss();
        }

        @Override
        protected Void doInBackground(Uri[]... urisArray) {
            Uri[] imageUris = urisArray[0];

            try {
                // Convert the thumbnail image
                Bitmap thumbnailBitmap = getBitmapFromUri(imageUris[0]);
                ByteArrayOutputStream thumbnailByteArrayOutputStream = new ByteArrayOutputStream();
                thumbnailBitmap.compress(Bitmap.CompressFormat.JPEG, 100, thumbnailByteArrayOutputStream);
                byte[] thumbnailImageBytes = thumbnailByteArrayOutputStream.toByteArray();

                // Convert the original image
                Bitmap originalBitmap = getBitmapFromUri(imageUris[1]);
                ByteArrayOutputStream originalByteArrayOutputStream = new ByteArrayOutputStream();
                originalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, originalByteArrayOutputStream);
                byte[] originalImageBytes = originalByteArrayOutputStream.toByteArray();

                // Get category from wherever you have it in your code

                // Retrofit setup
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/") // Change this to the URL of your PHP API
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                // Retrofit service interface
                ApiService apiService = retrofit.create(ApiService.class);
                Log.d("UploadImageTask", "API URL: " + retrofit.baseUrl());

                // Create request bodies with the image bytes
                RequestBody thumbnailRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), thumbnailImageBytes);
                MultipartBody.Part thumbnailBody = MultipartBody.Part.createFormData("thumbnail", "thumbnail.jpg", thumbnailRequestBody);

                RequestBody originalRequestBody = RequestBody.create(MediaType.parse("image/jpeg"), originalImageBytes);
                MultipartBody.Part originalBody = MultipartBody.Part.createFormData("original", "original.jpg", originalRequestBody);

                // Create request body for category
                RequestBody categoryRequestBody = RequestBody.create(MediaType.parse("text/plain"), category);

                // Make the API call
                Call<JsonObject> call = apiService.uploadImages(thumbnailBody, originalBody, categoryRequestBody);
                call.enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddWallaperActivity.this, "Images uploaded successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddWallaperActivity.this, MainActivity.class));
                        } else {
                            Toast.makeText(AddWallaperActivity.this, "Error uploading images", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(AddWallaperActivity.this, MainActivity.class));
                        }
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e("UploadImageTask", "Failure: " + t.getMessage());
                        Toast.makeText(AddWallaperActivity.this, "Failed to upload images", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }


        private Bitmap getBitmapFromUri(Uri uri) throws FileNotFoundException {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            return BitmapFactory.decodeStream(inputStream);
        }
    }


}