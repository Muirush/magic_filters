package com.magicFilter.magic_filters.utils;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BitmapUtils {

    public static Bitmap setTakePicktrueOrientation(int id, int angle, Bitmap bitmap) {
        //如果返回的图片宽度小于高度，说明FrameWork层已经做过处理直接返回即可
//        if (bitmap.getWidth() < bitmap.getHeight()) {
//            return bitmap;
//        }
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        bitmap = rotaingImageView(id, angle, bitmap);
        return bitmap;
    }

    private static Bitmap rotaingImageView(int id, int angle, Bitmap bitmap) {
        //矩阵
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }

    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    public static boolean SaveJpg(ImageView view) {
        try {
            Drawable drawable = view.getDrawable();
            if (drawable == null) {
                return false;
            }
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            Uri dataUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            Uri fileUri = view.getContext().getContentResolver().insert(dataUri, values);
            if (fileUri == null) {
                return false;
            }
            OutputStream outStream = view.getContext().getContentResolver().openOutputStream(fileUri);
            Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            outStream.flush();
            outStream.close();

            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            intent.setData(fileUri);
            view.getContext().sendBroadcast(intent);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean saveBitmapJpg(Bitmap bitmap, String path) {
        try {
            File file = new File(path);
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            FileOutputStream fos = new FileOutputStream(file);
            boolean b = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 质量压缩
     */
    public static boolean isCompressBitmapToFile(Bitmap image, File file, int maxSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中,这里从90开始
        int quality = 90;
        image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        Log.d("Bitmap", "before==" + baos.toByteArray().length);
        //  循环判断如果压缩后图片是否大于maxSize(例如:100kb),大于继续压缩
        while (baos.toByteArray().length / (1024 * 1024) > maxSize) {
            baos.reset(); // 重置baos即清空baos
            quality -= 10;// 每次都减少10
            // 这里压缩options，把压缩后的数据存放到baos中
            image.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        }
        Log.d("Bitmap", "after==" + baos.toByteArray().length / (1024 * 1024));
        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
