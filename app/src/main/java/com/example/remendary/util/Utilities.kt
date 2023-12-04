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
import java.lang.Exception
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
        //val userDoc: DocumentReference = db.collection("users").document(username)

        //Create and fill list of tasks

        var tasks: ArrayList<Task>? = arrayListOf()
        try {
            db.collection("users").document(username).collection("tasks").get().await().forEach { document ->
                val priority: Task.PriorityValues = when (document["priority"].toString().trim()) {
                    "HIGH" -> Task.PriorityValues.HIGH
                    "MEDIUM" -> Task.PriorityValues.MEDIUM
                    else -> Task.PriorityValues.LOW
                }
                tasks!!.add(
                    Task(
                        document["name"].toString().trim(),
                        document["description"].toString().trim(),
                        document["done"] as Boolean,
                        priority
                    )
                )
            }
        } catch (e: Exception) {
            tasks = null
        }

        //Create and fill list of events
        var events: ArrayList<Event>? = arrayListOf()
        try {
            db.collection("users").document(username).collection("events").get().await().forEach { document ->
                events!!.add(
                    Event(
                        document["dateTime"].toString().trim(),
                        document["name"].toString().trim()
                    )
                )
            }
        } catch (e: Exception) {
            events = null
        }

        //Create and fill list of routine elements
        var routine: ArrayList<RoutineElement>? = arrayListOf()
        try {
            db.collection("users").document(username).collection("routine").get().await().forEach { document ->
                routine!!.add(
                    RoutineElement(
                        document["name"].toString().trim(),
                        document["done"] as Boolean
                    )
                )
            }
        } catch (e: Exception) {
            routine = null
        }

        //Create and return user
        return User(username, useremail, tasks, events, routine)
    }

}
