package com.example.videoplayer_yas.adapterClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.videoplayer_yas.ui.FoldersActivity
import com.example.videoplayer_yas.dataClasses.Folder
import com.example.videoplayer_yas.databinding.FoldersViewBinding

class FolderAdapter(private val context:Context, private var folderList: ArrayList<Folder>):RecyclerView.Adapter<FolderAdapter.MyHolder>(){

     class MyHolder(binding:FoldersViewBinding):RecyclerView.ViewHolder(binding.root){
           val folderName = binding.folderNameFV
         val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FoldersViewBinding.inflate(LayoutInflater.from(context),parent,false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
       holder.folderName.text = folderList[position].folderName
        holder.root.setOnClickListener {
            val intent = Intent(context, FoldersActivity::class.java)
            intent.putExtra("position",position)
            //yah ek adapter hai Yahan sai hum startactiviy ka use nhi kr skte
            ContextCompat.startActivity(context,intent,null)
        }
    }

    override fun getItemCount(): Int {
        return folderList.size
    }

}
