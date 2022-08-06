package com.abudreas.qviewer

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.view.isVisible
import kotlin.math.roundToInt
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class Questions : AppCompatActivity() {
    var showCounter = 0
    var numberOfCorrect  = 0
    var numberOfAnswered = 0
    var TableName = ""
    var category =""
    var tableInfo =""
    var catgModifier = ""
    var sessionStart = false
    var imFactor = 1000
    val questList = arrayListOf<Quest>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_questions)

        //imFactor = this.findViewById<ConstraintLayout>(R.id.)
        //Setup Action bar
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        //this.setActionBar(actionBar)

         val btnNext = this.findViewById<Button>(R.id.btn_next)
       val intent = intent

     if(! loadQuest(intent.extras?.get("tableName").toString(),intent.extras?.get("catg").toString())){
         Toast.makeText(this, "No Question", Toast.LENGTH_SHORT).show()
         return
     }
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
        val btnShow :Button = this.findViewById(R.id.btn_showAnswer)
        btnShow.setOnClickListener {
            questList[showCounter].Answer="Wrong@#$@#@$@$@#$#FSFdfdgfddrgdr5trhg234Vd"
            colorIt()
            saveAnswer(false)
            this.findViewById<TextView>(R.id.tv_percent).text ="Correct = "+calculatePercent(false).toString() +" %"
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
        supportActionBar?.title = MainActivity.proseInfo(tableInfo,"info")+ " || "+ category
        supportActionBar?.subtitle=catgModifier
        // set up font size
        val fonSize = getSharedPreferences("Seek_pos", MODE_PRIVATE).getInt("Seek_pos",0)
        val tv = this.findViewById<ConstraintLayout>(R.id.cl_questionLayout)
        var x: Float = fonSize / 4.0f
        x += 15.0f
        setFontSize(tv,x)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.question_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.fontSize -> {
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
            popupWindow.showAtLocation(this.findViewById(R.id.btn_prev), Gravity.CENTER, 0, 0)
           val fonSize = getSharedPreferences("Seek_pos", MODE_PRIVATE).getInt("Seek_pos",0)

            // dismiss the popup window when touched
            val seek = popupView.findViewById<SeekBar>(R.id.seekBar)
            seek.progress = fonSize
            seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                    val tv = this@Questions.findViewById<ConstraintLayout>(R.id.cl_questionLayout)
                    var x: Float = p1 / 4.0f
                    x += 15.0f
                   setFontSize(tv,x)
                    getSharedPreferences("Seek_pos", MODE_PRIVATE).edit().putInt("Seek_pos",seek.progress).apply()
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {

                }

                override fun onStopTrackingTouch(p0: SeekBar?) {

                }

            })
            // dismiss the popup window when touched
            popupView.setOnTouchListener(object : View.OnTouchListener {
                override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                    popupWindow.dismiss()
                    return true
                }
            })
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }

    }
    fun setFontSize(group:ViewGroup,s:Float) {
        for (c in group.children) {
            if (c is TextView) {
                c.textSize = s
            } else if (c is ViewGroup) {

                setFontSize(c, s)
            }
        }
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
            saveAnswer(true)
        } else{
            this.findViewById<TextView>(R.id.tv_percent).text ="Correct = "+calculatePercent(false).toString() +" %"
            tvCorrect.setTextColor(getColor(android.R.color.holo_red_dark))
            tvCorrect.text="Wrong !"
            saveAnswer(false)
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
        //val tvIndicator = this.findViewById<TextView>(R.id.tv_indicator)
        val imQuest = this.findViewById<ImageView>(R.id.im_quest)
        val imExplain = this.findViewById<ImageView>(R.id.im_explain)
        radioGroup.clearCheck()
        colorIt()
         val thisQuest = questList[showCounter]
        tvQuest.text = thisQuest.question
        radio1.text = thisQuest.op1
       // radio1.layoutDirection = 2
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
        loadImage(imQuest,thisQuest.img)
        loadImage(imExplain,thisQuest.expImg)
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

                }else if (a.text.toString() == questList[showCounter].Answer && ! questList[showCounter].check(a.text.toString()) ) {
                    a.setBackgroundResource(R.drawable.wrong)

                }else{
                    a.setBackgroundResource(0)
                }
            }
        val tvExplain = this.findViewById<TextView>(R.id.tv_explain)
        val imExplain = this.findViewById<ImageView>(R.id.im_explain)
        tvExplain.isVisible = questList[showCounter].Answer != ""
        imExplain.isVisible = questList[showCounter].Answer != ""
    }
    fun loadQuest(tableName:String,catg:String):Boolean{
        TableName = tableName
        category = catg
        var sql = "SELECT * FROM `$tableName`"
        val db:SQLiteDatabase = SQLiteDatabase.openDatabase(MainActivity.sqlitePath,null,MODE_PRIVATE)

        var query:Cursor
        val sql2 = "SELECT `TableInfo` FROM $TableName WHERE `ID` = 1"
        query= db.rawQuery(sql2,null)
        query.moveToFirst()
        tableInfo = query.getString(0)
        if(catg!="All Questions")  sql+= " WHERE `catg` = '$catg'"

        if (MainActivity.wrongly){
            catgModifier ="wrongly answered"
            if (catg == "All Questions"){
                sql += " WHERE solved = 1"
            }else {
                sql += " AND solved = 1"
            }
        } else if (MainActivity.unAttemp) {
            catgModifier = "Only unattempted"
            if (catg == "All Questions") {
                sql += " WHERE solved = 0"
            } else {
                sql += " AND solved = 0"
            }

        }

            showCounter = MainActivity.proseInfo(tableInfo, "$category||$catgModifier").toIntOrNull()?:0



         query = db.rawQuery(sql,null)
        query.moveToFirst()
       if (query.count == 0) {
           //this.finishActivity(0)
           finish()
           return false
       }
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
        if (showCounter > questList.size-1) showCounter = questList.size-1
        query.close()
        db.close()
        return true
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
       val db:SQLiteDatabase = SQLiteDatabase.openDatabase(MainActivity.sqlitePath,null,MODE_PRIVATE)
        db.execSQL(sql)
        db.close()
    }
    fun saveProgress(){
       val db:SQLiteDatabase = SQLiteDatabase.openDatabase(MainActivity.sqlitePath,null,MODE_PRIVATE)
        tableInfo = MainActivity.proseInfo(tableInfo,category+"||"+catgModifier,showCounter.toString())
        val sql = "UPDATE `$TableName` SET TableInfo = '$tableInfo' WHERE ID = 1"
        db.execSQL(sql)
        db.close()
    }
    fun loadImage(imView:ImageView,imString:String){
        if (imString==""){
            imView.setImageBitmap(null)
            return
        }
        var byt = Base64.decode(imString, Base64.DEFAULT)
        //Glide.with(this).load(byt).into(imQuest)
        var bm = BitmapFactory.decodeByteArray(byt,0,byt.size)

        if(bm.width < imFactor && bm.width > 30){

            val h = (imFactor.toDouble()/bm.width.toDouble()).roundToInt()* bm.height
            val w = imFactor
            Glide.with(this).load(bm).apply(RequestOptions().override(w, h)).into(imView)
        }else{
            imView.setImageBitmap(bm)
        }


    }
}