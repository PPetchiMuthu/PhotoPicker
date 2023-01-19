package com.example.android.bottomsheetimage.fragment

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class ViewModel(application: Application) : AndroidViewModel(application) {
    val selectedImage : MutableLiveData<List<Uri>> = MutableLiveData()

    fun addSelectImage(imageUris: MutableList<Uri>) {
        selectedImage.postValue(imageUris)
    }
}