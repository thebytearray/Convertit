package com.nasahacker.convertit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.GONE
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ItemAudioBinding
import com.nasahacker.convertit.util.AppUtils
import java.io.File

class HomeAdapter(
    private val context: Context,
    private var fileList: List<File>
) : RecyclerView.Adapter<HomeAdapter.HomeViewHolder>() {

    inner class HomeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemAudioBinding = ItemAudioBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false)
        return HomeViewHolder(view)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: HomeViewHolder, position: Int) {
        val currentItem = fileList[position]
        with(holder.binding) {
            btnPlay.visibility = GONE
            btnShare.visibility = GONE
            songName.text = currentItem.name
            songSize.text = AppUtils.getFileSizeInReadableFormat(context, currentItem)
        }
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
