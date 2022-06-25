package com.abudreas.qviewer

class Quest(var id :Int,
            var question:String,
            var op1 :String,
            var op2 :String,
            var op3 :String,
            var op4 :String,
            var op5 :String,
            var correct:String,
            var explain:String
            ) {
var Answer = ""
    var img =""
    var expImg = ""


    fun check(answer:String):Boolean{
        if (answer==this.correct){
            return true
        }
        return  false
    }
}