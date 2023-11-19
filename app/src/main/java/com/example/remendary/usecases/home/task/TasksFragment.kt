package com.example.remendary.usecases.home.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities

class TasksFragment : Fragment() {
    private lateinit var currentUser: User
    private lateinit var lvHigh: ListView
    private lateinit var lvMedium: ListView
    private lateinit var lvLow: ListView
    private var listHigh: ArrayList<Task> = ArrayList()
    private var listMedium: ArrayList<Task> = ArrayList()
    private var listLow: ArrayList<Task> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Utilities.getCurrentUserInfo()
        if (!currentUser.tasks.isNullOrEmpty()){
            sortTasksByPriority()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tasks, container, false)
    }

    @Override
    public fun onViewCreated(view: View, savedInstanceState: Bundle) {
        lvHigh = view.findViewById(R.id.highPriorityLV)
        lvMedium = view.findViewById(R.id.mediumPriorityLV)
        lvLow = view.findViewById(R.id.lowPriorityLV)
    }

    private fun sortTasksByPriority(){
        for (task in currentUser.tasks!!){
            when{
                task.priority == Task.PriorityValues.HIGH -> listHigh.add(task)
                task.priority == Task.PriorityValues.MEDIUM -> listMedium.add(task)
                task.priority == Task.PriorityValues.LOW -> listLow.add(task)
            }
        }
    }

}