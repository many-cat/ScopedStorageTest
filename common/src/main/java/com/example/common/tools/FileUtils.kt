package com.example.common.tools

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.zibin.luban.Luban
import top.zibin.luban.OnCompressListener
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


/**
 * 保存位图到本地
 *
 * @param bitmap
 * @param path   本地路径
 * @return boolean
 */
suspend fun saveImage(bitmap: Bitmap): String? = withContext(Dispatchers.IO) {
    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).absolutePath + File.separator + "Camera"
    val file = File(path)
    val fileOutputStream: FileOutputStream
    //文件夹不存在，则创建它
    if (!file.exists()) {
        file.mkdir()
    }
    try {
        val imgName = path + File.separator + System.currentTimeMillis() + ".jpg"
        fileOutputStream = FileOutputStream(imgName)
        val isSuccess = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
        fileOutputStream.flush()
        fileOutputStream.close()

        logd("保存图片成功")
        return@withContext if (isSuccess) imgName else null
    } catch (e: Exception) {
        logd("保存图片失败 FileOutputStream 抛出异常 $e")
        e.printStackTrace()
    }

    return@withContext null
}

/**
 * 保存图片到相册
 */
suspend fun saveBitmapToPictures(context: Context, bitmap: Bitmap): Boolean = withContext(
    Dispatchers.IO
) {
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, getImagesContentValues())?.let {
        try {
            context.contentResolver.openOutputStream(it).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                logd("保存图片成功")
                return@withContext true
            }
        } catch (exception: FileNotFoundException) {
            exception.printStackTrace()
            logd("保存图片失败 FileOutputStream 抛出异常 $exception")
            return@withContext false
        }
    }
    false
}

/**
 *  PATH ： 相册路径
 *  MIME_TYPE ： image/jpeg
 *  DISPLAY_NAME： fileName
 */
fun getImagesContentValues(): ContentValues {
    val fileName = "${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
    }
    val path = getAppPicturePath()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path)
    } else {
        val fileDir = File(path)
        if (!fileDir.exists()) {
            fileDir.mkdir()
        }
        contentValues.put(MediaStore.MediaColumns.DATA, path + File.separator + fileName)
    }
    return contentValues
}


private fun getAppPicturePath(): String {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "Camera"
    } else {
        Environment.DIRECTORY_DCIM + File.separator + "Camera"
    }
}

/**
 * 共享目录原文件拷贝到cache
 * 如果发生压缩，压缩完成后删除cache 如果未压缩 则保留cache
 * 压缩文件放在targetDir
 */
fun luBanUri(context: Context, sourceFile: Uri, size: Int = 200, targetDir: String = cachePath(context), onSuccess: (File, Int) -> Unit, onStart: () -> Unit = {}, onError: (e: Throwable) -> Unit = {}, index: Int = -1) {
    val copyFile = copyToCache(context, sourceFile)
    Luban.with(context)
        .load(copyFile)                                   // 传人要压缩的图片列表
        .ignoreBy(size)                                  // 忽略不压缩图片的大小
        .setTargetDir(targetDir)                        // 设置压缩后文件存储位置
        .setCompressListener(object : OnCompressListener { //设置回调
            override fun onStart() {
                onStart()
            }

            override fun onSuccess(file: File) {
                if (file != copyFile) copyFile.delete()
                onSuccess.invoke(file, index)
            }

            override fun onError(e: Throwable) {
                onError(e)
            }
        }).launch()
}

/**
 * 将共享目录文件拷贝到cache文件
 */
fun copyToCache(context: Context, uri: Uri): File {
    val file = File("${cachePath(context)}${uri.fileName(context)}")
    file.parentFile?.mkdirs()
    context.contentResolver.openInputStream(uri).use { input ->
        file.outputStream().use { output ->
            input?.copyTo(output, DEFAULT_BUFFER_SIZE)
        }
    }
    return file
}

/**
 * 压缩文件临时cache目录
 */
private val separator = File.separator
private fun cachePath(context: Context) = "${context.filesDir.path}${separator}art${separator}image$separator"

fun Uri.fileName(context: Context): String {
    if (scheme == "content") {
        context.contentResolver.query(this, null, null, null, null).use { cursor ->
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        }
    }
    path?.let {
        val cut = it.lastIndexOf(File.separator)
        if (cut != -1) return it.substring(cut + 1)
        return it
    }
    return "${System.currentTimeMillis()}.jpg"
}