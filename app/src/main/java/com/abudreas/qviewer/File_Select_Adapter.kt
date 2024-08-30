package com.abudreas.qviewer

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class File_Select_Adapter (private val dataSet: List<file_description>,val context: Context) :
RecyclerView.Adapter<File_Select_Adapter.ViewHolder>() {
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvFileName = view.findViewById<TextView>(R.id.tv_fileName)
        val  tvDescribtion = view.findViewById<TextView>(R.id.tv_describtion)
        val tvRecommend = view.findViewById<TextView>(R.id.tv_recommended)
        val card = view.findViewById<ConstraintLayout>(R.id.fileSelectCard)
        fun bind(file:file_description) {
            tvFileName.text = file.absolutePath
            tvDescribtion.text = file.descrip
            if (file.absolutePath == MainActivity.sqlitePath){
                card.setBackgroundResource(R.drawable.card)
                tvRecommend.text = "Selected"

            }else{
                tvRecommend.text = ""
                card.setBackgroundResource(R.drawable.card_grey)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_select_card, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.bind( dataSet[position])
        viewHolder.card.setOnClickListener {
            val intent = Intent(context,MainActivity::class.java)

            copyDataBase(dataSet[position].absolutePath)
            MainActivity.sqlitePath = dataSet[position].absolutePath
            context.startActivity(intent)

        }
    }
    private fun copyDataBase( DATABASE_NAME: String) {
        //Open your local db as the input stream
      //  val myInput = context.assets.open(DATABASE_NAME)
        val myInput = File(DATABASE_NAME).inputStream()
        // Path to the just created empty db
        val outFileName = context.applicationInfo.dataDir+"/databases/"+MainActivity.DB_Name
        //Open the empty db as the output stream
        val f = File(outFileName)
       if(!f.exists()) f.createNewFile()
        val myOutput: OutputStream = FileOutputStream(outFileName)
        //transfer bytes from the input file to the output file
        val buffer = ByteArray(1024)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }

        //Close the streams
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }
    override fun getItemCount(): Int {
        return dataSet.count()
    }

}