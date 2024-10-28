package com.nasahacker.convertit.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.nasahacker.convertit.R
import com.nasahacker.convertit.databinding.ItemAudioBinding
import com.nasahacker.convertit.listener.OnLongPressListener
import com.nasahacker.convertit.util.AppUtils
import java.io.File
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConvertsAdapter(
    private val context: Context,
    private var fileList: List<File>,
    private val onLongPressListener: OnLongPressListener
) : RecyclerView.Adapter<ConvertsAdapter.ConvertsViewHolder>() {

    private var fullFileList: List<File> = ArrayList(fileList)
    private var filterJob: Job? = null

    inner class ConvertsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding: ItemAudioBinding = ItemAudioBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConvertsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_audio, parent, false)
        return ConvertsViewHolder(view)
    }

    override fun getItemCount(): Int = fileList.size

    override fun onBindViewHolder(holder: ConvertsViewHolder, position: Int) {
        val currentItem = fileList[position]
        with(holder.binding) {
            songName.text = currentItem.name
            songSize.text = AppUtils.getFileSizeInReadableFormat(context, currentItem)
            btnPlay.setOnClickListener { AppUtils.openMusicFileInPlayer(context, currentItem) }
            btnShare.setOnClickListener { AppUtils.shareMusicFile(context, currentItem) }
        }
        holder.itemView.setOnLongClickListener {
            onLongPressListener.onLongPressed(currentItem)
            true
        }
    }

    fun updateData(list: List<File>) {
        fullFileList = ArrayList(list)
        fileList = list
        notifyDataSetChanged()
    }

    // Coroutine-based filter function
    fun filter(query: String) {
        // Cancel any previous filtering job to avoid overlapping work
        filterJob?.cancel()
        filterJob = CoroutineScope(Dispatchers.Default).launch {
            val filteredList = if (query.isEmpty()) {
                fullFileList
            } else {
                fullFileList.filter {
                    it.name.contains(query, ignoreCase = true)
                }
            }
            // Update the list on the main thread
            withContext(Dispatchers.Main) {
                fileList = filteredList
                notifyDataSetChanged()
            }
        }
    }
}
