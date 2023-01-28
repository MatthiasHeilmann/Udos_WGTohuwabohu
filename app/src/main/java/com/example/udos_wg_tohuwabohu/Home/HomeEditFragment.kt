package com.example.udos_wg_tohuwabohu.Home

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.udos_wg_tohuwabohu.MainActivity
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentHome2Binding
import com.example.udos_wg_tohuwabohu.databinding.FragmentHomeEditBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler

class HomeEditFragment : Fragment() {
    private lateinit var _binding: FragmentHomeEditBinding
    private val dataHandler = DataHandler.getInstance()
    private var mainActivity: MainActivity? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeEditBinding.inflate(inflater,container,false)
        val view = _binding.root
        _binding.wgName.setText(dataHandler.wg?.name)
        _binding.contactName.setText(dataHandler.contactPerson?.forename + " " +dataHandler.contactPerson?.surname)
        _binding.contactEmail.setText(dataHandler.contactPerson?.email)
        _binding.contactPhone.setText(dataHandler.contactPerson?.telNr)
        _binding.contactIban.setText(dataHandler.contactPerson?.IBAN)

        _binding.buttonSave.setOnClickListener{
            if(_binding.wgName.text.toString() == dataHandler.wg?.name
                && _binding.contactName.text.toString() == dataHandler.contactPerson?.forename + " " +dataHandler.contactPerson?.surname
                && _binding.contactEmail.text.toString() == dataHandler.contactPerson?.email
                && _binding.contactPhone.text.toString() == dataHandler.contactPerson?.telNr
                && _binding.contactIban.text.toString() == dataHandler.contactPerson?.IBAN){
                mainActivity?.showHome()
                return@setOnClickListener
            }
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Änderungen speichern?")
            builder.setMessage("Bist Du sicher, dass Du die änderungen speichern willst?")
            builder.setPositiveButton("Ja"){dialogInterface, which ->
                val parts = _binding.contactName.text.toString().split(" ")
                DBWriter.getInstance().updateWgData(
                    _binding.wgName.text.toString(),
                    parts[1],
                    parts[0],
                    _binding.contactEmail.text.toString(),
                    _binding.contactPhone.text.toString(),
                    _binding.contactIban.text.toString()
                )
                mainActivity?.showHome()
            }
            builder.setNegativeButton("Abbrechen"){dialogInterface,which->
                mainActivity?.showHome()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }
        _binding.buttonCancel.setOnClickListener{
            if(_binding.wgName.text.toString() == dataHandler.wg?.name
                && _binding.contactName.text.toString() == dataHandler.contactPerson?.forename + " " +dataHandler.contactPerson?.surname
                && _binding.contactEmail.text.toString() == dataHandler.contactPerson?.email
                && _binding.contactPhone.text.toString() == dataHandler.contactPerson?.telNr
                && _binding.contactIban.text.toString() == dataHandler.contactPerson?.IBAN){
                mainActivity?.showHome()
                return@setOnClickListener
            }
            val builder = AlertDialog.Builder(activity)
            builder.setTitle("Änderungen verwerfen'?")
            builder.setMessage("Bist Du sicher, dass Du die änderungen verwerfen willst?")
            builder.setPositiveButton("Ja"){dialogInterface, which ->
                mainActivity?.showHome()
            }
            builder.setNegativeButton("Abbrechen"){dialogInterface,which->
                Log.d("dsff","NOO")
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
        }

        return view
    }
    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }
}