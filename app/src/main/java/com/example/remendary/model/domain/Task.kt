package com.example.remendary.model.domain

data class Task(val name: String, val description: String = "", val done: Boolean, val priority: PriorityValues){
    enum class PriorityValues{
        LOW, MEDIUM, HIGH
    }
}