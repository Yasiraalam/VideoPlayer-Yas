package com.example.videoplayer_yas.allfragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayer_yas.ui.MainActivity
import com.example.videoplayer_yas.R
import com.example.videoplayer_yas.adapterClasses.FolderAdapter
import com.example.videoplayer_yas.databinding.FragmentFolderBinding

class FolderFragment:Fragment() {
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_folder, container, false)
        val binding = FragmentFolderBinding.bind(view)
        inflater.inflate(R.layout.fragment_folder, container, false)
        binding.foldersRV.setHasFixedSize(true)   //jiska Matlab hai fix Rahay ga recycle view ki ise memory bachai gi
        binding.foldersRV.setItemViewCacheSize(10)
        binding.foldersRV.layoutManager = LinearLayoutManager(requireContext())
        binding.foldersRV.adapter = FolderAdapter(requireContext(), MainActivity.folderList)
        binding.totalFolders.text = "Total Folder ${MainActivity.folderList.size}"
        return view
    }
}