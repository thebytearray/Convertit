package com.nasahacker.convertit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.GONE
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ItemAudioBinding
import com.nasahacker.convertit.util.FileUtils
import java.io.File

class HomeAdapter(
    private val context: Context,
    private var fileList: List<File>
) :
    Adapter<HomeAdapter.HomeViewHolder>() {
    inner class HomeViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: ItemAudioBinding = ItemAudioBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder =
        HomeViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_audio, parent, false
            )
        )

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentItem = fileList[position]
        holder.binding.btnPlay.visibility = GONE
        holder.binding.btnShare.visibility = GONE
        holder.binding.songName.text = currentItem.name
        holder.binding.songSize.text = FileUtils.getFileSizeInReadableFormat(context, currentItem)

    }

    fun updateData(list: List<File>) {
        fileList = list
        notifyDataSetChanged()
    }

    fun clearAll() {
        fileList = emptyList()
        notifyDataSetChanged()
    }
}