package com.example.livenessdetector;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {
    MaterialButton detectorOne;
    MaterialButton detectorTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        detectorOne = findViewById(R.id.detector1_btn);
        detectorTwo = findViewById(R.id.detector2_btn);

        detectorOne.setOnClickListener(view -> {
            startActivity(new Intent(this, Detector1.class));
        });
        detectorTwo.setOnClickListener(view -> {
            startActivity(new Intent(this, Detector2.class));
        });
    }
}