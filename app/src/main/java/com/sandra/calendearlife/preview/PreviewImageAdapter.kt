package com.sandra.calendearlife.preview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sandra.calendearlife.databinding.ItemPreviewImageBinding

class PreviewImageAdapter(val images : IntArray) : RecyclerView.Adapter<PreviewImageAdapter.ImageViewHolder>(){


    class ImageViewHolder(binding: ItemPreviewImageBinding): RecyclerView.ViewHolder(binding.root)  {

        val image = binding.image

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {

        return ImageViewHolder(ItemPreviewImageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return images.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.image.setImageResource(images[position])
    }



}