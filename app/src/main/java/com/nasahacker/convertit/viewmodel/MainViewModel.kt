package com.nasahacker.convertit.viewmodel

import android.app.Application
import androidx.activity.result.IntentSenderRequest
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(application)

    // LiveData to observe status messages
    private val _updateStatus = MutableLiveData<String>()
    val updateStatus: LiveData<String> get() = _updateStatus

    private val _showRestartSnackbar = MutableLiveData<Boolean>()
    val showRestartSnackbar: LiveData<Boolean> get() = _showRestartSnackbar

    // InstallStateUpdatedListener as a class property so it can be unregistered in onCleared
    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        when (state.installStatus()) {
            InstallStatus.DOWNLOADED -> {
                _updateStatus.postValue("Update downloaded. Restart to complete.")
                _showRestartSnackbar.postValue(true)
            }
            InstallStatus.FAILED -> _updateStatus.postValue("Update failed. Try again later.")
            InstallStatus.CANCELED -> _updateStatus.postValue("Update canceled. You can update later.")
            InstallStatus.DOWNLOADING -> {
               //TODO
            }
            InstallStatus.INSTALLED -> _updateStatus.postValue("Update installed successfully!")
            InstallStatus.INSTALLING -> _updateStatus.postValue("Installing update...")
            InstallStatus.PENDING -> _updateStatus.postValue("Update pending. It will start soon.")
            InstallStatus.REQUIRES_UI_INTENT -> _updateStatus.postValue("Update requires additional user action.")
            InstallStatus.UNKNOWN -> _updateStatus.postValue("Update status unknown. Please check again later.")
        }
    }

    init {
        // Register the listener
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    // Method to check for updates
    fun checkForUpdate(activityResultLauncher: androidx.activity.result.ActivityResultLauncher<IntentSenderRequest>) {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            val updateType = if ((appUpdateInfo.clientVersionStalenessDays() ?: -1) >= 7)
                AppUpdateType.IMMEDIATE else AppUpdateType.FLEXIBLE

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                appUpdateInfo.isUpdateTypeAllowed(updateType)) {
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    activityResultLauncher,
                    AppUpdateOptions.newBuilder(updateType).build()
                )
            }
        }
    }

    // Unregister the listener to avoid memory leaks
    override fun onCleared() {
        super.onCleared()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    // Call this to complete the downloaded update
    fun completeUpdate() {
        appUpdateManager.completeUpdate()
    }
}
