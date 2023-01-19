package com.example.android.imagepicker

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.example.android.imagepicker.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val REQUEST_STORAGE_PERMISSION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val file = File(filesDir, "image.txt")

        if (file.exists()) {
            val inputStream = FileInputStream(file)
            val byte = ByteArray(file.length().toInt())
            inputStream.read(byte)
            inputStream.close()
            val string = String(byte)
            val uri = Uri.parse(string)
            println(uri)
            Glide.with(this)
                .load(uri)
                .into(binding.imageView)
        }

        val imagePicker = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imageView)
            GlobalScope.launch(Dispatchers.IO) {
                val outputStream = FileOutputStream(file)
                outputStream.write(uri.toString().toByteArray())
                outputStream.close()
            }
        }

        binding.button.setOnClickListener {
            requestStoragePermission()
//            imagePicker.launch("image/*")
        }

        binding.delete.setOnClickListener {
            if (file.exists()) {
                file.delete()
            }
            binding.imageView.setImageDrawable(null)
        }
    }

    private fun requestStoragePermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            // Request the permission.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {
                // If request is granted, proceed with the action
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val pickImage =
                        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                            Glide.with(this)
                                .load(uri)
                                .into(binding.imageView)
                            GlobalScope.launch(Dispatchers.IO) {
                                val file = File(filesDir, "image.txt")
                                val outputStream = FileOutputStream(file)
                                outputStream.write(uri.toString().toByteArray())
                                outputStream.close()
                            }
                        }
                    pickImage.launch("image/*")
                } else {
                    // If request is denied, display a message to the user explaining why the permission is needed
                    Toast.makeText(applicationContext, "Access Denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


}


//        binding.button.setOnClickListener {
//            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
//                addCategory(Intent.CATEGORY_OPENABLE)
//                type = "image/*"
//            }
//            startActivityForResult(intent, PICK_IMAGE)
//        }

//    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
//            val uri = data.data
//            // Perform further actions with the selected image's URI
//            Glide.with(this)
//                .load(uri)
//                .into(binding.imageView)
//        }
//    }
//
//}