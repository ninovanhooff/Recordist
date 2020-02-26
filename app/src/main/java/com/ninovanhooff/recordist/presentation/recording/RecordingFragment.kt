package com.ninovanhooff.recordist.presentation.recording

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ninovanhooff.recordist.databinding.RecordingFragmentBinding

class RecordingFragment : Fragment() {
    private var _binding: RecordingFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _binding = RecordingFragmentBinding.inflate(inflater, container, false)

        val model: RecordingViewModel by viewModels { RecordingViewModelFactory() }
        model.getRecording().observe(viewLifecycleOwner, Observer<Boolean>{ playing ->
            binding.textView.text = if (playing) "Recording" else "NOT Recording"
        })

        binding.recordButton.setOnClickListener { model.toggleRecording() }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}