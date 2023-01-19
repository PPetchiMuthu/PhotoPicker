package com.example.android.bottomsheetimage.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.android.bottomsheetimage.R
import com.example.android.bottomsheetimage.adapter.Adapter
import com.example.android.bottomsheetimage.databinding.FragmentMainBinding

class MainFragment : Fragment(){

    private lateinit var binding: FragmentMainBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater, container, false)
        val myViewModel = ViewModelProvider(requireActivity())[ViewModel::class.java]

//        binding.mainRecyclerView.layoutManager = GridLayoutManager(context,3)

        myViewModel.selectedImage.observe(viewLifecycleOwner, Observer { image ->
            if (image != null) {
                // Update the UI with the selected image
                binding.mainRecyclerView.adapter = Adapter(image)
            }
        })

        binding.floatingActionButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1
                )
            } else {
                findNavController().navigate(R.id.action_mainFragment_to_bottomSheetFragment)
            }
        }

        return binding.root
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            0 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(requireContext(), "Successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Access Denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}