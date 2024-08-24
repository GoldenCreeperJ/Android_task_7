package com.example.douyin

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.douyin.dyRetrofit.DyRetrofit
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

class DyApplication: Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        lateinit var retrofit: DyRetrofit
        lateinit var myId: String
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
        retrofit = Retrofit.Builder().baseUrl("http://10.133.14.2:4523/")
            .addConverterFactory(GsonConverterFactory.create()).build()
            .create(DyRetrofit::class.java)
        if (!File(filesDir, "data").exists()) {
            File(filesDir, "data").createNewFile()
            getSharedPreferences("data", Context.MODE_PRIVATE).apply {
                edit().putString("id", "-1").apply()
            }
        }
        myId = getSharedPreferences("data", Context.MODE_PRIVATE).getString("id", "-1")!!
    }
}

fun permissionRequester(activity: Activity, permission: String, block:()->Unit) {
    if(ContextCompat.checkSelfPermission(DyApplication.context, permission)!= PackageManager.PERMISSION_GRANTED){
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(permission),1)}
    else{block()}}

fun permissionRequesterPlus(activity: Activity,oldPermission: String,newPermission:String,version:Int, block:()->Unit) {
    if(Build.VERSION.SDK_INT < version)
        permissionRequester(activity,oldPermission){block()}
    else permissionRequester(activity,newPermission){block()}}

fun internetCheckCore(block: () -> Boolean): Boolean {
    return if (ContextCompat.checkSelfPermission(
            DyApplication.context,
            Manifest.permission.INTERNET
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        block()
    } else {
        val connectivityManager =
            DyApplication.context.getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)!!
                .hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ) {
            true
        } else {
            Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT)
                .show()
            false
        }
    }
}

fun internetCheck(): Boolean {
    return internetCheckCore {
        ActivityCompat.requestPermissions(
            DyApplication.context as Activity,
            arrayOf(Manifest.permission.INTERNET),
            1
        )
        internetCheckCore {
            Toast.makeText(DyApplication.context, "网络连接失败", Toast.LENGTH_SHORT)
                .show()
            return@internetCheckCore false
        }
    }
}

@Composable
fun IconWithLabel(icon: ImageVector, text: String, func: () -> Unit) {
    IconButton(onClick = { func() }) {
        Icon(imageVector = icon, contentDescription = null)
    }
    Text(text = text)
}

@Composable
fun AvatarImage(url: String,modifier: Modifier =Modifier){
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(
            CircleShape
        )
    )
}

fun getFilePathFromContentUri(context: Context, contentUri: Uri): String? {
    val projection: Array<String>
    val mediaType: String

    when {
        contentUri.toString().contains("images") -> {
            projection = arrayOf(MediaStore.Images.Media.DATA)
            mediaType = MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString()
        }
        contentUri.toString().contains("video") -> {
            projection = arrayOf(MediaStore.Video.Media.DATA)
            mediaType = MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString()
        }
        else -> return null
    }

    val cursor: Cursor? = context.contentResolver.query(contentUri, projection, null, null, null)
    cursor?.use {
        val columnIndex = it.getColumnIndexOrThrow(projection[0])
        if (it.moveToFirst()) {
            return it.getString(columnIndex)
        }
    }
    return null
}