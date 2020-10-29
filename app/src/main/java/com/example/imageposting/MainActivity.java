package com.example.imageposting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS = 10;
    private boolean isRecording;
    private ScreenRecorder screenRecorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenRecorder = new ScreenRecorder(MainActivity.this, this);
        isRecording = false;

        Button uploaderBtn = findViewById(R.id.uploader_activity_change_btn);
        uploaderBtn.setOnClickListener(v -> navigateToImageUploader());

        Button recordScreenBtn = findViewById(R.id.record_btn);
        recordScreenBtn.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                    .checkSelfPermission(MainActivity.this,
                            Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale
                        (MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                        ActivityCompat.shouldShowRequestPermissionRationale
                                (MainActivity.this, Manifest.permission.RECORD_AUDIO)) {
                    Snackbar.make(findViewById(android.R.id.content), "Permission",
                            Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                            v1 -> ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission
                                            .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                                    REQUEST_PERMISSIONS)).show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission
                                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                            REQUEST_PERMISSIONS);
                }
            } else {
                isRecording = !isRecording;
                screenRecorder.onToggleScreenShare(isRecording);
            }
        });
    }

    private void navigateToImageUploader() {
        Intent intent = new Intent(this, ImageUploaderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        screenRecorder.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        screenRecorder.destroyMediaProjection();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS) {
            if ((grantResults.length > 0) && (grantResults[0] +
                    grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                isRecording = !isRecording;
                screenRecorder.onToggleScreenShare(isRecording);
            } else {
                Snackbar.make(findViewById(android.R.id.content), "Permission",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        v -> {
                            Intent intent = new Intent();
                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setData(Uri.parse("package:" + getPackageName()));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                            startActivity(intent);
                        }).show();
            }
        }
    }
}