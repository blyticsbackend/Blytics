package com.nbt.blytics.modules.galleryBottomSheet

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.nbt.blytics.R

class GalleryAdapter(
    private val context: Context,
    private var imageList: List<Uri>,
    private val onImageSelected: (Uri) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    fun updateData(newList: List<Uri>) {
        imageList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.gallery_item, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val imageUri = imageList[position]
        Glide.with(context)
            .load(imageUri)
            .into(holder.imageView)

        holder.imageView.setOnClickListener {
            onImageSelected(imageUri)
        }
    }

    override fun getItemCount(): Int = imageList.size

    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ShapeableImageView = view.findViewById(R.id.img_gallery)
    }
}