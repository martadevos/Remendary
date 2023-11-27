package com.example.remendary.model.domain

import java.time.LocalDateTime

data class Event(val dateTime: LocalDateTime, val name: String, val description: String = "")