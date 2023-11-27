package com.example.remendary.util

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.remendary.model.domain.Event
import com.example.remendary.model.domain.RoutineElement
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.FirebaseUtils.firebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime


object Utilities {
    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCurrentUserInfo(): User {
        var username = ""
        var useremail = ""
        //Get current user username and email
        firebaseUser.let {
            if (it != null) {
                for (profile in it.providerData) {
                    username = profile.displayName.toString().trim()
                    useremail = profile.email.toString().trim()
                }
            }
        }
        val userDoc: DocumentReference = db.collection("users").document(username)

        //Create and fill list of tasks
        val tasks: ArrayList<Task> = arrayListOf()
        userDoc.collection("tasks").get().await().forEach { document ->
            val priority: Task.PriorityValues = when (document["priority"].toString().trim()) {
                "HIGH" -> Task.PriorityValues.HIGH
                "MEDIUM" -> Task.PriorityValues.MEDIUM
                else -> Task.PriorityValues.LOW
            }
            tasks.add(
                Task(
                    document["name"].toString().trim(),
                    document["description"].toString().trim(),
                    document["done"] as Boolean,
                    priority
                )
            )
        }

        //Create and fill list of events
        val events: ArrayList<Event> = arrayListOf()
        userDoc.collection("events").get().await().forEach { document ->
            events.add(
                Event(
                    LocalDateTime.parse(
                        document["dateTime"].toString()
                    ),
                    document["name"].toString().trim(),
                    document["description"].toString().trim()
                )
            )
        }

        //Create and fill list of routine elements
        val routine: ArrayList<RoutineElement> = arrayListOf()
        userDoc.collection("routine").get().await().forEach { document ->
            routine.add(
                RoutineElement(
                    document["name"].toString().trim(),
                    document["done"] as Boolean
                )
            )
        }

        //Create and return user
        return User(username, useremail, tasks, events, routine)
    }

}
