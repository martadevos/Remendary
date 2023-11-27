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
import android.widget.CompoundButton
import android.widget.ListView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class TasksFragment : Fragment() {
    private val db = Firebase.firestore

    private lateinit var currentUser: User
    private lateinit var rvTasks: RecyclerView
    private lateinit var newTaskBtn: Button
    private var tasksList: ArrayList<Task>? = null
    private var lyt: Int = R.layout.fragment_tasks

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }
        tasksList = currentUser.tasks
        if (tasksList.isNullOrEmpty()) {
            lyt = R.layout.fragment_tasks_empty
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(lyt, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireView().tag = "taskFragment"
        newTaskBtn = requireView().findViewById(R.id.newTaskbtn)
        newTaskBtn.setOnClickListener { createNewTask() }
        if (!tasksList.isNullOrEmpty()) {
            initRecyclerView()
        }
    }


    private fun initRecyclerView() {
        rvTasks = requireView().findViewById(R.id.tasksrv)
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.isClickable = true
        rvTasks.adapter = TaskAdapter(tasksList!!)
        /*val taskItems = lvTasks.findViewById<ListView>(R.id.taskItem)
        var checkbox = taskItems.findViewById<CheckBox>(R.id.chkbxDone)
            .setOnCheckedChangeListener { checkbox, checked -> updateTask(checkbox, checked) }*/
    }

    private fun updateTask(checkbox: CompoundButton, checked: Boolean) {
        /*checkbox.
        var task: Task =
        db.collection("users").document(currentUser.username).collection("tasks").document(task.name)*/

    }

    private fun createNewTask() {
        val dialog = CreateEditTaskFragment()
        dialog.show(parentFragmentManager, "createTaskDialog")

    }

}