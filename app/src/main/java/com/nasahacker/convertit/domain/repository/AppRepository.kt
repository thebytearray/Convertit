package com.nasahacker.convertit.domain.repository

import kotlinx.coroutines.flow.Flow

interface AppRepository {
    val isDontShowAgain: Flow<Boolean>
    val selectedCustomLocation: Flow<String>

    suspend fun saveIsDontShowAgain(value: Boolean)

    suspend fun saveSelectedCustomLocation(value: String)
}
