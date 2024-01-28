package com.wallpaper.appdev;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.Task;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.InstallStateUpdatedListener;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.InstallStatus;
import com.google.android.play.core.install.model.UpdateAvailability;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public DrawerLayout drawerLayout;
    public NavigationView navigationView;
    public ActionBarDrawerToggle actionBarDrawerToggle;
    Toolbar toolbar;
    private List<ImageItem> imageList;
    private ImageAdapter imageAdapter;
    GridLayoutManager gridLayoutManager;
    AppUpdateManager appUpdateManager;
    private static final int UPDATE_REQUEST_CODE = 2999;

    ImageView addImageButton;
    private RecyclerView recyclerView;

    ArrayList<ImageHolder> urlsArray;
    String status;

    Chip AllCategory, ThreeD, Abstract, Animals, Anime, Art, Black, BlackAndWhite, Cars, City, Dark, Flowers, Food, Love, Men, Minimalism, Motorcycles, Nature, Space, Sport, Technologies, Textures, Words;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSideNavigationMenu();
        findViews();
        CheckForAppUpdate();
        urlsArray = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        imageList = new ArrayList<>();
        //imageFetcher = new ImageFetcher(this);

        ChipGroup chipGroup = findViewById(R.id.chipGroup);

        chipGroup.setOnCheckedChangeListener(new ChipGroup.OnCheckedChangeListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onCheckedChanged(ChipGroup group, int checkedId) {
                // Iterate through all chips in the ChipGroup
                for (int i = 0; i < group.getChildCount(); i++) {
                    Chip chip = (Chip) group.getChildAt(i);

                    // Check if the current chip is selected
                    if (chip.isChecked()) {
                        // Do something with the selected chip
                        String selectedChipText = chip.getText().toString();
                        // Add your logic to handle the selected chip
                        System.out.println("Selected Chip: " + selectedChipText);
                        imageList = new ArrayList<>();
                        recyclerView.setLayoutManager(gridLayoutManager);
                        imageAdapter = new ImageAdapter(getApplicationContext(), imageList);
                        recyclerView.setAdapter(imageAdapter);
                        imageAdapter.notifyDataSetChanged();
                        GetURLs gu = new GetURLs();
                        if (selectedChipText.equals("All Categories")) {
                            gu.execute("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/getWallpaper.php");
                        } else {
                            gu.execute("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/getWallpaperCategoryWise.php?category=" + selectedChipText);
                        }

                    }
                }
            }
        });

        // showProgressDialog();
        gridLayoutManager = new GridLayoutManager(MainActivity.this, 3);
        addImageButton = findViewById(R.id.addImageButton);
        addImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, AddWallaperActivity.class));
            }
        });

        recyclerView.setLayoutManager(gridLayoutManager);
        imageAdapter = new ImageAdapter(this, imageList);
        recyclerView.setAdapter(imageAdapter);
        GetURLs gu = new GetURLs();
        gu.execute("https://www.gurbanistatus.in/Arqam/4K_Wallpaper/getWallpaper.php");

    }

    public void findViews() {
        AllCategory = findViewById(R.id.chip_allCat);
        ThreeD = findViewById(R.id.chip_3D);
        Abstract = findViewById(R.id.chip_Abstract);
        Animals = findViewById(R.id.chip_Animals);
        Anime = findViewById(R.id.chip_Anime);
        Art = findViewById(R.id.chip_Art);
        Black = findViewById(R.id.chip_Black);
        BlackAndWhite = findViewById(R.id.chip_BlackAndWhite);
        Cars = findViewById(R.id.chip_Cars);
        City = findViewById(R.id.chip_City);
        Dark = findViewById(R.id.chip_Dark);
        Flowers = findViewById(R.id.chip_Flowers);
        Food = findViewById(R.id.chip_Food);
        Love = findViewById(R.id.chip_Love);
        Men = findViewById(R.id.chip_Men);
        Minimalism = findViewById(R.id.chip_Minimalism);
        Motorcycles = findViewById(R.id.chip_Motorcycles);
        Nature = findViewById(R.id.chip_Nature);
        Space = findViewById(R.id.chip_Space);
        Sport = findViewById(R.id.chip_Sport);
        Technologies = findViewById(R.id.chip_Technologies);
        Textures = findViewById(R.id.chip_Textures);
        Words = findViewById(R.id.chip_Words);

    }

    class GetURLs extends AsyncTask<String, ArrayList<String>, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            BufferedReader bufferedReader = null;
            StringBuilder sb = new StringBuilder();
            URL url = null;
            try {
                url = new URL(strings[0]);
                HttpURLConnection con = null;
                con = (HttpURLConnection) url.openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while ((json = bufferedReader.readLine()) != null) {
                    sb.append(json + "\n");
                }


                JSONObject jsonResponse = new JSONObject(sb.toString());
                status = jsonResponse.getString("status");

                if ("success".equals(status)) {
                    JSONArray wallpapersArray = jsonResponse.getJSONArray("wallpapers");
                    // Extract image URLs
                    for (int i = wallpapersArray.length() - 1; i >= 0; i--) {
                        String st = wallpapersArray.getJSONObject(i).getString("thumbnail_url");
                        String originalUrlSt = wallpapersArray.getJSONObject(i).getString("original_url");
                        urlsArray.add(new ImageHolder(st, originalUrlSt));
                        imageList.add(new ImageItem(originalUrlSt, st));
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "No wallpapers found", Toast.LENGTH_SHORT).show());
                }
                Collections.shuffle(imageList);


            } catch (IOException | JSONException e) {
                throw new RuntimeException(e);
            }
            return sb.toString().trim();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            imageAdapter.notifyDataSetChanged();

        }
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
                    // startActivity(getIntent());
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);

                } else if (itemId == R.id.allCategory) {
                    AllCategory.setChecked(true);
                } else if (itemId == R.id.threeD) {
                    ThreeD.setChecked(true);
                } else if (itemId == R.id.abstrct) {
                    Abstract.setChecked(true);
                } else if (itemId == R.id.animals) {
                    Animals.setChecked(true);
                } else if (itemId == R.id.anime) {
                    Anime.setChecked(true);
                } else if (itemId == R.id.art) {
                    Art.setChecked(true);
                } else if (itemId == R.id.black) {
                    Black.setChecked(true);
                } else if (itemId == R.id.blackAndWhite) {
                    BlackAndWhite.setChecked(true);
                } else if (itemId == R.id.cars) {
                    Cars.setChecked(true);
                } else if (itemId == R.id.city) {
                    City.setChecked(true);
                } else if (itemId == R.id.dark) {
                    Dark.setChecked(true);
                } else if (itemId == R.id.flowers) {
                    Flowers.setChecked(true);
                } else if (itemId == R.id.food) {
                    Food.setChecked(true);
                } else if (itemId == R.id.love) {
                    Love.setChecked(true);
                } else if (itemId == R.id.men) {
                    Men.setChecked(true);
                } else if (itemId == R.id.minimalism) {
                    Minimalism.setChecked(true);
                } else if (itemId == R.id.motercycles) {
                    Motorcycles.setChecked(true);
                } else if (itemId == R.id.nature) {
                    Nature.setChecked(true);
                } else if (itemId == R.id.space) {
                    Space.setChecked(true);
                } else if (itemId == R.id.sport) {
                    Sport.setChecked(true);
                } else if (itemId == R.id.technologies) {
                    Technologies.setChecked(true);
                } else if (itemId == R.id.textures) {
                    Textures.setChecked(true);
                } else if (itemId == R.id.words) {
                    Words.setChecked(true);
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void CheckForAppUpdate() {
        Log.d("Arqam", "Check for update called");
        appUpdateManager = AppUpdateManagerFactory.create(this);
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            UPDATE_REQUEST_CODE);
                } catch (IntentSender.SendIntentException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        appUpdateManager.registerListener(listener);

    }

    InstallStateUpdatedListener listener = state -> {
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.
            popupSnackbarForCompleteUpdate();
        }
    };

    private void popupSnackbarForCompleteUpdate() {
        Snackbar snackbar =
                Snackbar.make(
                        findViewById(android.R.id.content),
                        "An update has just been downloaded.",
                        Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("INSTALL", view -> appUpdateManager.completeUpdate());
        snackbar.setActionTextColor(
                getResources().getColor(android.R.color.holo_blue_bright));
        snackbar.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                // If the update is cancelled or fails,
                // you can request to start the update again.
                Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show();
            }
        }
    }

}