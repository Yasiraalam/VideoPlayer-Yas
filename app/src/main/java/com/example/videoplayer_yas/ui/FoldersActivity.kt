package com.example.videoplayer_yas.ui

import android.annotation.SuppressLint
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.videoplayer_yas.R
import com.example.videoplayer_yas.adapterClasses.VideoAdapter
import com.example.videoplayer_yas.dataClasses.Video
import com.example.videoplayer_yas.databinding.ActivityFoldersBinding
import java.io.File

class FoldersActivity : AppCompatActivity() {
    companion object{
        lateinit var currentfolderVideos :ArrayList<Video>
    }
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFoldersBinding.inflate(layoutInflater)
        setTheme(R.style.coolPinkNav)
        setContentView(binding.root)
        val position = intent.getIntExtra("position",0)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = MainActivity.folderList[position].folderName
        currentfolderVideos = getAllVideos(MainActivity.folderList[position].id)
        binding.videoRVFA.setHasFixedSize(true)   //jiska Matlab hai fix Rahay ga recycle view jise memory bachai gi
        binding.videoRVFA.setItemViewCacheSize(10)
        binding.videoRVFA.layoutManager = LinearLayoutManager(this@FoldersActivity)
        binding.videoRVFA.adapter = VideoAdapter(this@FoldersActivity, currentfolderVideos,isFolder=true)
        binding.totalVideosFA.text = "Total Videos ${currentfolderVideos.size}"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return true
    }
    @SuppressLint("Recycle", "Range", "SuspiciousIndentation")
    private fun getAllVideos(folderID:String):ArrayList<Video>{
        val tempList = ArrayList<Video>()
        val Selection = MediaStore.Video.Media.BUCKET_ID +" like? "
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,//folder name deta hai
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
            //for folder (bucketId gives id of folder
            MediaStore.Video.Media.BUCKET_ID)
        val cursor = this.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,Selection, arrayOf(folderID),
            MediaStore.Video.Media.DATE_ADDED +" DESC")
        if(cursor!=null)
            if(cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val sizeC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.SIZE))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
                    val durationC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DURATION)).toLong()
                    try {
                        val file = File(pathC)
                        val artUriC = Uri.fromFile(file)
                        val video = Video(title = titleC, id = idC, folderName = folderC, size =sizeC, path = pathC, duration = durationC
                            , artUri = artUriC,)
                        if(file.exists()){
                            tempList.add(video)
                        }

                    }
                    catch (e:Exception){
                        Log.d("error", "exception: ",e)
                    }


                }while (cursor.moveToNext())
        cursor?.close()
        return tempList
    }
}