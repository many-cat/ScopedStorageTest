/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.common.media;

import android.app.Activity;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.fragment.app.Fragment;

import com.example.common.tools.FileUtilsKt;

import java.lang.ref.WeakReference;

public class MediaStoreCompat {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> fragment;
    //选中的图片地址
    private Uri currentPhotoUri;
    //裁剪后图片地址
    private Uri currentClipUri;

    public MediaStoreCompat(Activity activity) {
        mContext = new WeakReference<>(activity);
        fragment = null;
    }

    public MediaStoreCompat(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        this.fragment = new WeakReference<>(fragment);
    }

    /**
     * Checks whether the device has a camera feature or not.
     *
     * @param context a context to check for camera feature.
     * @return true if the device has a camera feature. false otherwise.
     */
    public static boolean hasCameraFeature(Context context) {
        PackageManager pm = context.getApplicationContext().getPackageManager();
        return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * 调起相机
     */
    public void dispatchCaptureIntent(int requestCode) {
        Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (captureIntent.resolveActivity(mContext.get().getPackageManager()) != null) {
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, insertPhoto());
            captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            startActivityForResult(requestCode, captureIntent);
        }
    }

    /**
     * 相册中插入图片
     */
    private Uri insertPhoto() {
        currentPhotoUri = createImageUri();
        return currentPhotoUri;
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android10 以后使用
     * <p>
     * Android10不需要读写权限  Android10以前需要读写权限
     *
     * @return 图片的uri
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return mContext.get().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, FileUtilsKt.getImagesContentValues());
        } else {
            return mContext.get().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, FileUtilsKt.getImagesContentValues());
        }
    }


    /**
     * 调起相册
     */
    public void dispatchPhotosIntent(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.INTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(requestCode, intent);
    }

    public void startPhotoZoom(Uri uri, int request) {
        currentClipUri = uri;
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        intent.putExtra("crop", "true");

        if (Build.MANUFACTURER.equals("HUAWEI")) {
            intent.putExtra("aspectX", 9998);
            intent.putExtra("aspectY", 9999);
        } else {
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
        }
        intent.putExtra("outputX", 1000);
        intent.putExtra("outputY", 1000);
        intent.putExtra("scale", true);

        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setClipData(ClipData.newRawUri(MediaStore.EXTRA_OUTPUT, currentClipUri));
        intent.putExtra(MediaStore.EXTRA_OUTPUT, currentClipUri);//裁剪完的图片保存的路径
        intent.putExtra("return-data", false);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(request, intent);
    }

    private void startActivityForResult(int request, Intent intent) {
        if (fragment != null) {
            fragment.get().startActivityForResult(intent, request);
        } else {
            mContext.get().startActivityForResult(intent, request);
        }
    }

    public Uri getCurrentClipUri() {
        return currentClipUri;
    }

    public Uri getCurrentPhotoUri() {
        return currentPhotoUri;
    }

}
