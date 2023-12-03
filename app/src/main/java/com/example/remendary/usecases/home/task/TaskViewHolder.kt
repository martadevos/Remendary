package com.example.remendary.usecases.home.task

import android.content.ContentValues.TAG
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task
import com.example.remendary.model.domain.User
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class TaskViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val db = Firebase.firestore
    fun render(task: Task, currentUserUsername: String) {
        val nameTxt: TextView = view.findViewById(R.id.taskItemNameTxt)
        val descriptionTxt: TextView = view.findViewById(R.id.taskItemDescriptionTxt)
        val doneChkBox: CheckBox = view.findViewById(R.id.taskItemDoneChkBox)
        val taskItemView: CardView = view.findViewById(R.id.taskItem)
        val deleteTaskBtn: ImageButton = view.findViewById(R.id.deleteTaskBtn)

        nameTxt.text = task.name
        if (task.description.isEmpty()){
            descriptionTxt.visibility = GONE
        }else {
            descriptionTxt.visibility = VISIBLE
            descriptionTxt.text = task.description
        }
        doneChkBox.isChecked = task.done
        doneChkBox.setOnCheckedChangeListener { _, checked ->
            updateTask(
                checked,
                currentUserUsername,
                task
            )
        }
        when (task.priority) {
            Task.PriorityValues.HIGH -> {
                nameTxt.setTextColor(Color.WHITE)
                descriptionTxt.setTextColor(Color.WHITE)
                taskItemView.setCardBackgroundColor(Color.RED)
            }

            Task.PriorityValues.MEDIUM -> {
                nameTxt.setTextColor(Color.WHITE)
                descriptionTxt.setTextColor(Color.WHITE)
                taskItemView.setCardBackgroundColor(Color.YELLOW)
            }

            Task.PriorityValues.LOW -> {
                nameTxt.setTextColor(Color.WHITE)
                descriptionTxt.setTextColor(Color.WHITE)
                taskItemView.setCardBackgroundColor(Color.GREEN)
            }
        }
    }

    private fun updateTask(checked: Boolean, currentUserUsername: String, task: Task) {
        val taskDB = db.collection("users")
            .document(currentUserUsername)
            .collection("tasks")
            .document(task.name)
        taskDB.update("done", checked)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }
}
