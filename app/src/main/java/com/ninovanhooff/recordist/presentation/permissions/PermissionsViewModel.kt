package com.ninovanhooff.recordist.presentation.permissions

import androidx.lifecycle.MutableLiveData
import com.ninovanhooff.recordist.presentation.BaseViewModel
import com.ninovanhooff.recordist.presentation.NavigationCommand

/**
 * Handles visible permission rationales and navigates Back when all permissions are granted
 */
class PermissionsViewModel : BaseViewModel(){

    val rationaleVisibilities: MutableLiveData<Collection<String>> = MutableLiveData()
    val permanentlyDenied: MutableLiveData<Collection<String>> = MutableLiveData()


    fun onRequiredPermissionsGranted() {
        navigate(NavigationCommand.Back)
    }

    fun permissionsNeedRationale(needsRationale: Collection<String>) {
        rationaleVisibilities.value = needsRationale
    }

    fun onPermanentlyDenied(permissions: Collection<String>) {
        permanentlyDenied.value = permissions
    }

}