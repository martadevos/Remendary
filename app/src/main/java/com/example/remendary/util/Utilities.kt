package com.example.remendary.util

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import com.example.remendary.model.domain.Event
import com.example.remendary.model.domain.RoutineElement
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.FirebaseUtils.firebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase


object Utilities {
    @SuppressLint("StaticFieldLeak")
    private val db = Firebase.firestore

    fun getCurrentUserInfo():User{
        var username = ""
        var useremail = ""
        //Get current user username and email
        firebaseUser.let {
            if (it != null) {
                for (profile in it.providerData) {
                    username = profile.displayName.toString()
                    useremail = profile.email.toString()
                }
            }
        }
        val userDoc: DocumentReference = db.collection("users").document(username)

        //Create and fill list of tasks
        var tasks: ArrayList<Task>? = null
        userDoc.collection("tasks").get().
        addOnSuccessListener{ collection ->
            if (!collection.isEmpty) {
                tasks = ArrayList()
                collection.forEach {documentSnapshot ->
                    tasks!!.add(documentSnapshot.toObject())
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }

        //Create and fill list of events
        var events: ArrayList<Event>? = null
        userDoc.collection("events").get().
        addOnSuccessListener{ collection ->
            events = ArrayList()
            if (!collection.isEmpty) {
                collection.forEach {documentSnapshot ->
                    events!!.add(documentSnapshot.toObject())
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }

        //Create and fill list of routine elements
        var routine: ArrayList<RoutineElement>? = null
        userDoc.collection("routine").get().
        addOnSuccessListener{ collection ->
            if (!collection.isEmpty) {
                routine = ArrayList()
                collection.forEach {documentSnapshot ->
                    routine!!.add(documentSnapshot.toObject())
                }
            } else {
                Log.d(TAG, "No such document")
            }
        }

        //Create and return user
        return User(username, useremail, tasks, events, routine)
    }

}
