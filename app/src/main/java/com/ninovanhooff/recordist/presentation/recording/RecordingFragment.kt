package com.ninovanhooff.recordist.presentation.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.dimowner.phonograph.util.TimeUtils
import com.ninovanhooff.recordist.databinding.RecordingFragmentBinding
import timber.log.Timber

class RecordingFragment : Fragment() {
    private var _binding: RecordingFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = RecordingFragmentBinding.inflate(inflater, container, false)

        val model: RecordingViewModel by viewModels { RecordingViewModelFactory() }
        model.recordingState.observe(viewLifecycleOwner, Observer{ recordingState ->
            binding.statusText.text = recordingState.name

            when(recordingState){
                RecordingState.STARTING -> binding.waveform.clearRecordingData()
                RecordingState.RECORDING -> binding.waveform.showRecording()
                RecordingState.IDLE -> binding.waveform.hideRecording()
                else -> {}
            }
        })

        model.amplitudeUpdates.observe(viewLifecycleOwner, Observer {
            binding.progressText.text = TimeUtils.formatTimeIntervalHourMinSec2(it.first)
            binding.waveform.addRecordAmp(it.second)
        })

        binding.recordButton.setOnClickListener { model.toggleRecording() }
        binding.monitorButton.setOnClickListener { model.toggleMonitoring() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}