package com.magicFilter.magic_filters;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import com.magicFilter.magic_filters.activity.BaseActivity;
import com.magicFilter.magic_filters.activity.CameraActivity;
import com.magicFilter.magic_filters.helper.TouchHelper;
import com.magicFilter.magic_filters.utils.SPUtils;

public class MainActivity extends BaseActivity {

    private String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ImageView ivTakePhoto = findViewById(R.id.iv_take_photos);
        ivTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!SPUtils.getBoolean(MainActivity.this, "takePhoto")) {

                    SPUtils.putBoolean(MainActivity.this, "takePhoto", true);
                }
                if (initPermission()) {
                    startActivity(new Intent(MainActivity.this, CameraActivity.class));
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this, permission, 100);
                }
            }
        });
        TouchHelper.touchAlpha(ivTakePhoto);

        if (!SPUtils.getBoolean(this, "first")) {

            SPUtils.putBoolean(this, "first", true);
        }


    }



    private boolean initPermission() {
        for (String str : permission) {
            return ContextCompat.checkSelfPermission(this, str) == PackageManager.PERMISSION_GRANTED;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100) {
            if (initPermission()) {
                startActivity(new Intent(MainActivity.this, CameraActivity.class));
            } else {
                Toast.makeText(this, "permission denied!", Toast.LENGTH_LONG).show();
            }
        }
    }

}