package com.abudreas.qviewer

class Category {
    var name: String
    var total: Int
    var attempted: Int
    var correct: String
    var continueIt: Boolean
        get() = field        // getter
        set(value) {         // setter
            field = value
        }

    constructor(name: String, total: Int, attempted: Int, correct: String, continueIt: Boolean) {
        this.name = name
        this.total = total
        this.attempted = attempted
        this.correct = correct
        this.continueIt = continueIt
    }

}