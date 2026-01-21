package com.parkmate.ui.startsession

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.parkmate.ParkMateApplication
import com.parkmate.R
import com.parkmate.databinding.FragmentStartSessionBinding
import com.parkmate.utils.LocationHelper
import kotlinx.coroutines.launch

class StartSessionFragment : Fragment() {
    private var _binding: FragmentStartSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StartSessionViewModel by viewModels {
        val repository = (requireActivity().application as ParkMateApplication).repository
        StartSessionViewModelFactory(repository, this)
    }

    private lateinit var locationHelper: LocationHelper

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            startParkingSession()
        } else {
            Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStartSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        locationHelper = LocationHelper(requireContext())

        binding.etNote.setText(viewModel.note)

        binding.btnStartSession.setOnClickListener {
            viewModel.note = binding.etNote.text.toString()
            checkLocationPermissionAndStart()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is StartSessionUiState.Idle -> {
                            binding.btnStartSession.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                        }
                        is StartSessionUiState.Loading -> {
                            binding.btnStartSession.isEnabled = false
                            binding.progressBar.visibility = View.VISIBLE
                        }
                        is StartSessionUiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            Toast.makeText(requireContext(), "Session started!", Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                            findNavController().navigate(R.id.action_startSession_to_activeSession)
                        }
                        is StartSessionUiState.Error -> {
                            binding.btnStartSession.isEnabled = true
                            binding.progressBar.visibility = View.GONE
                            Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                            viewModel.resetState()
                        }
                    }
                }
            }
        }
    }

    private fun checkLocationPermissionAndStart() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startParkingSession()
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    private fun startParkingSession() {
        viewLifecycleOwner.lifecycleScope.launch {
            val location = locationHelper.getCurrentLocation()
            if (location != null) {
                viewModel.startSession(
                    lat = location.latitude,
                    lng = location.longitude,
                    accuracy = location.accuracy,
                    note = binding.etNote.text.toString()
                )
            } else {
                Toast.makeText(requireContext(), "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
