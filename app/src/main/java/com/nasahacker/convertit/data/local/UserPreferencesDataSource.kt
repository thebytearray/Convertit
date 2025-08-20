package com.nasahacker.convertit.data.local

import android.os.Environment
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.nasahacker.convertit.util.AppConfig.FOLDER_DIR
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import javax.inject.Inject

class UserPreferencesDataSource
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) {
        private object Keys {
            val PREF_DONT_SHOW_AGAIN = booleanPreferencesKey("pref_dont_show_again")
            val PREF_CUSTOM_SAVE_LOCATION = stringPreferencesKey("pref_custom_save_location")
        }

        val isDontShowAgain: Flow<Boolean> =
            dataStore.data.map { prefs ->
                prefs[Keys.PREF_DONT_SHOW_AGAIN] ?: false
            }

        val selectedCustomSaveLocation: Flow<String> =
            dataStore.data.map { prefs ->
                val savedValue = prefs[Keys.PREF_CUSTOM_SAVE_LOCATION]
                val defaultValue = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                    FOLDER_DIR,
                ).absolutePath
                val result = savedValue ?: defaultValue
                android.util.Log.d("UserPreferencesDataSource", "Retrieved custom save location - saved: '$savedValue', default: '$defaultValue', returning: '$result'")
                result
            }

        suspend fun saveIsDontShowAgain(value: Boolean) {
            dataStore.edit { prefs ->
                prefs[Keys.PREF_DONT_SHOW_AGAIN] = value
            }
        }

        suspend fun saveSelectedCustomSaveLocation(value: String) {
            android.util.Log.d("UserPreferencesDataSource", "Saving custom save location: '$value'")
            dataStore.edit { prefs ->
                prefs[Keys.PREF_CUSTOM_SAVE_LOCATION] = value
                android.util.Log.d("UserPreferencesDataSource", "Successfully saved to DataStore")
            }
        }
    }
