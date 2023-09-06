package com.example.videoplayer_yas.allfragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayer_yas.ui.MainActivity
import com.example.videoplayer_yas.R
import com.example.videoplayer_yas.adapterClasses.VideoAdapter
import com.example.videoplayer_yas.databinding.FragmentVideosBinding


class VideosFragment : Fragment() {

    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_videos, container, false)
        val binding = FragmentVideosBinding.bind(view)
        inflater.inflate(R.layout.fragment_videos, container, false)
        binding.videoRV.setHasFixedSize(true)   //jiska Matlab hai fix Rahay ga recycle view jise memory bachai gi
        binding.videoRV.setItemViewCacheSize(10)
        binding.videoRV.layoutManager = LinearLayoutManager(requireContext())
        binding.videoRV.adapter = VideoAdapter(requireContext(), MainActivity.videoList)
        binding.totalVideos.text = "Total Videos ${MainActivity.videoList.size}"
        return view
    }


}