package com.nasahacker.convertit.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nasahacker.convertit.R
import com.nasahacker.convertit.adapter.ConvertsAdapter
import com.nasahacker.convertit.databinding.FragmentConvertsBinding
import com.nasahacker.convertit.listener.OnLongPressListener
import com.nasahacker.convertit.viewmodel.ConvertsViewModel
import java.io.File


class ConvertsFragment : Fragment(), OnLongPressListener {
    private lateinit var binding: FragmentConvertsBinding
    private val convertsViewModel: ConvertsViewModel by viewModels()
    private lateinit var adapter: ConvertsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentConvertsBinding.inflate(inflater, container, false)

        adapter = ConvertsAdapter(requireContext(), emptyList(), this)
        binding.rvConvertedTracks.adapter = adapter

        convertsViewModel.message.observe(viewLifecycleOwner)
        { msg ->
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        convertsViewModel.loadAllFiles(requireContext())

        convertsViewModel.audioFiles.observe(viewLifecycleOwner)
        { files ->
            adapter.updateData(files)
        }

        return binding.root
    }

    override fun onLongPressed(item: File) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete?")
            .setMessage("Are you sure you want to delete this?")
            .setPositiveButton(
                "Yes"
            ) { _, _ -> convertsViewModel.deleteFile(requireContext(), item) }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

}