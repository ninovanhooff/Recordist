package com.ninovanhooff.recordist.presentation

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

open class BaseFragment : Fragment(){

    fun connectNavigation(vm: BaseViewModel) {
        vm.navigationCommands.observe(viewLifecycleOwner, Observer{ command ->
        when (command) {
                is NavigationCommand.To ->
                    findNavController().navigate(command.directions)
            }
        })
    }
}