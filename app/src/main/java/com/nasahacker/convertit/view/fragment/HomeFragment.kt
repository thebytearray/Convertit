package com.nasahacker.convertit.view.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.google.android.material.button.MaterialButton
import com.nasahacker.convertit.ConvertItApplication
import com.nasahacker.convertit.R
import com.nasahacker.convertit.adapter.HomeAdapter
import com.nasahacker.convertit.databinding.FragmentHomeBinding
import com.nasahacker.convertit.model.AudioBitrate
import com.nasahacker.convertit.model.AudioFormat
import com.nasahacker.convertit.service.ConvertionService
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

        homeViewModel.startListenBroadcast(requireContext())
        adapter = HomeAdapter(requireContext(), emptyList())
        binding.rvSelectedAudioTracks.adapter = adapter
        homeViewModel.selectedFiles.observe(viewLifecycleOwner) { files ->
            adapter.updateData(files)
        }
        homeViewModel.selectedUris.observe(viewLifecycleOwner) { uris ->
            if (uris.isNotEmpty()) {
                showConversionDialog(uris as ArrayList<Uri>, adapter)
            }
        }
        binding.fabPick.setOnClickListener {
            homeViewModel.openPicker(requireActivity(), pickFileLauncher)
        }
        return binding.root
    }

    private fun showConversionDialog(uriList: ArrayList<Uri>, adapter: HomeAdapter) {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_audio)
        dialog.window?.setBackgroundDrawableResource(R.drawable.blank_bg)

        // Find the spinners and button
        val spinnerBitrate = dialog.findViewById<Spinner>(R.id.spinnerBitrate)
        val spinnerFormat = dialog.findViewById<Spinner>(R.id.spinnerFormat)
        val btnConvert = dialog.findViewById<MaterialButton>(R.id.btnConvert)

        // Set up the spinners
        val bitrateArray = BITRATE_ARRAY
        setupSpinner(spinnerBitrate, bitrateArray)
        val formatArray = FORMAT_ARRAY
        setupSpinner(spinnerFormat, formatArray)


        homeViewModel.isSuccess.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Successfully converted", Toast.LENGTH_SHORT)
                    .show()
                adapter.clearAll()
                homeViewModel.clearSelectedFiles()
            } else {
                Toast.makeText(requireContext(), "Failed to convert", Toast.LENGTH_SHORT).show()
                homeViewModel.clearSelectedFiles()
            }
        }


        // Handle the "Convert" button click
        btnConvert.setOnClickListener {
            // Get the selected bitrate and format
            val selectedBitrate = when (spinnerBitrate.selectedItem.toString()) {
                BITRATE_ARRAY[0] -> AudioBitrate.BITRATE_192K
                BITRATE_ARRAY[1] -> AudioBitrate.BITRATE_192K
                BITRATE_ARRAY[2] -> AudioBitrate.BITRATE_256K
                BITRATE_ARRAY[3] -> AudioBitrate.BITRATE_320K
                else -> AudioBitrate.BITRATE_192K
            }
            val selectedFormat =
                when (spinnerFormat.selectedItem.toString()) {
                    FORMAT_ARRAY[0] -> AudioFormat.FLAC
                    FORMAT_ARRAY[1] -> AudioFormat.MP3
                    FORMAT_ARRAY[2] -> AudioFormat.WAV
                    FORMAT_ARRAY[3] -> AudioFormat.AAC
                    FORMAT_ARRAY[4] -> AudioFormat.OGG
                    FORMAT_ARRAY[5] -> AudioFormat.M4A
                    else -> AudioFormat.MP3
                }

            val startIntent =
                Intent(ConvertItApplication.application, ConvertionService::class.java)
            startIntent.putParcelableArrayListExtra(URI_LIST, uriList)
            startIntent.putExtra(BITRATE, selectedBitrate.name)
            startIntent.putExtra(AUDIO_FORMAT, selectedFormat.name)
            ConvertItApplication.application.startService(startIntent)
            dialog.dismiss()

            // Inform the user that the conversion has started
            Toast.makeText(
                requireContext(),
                "Conversion started with $selectedBitrate bitrate and $selectedFormat format. Please wait.",
                Toast.LENGTH_SHORT
            ).show()
        }

        dialog.show()
    }


    private fun setupSpinner(spinner: Spinner, dataArray: Array<String>) {
        val adapter = object :
            ArrayAdapter<String>(requireContext(), R.layout.spinner_item_simple, dataArray) {
            override fun getDropDownView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                // Inflate custom dropdown layout for spinner items
                val view = layoutInflater.inflate(R.layout.spinner_dropdown_item, parent, false)
                val textView = view.findViewById<TextView>(R.id.spinnerItemText)
                textView.text = getItem(position) // Set the text for each dropdown item
                return view
            }
        }

        // Set the dropdown resource to use the custom dropdown layout
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)

        spinner.adapter = adapter
    }


    private fun getUriListFromIntent(intent: Intent): List<Uri> {
        val uriList = mutableListOf<Uri>()

        val clipData = intent.clipData
        if (clipData != null) {
            // Multiple files selected
            for (i in 0 until clipData.itemCount) {
                val uri: Uri = clipData.getItemAt(i).uri
                uriList.add(uri)
            }
        } else {
            // Single file selected
            intent.data?.let { uri ->
                uriList.add(uri)
            }
        }

        return uriList
    }


    private fun handleUriList(uriList: List<Uri>) {
        homeViewModel.setSelectedUris(requireActivity(), uriList)
    }


    private val pickFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                if (intent != null) {
                    // Get the list of URIs
                    val uriList = getUriListFromIntent(intent)
                    // Do something with the list of URIs
                    handleUriList(uriList)
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()
        homeViewModel.unregister(requireContext())
    }

}