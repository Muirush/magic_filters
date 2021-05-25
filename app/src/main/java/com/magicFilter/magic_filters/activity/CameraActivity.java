package com.magicFilter.magic_filters.activity;

import android.content.Intent;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import com.bumptech.glide.Glide;
import com.magicFilter.magic_filters.R;
import com.magicFilter.magic_filters.helper.TouchHelper;
import com.magicFilter.magic_filters.utils.ContentUriUtils;
import com.magicFilter.magic_filters.utils.GalleryUtils;
import com.magicFilter.magic_filters.utils.PhotosUtils;
import com.magicFilter.magic_filters.view.CameraSurface;
import com.magicFilter.magic_filters.view.OverCameraView;

public class CameraActivity extends AppCompatActivity implements View.OnClickListener {

    private Camera camera;
    private byte[] cameraData;
    private boolean isFocusable;
    private boolean isTakePhoto;
    private int cameraID = 1;

    private OverCameraView overCameraView;
    private CameraSurface cameraSurface;

    private Runnable mRunnable;
    private Handler mHandler = new Handler();

    private ImageView ivGallery;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            int option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            getWindow().getDecorView().setSystemUiVisibility(option);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
           // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_camera);
        initView();
        initData();
    }

    private void initView() {
        cameraSurface = findViewById(R.id.fl_camera_view);
        overCameraView = findViewById(R.id.over_camera);

        ivGallery = findViewById(R.id.iv_gallery);
        ImageView ivTakePhoto = findViewById(R.id.iv_take_photo);
        ImageView ivSwitchCamera = findViewById(R.id.iv_switch_camera);

        findViewById(R.id.iv_re).setOnClickListener(this);
        findViewById(R.id.iv_save).setOnClickListener(this);
        findViewById(R.id.iv_back).setOnClickListener(this);

        ivGallery.setOnClickListener(this);
        ivTakePhoto.setOnClickListener(this);
        ivSwitchCamera.setOnClickListener(this);
        TouchHelper.touchAlpha(ivTakePhoto, ivGallery, ivSwitchCamera);
    }

    private void initData() {
        Pair<Long, String> pair = GalleryUtils.getLatestPhoto(this);
        if (pair != null) {
            Glide.with(this)
                    .load(pair.second)
                    .into(ivGallery);
        } else {
            ivGallery.setVisibility(View.INVISIBLE);
        }
    }

    private void initCamera() {
        if (cameraID == 0) {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } else {
            camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        cameraSurface.setCamera(camera, this);
    }

    private void bottomMenuVis(int type) {
        if (type == 0) {
            findViewById(R.id.ll_bottom_photo_o).setVisibility(View.GONE);
            findViewById(R.id.ll_bottom).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.ll_bottom_photo_o).setVisibility(View.VISIBLE);
            findViewById(R.id.ll_bottom).setVisibility(View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_take_photo:
                isTakePhoto = true;
                camera.takePicture(null, null, new Camera.PictureCallback() {
                    @Override
                    public void onPictureTaken(byte[] data, Camera camera) {
                        camera.stopPreview();
                        cameraData = data;
                        bottomMenuVis(1);
                    }
                });
                break;
            case R.id.iv_re:
                isTakePhoto = false;
                bottomMenuVis(0);
                if (camera != null) {
                    camera.startPreview();
                    cameraData = null;
                }
                break;
            case R.id.iv_gallery:
                camera.stopPreview();
                camera.release();
                cameraSurface.setPreview(false);
                Intent intent = new Intent(
                        Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent,0);
                break;
            case R.id.iv_switch_camera:
                switchCamera();
                break;
            case R.id.iv_save:
                findViewById(R.id.iv_save).setEnabled(false);
                String path = PhotosUtils.saveCameraPhoto(this, cameraData, cameraID);
                Intent intent1 = new Intent(this, ShowActivity.class);
                intent1.putExtra("imagePath", path);
                startActivity(intent1);
                finishThis();
                break;
            case R.id.iv_back:
                finishThis();
                break;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!isFocusable) {
                float x = event.getX();
                float y = event.getY();
                isFocusable = true;
                if (camera != null && !isTakePhoto) {
                    overCameraView.setTouchFoucusRect(camera, autoFocusCallback, x, y);
                }
                mRunnable = new Runnable() {
                    @Override
                    public void run() {
                        isFocusable = false;
                        overCameraView.setFoucuing(false);
                        overCameraView.disDrawTouchFocusRect();
                    }
                };
                mHandler.postDelayed(mRunnable, 3000);
            }
        }
        return super.onTouchEvent(event);
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            isFocusable = false;
            overCameraView.setFoucuing(false);
            overCameraView.disDrawTouchFocusRect();
            mHandler.removeCallbacks(mRunnable);
        }
    };

    private void switchCamera() {
        int cameraCount;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        camera.stopPreview();
        camera.release();
        camera = null;
        for (int i = 0; i < cameraCount; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraID == 0) {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraID = 1;
                    initCamera();
                    cameraSurface.forcedRefresh(Camera.CameraInfo.CAMERA_FACING_FRONT);
                    break;
                }
            } else {
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraID = 0;
                    initCamera();
                    cameraSurface.forcedRefresh(Camera.CameraInfo.CAMERA_FACING_BACK);
                    break;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && data == null) {
            initCamera();
        } else if (data != null){
            String path = ContentUriUtils.getPath(this, data.getData());
            Intent intent = new Intent();
            intent.setClass(this, ShowActivity.class);
            intent.putExtra("imagePath", path);
            startActivity(intent);
            finishThis();
        }
    }

    private void finishThis() {
        //camera.stopPreview();
       // camera.release();
       // camera = null;
        //cameraSurface = null;
       // overCameraView = null;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
       // bottomMenuVis(0);
       // cameraData = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
        initCamera();
    }

}
