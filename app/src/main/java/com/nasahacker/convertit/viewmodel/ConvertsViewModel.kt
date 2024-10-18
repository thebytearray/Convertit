package com.nasahacker.convertit.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.util.FileUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ConvertsViewModel : ViewModel() {
    private val _audioFiles = MutableLiveData<List<File>>()
    val audioFiles : LiveData<List<File>> get() = _audioFiles
    private val _message = MutableLiveData<String>()
    val message : LiveData<String> get() = _message

    fun loadAllFiles(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val result = FileUtils.getAudioFilesFromConvertedFolder(context)
            _audioFiles.postValue(result)
        }
    }

    fun deleteFile(context: Context, file: File) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (file.delete()) {
                    _message.postValue("File deleted successfully.")
                    loadAllFiles(context)
                } else {
                    _message.postValue("Something went wrong, status : Failed !")
                }
            } catch (e: Exception) {
                _message.postValue("Something went wrong, status : Failed !")

            }
        }
    }


}