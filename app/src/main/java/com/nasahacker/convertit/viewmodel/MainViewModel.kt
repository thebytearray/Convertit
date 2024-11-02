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
import com.nasahacker.convertit.R

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
                _updateStatus.postValue(application.getString(R.string.label_update_downloaded_restart_to_complete))
                _showRestartSnackbar.postValue(true)
            }
            InstallStatus.FAILED -> _updateStatus.postValue(application.getString(R.string.label_update_failed_try_again_later))
            InstallStatus.CANCELED -> _updateStatus.postValue(application.getString(R.string.label_update_canceled_you_can_update_later))
            InstallStatus.DOWNLOADING -> {
               //TODO
            }
            InstallStatus.INSTALLED -> _updateStatus.postValue(application.getString(R.string.label_update_installed_successfully))
            InstallStatus.INSTALLING -> _updateStatus.postValue(application.getString(R.string.label_installing_update))
            InstallStatus.PENDING -> _updateStatus.postValue(application.getString(R.string.label_update_pending_it_will_start_soon))
            InstallStatus.REQUIRES_UI_INTENT -> _updateStatus.postValue(application.getString(R.string.label_update_requires_additional_user_action))
            InstallStatus.UNKNOWN -> _updateStatus.postValue(application.getString(R.string.label_update_status_unknown_please_check_again_later))
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
