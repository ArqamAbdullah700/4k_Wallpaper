package com.wallpaper.appdev;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity /*implements ImageFetcher.ImageFetchListener*/ {
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    private List<ImageItem> imageList;
    private ImageAdapter imageAdapter;
    GridLayoutManager gridLayoutManager;

    String apiUrl;
    ImageFetcher imageFetcher;
    private RecyclerView recyclerView;
    int pageIndex = 1;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSideNavigationMenu();
        recyclerView = findViewById(R.id.recyclerView);
        imageList = new ArrayList<>();
        //imageFetcher = new ImageFetcher(this);

        // showProgressDialog();
        gridLayoutManager = new GridLayoutManager(MainActivity.this, 3);


        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(gridLayoutManager);

//        apiUrl = "https://api.unsplash.com/search/photos/?client_id=d3wrZOMtXmwkEv_oH1wF92WCxJf5ED3DY0fvPAAkd0U&page=01&query=4k wallpaper&per_page=30";
//        imageFetcher.execute(apiUrl);
        imageAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);
        fetchImagesFromFirebase();
    }

//    private void showProgressDialog() {
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setTitle("Please Wait...");
//        progressDialog.setMessage("while images are loading.");
//        progressDialog.show();
//
//    }
//
//    private void hideProgressDialog() {
//        if (progressDialog != null && progressDialog.isShowing()) {
//            progressDialog.dismiss();
//        }
//    }

    private void setSideNavigationMenu() {
        drawerLayout = findViewById(R.id.my_drawer_layout);
        toolbar = findViewById(R.id.my_toolbar);
        navigationView = findViewById(R.id.navView);
        navigationView.bringToFront();
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                MainActivity.this,
                drawerLayout,
                toolbar,
                R.string.nav_open,
                R.string.nav_close
        );
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.privacyPolicy) {

                    startActivity(new Intent(MainActivity.this, PrivacyPolicyActivity.class));
                } else if (itemId == R.id.about) {
                    startActivity(new Intent(MainActivity.this, AboutUsActivity.class));
                } else if (itemId == R.id.islamicWallpaper) {
                    finish();
                    overridePendingTransition(0, 0);
                    startActivity(getIntent());
                    overridePendingTransition(0, 0);

                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }


    private void fetchImagesFromFirebase() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("4kWallpapers").child("Images");

        storageRef.listAll().addOnCompleteListener(new OnCompleteListener<ListResult>() {
            @Override
            public void onComplete(@NonNull Task<ListResult> task) {
                if (task.isSuccessful()) {
                    for (StorageReference item : task.getResult().getItems()) {
                        String imageName = item.getName();

                        // Use addOnSuccessListener to retrieve the download URL
                        item.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageUrl = uri.toString();
                                ImageItem imageItem = new ImageItem(imageName, imageUrl);
                                imageList.add(imageItem);
                                imageAdapter.notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Handle failure to get download URL
                                Log.e("FetchImages", "Failed to get download URL: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    // Handle failure
                    Log.e("FetchImages", "Failed to list items: " + task.getException().getMessage());
                }
            }
        });
    }
//    @Override
//    public void onImageFetchComplete(ArrayList<String> imageUrls) {
//        pageIndex++;
//        imageList = new ArrayList<>();
//        recyclerView = findViewById(R.id.recyclerView);
//        gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
//        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        recyclerView.setLayoutManager(gridLayoutManager);
//        for (String imageUrl : imageUrls) {
//            imageList.add(new ImageItem("", imageUrl));
//        }
//        if(pageIndex==2){
//            imageAdapter = new ImageAdapter(this, imageList);
//            recyclerView.setAdapter(imageAdapter);
//        } else {
//            imageAdapter.addImages(imageList);
//        }
//
//
//        Log.d("Arqam", "Index: " + pageIndex);
//        if(pageIndex<=10){
//            ImageFetcher imageFetcher1 = new ImageFetcher(this);
//            apiUrl = "https://api.unsplash.com/search/photos/?client_id=d3wrZOMtXmwkEv_oH1wF92WCxJf5ED3DY0fvPAAkd0U&page="+pageIndex+"&query=4k wallpaper&per_page=30";
//            imageFetcher1.execute(apiUrl);
//        } else {
//            hideProgressDialog();
//        }
//
//    }
//
//    @Override
//    public void onImageFetchError(String errorMessage) {
//        Log.e("Image Fetch Error", errorMessage);
//    }
}