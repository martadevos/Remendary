package com.example.remendary.usecases.home.task

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ListView
import android.widget.Spinner
import androidx.annotation.RequiresApi
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking



class CreateEditTaskFragment : DialogFragment() {


    private val db = Firebase.firestore

    private lateinit var task: Task
    private lateinit var currentUser: User
    private lateinit var etTaskName: EditText
    private lateinit var etDescription: EditText
    private lateinit var chkbxDone: CheckBox
    private lateinit var btnCreateTask: Button
    private lateinit var btnBack: Button
    private lateinit var spinnerPriority: Spinner
    private lateinit var spinnerAdapter: ArrayAdapter<CharSequence>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_edit_task, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etDescription = requireView().findViewById(R.id.etTaskDescription)
        chkbxDone = requireView().findViewById(R.id.chkbxDone)
        etTaskName = requireView().findViewById(R.id.etTaskName)


        btnCreateTask = requireView().findViewById(R.id.btnCreateTask)
        btnCreateTask.setOnClickListener { saveTask() }
        btnBack = requireView().findViewById(R.id.btnBack)
        btnBack.setOnClickListener{this.dismiss()}

        //Spinner for selecting the priority of the task
        spinnerPriority = requireView().findViewById(R.id.spinnerPriority)
        spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_values,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = spinnerAdapter


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {

        if (etTaskName.text.toString().trim().isNotEmpty()) {

            val priority: Task.PriorityValues = when (spinnerPriority.selectedItem.toString()) {
                "Baja" -> Task.PriorityValues.LOW
                "Media" -> Task.PriorityValues.MEDIUM
                "Alta" -> Task.PriorityValues.HIGH
                else -> {
                    Task.PriorityValues.LOW
                }
            }

            task = Task(
                etTaskName.text.toString().trim(),
                etDescription.text.toString().trim(),
                chkbxDone.isChecked,
                priority
            )

            val taskDB = hashMapOf(
                "name" to task.name,
                "description" to task.description,
                "done" to task.done,
                "priority" to task.priority
            )

            db.collection("users").document(currentUser.username).collection("tasks")
                .document(task.name).set(taskDB)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "DocumentSnapshot added with ID: ${task.name}")
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error adding document", e)
                }
            val taskFragment= activity?.supportFragmentManager?.fragments?.find{ fragment -> fragment.requireView().tag!! == "taskFragment" }


            val rvTask = taskFragment?.view?.findViewById<RecyclerView>(R.id.tasksrv)
            rvTask!!.isClickable = true

            runBlocking {
                currentUser = Utilities.getCurrentUserInfo()
            }

            rvTask.adapter = TaskAdapter(currentUser.tasks!!)
            this.dismiss()

        } else {
            etTaskName.error = "Task name is required"
        }

    }
}
