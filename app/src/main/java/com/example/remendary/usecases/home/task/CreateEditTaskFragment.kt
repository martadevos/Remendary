package com.example.remendary.usecases.home.task

import android.content.ContentValues
import android.os.Build
import android.os.Bundle
import android.text.Editable
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
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await


class CreateEditTaskFragment(val tipo: Int, val taskToEdit: Task?) : DialogFragment() {


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
        btnBack.setOnClickListener { this.dismiss() }

        //Spinner for selecting the priority of the task
        spinnerPriority = requireView().findViewById(R.id.spinnerPriority)
        spinnerAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.priority_values,
            android.R.layout.simple_spinner_item
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = spinnerAdapter

        if (taskToEdit != null) {
            showTaskInfo()
        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveTask() {
        val exit = when (tipo) {
            1 -> createNewTask()
            2 -> editTask()
            else -> {
                true
            }
        }

        if (exit) {
            backToTaskFragment()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun backToTaskFragment() {
        val taskFragment =
            activity?.supportFragmentManager?.fragments?.find { fragment -> fragment.requireView().tag!! == "taskFragment" }

        val rvTasks = taskFragment?.view?.findViewById<RecyclerView>(R.id.tasksrv)
        val emptyTasksTv = taskFragment?.view?.findViewById<TextView>(R.id.emptyTaskstv)

        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }

        if (!currentUser.tasks.isNullOrEmpty()) {
            emptyTasksTv?.visibility = View.GONE
            rvTasks?.visibility = View.VISIBLE

            rvTasks!!.layoutManager = LinearLayoutManager(taskFragment.requireContext())
            rvTasks.isClickable = true

            val taskAdapter = TaskAdapter(currentUser.tasks!!, currentUser.username)
            rvTasks.adapter = taskAdapter
            taskAdapter.setOnClickListener(object :
                TaskAdapter.OnClickListener {
                override fun onClick(position: Int, model: Task) {
                    rvTasks.isClickable = false
                    val dialog = CreateEditTaskFragment(2, currentUser.tasks!![position])
                    dialog.show(taskFragment.parentFragmentManager, "createTaskDialog")
                }
            })
        } else {
            emptyTasksTv?.visibility = View.VISIBLE
            rvTasks?.visibility = View.GONE
        }
        this.dismiss()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNewTask(): Boolean {
        val exit: Boolean
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
            exit = true

        } else {
            etTaskName.error = "Task name is required"
            exit = false
        }
        return exit
    }

    private fun showTaskInfo() {
        etTaskName.isEnabled = false
        etTaskName.setText(taskToEdit!!.name)
        spinnerPriority.setSelection(
            when (taskToEdit.priority) {
                Task.PriorityValues.LOW -> 0
                Task.PriorityValues.MEDIUM -> 1
                Task.PriorityValues.HIGH -> 2
            }
        )
        etDescription.setText(taskToEdit.description)
        chkbxDone.isChecked = taskToEdit.done
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun editTask(): Boolean {
        val exit: Boolean
        if (etTaskName.text.toString().trim().isNotEmpty() && (etDescription.text.toString().trim()
                .isNotEmpty() ||
                    (etDescription.text.toString().trim()
                        .isEmpty() && taskToEdit!!.description.isEmpty()))
        ) {

            val priority: Task.PriorityValues = when (spinnerPriority.selectedItem.toString()) {
                "Baja" -> Task.PriorityValues.LOW
                "Media" -> Task.PriorityValues.MEDIUM
                "Alta" -> Task.PriorityValues.HIGH
                else -> {
                    Task.PriorityValues.LOW
                }
            }
            taskToEdit!!.name = etTaskName.text.toString().trim()
            taskToEdit.description = etDescription.text.toString().trim()
            taskToEdit.priority = priority
            taskToEdit.done = chkbxDone.isChecked

            db.collection("users").document(currentUser.username).collection("tasks")
                .document(taskToEdit.name).update(
                    mapOf(
                        "name" to taskToEdit.name,
                        "description" to taskToEdit.description,
                        "done" to taskToEdit.done,
                        "priority" to taskToEdit.priority
                    ),
                )
            exit = true
        } else {
            etTaskName.error = "Task name is required"
            exit = false
        }
        return exit
    }

}
