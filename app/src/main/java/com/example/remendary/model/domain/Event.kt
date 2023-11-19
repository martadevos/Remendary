package com.example.remendary.model.domain

import com.google.type.DateTime

data class Event(val dateTime: DateTime, val name: String, val description: String = "")