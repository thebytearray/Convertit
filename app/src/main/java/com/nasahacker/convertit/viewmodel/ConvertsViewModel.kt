package com.nasahacker.convertit.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ConvertsViewModel : ViewModel() {

    private val _audioFiles = MutableLiveData<List<File>>()
    val audioFiles: LiveData<List<File>> get() = _audioFiles

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun loadAllFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val files = AppUtils.getAudioFilesFromConvertedFolder()
            _audioFiles.postValue(files)
        }
    }

    fun deleteFile(context: Context, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = file.delete()
            val resultMessage = if (success) {
                loadAllFiles(context)
                "File deleted successfully."
            } else {
                "Something went wrong, status: Failed!"
            }
            _message.postValue(resultMessage)
        }
    }
}
