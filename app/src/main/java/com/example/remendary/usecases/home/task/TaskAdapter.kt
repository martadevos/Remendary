package com.example.remendary.usecases.home.task

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TaskAdapter(private val arrayList: ArrayList<Task>, private val currentUserUsername: String) :
    RecyclerView.Adapter<TaskViewHolder>() {

    private var onClickListener: OnClickListener? = null
    private lateinit var fragment: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        fragment = parent.rootView
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(layoutInflater.inflate(R.layout.task_list_item, parent, false))
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val item = arrayList[position]
        holder.render(item, currentUserUsername)
        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, item)
            }
        }
        holder.itemView.findViewById<ImageButton>(R.id.deleteTaskBtn).setOnClickListener {
            removeTask(item)
        }
    }

    private fun removeTask(task: Task) {
        val db = Firebase.firestore
        db.collection("users")
            .document(currentUserUsername)
            .collection("tasks")
            .document(task.name)
            .delete()
        val position = arrayList.indexOf(task)
        arrayList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        if (arrayList.size<1) {
            fragment.findViewById<RecyclerView>(R.id.tasksrv).visibility = GONE
            fragment.findViewById<TextView>(R.id.emptyTaskstv).visibility = VISIBLE
        }
    }

    override fun getItemCount(): Int = arrayList.size

    // A function to bind the onclickListener.
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    // onClickListener Interface
    interface OnClickListener {
        fun onClick(position: Int, model: Task)
    }

}