package com.example.remendary.usecases.home.routine

import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.RoutineElement
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RoutineElementAdapter(private val arrayList: ArrayList<RoutineElement>, private val currentUserUsername: String) :
    RecyclerView.Adapter<RoutineElementViewHolder>() {

    private lateinit var fragment: View

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineElementViewHolder {
        fragment = parent.rootView
        val layoutInflater: LayoutInflater = LayoutInflater.from(parent.context)
        return RoutineElementViewHolder(layoutInflater.inflate(R.layout.routine_element, parent, false))
    }

    override fun onBindViewHolder(holder: RoutineElementViewHolder, position: Int) {
        val item = arrayList[position]
        holder.render(item, currentUserUsername)
        holder.itemView.findViewById<ImageButton>(R.id.deleteRoutineElementBtn).setOnClickListener {
            removeRoutineElement(item)
        }
    }

    private fun removeRoutineElement(rElement: RoutineElement) {
        val db = Firebase.firestore
        db.collection("users")
            .document(currentUserUsername)
            .collection("tasks")
            .document(rElement.name)
            .delete()
        val position = arrayList.indexOf(rElement)
        arrayList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        if (arrayList.size<1) {
            fragment.findViewById<RecyclerView>(R.id.routineElementsRV).visibility = GONE
            fragment.findViewById<TextView>(R.id.emptyRoutinetv).visibility = VISIBLE
        }
    }

    override fun getItemCount(): Int = arrayList.size


}