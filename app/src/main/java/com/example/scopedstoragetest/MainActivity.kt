package com.example.scopedstoragetest

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import coil.load
import com.example.common.media.CaptureStrategy
import com.example.common.media.MediaStoreCompat
import com.example.common.permission.PermissionListener
import com.example.common.permission.PermissionRequest
import com.example.common.tools.*
import com.example.scopedstoragetest.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_CAPTURE = 24
        const val REQUEST_CODE_PHOTO = 23
        const val REQUEST_CODE_CAPTURE_CLIP = 22
        const val REQUEST_CODE_CLIP = 21
        const val REQUEST_CODE_COMPRESS = 20
    }

    private val binding by easyLazy { ActivityMainBinding.inflate(layoutInflater) }

    private val mediaStoreCompat = MediaStoreCompat(this)
    private val permissionRequest by easyLazy { PermissionRequest(this) }

    private val write = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val camera = arrayOf(Manifest.permission.CAMERA)
    private val cameraAndWrite = arrayOf(Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initOnClick()
        mediaStoreCompat.setCaptureStrategy(CaptureStrategy(true, "$packageName.fileProvider"))
    }

    private fun initOnClick() {
        //**********************************************测试 - File访问SD卡*******************************************************
        //在SD卡下创建文件
        binding.btnCreatFile.setOnClickListener {
            testCreateFile()
        }

        //************************************************保存图片到相册************************************************
        /**
         * 保存图片到相册 - 通过File操作
         * 需要读写权限  不支持分区存储
         *
         * 失败抛出异常
         */
        binding.btnSaveBitmapOld.setOnClickListener {
            lifecycleScope.launch {
                saveImage(getBitmap(200f.dp.toInt(), resources, R.drawable.android))
            }
        }
        /**
         * 保存图片到相册 - 通过ContentProvieder
         * 不需要读写权限  不支持分区存储
         */
        binding.btnSaveBitmapNew.setOnClickListener {
            lifecycleScope.launch {
                saveBitmapToPictures(AppContext, getBitmap(200f.dp.toInt(), resources, R.drawable.android))
            }
        }


        //*********************************************上传图片-相机、相册、裁剪************************************************
        /**
         * 打开相册 - 展示图片
         *
         * 不需要读写权限
         */
        binding.btnPhotoAlbum.setOnClickListener {
            mediaStoreCompat.dispatchPhotosIntent(REQUEST_CODE_PHOTO)
        }

        /**
         * 打开相机拍照 - 展示图片
         *
         * 需要CAMERA权限、不需要读写权限  - 通过ContentProvieder
         * 需要CAMERA权限、需要读写权限    - 通过File创建文件-》FileProvider获取Uri
         *
         */
        binding.btnCamera.setOnClickListener {
            requestPermissions(camera){
                mediaStoreCompat.dispatchCaptureIntent(REQUEST_CODE_CAPTURE)
            }
        }

        /**
         * 打开相机裁剪 - 展示图片
         *
         * 需要CAMERA权限、不需要读写权限  - 通过ContentProvieder
         * 需要CAMERA权限、需要读写权限    - 通过File创建文件-》FileProvider获取Uri
         */
        binding.btnCameraTailor.setOnClickListener {
            requestPermissions(camera){
                mediaStoreCompat.dispatchCaptureIntent(REQUEST_CODE_CAPTURE_CLIP)
            }
        }


        //************************************************上传图片 - 压缩************************************************
        binding.btnCameraCompress.setOnClickListener {
            mediaStoreCompat.dispatchPhotosIntent(REQUEST_CODE_COMPRESS)
        }
    }


    /**
     * 开启分区存储+读写权限   创建失败
     * 关闭分区存储+读写权限   创建成功
     */
    private fun testCreateFile() {
        val file = File(Environment.getExternalStorageDirectory(), "AndroidDarren")
        file.delete()
        if (!file.exists()) {
            logd("${file.absolutePath} 创建文件是否成功：${file.mkdir()}   是否开启兼容模式 ${Environment.isExternalStorageLegacy()}")
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            REQUEST_CODE_PHOTO -> binding.ivPhoto.load(data?.data)
            REQUEST_CODE_CAPTURE -> binding.ivPhoto.load(mediaStoreCompat.currentPhotoUri)
            REQUEST_CODE_CAPTURE_CLIP -> mediaStoreCompat.startPhotoZoom(mediaStoreCompat.currentPhotoUri, REQUEST_CODE_CLIP)
            REQUEST_CODE_CLIP -> binding.ivPhoto.load(mediaStoreCompat.currentClipUri)
            REQUEST_CODE_COMPRESS -> data?.data?.let { luBanUri(AppContext, it, onSuccess = ::showImage) }
            else -> {
            }
        }
    }

    fun requestPermissions(args: Array<String>, onGranted: () -> Unit) {
        permissionRequest.requestPermissions(args, object : PermissionListener {
            override fun onGranted() {
                onGranted()
            }

            override fun onShouldShowRationale(deniedPermission: MutableList<String>?) {}

            override fun onDenied(deniedPermission: MutableList<String>?) {}
        })
    }

    private fun showImage(file: File, index: Int) {
        binding.ivPhoto.load(file)
    }
}