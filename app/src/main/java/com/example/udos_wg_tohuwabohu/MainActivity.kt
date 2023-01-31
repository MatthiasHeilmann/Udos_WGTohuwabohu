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
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.calendar.CalendarAddFragment
import com.example.udos_wg_tohuwabohu.calendar.CalendarFragment
import com.example.udos_wg_tohuwabohu.finances.FinanceAddFragment
import com.example.udos_wg_tohuwabohu.finances.FinanceFragment
import com.example.udos_wg_tohuwabohu.home.HomeEditFragment
import com.example.udos_wg_tohuwabohu.home.HomeFragment
import com.example.udos_wg_tohuwabohu.tasks.CreateTaskFragment
import com.example.udos_wg_tohuwabohu.tasks.TasksFragment
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference

class MainActivity : AppCompatActivity() {

    enum class FragmentTitle(val call: String) {
        WG("Eure WG"),
        Calendar("Kalender"),
        CalendarAdd("Kalendereintrag hinzufügen"),
        Shoppinglist("Einkaufsliste"),
        Chat("Chat"),
        EditWG("WG bearbeiten"),
        Finances("Finanzen"),
        FinancesAdd("Finanzeintrag erstellen"),
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

        createNotificationChannel()


        // load database
        dbLoader.setMainActivity(this)

        Log.d(userID.toString(),"User exists")
        dbWriter.setMainActivity(this)
        dbLoader.loadDatabase(userID!!)

        binding.homeButton.setOnClickListener{
            showHome()
        }
        binding.homeEdit.setOnClickListener{
            Log.d(TAG,"Displaying HomeEdit...")
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
                    Log.d(TAG,"Displaying Chat...")
                }
                R.id.nav_finance -> {
                    showFinanceFragment()
                }
                R.id.nav_task -> {
                    showTaskFragment()
                }
                R.id.nav_shopping -> {
                    replaceFragment(ShoppingFragment())
                    binding.textToolbar.text = FragmentTitle.Shoppinglist.call
                    Log.d(TAG,"Displaying Shopping...")
                }
                R.id.nav_calender -> {
                    showCalendarFragment()
                }
            }
            binding.homeEdit.visibility = View.INVISIBLE
            binding.homeButton.visibility = View.VISIBLE
            true
        }

        when {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
            and
            (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) == PackageManager.PERMISSION_GRANTED) -> {
                dataHandler.wg.first().calendar?.forEach { currentAppointment
                    ->
                    if(System.currentTimeMillis() < currentAppointment.values.first().seconds*1000) {
                        setAlarmCalendar(currentAppointment)
                    }
                }
                dataHandler.taskList.forEach{
                    task ->
                    if(task.value.roommate?.id == userID){
                        setAlarmTask(task.value, "due")
                    }
                }

                Log.d("Permissions granted!","123456")
            }

            else -> {

            }
        }

    }//ActivityResultContracts.RequestPermission()



    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    fun showCalendarFragment(){
        val f = CalendarFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.Calendar.call
        Log.d(TAG,"Displaying Calendar...")
    }

    fun showCalendarAddFragment(){
        val f = CalendarAddFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.CalendarAdd.call
        Log.d(TAG,"Displaying CalendarAdd...")
    }

    fun openAddFinanceFragment(){
        val f = FinanceAddFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.FinancesAdd.call
        Log.d(TAG,"Displaying FinancesAdd...")
    }

    fun showFinanceFragment(){
        val f = FinanceFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.Finances.call
        Log.d(TAG,"Displaying Finances...")
    }

    fun openCreateTaskFragment(){
        val f = CreateTaskFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.CreateNewTask.call
        Log.d(TAG,"Displaying TasksAdd...")
    }

    fun reloadTaskFragment(){
        if(binding.textToolbar.text == FragmentTitle.Tasks.call){
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
        Log.d(TAG,"Displaying Tasks...")
    }
    fun reloadHomeFragment(){
        if(binding.textToolbar.text == FragmentTitle.WG.call){
            showHome()
        }
    }
    fun reloadCalendarFragment(){
        if(binding.textToolbar.text == FragmentTitle.Calendar.call){
            showCalendarFragment()
        }
    }
    fun showHome(){
        val f = HomeFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.WG.call
        binding.homeEdit.visibility = View.VISIBLE
        binding.homeButton.visibility = View.INVISIBLE
        Log.d(TAG,"Displaying Home...")
    }
    fun restartApp() {
        val context = this@MainActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        (context as Activity).finish()
        Runtime.getRuntime().exit(0)
    }

    fun setAlarmCalendar(appointment: MutableMap<String, Timestamp>){

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.putExtra("notification_type","appointment")
        intent.putExtra("notification_name",appointment.keys.first())
        intent.putExtra("notification_id", appointment.values.first().seconds.toInt())
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, appointment.values.first().seconds.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            ((appointment.values.first().seconds-600)*1000),
            pendingIntent
        )
        Log.d("Millis in Timestamp:",(appointment.values.first().seconds*1000).toString() )
        Log.d("Millis now:", (Timestamp.now().seconds*1000).toString())
        Log.d("Millis in system", System.currentTimeMillis().toString())
    }

    fun setAlarmChat(chatMessage: ChatMessage){

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.putExtra("notification_type", "chat")
        intent.putExtra("notification_name",chatMessage.message)
        intent.putExtra("notification_id", chatMessage.timestamp)
        intent.putExtra("notification_user", chatMessage.user?.let { dataHandler.roommateList.get(it.id)?.username })

        val pendingIntent = (chatMessage.timestamp?.let { Timestamp(it) })?.seconds?.let {
            PendingIntent.getBroadcast(applicationContext,
                it.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
        }

        if(binding.textToolbar.text!= "Chat"){
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                (System.currentTimeMillis()),
                pendingIntent
            )
        }
    }

    fun setAlarmTask(task: Task, newOrCompletedOrDue: String){

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        var NotifTimeInMillis= System.currentTimeMillis()
        if(newOrCompletedOrDue=="new"){
        intent.putExtra("notification_type", "task_new")}
        else if(newOrCompletedOrDue=="completed"){
            intent.putExtra("notification_type", "task_completed")
        }
        else if(newOrCompletedOrDue=="due"){
            intent.putExtra("notification_type", "task_due")
            NotifTimeInMillis= task.dueDate?.let { (Timestamp(it).seconds-3600)*1000 }!!
        }


        intent.putExtra("notification_name",task.name)
        intent.putExtra("notification_id", NotifTimeInMillis)
        intent.putExtra("notification_user", task.roommate?.let { dataHandler.roommateList.get(it.id)?.username })
        intent.putExtra("notification_coins", task.points.toString())

        val pendingIntent = PendingIntent.getBroadcast(applicationContext,
                NotifTimeInMillis.toInt(), intent, PendingIntent.FLAG_IMMUTABLE)
        if((newOrCompletedOrDue!="new") or (binding.textToolbar.text!="Aufgaben")) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                NotifTimeInMillis,
                pendingIntent
            )
        }
    }

    fun setAlarmFinance(financeEntry: FinanceEntry){

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(applicationContext, AlarmReceiver::class.java)
        intent.putExtra("notification_type", "finance")
        intent.putExtra("notification_name",financeEntry.description)
        intent.putExtra("notification_price",financeEntry.price.toString())
        intent.putExtra("notification_moochers",
            financeEntry.moucherList?.let { convertRoommateArrayToString(it) })
        intent.putExtra("notification_id",
            financeEntry.timestamp?.let { Timestamp(it).seconds.toString() })
        intent.putExtra("notification_user", financeEntry.benefactor?.let {
            dataHandler.roommateList.get(
                it.id)?.username
        })

        val pendingIntent = financeEntry.timestamp?.let { Timestamp(it).seconds.toInt() }?.let {
            PendingIntent.getBroadcast(applicationContext,
                it, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            (System.currentTimeMillis()),
            pendingIntent
        )

    }



    private fun createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Log.d("NOTIFICATION CHANNEL CREATED",".")
            val importance= NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(CHANNEL_ID,"UdosChannel", importance)
            notificationChannel.description="UdosChannel"
            notificationChannel.setSound(null, null)
            val notificationManager= getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)

        }

    }

    private fun convertRoommateArrayToString(referenceList: java.util.ArrayList<DocumentReference>): String{
        var roommate_string = ""
        referenceList.forEach{
            reference ->
            val username = dataHandler.roommateList.get(
                reference.id)?.username
            roommate_string= roommate_string + username +", "
        }
        roommate_string= roommate_string.dropLast(2)
        return roommate_string
    }
}