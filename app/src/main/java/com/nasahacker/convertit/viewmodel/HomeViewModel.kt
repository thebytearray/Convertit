package com.nasahacker.convertit.viewmodel

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.util.AppUtils
import com.nasahacker.convertit.util.Constants.CONVERT_BROADCAST_ACTION
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class HomeViewModel : ViewModel() {

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    private val _selectedUris = MutableLiveData<List<Uri>>()
    val selectedUris: LiveData<List<Uri>> get() = _selectedUris

    private val _selectedFiles = MutableLiveData<List<File>>()
    val selectedFiles: LiveData<List<File>> get() = _selectedFiles

    private val _isSuccess = MutableLiveData<Boolean?>()
    val isSuccess: LiveData<Boolean?> get() = _isSuccess

    private val conversionReceiver = object : BroadcastReceiver() {
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
        val intentFilter = IntentFilter(CONVERT_BROADCAST_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(conversionReceiver, intentFilter, Context.RECEIVER_EXPORTED)
        } else {
            context.registerReceiver(conversionReceiver, intentFilter)
        }
    }

    fun openPicker(activity: Activity, pickFileLauncher: ActivityResultLauncher<Intent>) {
        AppUtils.openFilePicker(activity, pickFileLauncher)
    }

    fun setSelectedUris(activity: Activity, uris: List<Uri>) {
        _selectedUris.postValue(uris)
        setFilesListFromUri(activity, uris)
    }

    private fun setFilesListFromUri(activity: Activity, uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            _selectedFiles.postValue(AppUtils.getFilesFromUris(activity, uris))
        }
    }

    fun unregister(context: Context) {
        context.unregisterReceiver(conversionReceiver)
    }

    override fun onCleared() {
        super.onCleared()
    }
}
