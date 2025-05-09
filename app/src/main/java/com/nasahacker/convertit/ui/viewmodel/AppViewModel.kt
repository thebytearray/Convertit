package com.nasahacker.convertit.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nasahacker.convertit.App
import com.nasahacker.convertit.util.AppUtil
import com.nasahacker.convertit.util.Constant
import com.nasahacker.convertit.util.Constant.IS_SUCCESS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * @author Tamim Hossain
 * @email tamimh.dev@gmail.com
 * @license Apache-2.0
 *
 * ConvertIt is a free and easy-to-use audio converter app.
 * It supports popular audio formats like MP3 and M4A.
 * With options for high-quality bitrates ranging from 128k to 320k,
 * ConvertIt offers a seamless conversion experience tailored to your needs.
 */

class AppViewModel : ViewModel() {
    private val _uriList = MutableStateFlow<ArrayList<Uri>>(ArrayList())
    val uriList: StateFlow<ArrayList<Uri>> = _uriList

    private val _conversionStatus = MutableStateFlow<Boolean?>(null)
    val conversionStatus: StateFlow<Boolean?> = _conversionStatus

    private val conversionStatusReceiver =
        object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent?,
            ) {
                val isSuccess = intent?.getBooleanExtra(IS_SUCCESS, false) ?: false
                viewModelScope.launch {
                    _conversionStatus.value = isSuccess
                    if (isSuccess) {
                        clearUriList()
                    }
                }
            }
        }

    fun resetConversionStatus() {
        viewModelScope.launch {
            _conversionStatus.value = null
        }
    }

    init {
        startListeningForBroadcasts()
    }

    /**
     * Updates the URI list with new URIs from the given intent.
     */
    fun updateUriList(intent: Intent?) {
        viewModelScope.launch {
            intent?.let {
                val uris = AppUtil.getUriListFromIntent(it)
                if (uris.isNotEmpty()) {
                    val updatedList = ArrayList(_uriList.value).apply { addAll(uris) }
                    _uriList.value = updatedList
                }
            }
        }
    }

    /**
     * Registers the BroadcastReceiver to listen for conversion status updates.
     */
    private fun startListeningForBroadcasts() {
        val intentFilter = IntentFilter(Constant.CONVERT_BROADCAST_ACTION)
        ContextCompat.registerReceiver(
            App.application,
            conversionStatusReceiver,
            intentFilter,
            AppUtil.receiverFlags(),
        )
    }

    /**
     * Clears the URI list.
     */
    fun clearUriList() {
        viewModelScope.launch {
            _uriList.value = ArrayList()
        }
    }

    /**
     * Unregisters the BroadcastReceiver when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        App.application.unregisterReceiver(conversionStatusReceiver)
    }
}
