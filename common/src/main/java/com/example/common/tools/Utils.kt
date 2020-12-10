package com.example.common.tools

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast

fun <T> easyLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

val Float.dp
    get() = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, Resources.getSystem().displayMetrics)

fun logd(string: String){
    Log.d("darrenAndroid",string)
}

val Context.screenWidth: Int
    get() {
        val outMetrics = DisplayMetrics()
        (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getMetrics(outMetrics)
        return outMetrics.widthPixels
    }

fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()
fun Int.color(context: Context) = context.resources.getColor(this)
fun Int.layoutToView(context: Context, root: ViewGroup? = null): View = LayoutInflater.from(context).inflate(this, root, false)
fun View.setLayoutParams(width: Int? = null, height: Int? = null) {
    val params = layoutParams
    width?.apply { params.width = this }
    height?.apply { params.height = this }
    layoutParams = params
}

fun getBitmap(width: Int, resources: Resources, resId: Int): Bitmap {
    val options = BitmapFactory.Options()
    options.inJustDecodeBounds = true
    BitmapFactory.decodeResource(resources, resId, options)
    options.inJustDecodeBounds = false
    options.inDensity = options.outWidth
    options.inTargetDensity = width
    return BitmapFactory.decodeResource(resources, resId, options)
}

//************************************************String************************************************
fun String.toast(context: Context) = Toast.makeText(context, this, Toast.LENGTH_SHORT).show()