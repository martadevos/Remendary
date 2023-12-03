package com.example.remendary.model.domain

data class Task(var name: String = "", var description: String = "", var done: Boolean, var priority: PriorityValues){
    enum class PriorityValues{
        LOW, MEDIUM, HIGH
    }
}