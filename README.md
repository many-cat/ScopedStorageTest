# Android分区存储 - 适配

### Android分区存储

1. [https://juejin.cn/post/6844904063432130568](https://juejin.cn/post/6844904063432130568)
2. [https://developer.android.com/training/data-storage?hl=zh-cn](https://developer.android.com/training/data-storage?hl=zh-cn)
3. [https://developer.android.com/about/versions/11/privacy/storage?hl=zh-cn#test-scoped-storage](https://developer.android.com/about/versions/11/privacy/storage?hl=zh-cn#test-scoped-storage)
4. [https://developer.android.com/guide/topics/providers/content-providers](https://developer.android.com/guide/topics/providers/content-providers)

### 应用兼容模式

1. Android 9 以下 
2. Android10 可以使用requestLegacyExternalStorage=true，退出分区存储。
3. Android11 运行 targetSdkVersion = 29 仍可以请求requestLegacyExternalStorage，targetSdkVersion =30，系统会忽略requestLegacyExternalStorage标记

### 测试 - File访问SD卡

[sd卡下创建文件](https://www.notion.so/29467ce83b8a4466bd08825824546899)

# 适配

### 一、保存Bitmap到相册

[保存bitmap到相册](https://www.notion.so/d6812391473d48cead4c417b202fe0af)

## 1. saveBitmap-File

```kotlin
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
        return@withContext if (isSuccess) imgName else null
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return@withContext null
}
```

## 2. saveBitmap -  ContentProvider

```kotlin
suspend fun saveBitmapToPictures(context: Context, bitmap: Bitmap): Boolean = withContext(Dispatchers.IO) {
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
    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)?.let {
        try {
            context.contentResolver.openOutputStream(it).use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                return@withContext true
            }
        } catch (exception: FileNotFoundException) {
            exception.printStackTrace()
            return@withContext false
        }
    }
    false
}

private fun getAppPicturePath(): String {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "Camera"
    } else {
        Environment.DIRECTORY_DCIM + File.separator + "Camera"
    }
}
```

## 二、上传图片 - 调起相机、裁剪

1. 调起相机适配,指定图片保存图片的位置 - 通过ContentProvider获取需要插入的Uri

```kotlin
    /**
     * 拍照后保存位置
     */
    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, insertPhoto());

    /**
     * 相册中插入图片
     */
    private Uri insertPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            currentPhotoUri = createImageUri();
        } else {
						//第一种方式
            File photoFile = createImageFile();
            if (photoFile != null) {
                currentPhotoUri = FileProvider.getUriForFile(mContext.get(), captureStrategy.authority, photoFile);
            }
						//第二种
						currentPhotoUri = createImageUri();
        }
        return currentPhotoUri;
    }

   /**
     * 创建图片地址uri,用于保存拍照后的照片 
     *
     * @return 图片的uri
     */
    private Uri createImageUri() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED)) {
            return mContext.get().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues());
        } else {
            return mContext.get().getContentResolver().insert(MediaStore.Images.Media.INTERNAL_CONTENT_URI, new ContentValues());
        }
    }

    /**
     * 创建图片地址，用于保存图片
     */
    private File createImageFile() {
        // Create an image file name
        String timeStamp = FormatterUtilsKt.formatDate(System.currentTimeMillis(), "yyyyMMdd_HHmmss");
        String imageFileName = String.format("JPEG_%s.jpg", timeStamp);
        File storageDir;
        if (captureStrategy.isPublic) {
            storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!storageDir.exists()) storageDir.mkdirs();
        } else {
            storageDir = mContext.get().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        }
        if (captureStrategy.directory != null) {
            storageDir = new File(storageDir, captureStrategy.directory);
            if (!storageDir.exists()) storageDir.mkdirs();
        }

        // Avoid joining path components manually
        File tempFile = new File(storageDir, imageFileName);

        // Handle the situation that user's external storage is not ready
        if (!Environment.MEDIA_MOUNTED.equals(EnvironmentCompat.getStorageState(tempFile))) {
            return null;
        }

        return tempFile;
    }
```

2. 系统裁剪图片适配，指定图片保存的位置- 通过ContentProvider获取需要插入的Uri

```kotlin
		/**
     * 裁剪后保存位置
     */
    intent.putExtra(MediaStore.EXTRA_OUTPUT, insertClipPhoto())

    /**
     * 相册中插入裁切图片
     */
    private Uri insertClipPhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            currentClipUri = createImageUri();
        } else {
						//第一种
            currentClipUri = Uri.fromFile(createImageFile());
						//第二种
            currentClipUri = createImageUri();
        }
        return currentClipUri;
    }
```

3. onActivityResult适配 -统一使用Uri来操作图片

[权限](https://www.notion.so/907826dcf6fd48ae82d422e1828ad1ea)

### 二、上传图片 - Luban压缩适配

### 前置条件及其影响

1. 内部通过File 获取 大小、尺寸 等信息 来进行压缩 →需要有权限操作File
    - 需要将共享目录文件copy到应用专属目录
2. 压缩后生成的文件名称为时间戳
    - 无法通过使用copy文件与压缩后文件一致，覆写来减少临时文件的管理，只能通过压缩后删除临时文件
    - 多次选择同一个图片压缩后会生成多张图片
3. 不满足压缩条件会返回原文件
    - 压缩成功后需要进行条件判断在进行临时文件删除

### Luban适配策略

1. 将共享目录拷贝到cache,如果发生压缩，完成后删除cache,如果未发生压缩，则保留cache.
    - 将共享目录拷贝到cache目录(与target一致)：data/data/packagename/files/demo/imag

    ```kotlin
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
    ```

    - 压缩完成后，对cache文件进行处理

    ```kotlin
    override fun onSuccess(file: File) {
        if (file != copyFile) copyFile.delete()
        onSuccess(file, index)
    }
    ```

    - 整理的luBanFile(统一固定配置，减少参数传递，删除多余的方法)

    ```kotlin
    fun luBanUri(context: Context, sourceFile: Uri, size: Int = 200, targetDir: String = cachePath(context), onSuccess: (File, Int) -> Unit, onStart: () -> Unit = {}, onError: (e: Throwable) -> Unit = {}, index: Int = -1) {
        val copyFile = copyToCache(context, sourceFile)
        createDirNoExist(targetDir)
        Luban.with(context)
                .load(copyFile)                                   
                .ignoreBy(size)                                  
                .setTargetDir(targetDir)                        
                .setCompressListener(object : OnCompressListener { 
                    override fun onStart() {
                        onStart()
                    }

                    override fun onSuccess(file: File) {
                        if (file != copyFile) copyFile.delete()
                        onSuccess(file, index)
                    }

                    override fun onError(e: Throwable) {
                        onError(e)
                    }
                }).launch()
    }
    ```

    # 总结

    1. 从FileProvier 限制 使用 FileUri的出，到限制FileUri的入
    2. 遵循分区存储三个原则对外部存储文件访问方式重新设计。

### Demo

1. [https://github.com/many-cat/ScopedStorageTest](https://github.com/many-cat/ScopedStorageTest)
