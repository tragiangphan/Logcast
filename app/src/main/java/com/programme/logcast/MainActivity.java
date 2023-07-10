package com.programme.logcast;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final int READ_LOG_PERMISSION_CODE = 100;
    private static final int WRITE_EXTERNAL_PERMISSION_CODE = 200;

    File logFile;
//    private List<String> checkPackages, checkPkgError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        setEvent();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void setEvent() {
        // Check permission for App
        hasPermission();

        // Start reading logcat
        startWebServer();
    }

    private void hasPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_LOGS) != PackageManager.PERMISSION_GRANTED) {
            // if has not permission, require user
            requestReadLogPermissions();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // if has not permission, require user
            requestWriteStoragePermissions();
        }
    }

    // CHECK PERMISSION: READ_LOG, WRITE_EXTERNAL_STORAGE
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == READ_LOG_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Application cannot read log without READ_LOGS permission", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Application is reading logcat with READ_LOGS permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestReadLogPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_LOGS}, READ_LOG_PERMISSION_CODE);
    }

    private void requestWriteStoragePermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_CODE);
    }

    private void startWebServer() {
        // Start the LogcatWebService
        Intent serviceIntent = new Intent(this, LogcatService.class);
        startService(serviceIntent);
    }

    private void stopWebServer() {
        // Stop the LogcatWebService
        Intent serviceIntent = new Intent(this, LogcatService.class);
        stopService(serviceIntent);
        logFile.delete();
    }
}
