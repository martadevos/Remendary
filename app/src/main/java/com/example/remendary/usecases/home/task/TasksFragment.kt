package com.example.remendary.usecases.home.task

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking

class TasksFragment : Fragment() {
    private val db = Firebase.firestore

    private lateinit var currentUser: User
    private lateinit var rvTasks: RecyclerView
    private lateinit var newTaskBtn: Button
    private lateinit var emptyTasksTv: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireView().tag = "taskFragment"
        emptyTasksTv = requireView().findViewById(R.id.emptyTaskstv)
        rvTasks = requireView().findViewById(R.id.tasksrv)
        newTaskBtn = requireView().findViewById(R.id.newTaskbtn)
        newTaskBtn.setOnClickListener { createNewTask() }
        changeVisibilities()
    }

    private fun changeVisibilities() {
        if (!currentUser.tasks.isNullOrEmpty()) {
            initRecyclerView()
            emptyTasksTv.visibility = GONE
            rvTasks.visibility = VISIBLE
        }
        else{
            emptyTasksTv.visibility = VISIBLE
            rvTasks.visibility = GONE
        }
    }


    private fun initRecyclerView() {
        rvTasks.layoutManager = LinearLayoutManager(requireContext())
        rvTasks.isClickable = true

        val taskAdapter = TaskAdapter(currentUser.tasks!!, currentUser.username)
        rvTasks.adapter = taskAdapter
        taskAdapter.setOnClickListener(object :
            TaskAdapter.OnClickListener {
            override fun onClick(position: Int, model: Task) {
                rvTasks.isClickable = false
                val dialog = CreateEditTaskFragment(2, currentUser.tasks!![position])
                dialog.show(parentFragmentManager, "createTaskDialog")
            }
        })
    }

    private fun createNewTask() {
        val dialog = CreateEditTaskFragment(1, null)
        dialog.show(parentFragmentManager, "createTaskDialog")

    }
}