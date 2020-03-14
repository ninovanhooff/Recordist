package com.ninovanhooff.recordist.presentation

import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections

open class BaseViewModel : ViewModel() {

    val navigationCommands : SingleLiveEvent<NavigationCommand> = SingleLiveEvent()

    fun navigate(directions: NavDirections) {
        navigationCommands.postValue(NavigationCommand.To(directions))
    }

    fun navigate(command: NavigationCommand) {
        navigationCommands.postValue(command)
    }
}