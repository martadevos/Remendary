package com.example.remendary

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues.TAG
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
import com.example.remendary.usecases.home.event.AlarmNotification
import com.example.remendary.util.Utilities
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.runBlocking
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
            showAddEventDialog(eventsLsvw, noEventsTxtvw)
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
                var hour=eventHour.hour.toString()
                if(hour.length==1){
                    hour="0".plus(hour)
                }
                var minute=eventHour.minute.toString()
                if(minute.length==1){
                    minute="0".plus(minute)
                }
                calendar=calendar.plus(hour.plus(minute))
                Log.d(TAG,"calendarioso: "+calendar)
                //Guardar nuevo evento
                val eventDB = hashMapOf(
                    "name" to eventName.text.toString(),
                    "dateTime" to calendar //HAY QUE VER EL TIPO QUE NOS VIENE DE FIREBASE ASÍ QUE NO PUEDO CONTINUAR :(
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

                        scheduleNotification(eventName.text.toString())
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
    private fun scheduleNotification(title:String){
        val event= Calendar.getInstance()
        event.set(Integer.parseInt(calendar.substring(4,8)),
            (Integer.parseInt(calendar.substring(2,4))),//los meses van del 0 al 11 por eso se le resta 1
            Integer.parseInt(calendar.substring(0,2)),
            Integer.parseInt(calendar.substring(8,10)),
            Integer.parseInt(calendar.substring(10,12)))
        val intent=Intent(activity?.applicationContext, AlarmNotification::class.java)
        intent.type=title
        val pendingIntent = PendingIntent.getBroadcast(
            activity?.applicationContext,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val alarmManager= activity?.applicationContext?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        Log.d(TAG,"HORA ACTUAL: "+Calendar.getInstance().timeInMillis.toString())
        Log.d(TAG,"ALARMA: "+event.timeInMillis.toString())
        calendar=calendar.substring(0,8)
        alarmManager.set(AlarmManager.RTC_WAKEUP,event.timeInMillis,pendingIntent)
    }

    fun createChannel(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            val channel = NotificationChannel(
                "myChannel",
                "MySupperChannel",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply{
                description="Default_description"
            }
            val notificationManager:NotificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val calendarView = requireView().findViewById<CalendarView>(R.id.calendarVw)
        createChannel()
        calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            var dayWellFormated = if (dayOfMonth < 10) {
                "0".plus(dayOfMonth.toString())
            } else {
                dayOfMonth.toString()
            }
            var monthWellFormated = if (month < 10) {
                "0".plus(month.toString())
            } else {
                month.toString()
            }
            calendar = dayWellFormated.plus(monthWellFormated).plus(year)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private class EventsAdapter(
        private val context: Context,
        private var arrayList: java.util.ArrayList<Event>,
        private var currentUser: User,
        private val db: FirebaseFirestore,
        private val calendar: String,
        private val noEventsTxtvw: TextView,
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
            val timeTv = convertView.findViewById<TextView>(R.id.timeTV)
            timeTv.text = arrayList[position].dateTime.substring(8,10).plus(":").plus(arrayList[position].dateTime.substring(10,12))
            /*val deleteBtn = convertView.findViewById<ImageButton>(R.id.deleteEventBtn)
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
                                    && event.dateTime.substring(2, 4) == calendar.substring(2, 4)
                                    && event.dateTime.substring(4, 8) == calendar.substring(4, 8)
                        }?.let { it1 -> ArrayList(it1) }!!
                        notifyDataSetChanged()
                        if (arrayList.isEmpty()) {
                            noEventsTxtvw.visibility = View.VISIBLE
                            convertView.visibility = View.INVISIBLE
                        }
                        cancelAlarm()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Oops Something went wrong :(", Toast.LENGTH_SHORT)
                            .show()
                    }
            }*/

            return convertView
        }

        /*private fun cancelAlarm(){
            val intent = Intent(context.applicationContext, AlarmNotification::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context.applicationContext,
                1,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            val alarmManager = context.applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
            alarmManager!!.cancel(pendingIntent)

        }*/


    }
}
