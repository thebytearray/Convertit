package com.nasahacker.convertit.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.R
import com.nasahacker.convertit.util.AppUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ConvertsViewModel : ViewModel() {

    private val _audioFiles = MutableLiveData<List<File>>()
    val audioFiles: LiveData<List<File>> get() = _audioFiles

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> get() = _message

    fun loadAllFiles() {
        viewModelScope.launch(Dispatchers.IO) {
            val files = AppUtils.getAudioFilesFromConvertedFolder()
            _audioFiles.postValue(files)
        }
    }

    fun deleteFile(context: Context, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = file.delete()
            val resultMessage = if (success) {
                loadAllFiles()
                context.getString(R.string.label_file_deleted_successfully)
            } else {
                context.getString(R.string.label_something_went_wrong_status_failed)
            }
            _message.postValue(resultMessage)
        }
    }
}
