package com.sprtcoding.commutech;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageView;

public class AboutPage extends AppCompatActivity {
    private ImageView backBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_page);

        backBtn = findViewById(R.id.backBtn);

        backBtn.setOnClickListener(view -> {
            finish();
        });
    }
}