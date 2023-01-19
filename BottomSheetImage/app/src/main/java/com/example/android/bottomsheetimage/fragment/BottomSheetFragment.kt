package com.example.android.bottomsheetimage.fragment

import android.content.ContentUris
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.android.bottomsheetimage.R
import com.example.android.bottomsheetimage.adapter.Adapter
import com.example.android.bottomsheetimage.databinding.FragmentBottomSheetBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var binding: FragmentBottomSheetBinding
    private val selectedImages = mutableListOf<Uri>()
    private lateinit var _adapter: Adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentBottomSheetBinding.inflate(inflater, container, false)
        val myViewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]
        val imageUri: List<Uri> = getImageUris()

        _adapter = Adapter(imageUri)
        binding.bottomRecyclerView.apply {
            layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
            adapter = _adapter
        }

        _adapter.setOnItemClickListener {
            if (selectedImages.contains(it)) {
                selectedImages.remove(it)
            } else selectedImages.add(it)
        }

        binding.done.setOnClickListener {
            myViewModel.addSelectImage(selectedImages)
            findNavController().navigate(R.id.action_bottomSheetFragment_to_mainFragment)
        }
        binding.cancel.setOnClickListener {
            dismiss()
        }

        return binding.root
    }

    private fun getImageUris(): List<Uri> {
        val imageUris = mutableListOf<Uri>()
        val cursor = context?.contentResolver?.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, null, null,
            MediaStore.Images.Media.DATE_ADDED
        )
        cursor?.use {
            val columnIndex = it.getColumnIndex(MediaStore.Images.Media._ID)
            while (it.moveToNext()) {
                val imageId = it.getLong(columnIndex)
                val imageUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
                imageUris.add(imageUri)
            }
        }
        return imageUris
    }
}