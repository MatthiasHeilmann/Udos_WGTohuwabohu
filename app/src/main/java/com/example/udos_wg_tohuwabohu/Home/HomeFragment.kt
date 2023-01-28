package com.example.udos_wg_tohuwabohu.Home

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.example.udos_wg_tohuwabohu.LoginActivity
import com.example.udos_wg_tohuwabohu.databinding.FragmentHome2Binding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {
    lateinit var composeView: ComposeView
    private val dataHandler = DataHandler.getInstance()
    val TAG = "[HOME FRAGMENT]"
    private lateinit var _binding: FragmentHome2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        _binding = FragmentHome2Binding.inflate(inflater,container,false)
        val view = _binding.root
        dataHandler.wg?.name.toString().also { _binding.wgName.text = it }
        "Name: ${dataHandler.contactPerson?.forename.toString()} ${dataHandler.contactPerson?.surname.toString()}".also { _binding.contactName.text = it }
        "Email: ${dataHandler.contactPerson?.email.toString()}".also { _binding.contactEmail.text = it }
        "Telefon: ${dataHandler.contactPerson?.telNr.toString()}".also { _binding.contactPhone.text = it }
        "IBAN: ${dataHandler.contactPerson?.IBAN.toString()}".also { _binding.contactIban.text = it }
        _binding.entrycode.text = dataHandler.wg?.entryCode.toString()

        _binding.userEmail.text ="Email: "+ dataHandler.user?.email.toString()
        _binding.userFirstname.text ="Vorname: "+ dataHandler.user?.forename.toString()
        _binding.userSurname.text ="Nachname: "+ dataHandler.user?.surname.toString()
        _binding.userUsername.text ="Nutzername: "+ dataHandler.user?.username.toString()

        _binding.buttonLeaveWg.setOnClickListener{
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("WG verlassen?")
            builder.setMessage("Bist Du sicher, dass Du die WG verlassen willst?")
            builder.setPositiveButton("Ja"){dialogInterface, which ->
                Log.d(TAG,"YEEES")
                //TODO leave WG
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
        return view
    }


}