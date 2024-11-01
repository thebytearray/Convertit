package com.nasahacker.convertit.viewmodel

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

class AboutViewModel : ViewModel() {

    private val _avatarUrls = MutableLiveData<List<String>>()
    val avatarUrls: LiveData<List<String>> get() = _avatarUrls

    private val _bitmaps = MutableLiveData<List<Bitmap>>()
    val bitmaps: LiveData<List<Bitmap>> get() = _bitmaps

    // Fetch the contributors from the GitHub API
    fun loadContributors() {
        viewModelScope.launch(Dispatchers.IO) {
            val url = URL("https://api.github.com/repos/CodeWithTamim/Convertit/contributors")
            val urlConnection = url.openConnection() as HttpURLConnection
            val response = urlConnection.inputStream.bufferedReader().use { it.readText() }

            val contributorsArray = JSONArray(response)
            val avatarUrlsList = mutableListOf<String>()

            for (i in 0 until contributorsArray.length()) {
                val contributor = contributorsArray.getJSONObject(i)
                avatarUrlsList.add(contributor.getString("avatar_url"))
            }

            // Update the avatar URLs list
            withContext(Dispatchers.Main) {
                _avatarUrls.value = avatarUrlsList
                loadBitmaps(avatarUrlsList)
            }
        }
    }

    // Load Bitmaps from the avatar URLs
    private fun loadBitmaps(urls: List<String>) {
       viewModelScope.launch(Dispatchers.IO) {
            val bitmapsList = urls.mapNotNull { url -> loadBitmap(url) }
            withContext(Dispatchers.Main) {
                _bitmaps.value = bitmapsList
            }
        }
    }

    private suspend fun loadBitmap(url: String): Bitmap? {
        return withContext(Dispatchers.IO) {
            try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.doInput = true
                connection.connect()
                val input = connection.inputStream
                BitmapFactory.decodeStream(input)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
