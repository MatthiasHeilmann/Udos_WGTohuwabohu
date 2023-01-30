package com.example.udos_wg_tohuwabohu

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.Home.HomeEditFragment
import com.example.udos_wg_tohuwabohu.Home.HomeFragment
import com.example.udos_wg_tohuwabohu.Tasks.CreateTaskFragment
import com.example.udos_wg_tohuwabohu.Tasks.TasksFragment
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDateTime

class MainActivity : AppCompatActivity() {

    enum class FragmentTitle(val call: String) {
        WG("Eure WG"),
        Calendar("Kalender"),
        Shoppinglist("Einkaufsliste"),
        Chat("Chat"),
        EditWG("WG bearbeiten"),
        Finances("Finanzen"),
        CreateNewTask("Neue Aufgabe erstellen"),
        Tasks("Aufgaben");

        override fun toString(): String {
            return call
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val dbLoader = DBLoader.getInstance()
    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()
    val TAG = "[MainActivity]"

    private companion object{
        private const val CHANNEL_ID= "UdosChannel"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get user values and show
        val userID = FirebaseAuth.getInstance().currentUser!!.uid
        Log.d(FirebaseAuth.getInstance().currentUser!!.uid,"my id")
        val emailID = intent.getStringExtra("email_id")

        // load database
        dbLoader.setMainActivity(this)

        Log.d(userID.toString(),"User exists")
        dbWriter.setMainActivity(this)
        dbLoader.loadDatabase(userID!!)

        binding.textUserID.text = "User ID: $userID"
        binding.textUserEmail.text = "Email: $emailID"

        binding.buttonLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            finish()
        binding.homeButton.setOnClickListener{
            showHome()
        }
        binding.homeEdit.setOnClickListener{
            val f = HomeEditFragment()
            f.setMainActivity(this)
            replaceFragment(f)
            binding.textToolbar.text = FragmentTitle.EditWG.call
            binding.homeButton.visibility = View.VISIBLE
            binding.homeEdit.visibility = View.INVISIBLE
        }

        replaceFragment(ChatFragment())
        binding.textToolbar.text = FragmentTitle.Chat.call

        // navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_chat -> {
                    replaceFragment(ChatFragment())
                    binding.textToolbar.text = FragmentTitle.Chat.call
                }
                R.id.nav_finance -> {
                    replaceFragment(FinanceFragment())
                    binding.textToolbar.text = FragmentTitle.Finances.call
                }
                R.id.nav_task -> {
                    showTaskFragment()
                }
                R.id.nav_shopping -> {
                    replaceFragment(ShoppingFragment())
                    binding.textToolbar.text = FragmentTitle.Shoppinglist.call
                }
                R.id.nav_calender -> {
                    replaceFragment(CalendarFragment())
                    binding.textToolbar.text = FragmentTitle.Calendar.call
                }
                else -> {

                }
            }
            binding.homeEdit.visibility = View.INVISIBLE
            binding.homeButton.visibility = View.VISIBLE
            true
        }
        val requestMultiplePermissionsLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                permissions.entries.forEach {
                    Log.e("DEBUG", "${it.key} = ${it.value}")
                }
            }

        when {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            and
            (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) -> {
                /*dataHandler.wg.first().calendar?.forEach { currentAppointment
                    ->
                    setAlarmsCalendar(currentAppointment)
                }*/
                createNotificationChannel()
                var x= Pair<String,Timestamp>("Test",Timestamp.now())
                setAlarmsCalendar(mutableMapOf(x))
                Log.d("Permissions granted!","123456")
            }

            /*else -> {
                requestMultiplePermissionsLauncher.launch(
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS,Manifest.permission.SCHEDULE_EXACT_ALARM)
                )

            }*/
        }

    }//ActivityResultContracts.RequestPermission()



    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
    fun openCreateTaskFragment(){
        val f = CreateTaskFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.CreateNewTask.call
    }

    fun reloadTaskFragment(){
        if(binding.textToolbar.text == FragmentTitle.Tasks.call){// sorry for that again
            val f = TasksFragment()
            f.setMainActivity(this)
            replaceFragment(f)
        }
    }
    fun showTaskFragment(){
        val f = TasksFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.Tasks.call
    }
    fun reloadHomeFragment(){
        if(binding.textToolbar.text == FragmentTitle.WG.call){
            showHome()
        }
    }
    fun reloadCalendarFragment(){
        if(binding.textToolbar.text == FragmentTitle.Calendar.call){
            replaceFragment(CalendarFragment())
        }
    }
    fun showHome(){
        // TODO Navbar aktuallisieren
        val f = HomeFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.WG.call
        binding.homeEdit.visibility = View.VISIBLE
        binding.homeButton.visibility = View.INVISIBLE
    }
    fun restartApp() {
        val context = this@MainActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as Activity).finish()
        Runtime.getRuntime().exit(0)
    }

    fun setAlarmsCalendar(appointment: MutableMap<String, Timestamp>){

        /*val notificationID= appointment.values.first().seconds.toInt()
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
        val MainActivityIntent= Intent(this, MainActivity::class.java)
        var MainActivityPendingIntent= PendingIntent.getActivity(this,1, MainActivityIntent,PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)


        notificationBuilder.setSmallIcon(R.drawable.udo_v2_mit_haus)
        notificationBuilder.setContentTitle("You have an appointment coming up!")
        notificationBuilder.setContentText(appointment.keys.first())
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setContentIntent(MainActivityPendingIntent)

        MainActivityIntent.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK*/

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.putExtra("appointment_name",appointment.keys.first())
        intent.putExtra("appointment_time", appointment.values.first().seconds.toInt())
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, appointment.values.first().seconds.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            (appointment.values.first().seconds.toLong()*1000),
            pendingIntent
        )
        Log.d("Millis in Timestamp:",(appointment.values.first().seconds*1000).toString() )
        Log.d("Millis now:", (Timestamp.now().seconds*1000).toString())
        Log.d("Millis in system", System.currentTimeMillis().toString())
        //val notificationManagerCompat = NotificationManagerCompat.from(this)
        //notificationManagerCompat.notify(notificationID, notificationBuilder.build())

        /*val alarmManager = ContextCompat.getSystemService() as AlarmManager
        val intent = Intent(this, MyAlarm::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager.setRepeating(
            AlarmManager.RTC,
            timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )*/
        Toast.makeText(this, "Alarm is set", Toast.LENGTH_SHORT).show()
        binding.textToolbar.text
    }
    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d("NOTIFICATION CHANNEL CREATED",".")
            val importance= NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(CHANNEL_ID,"UdosChannel", importance)
            notificationChannel.description="UdosChannel"
            val notificationManager= getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }

    }
    /*private class MyAlarm : BroadcastReceiver() {
        override fun onReceive(
            context: Context,
            intent: Intent
        ) {
            Log.d("Alarm Bell", "Alarm just fired")
        }*/
}