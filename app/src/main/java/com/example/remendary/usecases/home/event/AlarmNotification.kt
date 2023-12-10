package com.example.remendary.usecases.home.event

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat


class AlarmNotification: BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {
        createNotification(context,intent)
        Log.d(TAG,"se ha llamado?")
    }
    private fun createNotification(context: Context?, intent: Intent?){
        val pendingIntent = PendingIntent.getActivity(context,0,intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        var notification = NotificationCompat.Builder(context!!,"myChannel").
        setSmallIcon(android.R.drawable.ic_delete).setContentTitle(intent?.type)
            .setContentText("")
            .setContentIntent(pendingIntent).setPriority(NotificationCompat.PRIORITY_DEFAULT).build()

        val manager=context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(1,notification)
        Log.d(TAG,"esto ha ido en  prinsipio")
    }

}