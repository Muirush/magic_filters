package com.magicFilter.magic_filters.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotosUtils {

    public static String saveCameraPhoto(Context context, byte[] data, int cameraID) {
        FileOutputStream fos = null;
        File cameraFolder = new File(Environment.getExternalStorageDirectory(), "Pictures/MagicAlbum");
        if (!cameraFolder.exists()) {
            if (!cameraFolder.mkdirs()) {
                Log.e("CameraActivity:", "mkdirs error!");
            }
        }
        String imagePath = cameraFolder.getAbsolutePath() + File.separator + "image_" + System.currentTimeMillis() + ".jpeg";
        File imageFile = new File(imagePath);
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    final int degree = readPictureDegree(imagePath);
                    fos.close();
                    Bitmap retBitmap = BitmapFactory.decodeFile(imagePath);
                    if (cameraID == 1){
                        if (degree == 90) {
                            retBitmap = BitmapUtils.setTakePicktrueOrientation(cameraID, 270, retBitmap);
                        }else {
                            retBitmap = BitmapUtils.setTakePicktrueOrientation(cameraID, 180, retBitmap);
                        }
                    }else {
                        retBitmap = BitmapUtils.setTakePicktrueOrientation(cameraID, degree, retBitmap);
                    }
                    BitmapUtils.saveBitmapJpg(retBitmap, imagePath);

                    /*Bitmap retBitmap = BitmapFactory.decodeFile(imagePath);
                    int i=readPictureDegree(imagePath);
                    retBitmap = BitmapUtils.setTakePicktrueOrientation(cameraID,i, retBitmap);
                    BitmapUtils.saveBitmapJpg(retBitmap, imagePath);

                    String newPath = context.getExternalCacheDir() + File.separator + System.currentTimeMillis() + "temp.jpg";
                    boolean isCompress = BitmapUtils.isCompressBitmapToFile(retBitmap, new File(newPath), 2);
                    if (isCompress) {
                        imagePath = newPath;
                    } else {
                        imagePath = null;
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return imagePath;
    }

    public static int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Log.e("TAG", "readPictureDegree: orientation-------->"+orientation);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return degree;
    }

}
