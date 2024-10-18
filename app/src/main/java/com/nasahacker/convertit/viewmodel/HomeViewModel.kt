package com.nasahacker.convertit.viewmodel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.ConvertItApplication
import com.nasahacker.convertit.adapter.HomeAdapter
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.acos

class HomeViewModel :ViewModel() {
    private val _message = MutableLiveData<String>()
    val message get() = _message

    private val _selectedUris = MutableLiveData<List<Uri>>()
    val selectedUris: LiveData<List<Uri>> get() = _selectedUris

    private val _selectedFiles = MutableLiveData<List<File>>()
    val selectedFiles: LiveData<List<File>> get() = _selectedFiles

    private val _isSuccess = MutableLiveData<Boolean?>()
    val isSuccess: LiveData<Boolean?> get() = _isSuccess

    private val mMsgReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val isSuccess = intent?.getBooleanExtra("isSuccess", false)
            _isSuccess.postValue(isSuccess)
        }

    }

    fun clearSelectedFiles() {
        _selectedUris.postValue(emptyList())
        _selectedFiles.postValue(emptyList())
    }


    fun startListenBroadcast(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                mMsgReceiver,
                IntentFilter("com.nasahacker.convertit.ACTION_CONVERSION_COMPLETE"),
                Context.RECEIVER_EXPORTED
            )
        } else {
            context.registerReceiver(
                mMsgReceiver,
                IntentFilter("com.nasahacker.convertit.ACTION_CONVERSION_COMPLETE")
            )
        }

    }


    fun openPicker(activity: Activity, pickFileLauncher: ActivityResultLauncher<Intent>) {
        FileUtils.openFilePicker(activity, pickFileLauncher)
    }

    fun setSelectedUris(activity: Activity, uris: List<Uri>) {
        _selectedUris.postValue(uris)
        setFilesListFromUri(activity, uris)
    }

    private fun setFilesListFromUri(activity: Activity, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedFiles.postValue(FileUtils.getFilesFromUris(activity, uris))
        }
    }

   /* fun convertAudio(
        context: Context,
        uris: List<Uri>,
        format: AudioFormat,
        bitrate: AudioBitrate,
        adapter: HomeAdapter
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            FileUtils.convertAudio(
                context, uris, format, bitrate,
                onSuccess = { filepaths ->
                    _message.postValue("Successfully converted !")
                    _selectedUris.postValue(emptyList())
                    adapter.clearAll()
                },
                onFailure = { error ->
                    _message.postValue("Failed to convert, error: $error")
                    _selectedUris.postValue(emptyList())
                    adapter.clearAll()
                },

                )
        }
    }*/
    fun unregister(context: Context) {
        context.unregisterReceiver(mMsgReceiver)
    }

    override fun onCleared() {
        super.onCleared()
    }


}