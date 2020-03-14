package com.ninovanhooff.recordist.presentation.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
            requestPermissions(checkPermissions(context!!), true)
        }

        vm.rationaleVisibilities.observe(viewLifecycleOwner, Observer { setRationaleVisibilities(it) })
        vm.permanentlyDenied.observe(viewLifecycleOwner, Observer { setDeniedStatus(it) })
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

    private fun setDeniedStatus(denied: Collection<String>){
        if(denied.contains("android.permission.RECORD_AUDIO")){
            binding.microphoneRationaleText.setTextColor(Color.RED)
        } else {
            binding.microphoneRationaleText.setTextColor(Color.GREEN)
        }

        if(denied.contains("android.permission.WRITE_EXTERNAL_STORAGE")){
            binding.storageRationaleText.setTextColor(Color.RED)
        } else {
            binding.storageRationaleText.setTextColor(Color.GREEN)
        }
    }

    /**
     * @param userRequested whether the user requested the permissions actively
     */
    private fun requestPermissions(permissions: Collection<String>, userRequested: Boolean = false){
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

        if(needsRationale.isNotEmpty() && !userRequested){
            return // when a rationale is needed, the user must be involved
        }

        val denied = vm.permanentlyDenied.value ?: listOf()
        if (denied.isNotEmpty()){
            if (userRequested){
                openPermissionSettings()
            }
            return
        }

        requestPermissions(permissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                 val permanentlyDenied = grantResults.zip(permissions).mapNotNull {
                     if (it.first != PERMISSION_GRANTED && !shouldShowRequestPermissionRationale(it.second)){
                         it.second
                     } else null
                 }

                if(permanentlyDenied.isNotEmpty()){
                    vm.onPermanentlyDenied(permanentlyDenied)
                }
            }
        }
    }

    private fun openPermissionSettings(){
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", activity!!.packageName, null)
        intent.data = uri
        startActivityForResult(intent, SETTINGS_REQUEST_CODE)
    }

    companion object {
        private val REQUIRED_PERMISSIONS = listOf(
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        const val PERMISSIONS_REQUEST_CODE = 1
        const val SETTINGS_REQUEST_CODE = 2
        //const val PERMISSION_GRANTED = PackageManager.PERMISSION_GRANTED

        /** Returns a list of required permissions which were not granted yet */
        internal fun checkPermissions(context: Context): Collection<String> {
            return REQUIRED_PERMISSIONS.mapNotNull {
                if (ContextCompat.checkSelfPermission(context, it) != PERMISSION_GRANTED) it else null
            }
    
        }
    }

}
