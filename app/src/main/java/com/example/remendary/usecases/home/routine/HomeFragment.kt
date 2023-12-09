package com.example.remendary.usecases.home.routine

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.remendary.R
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking


class HomeFragment : Fragment() {
    private val db = Firebase.firestore
    private lateinit var currentUser: User
    private lateinit var routineElementsRV: RecyclerView
    private lateinit var newElementBtn: FloatingActionButton
    private var waterBtnIdList: MutableMap<Int, Boolean> = mutableMapOf(
        R.id.drop1 to false,
        R.id.drop2 to false,
        R.id.drop3 to false,
        R.id.drop4 to false,
        R.id.drop5 to false,
        R.id.drop6 to false,
        R.id.drop7 to false,
        R.id.drop8 to false,
    )
    private lateinit var emptyRoutinetv: TextView
    private lateinit var waterCountTv: TextView
    private var watercount: Int = 0

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
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        requireView().tag = "taskFragment"
        emptyRoutinetv = requireView().findViewById(R.id.emptyRoutinetv)
        routineElementsRV = requireView().findViewById(R.id.routineElementsRV)
        waterCountTv = requireView().findViewById(R.id.waterCountTv)
        waterBtnIdList.forEach {
            val waterBtn: ImageButton = requireView().findViewById<ImageButton>(it.key)
            waterBtn.setOnClickListener { _ ->
                waterBtn.isClickable = false
                if (waterBtnIdList[it.key] == false && watercount <= 8) {
                    waterBtn.setImageResource(R.drawable.baseline_water_drop_filled_48)
                    watercount++
                    db.collection("users").document(currentUser.username)
                        .update("water", FieldValue.increment(1))
                } else if (waterBtnIdList[it.key] == true && watercount >= 0) {
                    waterBtn.setImageResource(R.drawable.twotone_water_drop_empty_48)
                    watercount--
                    db.collection("users").document(currentUser.username)
                        .update("water", FieldValue.increment(-1))
                }
                waterBtnIdList[it.key] = !waterBtnIdList[it.key]!!
                val waterText = "%.2f".format(0.25.times(watercount)) + "/2.00L"
                waterCountTv.text = (waterText)
                waterBtn.isClickable = true
            }
        }
        newElementBtn = requireView().findViewById(R.id.newElementBtn)
        newElementBtn.setOnClickListener { showAddEventDialog() }
        changeVisibilities()
    }

    private fun changeVisibilities() {
        if (!currentUser.routine.isNullOrEmpty()) {
            initRecyclerView()
            emptyRoutinetv.visibility = View.GONE
            routineElementsRV.visibility = View.VISIBLE
        } else {
            emptyRoutinetv.visibility = View.VISIBLE
            routineElementsRV.visibility = View.GONE
        }
    }

    private fun initRecyclerView() {
        routineElementsRV.layoutManager = LinearLayoutManager(requireContext())
        routineElementsRV.isClickable = true

        val routineElementAdapter =
            RoutineElementAdapter(currentUser.routine!!, currentUser.username)
        routineElementsRV.adapter = routineElementAdapter
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun showAddEventDialog() {
        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }
        val view = layoutInflater.inflate(R.layout.create_routine_element, null)
        val name = view.findViewById<EditText>(R.id.routineNameEt)
        val addBtn = view.findViewById<ImageButton>(R.id.addRoutineElementbtn)
        val dialog = MaterialAlertDialogBuilder(requireContext()).setCancelable(true)
            .setView(view).show()
        addBtn.setOnClickListener {
            if (!name.text.toString().isNullOrEmpty()) {
                //Guardar nuevo evento
                val routineElement = hashMapOf(
                    "name" to name.text.toString().trim(),
                    "done" to false
                )
                db.collection("users").document(currentUser.username).collection("routine")
                    .document(name.text.toString()).set(routineElement)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Added Correctly", Toast.LENGTH_SHORT)
                            .show()
                        runBlocking {
                            currentUser = Utilities.getCurrentUserInfo()
                        }
                        val routineElementAdapter =
                            RoutineElementAdapter(currentUser.routine!!, currentUser.username)
                        routineElementsRV.adapter = routineElementAdapter
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Oops Something went wrong :(",
                            Toast.LENGTH_SHORT
                        ).show()

                    }

                dialog.dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Name can't be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}