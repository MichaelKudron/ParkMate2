package com.parkmate.ui.history

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.parkmate.ParkMateApplication
import com.parkmate.R
import com.parkmate.data.local.entities.ParkingSession
import com.parkmate.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels {
        val repository = (requireActivity().application as ParkMateApplication).repository
        HistoryViewModelFactory(repository)
    }

    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = HistoryAdapter(
            onItemClick = { session ->
                val action = HistoryFragmentDirections.actionHistoryToSessionDetails(session.id)
                findNavController().navigate(action)
            },
            onItemLongClick = { session ->
                showDeleteDialog(session)
                true
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }

        viewModel.sessions.observe(viewLifecycleOwner) { sessions ->
            adapter.submitList(sessions)
            binding.tvEmptyState.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_history, menu)
        
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.search(newText ?: "")
                return true
            }
        })
    }

    private fun showDeleteDialog(session: ParkingSession) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Session")
            .setMessage("Are you sure you want to delete this parking session?")
            .setPositiveButton("Delete") { _, _ ->
                deleteSession(session)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteSession(session: ParkingSession) {
        viewModel.deleteSession(session) {
            Snackbar.make(binding.root, "Session deleted", Snackbar.LENGTH_LONG)
                .setAction("Undo") {
                    viewModel.restoreSession(session)
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
