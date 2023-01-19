package com.example.android.photossaver

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.android.photossaver.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var isReadPermissionGranted = false
    private var isWritePermissionGranted = false
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var imageBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
                isReadPermissionGranted =
                    permission[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadPermissionGranted
                isWritePermissionGranted = permission[Manifest.permission.WRITE_EXTERNAL_STORAGE]
                    ?: isWritePermissionGranted
            }

        requestPermission()

        val file = File(filesDir, "image.jpg")
        if (file.exists()) {
            lifecycleScope.launch {
                try {
                    val inputStream = openFileInput(file.name)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    imageBitmap = bitmap
                    inputStream.close()
                    binding.imageView.setImageBitmap(bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        val imagePicker = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            binding.imageView.setImageURI(uri)
            imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            Toast.makeText(this@MainActivity, "Photo Picked", Toast.LENGTH_SHORT).show()
        }

        binding.pick.setOnClickListener {
            if (isReadPermissionGranted && isWritePermissionGranted) {
                imagePicker.launch("image/*")
            } else {
                requestPermission()
            }
        }
        binding.file.setOnClickListener {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val outputStream = openFileOutput(file.name, Context.MODE_PRIVATE)
                    imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            Toast.makeText(this@MainActivity, "Photo Saved in File", Toast.LENGTH_SHORT).show()
        }
        binding.delete.setOnClickListener {
            if (file.exists()) {
                imageBitmap = null
                file.delete()
            }
            binding.imageView.setImageDrawable(null)
        }
        binding.save.setOnClickListener {
            lifecycleScope.launch {
//                downloadSpecificStorage(imageBitmap)
                if (downloadImage("image" + Random.nextInt(), imageBitmap)) {
                    Toast.makeText(this@MainActivity, "Photo Saved in Downloads", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@MainActivity, "Unable to save Photo", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }
    }

    private fun requestPermission() {
        val isReadPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        val isWritePermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdkLevel = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

        isWritePermissionGranted = isWritePermission || minSdkLevel
        isReadPermissionGranted = isReadPermission

        val permissionRequest = mutableListOf<String>()
        if (!isWritePermissionGranted) {
            permissionRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isReadPermissionGranted) {
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (permissionRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }
    }

    private fun downloadImage(string: String, bitmap: Bitmap?): Boolean {
        val imageCollection: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "$string.jpg")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (bitmap != null) {
                put(MediaStore.Images.Media.WIDTH, bitmap.width)
                put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            }
        }
        return try {
            contentResolver.insert(imageCollection, contentValues)?.also {
                contentResolver.openOutputStream(it).use { outputStream ->
                    if (bitmap != null) {
                        if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)) {
                            throw IOException("Failed to Save Image")
                        }
                    }
                }
            } ?: throw IOException("Failed to Save Image")
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

}


//    private fun downloadSpecificStorage(bitmap: Bitmap?) {
//        if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
//            // External storage is available and writable
//            val file = File(applicationContext.getExternalFilesDir(null), "image.jpg")
//            // Use the file
//            lifecycleScope.launch(Dispatchers.IO) {
//                try {
//                    val stream = FileOutputStream(file)
//                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                    stream.flush()
//                    stream.close()
//                } catch (e: IOException) {
//                    e.printStackTrace()
//                }
//            }
//        }else{
//            Toast.makeText(this, "Photo Not saved", Toast.LENGTH_SHORT).show()
//
//        }
//    }