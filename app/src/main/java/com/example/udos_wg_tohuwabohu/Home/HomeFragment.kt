package com.example.udos_wg_tohuwabohu.Home

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.layout.RowScopeInstance.weight
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentHome2Binding
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.Task
import com.google.firebase.auth.FirebaseAuth
import java.util.ArrayList


class HomeFragment : Fragment() {
    lateinit var composeView: ComposeView
    private val dataHandler = DataHandler.getInstance()
    val TAG = "[HOME FRAGMENT]"
    private lateinit var _binding: FragmentHome2Binding
    private var mainActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View{
        _binding = FragmentHome2Binding.inflate(inflater,container,false)
        val view = _binding.root
        dataHandler.wg.first().name.toString().also { _binding.wgName.text = it }
        "Name: ${dataHandler.contactPerson?.forename.toString()} ${dataHandler.contactPerson?.surname.toString()}".also { _binding.contactName.text = it }
        "Email: ${dataHandler.contactPerson?.email.toString()}".also { _binding.contactEmail.text = it }
        "Telefon: ${dataHandler.contactPerson?.telNr.toString()}".also { _binding.contactPhone.text = it }
        "IBAN: ${dataHandler.contactPerson?.IBAN.toString()}".also { _binding.contactIban.text = it }
        _binding.entrycode.text = dataHandler.wg.first().entryCode.toString()

        _binding.userEmail.text = "Email: ${dataHandler.user?.email.toString()}"
        _binding.userFirstname.text = "Vorname: ${dataHandler.user?.forename.toString()}"
        _binding.userSurname.text = "Nachname: ${dataHandler.user?.surname.toString()}"
        _binding.userUsername.text = "Nutzername: ${dataHandler.user?.username.toString()}"

        _binding.buttonLeaveWg.setOnClickListener{
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("WG verlassen?")
            builder.setMessage("Bist Du sicher, dass Du die WG verlassen willst?")
            builder.setPositiveButton("Ja"){dialogInterface, which ->
                DBWriter.getInstance().leaveWG(mainActivity!!)
            }
            builder.setNegativeButton("Abbrechen"){dialogInterface,which->
                Log.d(TAG,"Abgebrochen")
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
        _binding.buttonLogout2.setOnClickListener{
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Wirklich abmelden?")
            builder.setMessage("Bist Du sicher, dass Du Dich abmelden willst?")
            builder.setPositiveButton("Ja"){dialogInterface, which ->
                FirebaseAuth.getInstance().signOut()
                val activity: Activity = requireActivity()
                startActivity(Intent(activity, LoginActivity::class.java))
                activity.finish()
            }
            builder.setNegativeButton("Abbrechen"){dialogInterface,which->
                Log.d(TAG,"Abgebrochen")
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
        composeView = view.findViewById(R.id.compose_view)
        composeView.setContent { 
            AllRoommateCards()
        }
        return view
    }
    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }
    @Composable
    fun AllRoommateCards(){
        val unSortedMateList: ArrayList<Roommate> = ArrayList()
        dataHandler.roommateList.forEach { mate ->
            unSortedMateList.add(mate.value)
        }
        val sortedMateList = unSortedMateList.sortedWith(compareBy { it.guteNudel_count }).reversed()
        Column {
            sortedMateList.forEach{ mate ->
                Log.d(TAG,mate.toString())
                RoommateCard(mate)
            }
        }
    }
    @Composable
    fun RoommateCard(roommate: Roommate){
        Card(colors = UdoCardTheme(),
        modifier = Modifier.padding(45.dp,5.dp))
        {
            Row(modifier = Modifier.padding(10.dp)){
                Column{
                    Text(text = roommate.username.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(text = roommate.forename.toString() + " " +roommate.surname.toString(), fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.weight(0.5f))
                Column {
                    Text(text = "\uD83C\uDF5C Gute Nudel: "+roommate.guteNudel_count.toString(), fontSize = 16.sp)
                    Text(text = "\uD83D\uDCB0 Coins: " + roommate.coin_count.toString(), fontSize = 16.sp)
                }
            }
        }
    }
    @Preview
    @Composable
    fun PreviewRoommateCard(){
        val mate = Roommate("id","MatthiasHeilmann@hotmail.com","Matthias","Heilmann","Matthi",3.toLong(),5.toLong(),50.0,null)
        RoommateCard(roommate = mate)
    }
}