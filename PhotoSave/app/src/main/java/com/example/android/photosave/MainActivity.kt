package com.example.android.photosave

import android.Manifest
import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.android.photosave.databinding.ActivityMainBinding
import java.io.File
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val PICK_IMAGE_REQUEST_CODE = 1
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val file = File(filesDir, "image.jpg")

        if (file.exists()) {
            try {
                val inputStream = openFileInput(file.name)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream.close()
                binding.imageView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        binding.pick.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PICK_IMAGE_REQUEST_CODE
                )
            } else {
                pickImageFromGallery()
            }
        }

        binding.delete.setOnClickListener {
            if (file.exists()) {
                file.delete()
            }
            binding.imageView.setImageDrawable(null)
        }
        binding.save.setOnClickListener {
            downloadImage()

//            val contentResolver = applicationContext.contentResolver
//            val inputStream = contentResolver.openInputStream()
//            val downloadsDir =
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//            if (downloadsDir.exists() && downloadsDir.isDirectory && downloadsDir.canWrite()) {
//                //write the file
//                val file1 = File(downloadsDir, "image.jpg")
//                val outputStream = FileOutputStream(file1)
//                inputStream.use { input ->
//                    outputStream.use { output ->
//                        input!!.copyTo(output)
//                    }
//                }
//                outputStream.close()
//            }
        }
    }

    private fun downloadImage() {
        val file = File(filesDir, "image.jpg")
        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.fromFile(file))
            .setTitle("My Image")
            .setDescription("Downloading")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.jpg")
        val downloadId = downloadManager.enqueue(request)

        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (downloadId == id) {
                    val query = DownloadManager.Query()
                    query.setFilterById(downloadId)
                    val cursor = downloadManager.query(query)
                    if (cursor.moveToFirst()) {
                        val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                        if (columnIndex >= 0) {
                            val status = cursor.getInt(columnIndex)
                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                // Do something with the downloaded file
                            } else {
                                //Handle download failure
                            }
                        }
                    }
                    cursor.close()
                }
            }
        }
        val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(onComplete, filter)
    }


    private fun pickImageFromGallery() {
        val intent =
            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            uri = data.data!!
            binding.imageView.setImageURI(uri)
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                val file = File(filesDir, "image.jpg")
                val outputStream = openFileOutput(file.name, Context.MODE_PRIVATE)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show()
                    // permission denied
                    // you can show a message to the user or take other action
                }
                return
            }
        }
    }

}


//    private fun getPermission() {
//        val permissions = arrayOf(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//        )
//
//        if (ContextCompat.checkSelfPermission(
//                this,
//                permissions[0]
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            ActivityCompat.requestPermissions(this, permissions, 0)
//        }
//
//    }


// To download http / https
//val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
//val request = DownloadManager.Request(imageUri)
//    .setTitle("My Image")
//    .setDescription("Downloading")
//    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
//    .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "image.jpg")
//val downloadId = downloadManager.enqueue(request)
//
//val onComplete = object : BroadcastReceiver() {
//    override fun onReceive(context: Context?, intent: Intent?) {
//        val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
//        if (downloadId == id) {
//            val query = DownloadManager.Query()
//            query.setFilterById(downloadId)
//            val cursor = downloadManager.query(query)
//            if (cursor.moveToFirst()) {
//                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
//                if (columnIndex >= 0) {
//                    val status = cursor.getInt(columnIndex)
//                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
//                        // Do something with the downloaded file
//                    } else {
//                        //Handle download failure
//                    }
//                }
//            }
//            cursor.close()
//        }
//    }
//}
//val filter = IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
//registerReceiver(onComplete, filter)

