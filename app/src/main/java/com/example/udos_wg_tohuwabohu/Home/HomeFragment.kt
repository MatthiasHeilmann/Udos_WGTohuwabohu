package com.example.udos_wg_tohuwabohu.Home

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.example.udos_wg_tohuwabohu.LoginActivity
import com.example.udos_wg_tohuwabohu.LonelyPageActivity
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentHome2Binding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {
    lateinit var composeView: ComposeView
    private val dataHandler = DataHandler.getInstance()
    val TAG = "[HOME FRAGMENT]"
    private lateinit var binding: FragmentHome2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHome2Binding.inflate(layoutInflater)
        Log.d(TAG,dataHandler.wg?.name.toString())
//        Log.d(TAG,dataHandler.wg?.entryCode.toString())
        Log.d(TAG,dataHandler.contactPerson?.forename.toString())
        Log.d(TAG,dataHandler.contactPerson?.email.toString())
        Log.d(TAG,dataHandler.contactPerson?.telNr.toString())
        Log.d(TAG,dataHandler.contactPerson?.IBAN.toString())
        binding.wgName.text = dataHandler.wg?.name.toString()
//        binding.entrycode.text = dataHandler.wg?.entryCode.toString()
        binding.contactName.text = dataHandler.contactPerson?.forename.toString() + " " +dataHandler.contactPerson?.surname.toString()
        binding.contactEmail.text = dataHandler.contactPerson?.email.toString()
        binding.contactPhone.text = dataHandler.contactPerson?.telNr.toString()
        binding.contactIban.text = dataHandler.contactPerson?.IBAN.toString()


        binding.buttonLeaveWg.setOnClickListener{
            //TODO leave WG
            val activity: Activity = requireActivity()
            startActivity(Intent(activity, LonelyPageActivity::class.java))
            activity.finish()
        }

        binding.buttonLogout2.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            val activity: Activity = requireActivity()
            startActivity(Intent(activity, LoginActivity::class.java))
            activity.finish()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val v: View = inflater.inflate(R.layout.fragment_home2, container, false)

        return v
    }


}