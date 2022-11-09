package com.example.mycoffee.ui.fragments

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.mycoffee.R
import com.example.mycoffee.databinding.FragmentMemoryBinding
import com.example.mycoffee.models.File
import com.example.mycoffee.models.FilePath
import com.example.mycoffee.viewModels.MemoryViewModel
import com.example.mycoffee.viewModels.MemoryViewModelProvider
import com.example.mycoffee.ui.adapters.MemoryRecyclerViewAdapter
import com.example.mycoffee.views.adapters.PathRecyclerViewAdapter
import kotlinx.coroutines.launch

class MemoryFragment : Fragment(R.layout.fragment_memory), MemoryRecyclerViewAdapter.Interaction,
    PathRecyclerViewAdapter.Interaction {
    private lateinit var memoryViewModel: MemoryViewModel
    private lateinit var binding: FragmentMemoryBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMemoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if(!checkStoragePermission())
            requestStoragePermission()
        val memoryItemsRecyclerViewAdapter = MemoryRecyclerViewAdapter(this)
        var pathRecyclerViewAdapter = PathRecyclerViewAdapter(this)
        binding.included.filesListView.apply {
            adapter = memoryItemsRecyclerViewAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        memoryViewModel.filesListMutableLiveData.observe(viewLifecycleOwner) {
            memoryItemsRecyclerViewAdapter.differ.submitList(it)
            binding.included.spinner.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val memoryViewModelProvider = MemoryViewModelProvider(requireActivity().application)
        memoryViewModel =
            ViewModelProvider(this, memoryViewModelProvider)[MemoryViewModel::class.java]
    }

    override fun onItemClicked(position: Int, file: File) {
        lifecycleScope.launch {
            memoryViewModel.newFolders(file.file, true)
        }
    }

    override fun onItemClicked(position: Int, filePath: FilePath) {
        TODO("Not yet implemented")
    }

    private fun checkStoragePermission(): Boolean {
        return if(SDK_INT >= 30)
            Environment.isExternalStorageManager()
        else{
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(requireContext(), WRITE_EXTERNAL_STORAGE)
        }
    }

    private fun requestStoragePermission() {
        if(SDK_INT >= 30)
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    String.format(
                        "package:%s",
                        getApplicationContext<Context>().packageName
                    )
                )
                startActivityForResult(intent, 2296)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 2296)
            }
        else{
            ActivityCompat.requestPermissions(requireActivity(),
                arrayOf(WRITE_EXTERNAL_STORAGE), 2296)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2296) {
            if (SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // perform action when allow permission success
                } else {
                    makeText(context, "Allow permission for storage access!", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }
}