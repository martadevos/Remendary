package com.example.remendary.model.domain

data class User(val username: String, val email: String, var tasks: ArrayList<Task>? = null, var events: ArrayList<Event>? = null, var routine: ArrayList<RoutineElement>? = null)

