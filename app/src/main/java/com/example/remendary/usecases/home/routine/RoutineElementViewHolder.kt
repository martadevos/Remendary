package com.example.remendary.usecases.home.routine
import android.content.ContentValues.TAG
import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.RoutineElement
import com.example.remendary.model.domain.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RoutineElementViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {

    private val db = Firebase.firestore
    fun render(rElement: RoutineElement, currentUserUsername: String) {
        val nameTxt: TextView = view.findViewById(R.id.routineElementNameTxt)
        val doneChkBox: CheckBox = view.findViewById(R.id.routineElementDoneChkBox)

        nameTxt.text = rElement.name
        doneChkBox.isChecked = rElement.done
        doneChkBox.setOnCheckedChangeListener { _, checked ->
            updateRoutineElement(
                checked,
                currentUserUsername,
                rElement
            )
        }
    }

    private fun updateRoutineElement(checked: Boolean, currentUserUsername: String, rElement: RoutineElement) {
        val taskDB = db.collection("users")
            .document(currentUserUsername)
            .collection("routine")
            .document(rElement.name)
        taskDB.update("done", checked)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully updated!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error updating document", e) }
    }
}