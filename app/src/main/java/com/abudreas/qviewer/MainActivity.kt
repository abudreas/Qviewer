package com.abudreas.qviewer



import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.database.sqlite.SQLiteDatabase

import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.lang.StringBuilder
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    var listOfCategory = arrayListOf<Category>()
    var tableNames = arrayOf<String>()
    val All = "All Questions"
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
                val s = tableNames[pos]
                val ct = loadCatg(s.toString())
                createCatgList(loadStat(s.toString(),ct),s.toString())
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

        val listOfNames = arrayListOf<String>()
        val listOfLables = arrayListOf<String>()
        do {
            val reslt = query.getString(0)
            if (reslt != "android_metadata") {
                sql = "SELECT TableInfo FROM $reslt WHERE ID = 1"
                val tableInfo = db.rawQuery(sql, null)
                tableInfo.moveToFirst()
                listOfLables.add(proseInfo(tableInfo.getString(0),"info"))
                listOfNames.add(reslt)
                tableInfo.close()
            }
        } while (query.moveToNext())
        val adp = ArrayAdapter  (this,android.R.layout.simple_list_item_1,listOfLables)
        tableNames = listOfNames.toTypedArray()
        spinner.adapter = adp
       // Toast.makeText(this, "Ok", Toast.LENGTH_SHORT).show()
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
        mylst.add(All)
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
            sql = "SELECT count(ID) FROM `$tableName`"
            if (catg[i]!=All) sql += "WHERE catg ='${catg[i]}'"
            query = db.rawQuery(sql,null)
            query.moveToFirst()
            arrTable[i  ][1]= query.getString(0)
            if(catg[i]==All){
                sql += " WHERE "
            }else{
                sql +=" AND "
            }
            sql += "solved <> '0'"
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
        recycler.adapter = Adapter(listOfCategory,this)
        recycler.layoutManager = LinearLayoutManager(this)
    }
    fun createCatgList(arr :Array<Array<String>>,tableName: String){
        listOfCategory.clear()
        for (a in arr){
            listOfCategory.add(Category(a[0],a[1].toInt(),a[2].toInt(),a[3],true,tableName))
        }
    }
    companion object{
        fun proseInfo  (theInfo:String,theOpt:String,setValue:String ="") :String{
            var found = false
            var cnt = 0
            var arr = theInfo.toCharArray()
            var s = ""
            for(i in arr.indices){
                if ((arr[i]=="*".toCharArray()[0]) && ! found ){
                    cnt = i +1
                } else if (arr[i]==":".toCharArray()[0]) {
                    if (theInfo.substring(cnt,i) == theOpt) found = true

                }else if ((arr[i]=="*".toCharArray()[0]) && found ){
                    if (setValue=="") {
                        s = theInfo.substring(cnt+theOpt.length+1,i)
                    }else{
                        s = theInfo.removeRange(cnt+theOpt.length+1,i)
                        s = StringBuilder(s).insert(cnt+theOpt.length+1,setValue).toString()

                    }
                    break
                }
            }
            if (!found && setValue !=""){
                s=theInfo+"$theOpt:$setValue*"
            }
            return s
        }
    }
}