package com.example.videoplayer_yas.ui

import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.videoplayer_yas.R
import com.example.videoplayer_yas.allfragments.FolderFragment
import com.example.videoplayer_yas.allfragments.VideosFragment
import com.example.videoplayer_yas.dataClasses.Folder
import com.example.videoplayer_yas.dataClasses.Video
import com.example.videoplayer_yas.databinding.ActivityMainBinding
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle:ActionBarDrawerToggle
    companion object{
        lateinit var videoList: ArrayList<Video>
        lateinit var folderList :ArrayList<Folder>
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setTheme(R.style.coolPinkNav)
        setContentView(binding.root)
        //for nav drawer
        toggle = ActionBarDrawerToggle(this,binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(requestRuntimepermission()){
            folderList = ArrayList()
            videoList = getAllVideos()
            setFragments(VideosFragment())
        }
        setFragments(VideosFragment())
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.VideoView -> setFragments(VideosFragment())
                R.id.foldersView -> setFragments(FolderFragment())
            }
            return@setOnItemSelectedListener true
        }
        binding.NavView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.feedbackNav -> Toast.makeText(this, "feedback clicked", Toast.LENGTH_SHORT).show()
                R.id.themesNav -> Toast.makeText(this, "theme clicked", Toast.LENGTH_SHORT).show()
                R.id.sortOrderNav -> Toast.makeText(this, "sort Order clicked", Toast.LENGTH_SHORT).show()
                R.id.aboutNav -> Toast.makeText(this, "about clicked", Toast.LENGTH_SHORT).show()
                R.id.exitNav -> exitProcess(1)
            }
            return@setNavigationItemSelectedListener true
        }
    }
    private fun setFragments(fragment:Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentFl,fragment)
        transaction.disallowAddToBackStack()
        transaction.commit()
    }
    //for request permission from user
    private fun requestRuntimepermission():Boolean{
        if(ActivityCompat.checkSelfPermission(this,WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),10)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
              if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                  Toast.makeText(this, "storage permission is granted", Toast.LENGTH_SHORT).show()
                  folderList = ArrayList()
                  videoList = getAllVideos()
                  setFragments(VideosFragment())
              }
              else
                   //get permission
                    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),10)
        }

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item))
            return true
        return super.onOptionsItemSelected(item)
    }
    @SuppressLint("Recycle", "Range", "SuspiciousIndentation")
    private fun getAllVideos():ArrayList<Video>{
        val tempList = ArrayList<Video>()
        val tempFolderList =ArrayList<String>()
        val projection = arrayOf(
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,   //gives folder name  hai
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_ADDED,
           //for folder (bucketId gives id of folder)
            MediaStore.Video.Media.BUCKET_ID)
        val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection,null,null,
        MediaStore.Video.Media.DATE_ADDED +" DESC")
        if(cursor!=null)
            if(cursor.moveToNext())
                do {
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media._ID))
                    val folderC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME))
                    val folderIDC = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_ID))
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
                        //for folders
                        if(!tempFolderList.contains(folderC)) {      //if folderC is not inside the TempFolder List
                              tempFolderList.add(folderC)
                              folderList.add(Folder(id = folderIDC, folderName = folderC))
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