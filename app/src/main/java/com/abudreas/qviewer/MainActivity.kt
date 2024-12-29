package com.abudreas.qviewer


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteDatabase.openDatabase
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.*
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_DENIED
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    var listOfCategory = arrayListOf<Category>()
    var tableNames = arrayOf<String>()
    private val all = "All Questions"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

      //  Toast.makeText(this, this.applicationInfo.dataDir, Toast.LENGTH_LONG).show()
        if (
            ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) != PERMISSION_GRANTED
        ) {

            requestPermissions(
                arrayOf(

                    "android.permission.READ_EXTERNAL_STORAGE"
                ), 0
            )
        } else if (
            ContextCompat.checkSelfPermission(
                this,
                "android.permission.READ_EXTERNAL_STORAGE"
            ) == PERMISSION_DENIED
        ) {
            AlertDialog.Builder(this).setTitle("ERROR").setMessage("Pleas capture the screen and send it to Ammar \n Permission is not granted to access  files in your device \n " + android.os.Build.VERSION.SDK_INT.toString()).show()

            return
        }
        if (  android.os.Build.VERSION.SDK_INT > 29 && ! Environment.isExternalStorageManager()) {
            val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")

            startActivity(
                Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    uri
                )
            )
        } else {
            startIt()
        }
        setSupportActionBar(findViewById(R.id.toolbar2))

    if (getSharedPreferences("file_name", AppCompatActivity.MODE_PRIVATE).getString("file_name","") ==""){
       val tv : TextView =this.findViewById(R.id.tv_noDB)
        tv.visibility = View.VISIBLE
    }


    }

    private fun startIt() {
        //check sqlite file

       // sqlitePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath + "/sqlite.db"
       /* sqlitePath = getSharedPreferences("file_name", AppCompatActivity.MODE_PRIVATE).getString("file_name","").toString()
        val file = File(sqlitePath)
        if (!file.exists() || !file.canRead()){
            sqlitePath =""
            Toast.makeText(this, "No data base file found, please download 'sqlite.db' and select it from the menu", Toast.LENGTH_LONG).show()
            return
        }*/
        // Create an ArrayAdapter using the string array and a default spinner layout
        if (!loadSql()) return

        //val mylst = arrayListOf<String>("1","2","3")
        val spinner: Spinner = this.findViewById(R.id.spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val s = tableNames[pos]
                val ct = loadCatg(s.toString())
                createCatgList(loadStat(s.toString(), ct), s.toString())
                createRecycle()
                this@MainActivity.getSharedPreferences("Spinner_pos", MODE_PRIVATE)
                    .edit().putInt("Spinner_pos", pos)
                    .apply()
            }

        }
        val spinner_pos = getSharedPreferences("Spinner_pos", MODE_PRIVATE).getInt("Spinner_pos", 0)
        if (spinner_pos < spinner.adapter.count) spinner.setSelection(spinner_pos)

        //Setup CheckBoxes
        val cbWrong: CheckBox = this.findViewById(R.id.cb_Wrongly)
        val cbUnattm: CheckBox = this.findViewById(R.id.cb_unattempted)
        cbWrong.setOnCheckedChangeListener { _, b ->
            if (b) {
                cbUnattm.isChecked = false
                wrongly = true
                unAttemp = false
            } else {
                wrongly = false
            }
        }
        cbUnattm.setOnCheckedChangeListener { _, b ->
            if (b) {
                cbWrong.isChecked = false
                wrongly = false
                unAttemp = true
            } else {
                unAttemp = false
            }
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        val PERMS_STORAGE = 1337
        if (requestCode == PERMS_STORAGE) {
            startIt()
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    override fun onResume() {
        super.onResume()
        if (sqlitePath=="" /*|| !File(sqlitePath).canRead()*/) return
        wrongly = false
        unAttemp = false
        val spinner: Spinner = this.findViewById(R.id.spinner)
        this.findViewById<CheckBox>(R.id.cb_Wrongly).isChecked = false
        this.findViewById<CheckBox>(R.id.cb_unattempted).isChecked = false
        if (sqlitePath != getSharedPreferences("file_name", AppCompatActivity.MODE_PRIVATE)
                .getString("file_name","").toString()){
            startIt()
            getSharedPreferences("file_name", AppCompatActivity.MODE_PRIVATE).edit().putString("file_name", sqlitePath)
                .apply()
            val tv : TextView =this.findViewById(R.id.tv_noDB)
            tv.visibility = View.GONE
            return
        }
        if(tableNames.size >0) {
            var s = tableNames[0]
            if (spinner.selectedItemPosition >= 0) {
                s = tableNames[spinner.selectedItemPosition]
            }

        val ct = loadCatg(s.toString())
        createCatgList(loadStat(s.toString(), ct), s.toString())
        createRecycle()
    }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {


        R.id.action_info -> {
            /* // User chose the "Favorite" action, mark the current item
             // as a favorite...
             // inflate the layout of the popup window
             // inflate the layout of the popup window
             val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
             val popupView: View = inflater.inflate(R.layout.popup_window, null)

             // create the popup window

             // create the popup window
             val width = LinearLayout.LayoutParams.WRAP_CONTENT
             val height = LinearLayout.LayoutParams.WRAP_CONTENT
             val focusable = true // lets taps outside the popup also dismiss it

             val popupWindow = PopupWindow(popupView, width, height, focusable)

             // show the popup window
             // which view you pass in doesn't matter, it is only used for the window tolken
             popupWindow.animationStyle = 2
             // show the popup window
             // which view you pass in doesn't matter, it is only used for the window tolken
             popupWindow.showAtLocation(this.findViewById(R.id.layaout_main), Gravity.CENTER, 0, 0)

             // dismiss the popup window when touched

             // dismiss the popup window when touched
             popupView.setOnTouchListener(object : View.OnTouchListener {
                 override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                     popupWindow.dismiss()
                     return true
                 }
             })*/
            AlertDialog.Builder(this).setTitle("About").setMessage(getString(R.string.About)).show()
            true
        }
        R.id.Reset -> {
            val spinner: Spinner = this.findViewById(R.id.spinner)
            AlertDialog.Builder(this).setTitle("Warning")
                .setMessage("Do you want to reset all your progress on \n  '${spinner.selectedItem.toString()}' ?")
                .setPositiveButton("Yes", DialogInterface.OnClickListener { _, _ ->
                    reset()
                }).setNegativeButton("No", null)
                .show()
            true
        }
        R.id.Select_db -> {
            val intent = Intent(this,File_Select::class.java)
            this.startActivity(intent)
            true
        }
        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    private fun reset() {
        val spinner: Spinner = this.findViewById(R.id.spinner)
        try {
            val db: SQLiteDatabase = openOrCreateDatabase(DB_Name,  MODE_PRIVATE,null)
            var sql = "UPDATE `${tableNames[spinner.selectedItemPosition]}` SET solved = '0'"
            db.execSQL(sql)
            sql = "SELECT TableInfo FROM `${tableNames[spinner.selectedItemPosition]}` WHERE ID = 1"
            val query = db.rawQuery(sql, null)
            query.moveToFirst()
            var info = query.getString(0)
            query.close()
            var label = proseInfo(info, "info")
            info = proseInfo("", "info", label)
            sql =
                "UPDATE `${tableNames[spinner.selectedItemPosition]}` SET TableInfo = '$info' WHERE ID = 1"
            db.execSQL(sql)
            db.close()
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()

        } finally {
            val s = tableNames[spinner.selectedItemPosition]
            val ct = loadCatg(s.toString())
            createCatgList(loadStat(s.toString(), ct), s.toString())
            createRecycle()
            Toast.makeText(
                this,
                "'${spinner.selectedItem.toString()}' Successfully been reset",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun loadSql(): Boolean {

        val spinner: Spinner = this.findViewById(R.id.spinner)
        var sql = "SELECT name FROM sqlite_master WHERE type='table'"
        var db: SQLiteDatabase
        try {
            db = openOrCreateDatabase(DB_Name, Context.MODE_PRIVATE,null)


            //  val cmd : Unit = con.execSQL(sql)
            val query = db.rawQuery(sql, null)
            query.moveToFirst()

            val listOfNames = arrayListOf<String>()
            val listOfLables = arrayListOf<String>()
            do {
                val reslt = query.getString(0)
                if (reslt != "android_metadata" && reslt != "qviewer" && reslt != "sqlite_sequence") {
                    sql = "SELECT TableInfo FROM $reslt WHERE ID = 1"
                    val tableInfo = db.rawQuery(sql, null)
                    tableInfo.moveToFirst()
                    listOfLables.add(proseInfo(tableInfo.getString(0), "info"))
                    listOfNames.add(reslt)
                    tableInfo.close()
                }
            } while (query.moveToNext())
            val adp = ArrayAdapter(this, android.R.layout.simple_list_item_1, listOfLables)
            tableNames = listOfNames.toTypedArray()
            spinner.adapter = adp
            // Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show()
            query.close()
            db.close()
        } catch (ex: Exception) {
            Toast.makeText(this, ex.message, Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }

    fun loadCatg(tableName: String): Array<String> {
        val db: SQLiteDatabase = openOrCreateDatabase(DB_Name,  MODE_PRIVATE,null)
        var sql = "SELECT DISTINCT catg FROM `$tableName`"
        val query = db.rawQuery(sql, null)
        query.moveToFirst()
        val mylst = arrayListOf<String>()
        do {
            val reslt = query.getString(0)
            if (reslt != "android_metadata" && reslt != "sqlite_sequence") {
                mylst.add(reslt)
            }
        } while (query.moveToNext())
        mylst.add(all)
        query.close()
        db.close()
        return mylst.toTypedArray()
    }

    fun loadStat(tableName: String, catg: Array<String>): Array<Array<String>> {
        val db: SQLiteDatabase = openOrCreateDatabase(DB_Name,  MODE_PRIVATE,null)
        var query: Cursor
        val arrTable = Array(catg.size) {
            Array(4) { _ -> "" }
        }

        var sql: String
        var x: Double
        var t: Double
        for (i in catg.indices) {
            arrTable[i][0] = catg[i]
            sql = "SELECT count(ID) FROM `$tableName`"
            if (catg[i] != all) sql += "WHERE catg ='${catg[i]}'"
            query = db.rawQuery(sql, null)
            query.moveToFirst()
            arrTable[i][1] = query.getString(0)
            query.close()
            if (catg[i] == all) {
                sql += " WHERE "
            } else {
                sql += " AND "
            }
            sql += "solved <> '0'"
            query = db.rawQuery(sql, null)
            query.moveToFirst()
            arrTable[i][2] = query.getString(0)
            query.close()
            sql = sql.replace(" <> '0'", " = '2'")
            query = db.rawQuery(sql, null)
            query.moveToFirst()
            x = query.getDouble(0)
            t = arrTable[i][2].toDouble()
            if ((x < 0) || (x > 0)) {
                x /= t
            }
            arrTable[i][3] = (x * 100).roundToInt().toString() + " %"
            // sql = "SELECT count(ID) FROM `$tableName`"
            query.close()
        }

        db.close()
        return arrTable
    }

    fun createRecycle() {
        val recycler = this.findViewById<RecyclerView>(R.id.recycle)
        recycler.adapter = Adapter(listOfCategory, this)
        recycler.layoutManager = LinearLayoutManager(this)
        recycler.layoutAnimation = LayoutAnimationController(
            AnimationUtils.loadAnimation(this, R.anim.popup_animation),
            0.2f
        )
    }

    fun createCatgList(arr: Array<Array<String>>, tableName: String) {
        listOfCategory.clear()
        for (a in arr) {
            listOfCategory.add(Category(a[0], a[1].toInt(), a[2].toInt(), a[3], true, tableName))
        }
    }

    companion object {
        public var wrongly = false
        public var unAttemp = false
        public var sqlitePath = ""//"/storage/emulated/0/Download/sqlite.db"
        var DB_Name = "Sqlite.db"
        fun proseInfo(theInfo: String, theOpt: String, setValue: String = ""): String {
            var found = false
            var cnt = 0
            val arr = theInfo.toCharArray()
            var s = ""
            for (i in arr.indices) {
                if ((arr[i] == "*".toCharArray()[0]) && !found) {
                    cnt = i + 1
                } else if (arr[i] == ":".toCharArray()[0]) {
                    if (theInfo.substring(cnt, i) == theOpt) found = true

                } else if ((arr[i] == "*".toCharArray()[0]) && found) {
                    if (setValue == "") {
                        s = theInfo.substring(cnt + theOpt.length + 1, i)
                    } else {
                        s = theInfo.removeRange(cnt + theOpt.length + 1, i)
                        s = StringBuilder(s).insert(cnt + theOpt.length + 1, setValue).toString()

                    }
                    break
                }
            }
            if (!found && setValue != "") {
                if (theInfo == "") s = "*"
                s += theInfo + "$theOpt:$setValue*"

            }
            return s
        }
    }
}