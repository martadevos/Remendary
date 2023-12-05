package com.example.remendary.model.domain

data class User(val username: String, val email: String, var water: Int = 0, var tasks: ArrayList<Task>? = null, var events: ArrayList<Event>? = null, var routine: ArrayList<RoutineElement>? = null)

