package com.abudreas.qviewer

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class File_Select : AppCompatActivity() {
    private val filteredList = mutableListOf<file_description>()
    private val handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Action bar
        setContentView(R.layout.activity_file_select)
        setSupportActionBar(findViewById(R.id.toolbar3))
        //////////////
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val progressBar = this.findViewById<ProgressBar>(R.id.pb_fileSelect)
       //var external: File = Environment.getRootDirectory()
        var emulated = Environment.getExternalStorageDirectory()
        //if (!emulated.canRead()) emulated = File( "/sdcard/Download/")
        if (!emulated.canRead()) emulated = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Toast.makeText(this, emulated.absolutePath, Toast.LENGTH_SHORT).show()
        if (!emulated.canRead()) {


        AlertDialog.Builder(this).setTitle("ERROR").setMessage("Pleas capture the screen and send it to Ammar \n "+Environment.getExternalStorageState()+"\n !!" + emulated.absolutePath +"\n cant read emulated \n" + android.os.Build.VERSION.SDK_INT.toString()).show()
        return
        }
        Thread {


                // Update the progress bar and display the
                //current value in the text view

            handler.post(Runnable {
                    progressBar.progress
                })

            try {
              //   lookUp(external)
                lookUp(emulated)
            } catch (e:Exception){
                handler.post(Runnable {   AlertDialog.Builder(this).setTitle("ERROR").setMessage(e.message.toString()).show()
            })}

            handler.post(Runnable {
                progressBar.visibility = View.GONE
                this.findViewById<TextView>(R.id.tv_wait).visibility = View.GONE
                createRecycle()
            })
        }.start()


    }
    fun lookUp(file: File):List<File>{



            val myList: List<File> = file.listFiles().asList()

            if (myList.isEmpty()) {
                return myList
            }
            val tempList = myList.toMutableList()
            for (f in myList) {
                Log.println(Log.INFO,"msg",f.absolutePath)
                if (f.isDirectory && f.canRead()) {
                    tempList.addAll(lookUp(f))

                } else {
                    if (f.extension == "db") {
                        val desc = extractDescription(f)
                        if (desc.absolutePath != "" && !doesContainDesc(desc)) filteredList.add(desc)
                        Log.println(Log.INFO, "files", f.absolutePath)
                    }
                }
            }
            return tempList.toList()

    }
    fun extractDescription(file:File):file_description{
        var sql = "SELECT `info` FROM `qviewer` WHERE id = 1"

        var fileDesc =file_description ("","",0,"")
        try {
            val db: SQLiteDatabase = SQLiteDatabase.openDatabase(file.absolutePath,null, SQLiteDatabase.OPEN_READONLY)
            val query = db.rawQuery(sql,null)
            query.moveToFirst()
            val info = query.getString(0)
            fileDesc = file_description(MainActivity.proseInfo(info,"title"),MainActivity.proseInfo(info,"description"),MainActivity.proseInfo(info,"ver").toInt(),file.absolutePath)
        } catch (e : Exception){
            Log.println(Log.INFO,"Error",e.message.toString())
        }
        return  fileDesc
    }
    fun createRecycle() {
        if (filteredList.size == 0){
            this.findViewById<TextView>(R.id.tv_wait).text ="No compatible data base file found in your device "
            this.findViewById<TextView>(R.id.tv_wait).visibility = View.VISIBLE
            return
        }
        val recycler = this.findViewById<RecyclerView>(R.id.rv_fileList)
        recycler.visibility = View.VISIBLE
        recycler.adapter = File_Select_Adapter(filteredList, this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.layoutAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(this, R.anim.popup_animation),
            0.2f
        )

    }
    fun doesContainDesc(d:file_description):Boolean{
        for (i in filteredList){
            if (i.absolutePath == d.absolutePath) return true
        }
        return false
    }
}