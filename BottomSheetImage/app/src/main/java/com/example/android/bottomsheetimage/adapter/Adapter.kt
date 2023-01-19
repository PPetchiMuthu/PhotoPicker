package com.example.android.bottomsheetimage.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.android.bottomsheetimage.R
import com.example.android.bottomsheetimage.databinding.ListViewBinding

class Adapter(private val imageUri: List<Uri>) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    inner class ViewHolder(private val binding: ListViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setImage(uri: Uri?) {
            Glide.with(binding.root).load(uri).into(binding.imageView)
        }
    }

    private var onItemClickListener: ((Uri) -> Unit)? = null

    fun setOnItemClickListener(listener: (Uri) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ListViewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uri = imageUri[position]
        holder.setImage(uri)
        holder.itemView.setOnClickListener {
            val drawable = holder.itemView.background
            val color = (drawable as? ColorDrawable)?.color
            if (color==R.color.black) {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            } else {
                holder.itemView.setBackgroundColor(R.color.black)
            }
            onItemClickListener?.let { it(uri) }
        }
    }

    override fun getItemCount(): Int {
        return imageUri.size
    }
}