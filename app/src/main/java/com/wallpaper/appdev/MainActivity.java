package com.wallpaper.appdev;

import android.content.Intent;
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

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ImageFetcher.ImageFetchListener {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSideNavigationMenu();
        recyclerView = findViewById(R.id.recyclerView);
        imageList = new ArrayList<>();
        imageFetcher = new ImageFetcher(this);


        apiUrl = "https://api.unsplash.com/search/photos/?client_id=d3wrZOMtXmwkEv_oH1wF92WCxJf5ED3DY0fvPAAkd0U&page=10&query=office&per_page=30";
        imageFetcher.execute(apiUrl);


    }

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

    @Override
    public void onImageFetchComplete(ArrayList<String> imageUrls) {
        recyclerView = findViewById(R.id.recyclerView);
        gridLayoutManager = new GridLayoutManager(MainActivity.this, 2);
        //recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setLayoutManager(gridLayoutManager);
        for (String imageUrl : imageUrls) {
            imageList.add(new ImageItem("", imageUrl));
        }
        imageAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);
        imageAdapter.notifyDataSetChanged();
    }

    @Override
    public void onImageFetchError(String errorMessage) {
        Log.e("Image Fetch Error", errorMessage);
    }
}