package com.example.udos_wg_tohuwabohu

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.Finances.FinanceAddFragment
import com.example.udos_wg_tohuwabohu.Finances.FinanceFragment
import com.example.udos_wg_tohuwabohu.Home.HomeEditFragment
import com.example.udos_wg_tohuwabohu.Home.HomeFragment
import com.example.udos_wg_tohuwabohu.Tasks.CreateTaskFragment
import com.example.udos_wg_tohuwabohu.Tasks.TasksFragment
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*


class MainActivity : AppCompatActivity() {

    enum class FragmentTitle(val call: String) {
        WG("Eure WG"),
        Calendar("Kalender"),
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
            binding.textToolbar.text = FragmentTitle.EditWG.call
            binding.homeButton.visibility = View.VISIBLE
            binding.homeEdit.visibility = View.INVISIBLE
        }

        replaceFragment(ChatFragment())
        binding.textToolbar.text = FragmentTitle.Chat.call
        // navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_chat -> {
                    replaceFragment(ChatFragment())
                    binding.textToolbar.text = FragmentTitle.Chat.call
                }
                R.id.nav_finance -> {
                    val f = FinanceFragment()
                    f.setMainActivity(this)
                    replaceFragment(f)
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
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    fun openAddFinanceFragment(){
        val f = FinanceAddFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.FinancesAdd.call
    }

    fun showFinanceFragment(){
        val f = FinanceFragment()
        f.setMainActivity(this)
        replaceFragment(f)
        binding.textToolbar.text = FragmentTitle.Finances.call
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
}