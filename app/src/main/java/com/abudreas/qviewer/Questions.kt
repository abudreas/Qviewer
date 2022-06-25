package com.abudreas.qviewer

import android.database.sqlite.SQLiteDatabase
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.*
import androidx.core.view.children
import androidx.core.view.isVisible
import kotlin.math.roundToInt
import com.bumptech.glide.Glide

class Questions : AppCompatActivity() {
    var showCounter = 0
    var numberOfCorrect  = 0
    var numberOfAnswered = 0
    var TableName = ""
    var category =""
    var tableInfo =""
    var sessionStart = false
    val questList = arrayListOf<Quest>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)
        //Setup Action bar
        //var actionBar = this.findViewById<Toolbar>(R.id.toolbar)

        //this.setActionBar(actionBar)
         val btnNext = this.findViewById<Button>(R.id.btn_next)
       val intent = intent

      loadQuest(intent.extras?.get("tableName").toString(),intent.extras?.get("catg").toString())
       showNextQuest()
        //actionBar.title = category
       btnNext.setOnClickListener {
          if (!checkAnswer()) {
              if (showCounter == questList.size -1){
                  Toast.makeText(this, "No More Questions", Toast.LENGTH_SHORT).show()
                  return@setOnClickListener
              }
              showCounter++
              val spin :Spinner = this.findViewById(R.id.sp_selectQuest)
              spin.setSelection(showCounter,true)
              showNextQuest()
          }
       }
        val btnPrev :Button = this.findViewById(R.id.btn_prev)
        btnPrev.setOnClickListener {
            if (showCounter == 0){

                return@setOnClickListener
            }
            showCounter--
            val spin :Spinner = this.findViewById(R.id.sp_selectQuest)
            spin.setSelection(showCounter,true)
            showNextQuest()
            colorIt()
        }
        //initiate spinner :
        val spin :Spinner = this.findViewById(R.id.sp_selectQuest)
        var questNumber = Array<Int>(questList.size){i->i+1}
        val spinAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,questNumber)
        spin.adapter = spinAdapter
        spin.setSelection(showCounter,true)
        spin.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {

            }

            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                showCounter = pos
                showNextQuest()
                colorIt()
            }

        }
        val tn :TextView=this.findViewById(R.id.tv_ofTotal)
        tn.text="of " + questList.size.toString()
        sessionStart = true

    }
    fun checkAnswer():Boolean{
        val radGroup :RadioGroup= this.findViewById(R.id.radioGroup)
        val answer:RadioButton? = this.findViewById( radGroup.checkedRadioButtonId)
        val tvCorrect:TextView = this.findViewById(R.id.tv_indicator)
        if (answer == null ||questList[showCounter].Answer !="" ) return false
        if (questList[showCounter].check(answer.text.toString())) {
        this.findViewById<TextView>(R.id.tv_percent).text ="Correct = "+ calculatePercent(true).toString() +" %"
           tvCorrect.setTextColor(getColor(android.R.color.holo_green_dark))
            tvCorrect.text="Correct !"
        } else{
            this.findViewById<TextView>(R.id.tv_percent).text ="Correct = "+calculatePercent(false).toString() +" %"
            tvCorrect.setTextColor(getColor(android.R.color.holo_red_dark))
            tvCorrect.text="Wrong !"
        }
        questList[showCounter].Answer = answer.text.toString()
        colorIt()

        return true
    }
    private fun showNextQuest(){
        val tvCorrect:TextView = this.findViewById(R.id.tv_indicator)
        val tvQuest = this.findViewById<TextView>(R.id.tv_quest)
        val radio1 = this.findViewById<RadioButton>(R.id.radioButton)
        val radio2 = this.findViewById<RadioButton>(R.id.radioButton2)
        val radio3 = this.findViewById<RadioButton>(R.id.radioButton3)
        val radio4 = this.findViewById<RadioButton>(R.id.radioButton4)
        val radio5 = this.findViewById<RadioButton>(R.id.radioButton5)
        val radioGroup = this.findViewById<RadioGroup>(R.id.radioGroup)
        val tvExplain = this.findViewById<TextView>(R.id.tv_explain)
        val tvIndicator = this.findViewById<TextView>(R.id.tv_indicator)
        val imQuest = this.findViewById<ImageView>(R.id.im_quest)
        val imExplain = this.findViewById<ImageView>(R.id.im_explain)
        radioGroup.clearCheck()
        colorIt()
         val thisQuest = questList[showCounter]
        tvQuest.text = thisQuest.question
        radio1.text = thisQuest.op1
        radio2.text = thisQuest.op2
        radio3.text = thisQuest.op3
        radio4.text = thisQuest.op4
        if (thisQuest.op5 =="" || thisQuest.op5 ==" "){
            radio5.isVisible = false
        }else {
            radio5.isVisible = true
            radio5.text = thisQuest.op5
        }
        tvExplain.text = thisQuest.explain
        var byt = Base64.decode(thisQuest.img, Base64.DEFAULT)
        Glide.with(this).load(byt).into(imQuest)
        byt = Base64.decode(thisQuest.expImg, Base64.DEFAULT)
        Glide.with(this).load(byt).into(imExplain)
        tvExplain.isVisible = false
        imExplain.isVisible= false
        tvCorrect.text=""
        saveProgress()
    }
    fun colorIt(){
        val radGroup :RadioGroup= this.findViewById(R.id.radioGroup)
        val answer = questList[showCounter].Answer

            if (answer == ""){
                for (r in radGroup.children){
                    r.setBackgroundResource(0)
                }
                return
            }
            for (r in radGroup.children){
                val a:RadioButton = this.findViewById(r.id)
                if (questList[showCounter].check(a.text.toString())){
                    a.setBackgroundResource(R.drawable.correct)
                    saveAnswer(true)
                }else if (a.text.toString() == questList[showCounter].Answer && ! questList[showCounter].check(a.text.toString()) ) {
                    a.setBackgroundResource(R.drawable.wrong)
                    saveAnswer(false)
                }else{
                    a.setBackgroundResource(0)
                }
            }
        val tvExplain = this.findViewById<TextView>(R.id.tv_explain)
        val imExplain = this.findViewById<ImageView>(R.id.im_explain)
        tvExplain.isVisible = questList[showCounter].Answer != ""
        imExplain.isVisible = questList[showCounter].Answer != ""
    }
    fun loadQuest(tableName:String,catg:String){
        TableName = tableName
        category = catg
        var sql = "SELECT * FROM `$tableName`"
        if(catg!="All Questions")  sql+= " WHERE `catg` = '$catg'"
        val db : SQLiteDatabase = openOrCreateDatabase("sqlite.db", MODE_PRIVATE,null)
        var query = db.rawQuery(sql,null)
        query.moveToFirst()
        query.getInt(0)

        var q6 :String
        var q11:String
        var q12:String

        do {
            if (query.getType(6)!=3){
                q6 = ""
            }else{
                q6= query.getString(6)
            }
            if (query.getType(11)!=3){
                q11 = ""
            }else{
                q11= query.getString(11)
            }
            if (query.getType(12)!=3){
                q12 = ""
            }else{
                q12= query.getString(12)
            }
            val result=Quest (query.getInt(0),query.getString(1),query.getString(2),query.getString(3),query.getString(4),query.getString(5),q6,query.getString(7),query.getString(8))

            result.img = q11
            result.expImg=q12
            questList.add(result)
        } while (query.moveToNext())
        sql = "SELECT `TableInfo` FROM $TableName WHERE `ID` = 1"
        query= db.rawQuery(sql,null)
        query.moveToFirst()
        tableInfo = query.getString(0)

        showCounter = MainActivity.proseInfo(tableInfo,category).toIntOrNull()?:0
        query.close()
        db.close()
    }
    fun calculatePercent(correct:Boolean):Int{
        if(correct) numberOfCorrect ++
        numberOfAnswered ++
        return ((numberOfCorrect.toDouble()/numberOfAnswered.toDouble()) * 100.0).roundToInt()
    }
    fun saveAnswer(b:Boolean){
        var a = "1"
        if (b) a = "2"
        val sql = "UPDATE `$TableName` SET solved =$a WHERE ID =${questList[showCounter].id.toString()}"
        val db : SQLiteDatabase = openOrCreateDatabase("sqlite.db", MODE_PRIVATE,null)
        db.execSQL(sql)
        db.close()
    }
    fun saveProgress(){
        val db : SQLiteDatabase = openOrCreateDatabase("sqlite.db", MODE_PRIVATE,null)
        tableInfo = MainActivity.proseInfo(tableInfo,category,showCounter.toString())
        val sql = "UPDATE `$TableName` SET TableInfo = '$tableInfo' WHERE ID = 1"
        db.execSQL(sql)
        db.close()
    }
}