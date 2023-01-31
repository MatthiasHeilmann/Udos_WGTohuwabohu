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
        val notificationType = intent?.getStringExtra("notification_type")
        val notificationName = intent?.getStringExtra("notification_name")
        val notificationID = intent?.getIntExtra("notification_id", 1000)
        val notificationUser = intent?.getStringExtra("notification_user")
        val notificationCoins = intent?.getStringExtra("notification_coins")
        val notificationPrice = intent?.getStringExtra("notification_price")
        val notificationMoochers = intent?.getStringExtra("notification_moochers")



        val tapResultIntent = Intent(mContext, MainActivity::class.java)
        tapResultIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent: PendingIntent = getActivity( mContext,0,tapResultIntent,FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE)

        var notification = mContext?.let {
            NotificationCompat.Builder(it, "UdosChannel")

                //.setContentText(notificationName)
                .setSmallIcon(R.drawable.udo_v2_mit_haus)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)

        }
        if (notification != null) {
            when(notificationType){
                "appointment" ->        {notification.setContentTitle("Termin in weniger als 10 Minuten!")
                                        notification.setContentText(notificationName)}
                "chat" ->               {notification.setContentTitle("Neue Nachricht von "+ notificationUser)
                                        notification.setContentText(notificationName)}
                "task_new" ->           {notification.setContentTitle(notificationUser+" wurde eine neue Aufgabe zugewiesen!")
                                        notification.setContentText(notificationName)}
                "task_completed" ->     {notification.setContentTitle("Eine Aufgabe wurde abgeschlossen!")
                                        notification.setContentText("Für die Aufgabe "+ notificationName+ " wurden " + notificationCoins + "\uD83D\uDCB0 verteilt")}
                "task_due" ->           {notification.setContentTitle("Eine Aufgabe ist in einer Stunde fällig!")
                                        notification.setContentText("Bei Abschluss werden für die Aufgabe"+ notificationName+" "+ notificationCoins + "\uD83D\uDCB0 verteilt") }
                "finance" ->            {notification.setContentTitle(notificationUser + " hat " + notificationPrice +"€ für " + notificationName + " ausgegeben.")
                                        notification.setContentText("Geschnorrt haben: " + notificationMoochers)}
            }
        }
        val finishedNotification= notification?.build()

        notificationManager = mContext?.let { NotificationManagerCompat.from(it) }
            finishedNotification?.let { it ->
                if (notificationID != null) {
                    notificationManager?.notify(notificationID, it)
                }
            }

    }

}


