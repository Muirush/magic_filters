package com.magicFilter.magic_filters.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.magicFilter.magic_filters.utils.camera.AspectRatio;
import com.magicFilter.magic_filters.utils.camera.Size;
import com.magicFilter.magic_filters.utils.camera.SizeMap;

import java.io.IOException;
import java.util.SortedSet;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {

    private Camera camera;
    private SurfaceHolder surfaceHolder;
    private AspectRatio mAspectRatio;
    private final SizeMap mPreviewSizes = new SizeMap();
    private final SizeMap mPictureSizes = new SizeMap();

    private int displayOrientation;
    private boolean isPreview;

    private Activity activity;
    private int cameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public CameraSurface(Context context) {
        super(context);
    }

    public CameraSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCamera(Camera camera, Activity activity) {
        this.camera = camera;
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        this.activity = activity;
        displayOrientation = activity.getWindowManager().getDefaultDisplay().getRotation();
        mAspectRatio = AspectRatio.of(9, 16);
    }

    public void forcedRefresh(int cameraID) {
        this.cameraID = cameraID;
        surfaceCreated(surfaceHolder);
    }

    public void setPreview(boolean preview) {
        isPreview = preview;
    }

    public boolean isPreview() {
        return isPreview;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        try {
            //设置设备高宽比
            // mAspectRatio = getDeviceAspectRatio((Activity) context);
            mAspectRatio = AspectRatio.of(9, 16);
            //设置预览方向
            camera.setDisplayOrientation(setCameraDisplayOrientation(cameraID));
            Camera.Parameters parameters = camera.getParameters();
            //获取所有支持的预览尺寸
            mPreviewSizes.clear();
            for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
                int width = Math.min(size.width, size.height);
                int heigth = Math.max(size.width, size.height);
                   mPreviewSizes.add(new Size(width, heigth));
            }
            //获取所有支持的图片尺寸
            mPictureSizes.clear();
            for (Camera.Size size : parameters.getSupportedPictureSizes()) {
                int width = Math.min(size.width, size.height);
                int height = Math.max(size.width, size.height);
                 mPictureSizes.add(new Size(width, height));
            }
            Size previewSize = chooseOptimalSize(mPreviewSizes.sizes(mAspectRatio));
            Size pictureSize = mPictureSizes.sizes(mAspectRatio).last();
            //设置相机参数
            parameters.setPreviewSize(Math.max(previewSize.getWidth(), previewSize.getHeight()), Math.min(previewSize.getWidth(), previewSize.getHeight()));
            parameters.setPictureSize(Math.max(pictureSize.getWidth(), pictureSize.getHeight()), Math.min(pictureSize.getWidth(), pictureSize.getHeight()));
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setRotation(setCameraDisplayOrientation(cameraID));
            camera.setParameters(parameters);
            //把这个预览效果展示在SurfaceView上面
            camera.setPreviewDisplay(holder);
            //开启预览效果
            camera.startPreview();
            isPreview = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        if (holder.getSurface() != null) {
            camera.stopPreview();
            try {
                camera.setPreviewDisplay(surfaceHolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        if (camera != null) {
            if (isPreview) {
                camera.stopPreview();
                camera.release();
            }
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private Size chooseOptimalSize(SortedSet<Size> sizes) {
        int desiredWidth;
        int desiredHeight;
        final int surfaceWidth = getWidth();
        final int surfaceHeight = getHeight();
        if (isLandscape(displayOrientation)) {
            desiredWidth = surfaceHeight;
            desiredHeight = surfaceWidth;
        } else {
            desiredWidth = surfaceWidth;
            desiredHeight = surfaceHeight;
        }
        Size result = new Size(desiredWidth, desiredHeight);
        if (sizes != null && !sizes.isEmpty()) {
            for (Size size : sizes) {
                if (desiredWidth <= size.getWidth() && desiredHeight <= size.getHeight()) {
                    return size;
                }
                result = size;
            }
        }
        return result;
    }

    private boolean isLandscape(int orientationDegrees) {
        return (orientationDegrees == Surface.ROTATION_90 ||
                orientationDegrees == Surface.ROTATION_270);
    }

    private int setCameraDisplayOrientation(int cameraID) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraID, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
       // camera.setDisplayOrientation(result);
        return result;
    }

/*    private int getDisplayOrientation() {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int orientation;
        int degrees;
        if (displayOrientation == Surface.ROTATION_0) {
            degrees = 0;
        } else if (displayOrientation == Surface.ROTATION_90) {
            degrees = 90;
        } else if (displayOrientation == Surface.ROTATION_180) {
            degrees = 180;
        } else if (displayOrientation == Surface.ROTATION_270) {
            degrees = 270;
        } else {
            degrees = 0;
        }
        orientation = (degrees + 45) / 90 * 90;
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation - orientation + 360) % 360;
        } else {
            result = (info.orientation + orientation) % 360;
        }
        return result;
    }*/

}
