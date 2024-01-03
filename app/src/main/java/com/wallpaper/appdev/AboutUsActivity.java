package com.wallpaper.appdev;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        ImageView backImageBtn = findViewById(R.id.backImageBtn);
        backImageBtn.setOnClickListener(view -> startActivity(new Intent(AboutUsActivity.this,MainActivity.class)));
    }
}