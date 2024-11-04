package com.nasahacker.convertit.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nasahacker.convertit.R
import com.nasahacker.convertit.adapter.ConvertsAdapter
import com.nasahacker.convertit.databinding.FragmentConvertsBinding
import com.nasahacker.convertit.listener.OnLongPressListener
import com.nasahacker.convertit.util.AppUtils
import com.nasahacker.convertit.viewmodel.ConvertsViewModel
import java.io.File

class ConvertsFragment : Fragment(), OnLongPressListener {

    private lateinit var binding: FragmentConvertsBinding
    private val convertsViewModel: ConvertsViewModel by viewModels()
    private lateinit var adapter: ConvertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentConvertsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupSearchView()
        observeViewModel()
        convertsViewModel.loadAllFiles(requireContext())
        return binding.root
    }

    private fun setupRecyclerView() {
        // Initialize the adapter for both views
        adapter = ConvertsAdapter(requireContext(), emptyList(), this)
        binding.rvConvertedTracks.adapter = adapter
        binding.rvFilteredFiles.adapter = adapter
    }

    private fun setupSearchView() {
        val searchView = binding.searchView
        searchView.editText.setOnEditorActionListener { _, _, _ ->
            val query = searchView.text.toString()
            performSearch(query)
            searchView.hide()
            true
        }

        searchView.editText.addTextChangedListener { text ->
            performSuggestionSearch(text.toString())
        }
    }

    private fun performSearch(query: String) {
        adapter.filter(query)
        binding.rvConvertedTracks.visibility = View.VISIBLE
        binding.rvFilteredFiles.visibility = View.GONE
    }

    private fun performSuggestionSearch(query: String) {
        if (query.isNotEmpty()) {
            adapter.filter(query) // Filter suggestions using the adapter's filter method
            binding.rvConvertedTracks.visibility = View.GONE
            binding.rvFilteredFiles.visibility = View.VISIBLE
        } else {
            binding.rvConvertedTracks.visibility = View.VISIBLE
            binding.rvFilteredFiles.visibility = View.GONE
        }
    }

    private fun observeViewModel() {
        convertsViewModel.message.observe(viewLifecycleOwner) { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        convertsViewModel.audioFiles.observe(viewLifecycleOwner) { files ->
            adapter.updateData(files) // Load the full list of files initially
        }
    }

    override fun onLongPressed(item: File) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.label_title_action_dialog))
            .setMessage(getString(R.string.label_choose_action))
            .setNeutralButton(getString(R.string.label_view_metadata)) { _, _ ->
                AppUtils.showAudioInfoDialog(requireContext(), lifecycleScope, item)
            }
            .setPositiveButton(getString(R.string.label_delete)) { _, _ ->
                convertsViewModel.deleteFile(requireContext(), item)
            }
            .setNegativeButton(getString(R.string.label_cancel)) { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
