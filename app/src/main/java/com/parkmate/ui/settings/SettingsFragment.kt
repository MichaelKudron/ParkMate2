package com.parkmate.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.parkmate.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        // Load current settings
        val reminderMinutes = sharedPreferences.getInt("reminder_minutes", 10)
        val useKm = sharedPreferences.getBoolean("use_km", true)

        binding.etReminderMinutes.setText(reminderMinutes.toString())
        binding.switchUnits.isChecked = useKm

        // Save settings
        binding.btnSave.setOnClickListener {
            val minutes = binding.etReminderMinutes.text.toString().toIntOrNull() ?: 10
            val useKilometers = binding.switchUnits.isChecked

            sharedPreferences.edit()
                .putInt("reminder_minutes", minutes)
                .putBoolean("use_km", useKilometers)
                .apply()

            // Show confirmation
            binding.tvSaveConfirmation.visibility = View.VISIBLE
            binding.tvSaveConfirmation.postDelayed({
                binding.tvSaveConfirmation.visibility = View.GONE
            }, 2000)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
