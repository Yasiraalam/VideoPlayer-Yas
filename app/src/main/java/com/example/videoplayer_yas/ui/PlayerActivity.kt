package com.example.videoplayer_yas.ui

import android.annotation.SuppressLint
import android.app.AppOpsManager
import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.videoplayer_yas.R
import com.example.videoplayer_yas.dataClasses.Video
import com.example.videoplayer_yas.databinding.ActivityPlayerBinding
import com.example.videoplayer_yas.databinding.MoreFeaturesBinding
import com.example.videoplayer_yas.databinding.SpeedDialogBinding
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.DecimalFormat
import java.util.Locale
import java.util.Timer
import java.util.TimerTask
import kotlin.system.exitProcess

class PlayerActivity : AppCompatActivity() {
    private lateinit var binding :ActivityPlayerBinding
    private lateinit var runnable: Runnable
    private var isSubtitle:Boolean = true
    companion object{
        private var timer :Timer? =null
        private lateinit var player :SimpleExoPlayer
        lateinit var playerList:ArrayList<Video>
        var position:Int =-1
        var repeat :Boolean=false
        private var isFullScreen:Boolean =false
        private var isLocked:Boolean =false
        @SuppressLint("StaticFieldLeak")
        lateinit var trackSelector:DefaultTrackSelector
        private  var speed:Float = 0.1f
        var pipStatus:Int =0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setTheme(R.style.playerActivityTheme)
        setContentView(binding.root)
        //for immersive mode1
        WindowCompat.setDecorFitsSystemWindows(window , true)
        WindowInsetsControllerCompat(window , binding.root).let { controller ->
            controller.hide(WindowInsetsCompat.Type.statusBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        initializeLayout()
        initializeBinding()
    }
    private fun initializeLayout(){
        when(intent.getStringExtra("class")){
            "AllVideos" ->{
                playerList = ArrayList()
                playerList.addAll(MainActivity.videoList)
                createPlayer()
            }
            "FolderActivity" ->{
                playerList = ArrayList()
                playerList.addAll(FoldersActivity.currentfolderVideos)
                createPlayer()
            }
        }
        //if(repeat) binding.exoRepeatToggle.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_all)
    }
    @SuppressLint("PrivateResource", "SetTextI18n")
    private fun initializeBinding(){
        binding.backBtn.setOnClickListener {
            finish()
        }
        binding.playPauseBtn.setOnClickListener {
            if(player.isPlaying) pauseVideo()
            else playVideo()
        }
        binding.exoNext.setOnClickListener { nextPreVideo() }
        binding.exoPrev.setOnClickListener { nextPreVideo(isNext = false) }
        binding.exoRepeatToggle.setOnClickListener {
            if(repeat) {
                repeat =false
                player.repeatMode = Player.REPEAT_MODE_OFF
                binding.exoRepeatToggle.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_off)
            }else{
                repeat =true
                player.repeatMode = Player.REPEAT_MODE_ONE
                binding.exoRepeatToggle.setImageResource(com.google.android.exoplayer2.ui.R.drawable.exo_controls_repeat_one)
            }
        }
        //for FullScreen
        binding.fullScreenbtn.setOnClickListener {
            if(isFullScreen){
                isFullScreen =false
                playInFullscreen(false)
            }else{
                isFullScreen =true
                playInFullscreen(true)
            }
        }
        //for lock btn
        binding.lockButton.setOnClickListener {
            if(!isLocked){
                //for Hiding
                isLocked =true
                binding.playerView.hideController()
                binding.playerView.useController =false
                binding.lockButton.setImageResource(R.drawable.close_lock_icon)

            } else{
                //for showing
                isLocked =false
                binding.playerView.useController =true
                binding.playerView.showController()
                binding.lockButton.setImageResource(R.drawable.lock_open_icon)
            }
        }
        binding.moreFeaturesBtn.setOnClickListener {
            pauseVideo()
            val customDialog = LayoutInflater.from(this).inflate(R.layout.more_features,binding.root,false)
            val bindingMF = MoreFeaturesBinding.bind(customDialog)
            val dialog = MaterialAlertDialogBuilder(this).setView(customDialog)
                .setOnCancelListener { playVideo() }
                .setBackground(ColorDrawable(0x803848A1.toInt()))
                .create()
            dialog.show()

            bindingMF.audioTrack.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val audioTrack = ArrayList<String>()
                for(i in 0 until player.currentTrackGroups.length){
                    if(player.currentTrackGroups.get(i).getFormat(0).selectionFlags == C.SELECTION_FLAG_DEFAULT){
                        audioTrack.add(Locale(player.currentTrackGroups.get(i).getFormat(0).language.toString()).displayLanguage)
                    }
                }
                // array ko charsequence mai change krne k liya
                val tempTracks = audioTrack.toArray(arrayOfNulls<CharSequence>(audioTrack.size))
                MaterialAlertDialogBuilder(this, R.style.alertDialog)
                    .setTitle("Select Language")
                    .setOnCancelListener { playVideo() }
                    .setBackground(ColorDrawable(0x803848A1.toInt()))
                    .setItems(tempTracks){_,position->
                        Toast.makeText(this,  audioTrack[position] + " Selected", Toast.LENGTH_SHORT).show()
                        trackSelector.setParameters(trackSelector.buildUponParameters().setPreferredAudioLanguage(audioTrack[position]))
                    }
                    .create()
                    .show()
            }
            //subtitles btn
            bindingMF.subtitlesBtn.setOnClickListener {
                if(isSubtitle){
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO,true
                    ).build()
                    Toast.makeText(this, "Subtitle Off", Toast.LENGTH_SHORT).show()
                    isSubtitle = false
                }else{
                    trackSelector.parameters = DefaultTrackSelector.ParametersBuilder(this).setRendererDisabled(
                        C.TRACK_TYPE_VIDEO,false
                    ).build()
                    Toast.makeText(this, "Subtitle On", Toast.LENGTH_SHORT).show()
                    isSubtitle= true
                }
                dialog.dismiss()
                playVideo()
            }
            //speed btn
            bindingMF.speedBtn.setOnClickListener {
                dialog.dismiss()
                playVideo()
                val customDialogS = LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                val bindingS = SpeedDialogBinding.bind(customDialogS)
                val dialogS = MaterialAlertDialogBuilder(this).setView(customDialogS)
                    .setCancelable(false)
                    .setPositiveButton("OK"){self,_ ->
                        self.dismiss()
                    }
                    .setBackground(ColorDrawable(0x803848A1.toInt()))
                    .create()
                dialogS.show()
                bindingS.speedText.text = "$speed X"
                bindingS.minusBtn.setOnClickListener {
                    changeSpeed(isIncrement = false)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)}X"
                }
                bindingS.plusBtn.setOnClickListener {
                    changeSpeed(isIncrement = true)
                    bindingS.speedText.text = "${DecimalFormat("#.##").format(speed)}X"
                }
            }
            //sleep timer
            bindingMF.sleepTimer.setOnClickListener {
                dialog.dismiss()
                if(timer != null)
                    Toast.makeText(this, "Timer is Already Running!\nClose App to Reset Timer!!.", Toast.LENGTH_SHORT).show()
                else{
                    var sleepTime =15
                    val customDialogS = LayoutInflater.from(this).inflate(R.layout.speed_dialog,binding.root,false)
                    val bindingS = SpeedDialogBinding.bind(customDialogS)
                    val dialogS = MaterialAlertDialogBuilder(this).setView(customDialogS)
                        .setCancelable(false)
                        .setPositiveButton("OK"){self,_ ->
                            timer =Timer()
                            val task = object: TimerTask(){
                                override fun run() {
                                    moveTaskToBack(true)
                                    exitProcess(1)
                                }
                            }
                            timer !!.schedule(task,sleepTime*60*1000.toLong())
                            self.dismiss()
                        }
                        .setBackground(ColorDrawable(0x803848A1.toInt()))
                        .create()
                    dialogS.show()
                    bindingS.speedText.text = "$sleepTime min"
                    bindingS.minusBtn.setOnClickListener {
                        if(sleepTime > 15)
                            sleepTime-=15
                        bindingS.speedText.text = "$sleepTime min"
                    }
                    bindingS.plusBtn.setOnClickListener {
                        if(sleepTime < 120)
                            sleepTime+=15
                        bindingS.speedText.text = "$sleepTime min"
                    }
                }
            }
            bindingMF.pipModeBtn.setOnClickListener {
                val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
                val status = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appOps.checkOpNoThrow(AppOpsManager.OPSTR_PICTURE_IN_PICTURE, android.os.Process.myUid(),packageName)==AppOpsManager.MODE_ALLOWED
                } else { false }

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if (status) {
                        this.enterPictureInPictureMode(PictureInPictureParams.Builder().build())
                        dialog.dismiss()
                        binding.playerView.hideController()
                        playVideo()
                        pipStatus = 0

                    }else{
                        val intent = Intent("android.settings.PICTURE_IN_PICTURE_SETTINGS",Uri.parse("package: $packageName"))
                        startActivity(intent)
                    }
                }else{
                    Toast.makeText(this, "Feature  Not Supported!!", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    playVideo()
                }
            }
        }
    }
    private fun createPlayer(){
        try {
            player.release()
        }catch (_:Exception){}
        speed =1.0f
        trackSelector = DefaultTrackSelector(this)
        binding.videoTitle.text = playerList[position].title
        binding.videoTitle.isSelected = true
        player = SimpleExoPlayer.Builder(this).setTrackSelector(trackSelector).build()
        binding.playerView.player = player
        val mediaItem = MediaItem.fromUri(playerList[position].artUri)
        player.setMediaItem(mediaItem)
        player.prepare()
        playVideo()
        player.addListener(object: Player.Listener{
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if(playbackState==Player.STATE_ENDED) nextPreVideo()
            }
        })
        playInFullscreen(enable = isFullScreen)
        setVisibility()

    }
    private fun nextPreVideo(isNext:Boolean =true){
        if(isNext) setPosition()
        else setPosition(isIncrement =false)
        createPlayer()

    }
    private fun setPosition(isIncrement:Boolean = true){
       if(!repeat){
           if (isIncrement){
               if(playerList.size-1 == position)
                   position = 0
               else ++position
           }else{
               if(position == 0)
                   position = playerList.size-1
               else
                   --position
           }
       }

    }
    private fun playVideo(){
        binding.playPauseBtn.setImageResource(R.drawable.pause_icon)
        player.play()
    }
    private fun pauseVideo(){
        binding.playPauseBtn.setImageResource(R.drawable.play_icon)
        player.pause()
    }
    private fun playInFullscreen(enable:Boolean){
        if(enable){
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            binding.fullScreenbtn.setImageResource(R.drawable.fullscreen_exit_icon)
        }else{
            binding.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            binding.fullScreenbtn.setImageResource(R.drawable.fullscreen_icon)
        }
    }
    private fun setVisibility(){
        runnable = Runnable {
            if(binding.playerView.isControllerVisible) ChangeVisibility(View.VISIBLE)
            else ChangeVisibility(View.INVISIBLE)
            Handler(Looper.getMainLooper()).postDelayed(runnable,300)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable,0)
    }
    private fun ChangeVisibility(visibility:Int){
        binding.topController.visibility =visibility
        binding.bottomController.visibility =visibility
        binding.playPauseBtn.visibility =visibility
        if(isLocked){
            binding.lockButton.visibility = View.VISIBLE
        }else{
            binding.lockButton.visibility = visibility
        }
    }
    private fun changeSpeed(isIncrement:Boolean){
        if(isIncrement){
            if(speed < 3.0f){
                speed +=0.10f
            }
        }
        else{
            if (speed >0.20f){
                speed -=0.10f
            }
        }
        player.setPlaybackSpeed(speed)
    }

    @SuppressLint("MissingSuperCall")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        if(pipStatus !=0){
            finish()
            val intent = Intent(this, PlayerActivity::class.java)
            when(pipStatus){
                1 -> intent.putExtra("class","FolderActivity")
                2 -> intent.putExtra("class","AllVideos")
            }

           startActivity(intent)
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }


}