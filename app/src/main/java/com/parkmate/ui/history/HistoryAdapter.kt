package com.parkmate.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.databinding.ItemSessionBinding
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onItemClick: (ParkingSession) -> Unit,
    private val onItemLongClick: (ParkingSession) -> Boolean
) : ListAdapter<ParkingSession, HistoryAdapter.SessionViewHolder>(SessionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SessionViewHolder {
        val binding = ItemSessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SessionViewHolder(
        private val binding: ItemSessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
            binding.root.setOnLongClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemLongClick(getItem(position))
                } else {
                    false
                }
            }
        }

        fun bind(session: ParkingSession) {
            binding.tvAddress.text = session.address ?: "Unknown location"
            binding.tvNote.text = session.note ?: ""
            binding.tvNote.visibility = if (session.note.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            binding.tvStartTime.text = "Started: ${dateFormat.format(Date(session.startTime))}"
            
            session.endTime?.let {
                binding.tvEndTime.text = "Ended: ${dateFormat.format(Date(it))}"
                binding.tvEndTime.visibility = View.VISIBLE
            } ?: run {
                binding.tvEndTime.visibility = View.GONE
            }
        }
    }

    private class SessionDiffCallback : DiffUtil.ItemCallback<ParkingSession>() {
        override fun areItemsTheSame(oldItem: ParkingSession, newItem: ParkingSession): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ParkingSession, newItem: ParkingSession): Boolean {
            return oldItem == newItem
        }
    }
}
