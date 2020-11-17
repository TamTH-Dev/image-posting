package com.example.imageposting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_PERMISSIONS_CODE = 1000;
    private static final int REQUEST_RECORD_CODE = 1001;
    private static final int REQUEST_FLOATING_BUBBLE = 1002;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 1003;
    private ScreenRecorder screenRecorder;
    private ScreenCapture screenCapture;
    private FloatingWindow floatingWindow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        screenRecorder = new ScreenRecorder(MainActivity.this, this, REQUEST_RECORD_CODE);
        screenCapture = new ScreenCapture(MainActivity.this, this, REQUEST_IMAGE_CAPTURE_CODE);
        floatingWindow = new FloatingWindow(MainActivity.this, this, screenRecorder);

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
                                    REQUEST_PERMISSIONS_CODE)).show();
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission
                                    .WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO},
                            REQUEST_PERMISSIONS_CODE);
                }
            } else {
                if (!screenRecorder.getIsRecording()) {
                    floatingWindow.addNewBubble();
                    screenRecorder.startRecord();
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!Settings.canDrawOverlays(MainActivity.this)) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:" + getPackageName()));
                            startActivityForResult(intent, REQUEST_FLOATING_BUBBLE);
                        }
                    } else {
                        Intent intent = new Intent(MainActivity.this, Service.class);
                        startService(intent);
                    }
                }
            }
        });

        Button imageCaptureBtn = findViewById(R.id.image_capture_btn);
        imageCaptureBtn.setOnClickListener(v -> screenCapture.takeImage());
    }

    private void navigateToImageUploader() {
        Intent intent = new Intent(this, ImageUploaderActivity.class);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RECORD_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this,
                        "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
                return;
            }
            screenRecorder.onActivityResult(resultCode, data);
        } else if (requestCode == REQUEST_IMAGE_CAPTURE_CODE) {
            if (resultCode != Activity.RESULT_OK) {
                Toast.makeText(this,
                        "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show();
                return;
            }
            screenCapture.executeCapturedImage();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        screenRecorder.destroyMediaProjection();
        floatingWindow.destroyFloatingWindow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSIONS_CODE) {
            if ((grantResults.length > 0) && (grantResults[0] +
                    grantResults[1]) == PackageManager.PERMISSION_GRANTED) {
                if (!screenRecorder.getIsRecording()) {
                    screenRecorder.startRecord();
                }
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