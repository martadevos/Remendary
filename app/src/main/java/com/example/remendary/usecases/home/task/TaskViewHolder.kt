package com.example.remendary.usecases.home.task

import android.graphics.Color
import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.Task

class TaskViewHolder(private val view: View): RecyclerView.ViewHolder(view) {

    fun render(task: Task){
        val nameTxt: TextView = view.findViewById(R.id.taskItemNameTxt)
        val descriptionTxt: TextView = view.findViewById(R.id.taskItemDescriptionTxt)
        val doneChkBox: CheckBox = view.findViewById(R.id.taskItemDoneChkBox)
        val taskItemView: CardView = view.findViewById(R.id.taskItem)

        nameTxt.text = task.name
        descriptionTxt.text = task.description
        doneChkBox.isChecked = task.done
        when(task.priority){
            Task.PriorityValues.HIGH ->{
                nameTxt.setTextColor(Color.WHITE)
                descriptionTxt.setTextColor(Color.WHITE)
                taskItemView.setCardBackgroundColor(Color.RED)
            }
            Task.PriorityValues.MEDIUM ->{
                nameTxt.setTextColor(Color.WHITE)
                descriptionTxt.setTextColor(Color.WHITE)
                taskItemView.setCardBackgroundColor(Color.YELLOW)
            }
            Task.PriorityValues.LOW ->{
                nameTxt.setTextColor(Color.WHITE)
                descriptionTxt.setTextColor(Color.WHITE)
                taskItemView.setCardBackgroundColor(Color.GREEN)
            }
        }
    }
}
