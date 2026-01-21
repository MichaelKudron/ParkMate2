package com.parkmate.ui.sessiondetails

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.parkmate.ParkMateApplication
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.databinding.FragmentSessionDetailsBinding
import java.text.SimpleDateFormat
import java.util.*

class SessionDetailsFragment : Fragment() {
    private var _binding: FragmentSessionDetailsBinding? = null
    private val binding get() = _binding!!

    private val args: SessionDetailsFragmentArgs by navArgs()

    private val viewModel: SessionDetailsViewModel by viewModels {
        val repository = (requireActivity().application as ParkMateApplication).repository
        SessionDetailsViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSessionDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.loadSession(args.sessionId)

        viewModel.session.observe(viewLifecycleOwner) { session ->
            session?.let { updateUI(it) }
        }

        binding.btnShare.setOnClickListener {
            viewModel.session.value?.let { session ->
                shareSession(session)
            }
        }
    }

    private fun updateUI(session: ParkingSession) {
        binding.tvAddress.text = session.address ?: "Unknown location"
        binding.tvNote.text = session.note ?: "No notes"
        
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        binding.tvStartTime.text = "Started: ${dateFormat.format(Date(session.startTime))}"
        
        session.endTime?.let {
            binding.tvEndTime.text = "Ended: ${dateFormat.format(Date(it))}"
            binding.tvEndTime.visibility = View.VISIBLE
        } ?: run {
            binding.tvEndTime.visibility = View.GONE
        }

        binding.tvCoordinates.text = "Coordinates: ${session.lat}, ${session.lng}"
        
        session.accuracyMeters?.let {
            binding.tvAccuracy.text = "Accuracy: ${it.toInt()}m"
            binding.tvAccuracy.visibility = View.VISIBLE
        } ?: run {
            binding.tvAccuracy.visibility = View.GONE
        }
    }

    private fun shareSession(session: ParkingSession) {
        val shareText = buildString {
            append("Parking Session Details\n\n")
            append("Address: ${session.address ?: "Unknown"}\n")
            append("Coordinates: ${session.lat}, ${session.lng}\n")
            session.note?.let { append("Note: $it\n") }
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            append("Started: ${dateFormat.format(Date(session.startTime))}\n")
            session.endTime?.let { 
                append("Ended: ${dateFormat.format(Date(it))}\n") 
            }
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
