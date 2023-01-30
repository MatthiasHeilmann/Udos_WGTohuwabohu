package com.example.udos_wg_tohuwabohu

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.app.TaskInfo
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.Timestamp

class AlarmReceiver : BroadcastReceiver() {
    private var notificationManager: NotificationManagerCompat? = null

    override fun onReceive(mContext: Context?, intent: Intent?){
        val appointmentName = intent?.getStringExtra("appointment_name")
        val appointmentTime = intent?.getIntExtra("appointment_time", 1000)

        val tapResultIntent = Intent(mContext, MainActivity::class.java)
        tapResultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent = getActivity( mContext,0,tapResultIntent,FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

        val notification = mContext?.let {
            NotificationCompat.Builder(it, "UdosChannel")
                .setContentTitle("Termin in 10 Minuten!")
                .setContentText(appointmentName)
                .setSmallIcon(R.drawable.udo_v2_mit_haus)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .build()
        }
        notificationManager = mContext?.let { NotificationManagerCompat.from(it) }
            notification?.let { it ->
                if (appointmentTime != null) {
                    notificationManager?.notify(appointmentTime , it)
                }
            }

    }

}


