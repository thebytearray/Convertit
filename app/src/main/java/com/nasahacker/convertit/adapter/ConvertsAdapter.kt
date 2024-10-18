package com.nasahacker.convertit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ItemAudioBinding
import com.nasahacker.convertit.listener.OnLongPressListener
import com.nasahacker.convertit.util.FileUtils
import java.io.File

class ConvertsAdapter(
    private val context: Context,
    private var fileList: List<File>,
    private val onLongPressListener: OnLongPressListener
) :
    Adapter<ConvertsAdapter.ConvertsViewHolder>() {
    inner class ConvertsViewHolder(itemView: View) : ViewHolder(itemView) {
        val binding: ItemAudioBinding = ItemAudioBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvertsViewHolder =
        ConvertsViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_audio, parent, false
            )
        )

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: ConvertsViewHolder, position: Int) {
        val currentItem = fileList[position]
        holder.binding.songName.text = currentItem.name
        holder.binding.songSize.text = FileUtils.getFileSizeInReadableFormat(context, currentItem)
        holder.itemView.setOnLongClickListener {
            onLongPressListener.onLongPressed(currentItem)
            true
        }
        holder.binding.btnPlay.setOnClickListener {
            FileUtils.openMusicFileInPlayer(context, currentItem)
        }
        holder.binding.btnShare.setOnClickListener {
            FileUtils.shareMusicFile(context, currentItem)
        }
    }

    fun updateData(list: List<File>) {
        fileList = list
        notifyDataSetChanged()
    }
}