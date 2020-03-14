package com.ninovanhooff.recordist.presentation.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ninovanhooff.recordist.databinding.PermissionsFragmentBinding
import com.ninovanhooff.recordist.presentation.BaseFragment

/**
 * Shows required permissions and handles the request (results).
 */
class PermissionsFragment : BaseFragment() {

    private var _binding: PermissionsFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override val vm: PermissionsViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = PermissionsFragmentBinding.inflate(inflater, container, false)
        binding.grantPermissionsButton.setOnClickListener{ 
            requestPermissions(checkPermissions(context!!))
        }

        vm.rationaleVisibilities.observe(viewLifecycleOwner, Observer { setRationaleVisibilities(it) })
        return binding.root
    }

    override fun onResume() {
        super.onResume()

        val missingPermissions = checkPermissions(context!!)
        if(missingPermissions.isEmpty()){
            vm.onRequiredPermissionsGranted()
        } else {
            requestPermissions(missingPermissions)
        }
    }

    private fun setRationaleVisibilities(visibleRationales: Collection<String>){
        if(visibleRationales.contains("android.permission.RECORD_AUDIO")){
            binding.microphoneRationaleText.alpha = 1f
        } else {
            binding.microphoneRationaleText.alpha = .5f
        }

        if(visibleRationales.contains("android.permission.WRITE_EXTERNAL_STORAGE")){
            binding.storageRationaleText.alpha = 1f
        } else {
            binding.storageRationaleText.alpha = .5f
        }
    }

    private fun requestPermissions(permissions: Collection<String>){
        // Permission is not granted
        // Should we show an explanation?
        val needsRationale = permissions.mapNotNull {
            if (shouldShowRequestPermissionRationale(it)) it else null
        }

        if(needsRationale.isNotEmpty() && vm.rationaleVisibilities.value != needsRationale){
            // The correct rationale is not shown
            vm.permissionsNeedRationale(needsRationale)
            return
        }

        requestPermissions(permissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        const val PERMISSIONS_REQUEST_CODE = 1
        /** Returns a list of required permissions which were not granted yet */
        internal fun checkPermissions(context: Context): Collection<String> {
            val permissionGranted = PackageManager.PERMISSION_GRANTED
            return REQUIRED_PERMISSIONS.mapNotNull {
                if (ContextCompat.checkSelfPermission(context, it) != permissionGranted) it else null
            }
    
        }
    }

}
