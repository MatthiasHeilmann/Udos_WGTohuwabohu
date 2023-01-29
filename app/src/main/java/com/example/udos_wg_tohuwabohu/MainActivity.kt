package com.example.udos_wg_tohuwabohu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.Home.HomeEditFragment
import com.example.udos_wg_tohuwabohu.Home.HomeFragment
import com.example.udos_wg_tohuwabohu.Tasks.CreateTaskFragment
import com.example.udos_wg_tohuwabohu.Tasks.TasksFragment
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val dbLoader = DBLoader.getInstance()
    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()
    val TAG = "[MainActivity]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get user values and show
        val userID = intent.getStringExtra("user_id")
        val emailID = intent.getStringExtra("email_id")

        // load database
        dbLoader.setMainActivity(this)
        dbWriter.setMainActivity(this)
        dbLoader.loadDatabase(userID!!)

        binding.homeButton.setOnClickListener{
            showHome()
        }
        binding.homeEdit.setOnClickListener{
            val f = HomeEditFragment()
            f.setMainActivity(this)
            replaceFragment(f)
            binding.textToolbar.text = "WG bearbeiten"
            binding.homeButton.visibility = View.VISIBLE
            binding.homeEdit.visibility = View.INVISIBLE
        }

        replaceFragment(ChatFragment())
        binding.textToolbar.text = "Chat"
        // navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_chat -> {
                    replaceFragment(ChatFragment())
                    binding.textToolbar.text = "Chat"
                }
                R.id.nav_finance -> {
                    replaceFragment(FinanceFragment())
                    binding.textToolbar.text = "Finanzen"
                }
                R.id.nav_task -> {
                    val f = TasksFragment()
                    f.setMainActivity(this)
                    replaceFragment(f)
                    binding.textToolbar.text = "Aufgaben"
                }
                R.id.nav_shopping -> {
                    replaceFragment(ShoppingFragment())
                    binding.textToolbar.text = "Einkaufsliste"
                }
                R.id.nav_calender -> {
                    replaceFragment(CalendarFragment())
                    binding.textToolbar.text = "Kalender"
                }
                else -> {

                }
            }
            binding.homeEdit.visibility = View.INVISIBLE
            binding.homeButton.visibility = View.VISIBLE
            true
        }
    }

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
        binding.textToolbar.text = "Neue Aufgabe erstellen"
    }

    fun reloadTaskFragment(){
        if(binding.textToolbar.text == "Aufgaben"){// sorry for that again
            val f = TasksFragment()
            f.setMainActivity(this)
            replaceFragment(f)
        }
    }
    fun showTaskFragment(){
        val f = TasksFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = "Aufgaben"
    }
    fun reloadHomeFragment(){
        if(binding.textToolbar.text == "Eure WG"){
            showHome()
        }
    }
    fun showHome(){
        val f = HomeFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = "Eure WG"
        binding.homeEdit.visibility = View.VISIBLE
        binding.homeButton.visibility = View.INVISIBLE
    }
    fun restartApp() {
        val context = this@MainActivity
        val intent = Intent(context, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        if (context is Activity) {
            (context as Activity).finish()
        }
        Runtime.getRuntime().exit(0)
    }
}