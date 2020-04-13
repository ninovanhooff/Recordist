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
import com.ninovanhooff.recordist.presentation.permissions.PermissionsFragment

class RecordingFragment : BaseFragment() {
    private var _binding: RecordingFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    private val refreshInterval = 30L
    private var lastFrameTime: Long = 0

    override val vm: RecordingViewModel by viewModels { RecordingViewModelFactory() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = RecordingFragmentBinding.inflate(inflater, container, false)

        lifecycle.addObserver(vm)

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
            val invalidate = lastFrameTime + refreshInterval < System.currentTimeMillis()
            binding.levels.setAmplitude(it.amplitude, invalidate)
            if (invalidate) {
                // prevent jitter in the levels view
                lastFrameTime = System.currentTimeMillis()
            }


            if (it.isRecording){
                binding.waveform.addRecordAmp(it.amplitude)
            }

        })

        vm.progressTextUpdates.observe(viewLifecycleOwner, Observer {
            binding.progressText.text = it
        })

        binding.waveform.setRecordingData(vm.getRecordingData())
        binding.recordButton.setOnClickListener { vm.toggleRecording() }
        binding.monitorButton.setOnClickListener { vm.toggleMonitoring() }
        binding.preferenceButton.setOnClickListener { vm.openPreferences() }

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