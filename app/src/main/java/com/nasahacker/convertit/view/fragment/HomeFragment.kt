package com.nasahacker.convertit.view.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nasahacker.convertit.ConvertItApplication
import com.nasahacker.convertit.R
import com.nasahacker.convertit.adapter.HomeAdapter
import com.nasahacker.convertit.databinding.FragmentHomeBinding
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.service.AudioConversionService
import com.nasahacker.convertit.util.Constants.AUDIO_FORMAT
import com.nasahacker.convertit.util.Constants.BITRATE
import com.nasahacker.convertit.util.Constants.BITRATE_ARRAY
import com.nasahacker.convertit.util.Constants.FORMAT_ARRAY
import com.nasahacker.convertit.util.Constants.URI_LIST
import com.nasahacker.convertit.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapter: HomeAdapter
    private val homeViewModel: HomeViewModel by viewModels()

    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.let { intent -> handleUriList(getUriListFromIntent(intent)) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupObservers()
        setupFabClickListener()
        homeViewModel.startListenBroadcast(requireContext())
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = HomeAdapter(requireContext(), emptyList())
        binding.rvSelectedAudioTracks.adapter = adapter
    }

    private fun setupObservers() {
        homeViewModel.selectedFiles.observe(viewLifecycleOwner) { files ->
            adapter.updateData(files)
        }

        homeViewModel.selectedUris.observe(viewLifecycleOwner) { uris ->
            if (uris.isNotEmpty()) {
                showConversionDialog(ArrayList(uris), adapter)
            }
        }

        homeViewModel.isSuccess.observe(viewLifecycleOwner) { success ->
            Toast.makeText(
                requireContext(),
                if (success == true) "Successfully converted" else "Failed to convert",
                Toast.LENGTH_SHORT
            ).show()
            adapter.clearAll()
            homeViewModel.clearSelectedFiles()
        }
    }

    private fun setupFabClickListener() {
        binding.fabPick.setOnClickListener {
            homeViewModel.openPicker(requireActivity(), pickFileLauncher)
        }
    }

    private fun showConversionDialog(uriList: ArrayList<Uri>, adapter: HomeAdapter) {
        val dialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_audio)
            window?.setBackgroundDrawableResource(R.drawable.blank_bg)
        }

        val spinnerBitrate = dialog.findViewById<Spinner>(R.id.spinnerBitrate)
        val spinnerFormat = dialog.findViewById<Spinner>(R.id.spinnerFormat)
        val btnConvert = dialog.findViewById<MaterialButton>(R.id.btnConvert)

        setupSpinner(spinnerBitrate, BITRATE_ARRAY)
        setupSpinner(spinnerFormat, FORMAT_ARRAY)

        btnConvert.setOnClickListener {
            val selectedBitrate = getSelectedBitrate(spinnerBitrate.selectedItem.toString())
            val selectedFormat = getSelectedFormat(spinnerFormat.selectedItem.toString())

            Intent(ConvertItApplication.instance, AudioConversionService::class.java).apply {
                putParcelableArrayListExtra(URI_LIST, uriList)
                putExtra(BITRATE, selectedBitrate.bitrate)
                putExtra(AUDIO_FORMAT, selectedFormat.extension)
                ConvertItApplication.instance.startService(this)
            }
            Log.d("HACKER", "showConversionDialog: ${selectedFormat.name}")
            Log.d("HACKER", "showConversionDialog: ${selectedFormat.extension}")
            dialog.dismiss()

            Toast.makeText(
                requireContext(),
                "Conversion started with $selectedBitrate bitrate and $selectedFormat format. Please wait.",
                Toast.LENGTH_SHORT
            ).show()
        }

        dialog.show()
    }

    private fun setupSpinner(spinner: Spinner, dataArray: Array<String>) {
        val adapter = object : ArrayAdapter<String>(requireContext(), R.layout.spinner_item_simple, dataArray) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                return layoutInflater.inflate(R.layout.spinner_dropdown_item, parent, false).apply {
                    findViewById<TextView>(R.id.spinnerItemText).text = getItem(position)
                }
            }
        }
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun getSelectedBitrate(selectedItem: String): AudioBitrate = when (selectedItem) {
        BITRATE_ARRAY[0] -> AudioBitrate.BITRATE_192K
        BITRATE_ARRAY[1] -> AudioBitrate.BITRATE_192K
        BITRATE_ARRAY[2] -> AudioBitrate.BITRATE_256K
        BITRATE_ARRAY[3] -> AudioBitrate.BITRATE_320K
        else -> AudioBitrate.BITRATE_192K
    }

    private fun getSelectedFormat(selectedItem: String): AudioFormat = when (selectedItem) {
        FORMAT_ARRAY[0] -> AudioFormat.FLAC
        FORMAT_ARRAY[1] -> AudioFormat.MP3
        FORMAT_ARRAY[2] -> AudioFormat.WAV
        FORMAT_ARRAY[3] -> AudioFormat.AAC
        FORMAT_ARRAY[4] -> AudioFormat.OGG
        FORMAT_ARRAY[5] -> AudioFormat.M4A
        else -> AudioFormat.MP3
    }

    private fun getUriListFromIntent(intent: Intent): List<Uri> {
        val uriList = mutableListOf<Uri>()
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                uriList.add(clipData.getItemAt(i).uri)
            }
        } ?: intent.data?.let { uriList.add(it) }
        return uriList
    }

    private fun handleUriList(uriList: List<Uri>) {
        homeViewModel.setSelectedUris(requireActivity(), uriList)
    }

    override fun onDestroy() {
        super.onDestroy()
        homeViewModel.unregister(requireContext())
    }
}
