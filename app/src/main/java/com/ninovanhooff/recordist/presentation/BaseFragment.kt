package com.ninovanhooff.recordist.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

abstract class BaseFragment : Fragment(){

    abstract val vm: BaseViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.navigationCommands.observe(viewLifecycleOwner, Observer{ command ->
            when (command) {
                is NavigationCommand.To ->
                    findNavController().navigate(command.directions)
            }
        })
    }
}