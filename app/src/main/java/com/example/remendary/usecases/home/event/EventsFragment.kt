package com.example.remendary.usecases.home.event

import android.content.ContentValues
import android.content.Context
import android.opengl.Visibility
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.remendary.R
import com.example.remendary.model.domain.Event
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.type.DateTime
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [EventsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@RequiresApi(Build.VERSION_CODES.O)
class EventsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    lateinit var calendar: LocalDateTime
    private val db = Firebase.firestore
    private lateinit var currentUser: User

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

        return inflater.inflate(R.layout.fragment_events, container, false)
    }


    fun showEventDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_events_lyt, null)
        val addReminderBtn = view.findViewById<Button>(R.id.addReminderBtn)
        val eventsLsvw = view.findViewById<ListView>(R.id.eventsLsvw)
        val noEventsTxtvw=view.findViewById<TextView>(R.id.noEventsTxtvw)
        val eventsOnDateSelected=compareDates()
        if(!eventsOnDateSelected.isNullOrEmpty()){
            noEventsTxtvw.visibility=View.INVISIBLE
            eventsLsvw.adapter = EventsAdapter(requireContext(),ArrayList(eventsOnDateSelected),currentUser,db,calendar)
        }else{
            eventsLsvw.visibility=View.INVISIBLE
            noEventsTxtvw.visibility=View.VISIBLE
        }
        addReminderBtn.setOnClickListener {
            showAddEventDialog()
        }
        MaterialAlertDialogBuilder(requireContext())
            .setView(view).show()


    }

    fun showAddEventDialog(){
        val view = layoutInflater.inflate(R.layout.add_event_layout, null)
        val eventHour=view.findViewById<TimePicker>(R.id.eventHourTmpck)
        val eventName=view.findViewById<EditText>(R.id.eventNameEd)
        val addBtn=view.findViewById<Button>(R.id.addEventBtn)

       val eventDB = hashMapOf(
            "name" to eventName.text.toString(),
            "description" to "",
            "dateTime" to ""//HAY QUE VER EL TIPO QUE NOS VIENE DE FIREBASE
        )
        MaterialAlertDialogBuilder(requireContext())
            .setView(view).show()
        addBtn.setOnClickListener{
            //Guardar nuevo evento
            db.collection("users").document(currentUser.username).collection("events")
                .document(eventName.text.toString()).set(eventDB)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Added Correctly", Toast.LENGTH_SHORT)

                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Oops Something went wrong :(", Toast.LENGTH_SHORT)

                }
        }

            runBlocking {
                currentUser = Utilities.getCurrentUserInfo()
            }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarView = requireView().findViewById<CalendarView>(R.id.calendarVw)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            var dayWellFormated = ""
            if (dayOfMonth < 10) {
                dayWellFormated = "0".plus(dayOfMonth.toString())
            } else {
                dayWellFormated = dayOfMonth.toString()
            }
            calendar = LocalDate.parse(
                dayWellFormated.plus(month).plus(year),
                DateTimeFormatter.ofPattern("ddMMyyyy")
            )
                .atStartOfDay()
            showEventDialog()
        }
    }


    private fun compareDates(): List<Event>? {
        return currentUser.events?.filter { event ->
            event.dateTime.dayOfMonth == calendar.dayOfMonth
                    && event.dateTime.month == calendar.month && event.dateTime.year == calendar.year
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment Events.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventsFragment().apply {
            }
    }

    private class EventsAdapter(
        private val context: Context,
        private var arrayList: java.util.ArrayList<Event>,
        private var currentUser:User,
        private val db: FirebaseFirestore,
        private val calendar: LocalDateTime
    ) : BaseAdapter() {

        override fun getCount(): Int {
            return arrayList.size
        }

        override fun getItem(p0: Int): Any {
            return p0
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            convertView = LayoutInflater.from(context).inflate(R.layout.events_row, parent, false)
            val eventTitle = convertView.findViewById<TextView>(R.id.eventTitle)
            eventTitle.text = arrayList[position].name
            val deleteBtn = convertView.findViewById<Button>(R.id.deleteEventBtn)
            deleteBtn.setOnClickListener {
                db.collection("users").document(currentUser.username).collection("events")
                    .document(eventTitle.text.toString()).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted Succesfully", Toast.LENGTH_SHORT)

                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Oops Something went wrong :(", Toast.LENGTH_SHORT)

                    }
            }
            runBlocking {
                currentUser = Utilities.getCurrentUserInfo()
            }
            arrayList= currentUser.events?.filter { event ->
                event.dateTime.dayOfMonth == calendar.dayOfMonth
                        && event.dateTime.month == calendar.month && event.dateTime.year == calendar.year
            }?.let { ArrayList(it) }!!
            notifyDataSetChanged()
            return convertView
        }

    }
}