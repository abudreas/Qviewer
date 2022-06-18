package com.abudreas.qviewer



import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    var listOfCategory = arrayListOf<Category>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

// Create an ArrayAdapter using the string array and a default spinner layout
        loadSql()

        //val mylst = arrayListOf<String>("1","2","3")
        val spinner: Spinner = this.findViewById(R.id.spinner)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                val s = parent.getItemAtPosition(pos)
                val tn = loadCatg(s.toString())
                createCatgList(loadStat(s.toString(),tn))
                createRecycle()
            }

        }


    }
    fun loadSql(){
        val spinner: Spinner = this.findViewById(R.id.spinner)
        var sql = "SELECT name FROM sqlite_master WHERE type='table'"
        val db : SQLiteDatabase  = openOrCreateDatabase("sqlite.db", MODE_PRIVATE,null)
        //  val cmd : Unit = con.execSQL(sql)
        val query = db.rawQuery(sql, null)
        query.moveToFirst()

        val mylst = arrayListOf<String>()
        do {
            val reslt = query.getString(0)
            if (reslt != "android_metadata") {
                mylst.add(reslt)
            }
        } while (query.moveToNext())
        val adp = ArrayAdapter  (this,android.R.layout.simple_list_item_1,mylst)
        spinner.adapter = adp
        Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show()
        query.close()
        db.close()
    }

    fun loadCatg(tableName:String):Array<String> {
        val db : SQLiteDatabase  = openOrCreateDatabase("sqlite.db", MODE_PRIVATE,null)
        var sql = "SELECT DISTINCT catg FROM `$tableName`"
        val query = db.rawQuery(sql,null)
        query.moveToFirst()
        val mylst = arrayListOf<String>()
        do {
            val reslt = query.getString(0)
            if (reslt != "android_metadata") {
                mylst.add(reslt)
            }
        } while (query.moveToNext())
        return mylst.toTypedArray()
    }

    fun loadStat(tableName: String,catg:Array<String>):Array<Array<String>>{
        val db : SQLiteDatabase  = openOrCreateDatabase("sqlite.db", MODE_PRIVATE,null)
        var  query :Cursor
        val arrTable = Array(catg.size) {
            Array(4) { _ -> "" }
        }

        var sql :String
        var x :Double
        var t :Double
        for (i in  catg.indices){
            arrTable[i  ] [0]= catg[i]
            sql = "SELECT count(ID) FROM `$tableName` WHERE catg ='${catg[i]}'"
            query = db.rawQuery(sql,null)
            query.moveToFirst()
            arrTable[i  ][1]= query.getString(0)

            sql += " AND solved <> '0'"
            query = db.rawQuery(sql,null)
            query.moveToFirst()
            arrTable[i  ][2]=query.getString(0)

            sql = sql.replace(" <> '0'", " = '2'")
            query = db.rawQuery(sql,null)
            query.moveToFirst()
            x=query.getDouble(0)
            t =  arrTable[i  ][2].toDouble()
            if ((x < 0) || (x > 0)){
                x /= t
            }
            arrTable[i  ][3] = (x * 100).roundToInt().toString() + " %"
           // sql = "SELECT count(ID) FROM `$tableName`"
        }

        return arrTable
    }

    fun createRecycle(){
        val recycler = this.findViewById<RecyclerView>(R.id.recycle)
        recycler.adapter = Adapter(listOfCategory)
        recycler.layoutManager = LinearLayoutManager(this)
    }
    fun createCatgList(arr :Array<Array<String>>){
        listOfCategory.clear()
        for (a in arr){
            listOfCategory.add(Category(a[0],a[1].toInt(),a[2].toInt(),a[3],true))
        }
    }

}