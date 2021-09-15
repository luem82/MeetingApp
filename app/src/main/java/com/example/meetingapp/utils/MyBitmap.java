package com.example.meetingapp.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class MyBitmap {

    public static String getEndcodeFromBitmap(Bitmap bitmap) {
        int width = 150;
        int height = bitmap.getHeight() * width / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        String encode = Base64.encodeToString(bytes, Base64.DEFAULT);
        return encode;
    }

    public static Bitmap getBitmapFromStringEndcode(String endcodeString) {
        byte[] bytes = Base64.decode(endcodeString, Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

}
