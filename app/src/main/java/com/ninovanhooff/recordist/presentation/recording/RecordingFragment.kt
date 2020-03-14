package com.ninovanhooff.recordist.presentation.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.ninovanhooff.phonograph.util.TimeUtils
import com.ninovanhooff.recordist.databinding.RecordingFragmentBinding
import com.ninovanhooff.recordist.presentation.BaseFragment
import com.ninovanhooff.recordist.presentation.BaseViewModel
import com.ninovanhooff.recordist.presentation.permissions.PermissionsFragment

class RecordingFragment : BaseFragment() {
    private var _binding: RecordingFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override val vm: RecordingViewModel by viewModels { RecordingViewModelFactory() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = RecordingFragmentBinding.inflate(inflater, container, false)

        vm.recordingState.observe(viewLifecycleOwner, Observer{ recordingState ->
            binding.statusText.text = recordingState.name

            when(recordingState){
                RecordingState.STARTING -> binding.waveform.clearRecordingData()
                RecordingState.RECORDING -> binding.waveform.showRecording()
                RecordingState.IDLE -> binding.waveform.hideRecording()
                else -> {}
            }
        })

        vm.amplitudeUpdates.observe(viewLifecycleOwner, Observer {
            binding.progressText.text = TimeUtils.formatTimeIntervalHourMinSec2(it.first)
            binding.waveform.addRecordAmp(it.second)
        })

        binding.recordButton.setOnClickListener { vm.toggleRecording() }
        binding.monitorButton.setOnClickListener { vm.toggleMonitoring() }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if(PermissionsFragment.checkPermissions(context!!).isNotEmpty()){
            vm.onRequiredPermissionsMissing()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}