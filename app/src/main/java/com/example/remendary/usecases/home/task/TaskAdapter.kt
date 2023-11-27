package com.example.remendary.usecases.home.task

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task

class TaskAdapter(private val arrayList: ArrayList<Task>): RecyclerView.Adapter<TaskViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(layoutInflater.inflate(R.layout.task_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = arrayList[position]
        holder.render(item)
    }

    override fun getItemCount(): Int = arrayList.size
}