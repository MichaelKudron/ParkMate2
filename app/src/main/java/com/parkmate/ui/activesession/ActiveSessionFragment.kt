package com.parkmate.ui.activesession

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.parkmate.ParkMateApplication
import com.parkmate.R
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.databinding.FragmentActiveSessionBinding
import com.parkmate.utils.CompassHelper
import com.parkmate.utils.LocationHelper
import com.parkmate.workers.ReminderWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class ActiveSessionFragment : Fragment() {
    private var _binding: FragmentActiveSessionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ActiveSessionViewModel by viewModels {
        val repository = (requireActivity().application as ParkMateApplication).repository
        ActiveSessionViewModelFactory(repository)
    }

    private lateinit var locationHelper: LocationHelper
    private lateinit var compassHelper: CompassHelper
    private var currentSession: ParkingSession? = null
    private var currentAzimuth: Float = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentActiveSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationHelper = LocationHelper(requireContext())
        compassHelper = CompassHelper(requireContext())

        viewModel.activeSession.observe(viewLifecycleOwner) { session ->
            if (session != null) {
                currentSession = session
                updateUI(session)
                updateDistanceAndDirection()
            } else {
                // No active session, navigate to start session
                findNavController().navigate(R.id.action_activeSession_to_startSession)
            }
        }

        binding.btnNavigate.setOnClickListener {
            currentSession?.let { session ->
                val uri = "google.navigation:q=${session.lat},${session.lng}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                intent.setPackage("com.google.android.apps.maps")
                startActivity(intent)
            }
        }

        binding.btnEndSession.setOnClickListener {
            currentSession?.let { session ->
                viewModel.endSession(session.id)
            }
        }

        binding.btnSetPaidUntil.setOnClickListener {
            showDateTimePicker()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.endSessionState.collect { state ->
                    when (state) {
                        is EndSessionState.Success -> {
                            Toast.makeText(requireContext(), "Session ended", Toast.LENGTH_SHORT).show()
                            viewModel.resetEndSessionState()
                            findNavController().navigate(R.id.action_activeSession_to_history)
                        }
                        is EndSessionState.Error -> {
                            Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetEndSessionState()
                        }
                        else -> {}
                    }
                }
            }
        }

        compassHelper.startListening { azimuth ->
            currentAzimuth = azimuth
            updateDirectionArrow()
        }
    }

    private fun updateUI(session: ParkingSession) {
        binding.tvAddress.text = session.address ?: "Address loading..."
        binding.tvStartTime.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            .format(Date(session.startTime))
        
        session.paidUntil?.let { paidUntil ->
            binding.tvPaidUntil.text = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
                .format(Date(paidUntil))
        }
    }

    private fun updateDistanceAndDirection() {
        lifecycleScope.launch {
            currentSession?.let { session ->
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    val distance = locationHelper.calculateDistance(
                        location.latitude, location.longitude,
                        session.lat, session.lng
                    )
                    binding.tvDistance.text = if (distance < 1000) {
                        "${distance.toInt()} m"
                    } else {
                        String.format("%.2f km", distance / 1000)
                    }
                }
            }
        }
    }

    private fun updateDirectionArrow() {
        currentSession?.let { session ->
            lifecycleScope.launch {
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    val bearing = locationHelper.calculateBearing(
                        location.latitude, location.longitude,
                        session.lat, session.lng
                    )
                    val rotation = bearing - currentAzimuth
                    binding.ivDirectionArrow.rotation = rotation
                }
            }
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                calendar.set(year, month, day)
                
                TimePickerDialog(
                    requireContext(),
                    { _, hour, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        
                        val paidUntil = calendar.timeInMillis
                        currentSession?.let { session ->
                            viewModel.setPaidUntil(session.id, paidUntil)
                            scheduleReminder(paidUntil)
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true
                ).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun scheduleReminder(paidUntil: Long) {
        val reminderTime = paidUntil - TimeUnit.MINUTES.toMillis(10)
        val delay = reminderTime - System.currentTimeMillis()
        
        if (delay > 0) {
            val data = Data.Builder()
                .putString(ReminderWorker.KEY_MESSAGE, "Your parking meter expires in 10 minutes!")
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(data)
                .addTag(ReminderWorker.WORK_TAG)
                .build()

            WorkManager.getInstance(requireContext()).enqueue(workRequest)
            Toast.makeText(requireContext(), "Reminder set for 10 minutes before expiry", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compassHelper.stopListening()
        _binding = null
    }
}
