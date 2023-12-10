package com.example.remendary.util

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.remendary.model.domain.Event
import com.example.remendary.model.domain.RoutineElement
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.FirebaseUtils.firebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.lang.Exception
import java.time.LocalDateTime
import java.util.Calendar


object Utilities {
    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore


    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getCurrentUserInfo(): User {
        var username = ""
        var useremail = ""
        //Get current user username and email
        Firebase.auth.currentUser?.let {
            username = it.displayName.toString().trim()
            useremail = it.email.toString().trim()
        }
        //Create and fill list of tasks

        var tasks: ArrayList<Task>? = arrayListOf()
        try {
            db.collection("users").document(username).collection("tasks").get().await()
                .forEach { document ->
                    val priority: Task.PriorityValues =
                        when (document["priority"].toString().trim()) {
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
            db.collection("users").document(username).collection("events").get().await()
                .forEach { document ->
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
            db.collection("users").document(username).collection("routine").get().await()
                .forEach { document ->
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
        var user = User(username, useremail, 0)

        db.collection("users").document(username).get().addOnSuccessListener{ document ->
            user = User(document["username"] as String, document["email"] as String, (document["water"] as Long).toInt())
        }.await()

        user.tasks = tasks
        user.events = events
        user.routine = routine

        return user
    }

}
