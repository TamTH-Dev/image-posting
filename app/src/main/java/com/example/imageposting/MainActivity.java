package com.example.imageposting;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button uploaderButton = findViewById(R.id.uploader_activity_change_btn);
        uploaderButton.setOnClickListener(v -> navigateToImageUploader());
    }

    private void navigateToImageUploader(){
        Intent intent = new Intent(this, ImageUploaderActivity.class);
        startActivity(intent);
    }
}