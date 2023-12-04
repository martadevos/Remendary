package com.example.remendary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.remendary.model.domain.Event
import com.example.remendary.model.domain.User
import com.example.remendary.util.Utilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
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
    lateinit var calendar: String
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
        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }
        val view = layoutInflater.inflate(R.layout.dialog_events_lyt, null)
        val addReminderBtn = view.findViewById<Button>(R.id.addReminderBtn)
        val eventsLsvw = view.findViewById<ListView>(R.id.eventsLsvw)
        val noEventsTxtvw = view.findViewById<TextView>(R.id.noEventsTxtvw)
        var eventsOnDateSelected: List<Event>? = emptyList()
        if (!currentUser.events.isNullOrEmpty() && currentUser.events?.first() != null && !currentUser.events?.first()!!.dateTime.isNullOrEmpty()) {
            eventsOnDateSelected = compareDates()
            if (!eventsOnDateSelected.isNullOrEmpty()) {
                noEventsTxtvw.visibility = View.INVISIBLE
                eventsLsvw.adapter = EventsAdapter(
                    requireContext(),
                    ArrayList(eventsOnDateSelected),
                    currentUser,
                    db,
                    calendar,
                    noEventsTxtvw
                )
            } else {
                showNoEvents(eventsLsvw, noEventsTxtvw)
            }
        } else {
            showNoEvents(eventsLsvw, noEventsTxtvw)
        }
        addReminderBtn.setOnClickListener {
            showAddEventDialog(eventsLsvw, ArrayList(eventsOnDateSelected), noEventsTxtvw)
        }
        MaterialAlertDialogBuilder(requireContext())
            .setView(view).show()


    }

    fun showNoEvents(eventsLsvw: ListView, noEventsTxtvw: TextView) {
        eventsLsvw.visibility = View.INVISIBLE
        noEventsTxtvw.visibility = View.VISIBLE
    }


    fun showAddEventDialog(
        eventsLsvw: ListView,
        eventsOnDateSelected: ArrayList<Event>,
        noEventsTxtvw: TextView
    ) {
        runBlocking {
            currentUser = Utilities.getCurrentUserInfo()
        }
        val view = layoutInflater.inflate(R.layout.add_event_layout, null)
        val eventHour = view.findViewById<TimePicker>(R.id.eventHourTmpck)
        val eventName = view.findViewById<EditText>(R.id.eventNameEd)
        val addBtn = view.findViewById<Button>(R.id.addEventBtn)
        val dialog = MaterialAlertDialogBuilder(requireContext()).setCancelable(true)
            .setView(view).show()
        addBtn.setOnClickListener {
            if (!eventName.text.toString().isNullOrEmpty()) {
                //Guardar nuevo evento
                val eventDB = hashMapOf(
                    "name" to eventName.text.toString(),
                    "dateTime" to calendar.plus(
                        eventHour.hour.toString().plus(eventHour.minute.toString())
                    )//HAY QUE VER EL TIPO QUE NOS VIENE DE FIREBASE ASÍ QUE NO PUEDO CONTINUAR :(
                )
                db.collection("users").document(currentUser.username).collection("events")
                    .document(eventName.text.toString()).set(eventDB)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Added Correctly", Toast.LENGTH_SHORT)
                            .show()
                        runBlocking {
                            currentUser = Utilities.getCurrentUserInfo()
                        }
                        eventsLsvw.adapter = EventsAdapter(
                            requireContext(),
                            ArrayList(compareDates()),
                            currentUser,
                            db,
                            calendar,
                            noEventsTxtvw
                        )
                        noEventsTxtvw.visibility = View.INVISIBLE
                        eventsLsvw.visibility = View.VISIBLE
                        setAlarm(
                            calendar.plus(
                                eventHour.hour.toString().plus(eventHour.minute.toString())
                            )
                        )
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


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarView = requireView().findViewById<CalendarView>(R.id.calendarVw)
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            var dayWellFormated = ""
            if (dayOfMonth < 10) {
                dayWellFormated = "0".plus(dayOfMonth.toString())
            } else {
                dayWellFormated = dayOfMonth.toString()
            }
            calendar = dayWellFormated.plus(month).plus(year)
            showEventDialog()
        }
    }


    private fun compareDates(): List<Event>? {
        return currentUser.events?.filter { event ->
            event.dateTime.substring(0, 2) == calendar.substring(0, 2)
                    && event.dateTime.substring(2, 4) == calendar.substring(2, 4)
                    && event.dateTime.substring(4, 8) == calendar.substring(4, 8)
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

    private fun setAlarm(date: String) {
        val cal = Calendar.getInstance()
        var dateFormated = Date()
        val sdf = SimpleDateFormat("ddMMyyyyHHmm", Locale.ENGLISH)
        dateFormated = sdf.parse(date)
        cal.time = dateFormated

        /*
        var alarmMgr: AlarmManager? = null

        alarmMgr = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Intent(requireContext(), Alarm)
        var alarmIntent: PendingIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        alarmMgr?.set(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            cal.timeInMillis,
            alarmIntent*/

    }

    class OnAlarmReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.i("ALARM", "Alarm activated! " + System.currentTimeMillis())

            //Aquí llama tu notificación.
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private class EventsAdapter(
        private val context: Context,
        private var arrayList: java.util.ArrayList<Event>,
        private var currentUser: User,
        private val db: FirebaseFirestore,
        private val calendar: String,
        private val noEventsTxtvw: TextView
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
            val deleteBtn = convertView.findViewById<ImageButton>(R.id.deleteEventBtn)
            deleteBtn.setOnClickListener {
                db.collection("users").document(currentUser.username).collection("events")
                    .document(eventTitle.text.toString()).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "Deleted Succesfully", Toast.LENGTH_SHORT).show()
                        runBlocking {
                            currentUser = Utilities.getCurrentUserInfo()
                        }
                        arrayList = currentUser.events?.filter { event ->
                            event.dateTime.substring(0, 2) == calendar.substring(0, 2)
                                    && event.dateTime.substring(2, 4) == calendar.substring(
                                2,
                                4
                            ) && event.dateTime.substring(4, 8) == calendar.substring(4, 8)
                        }?.let { ArrayList(it) }!!
                        notifyDataSetChanged()
                        if (arrayList.isEmpty()) {
                            noEventsTxtvw.visibility = View.VISIBLE
                            convertView.visibility = View.INVISIBLE
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Oops Something went wrong :(", Toast.LENGTH_SHORT)
                            .show()

                    }
            }

            return convertView
        }

    }
}
