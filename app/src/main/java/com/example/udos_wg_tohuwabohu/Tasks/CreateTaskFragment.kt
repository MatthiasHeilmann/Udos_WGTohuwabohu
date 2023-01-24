package com.example.udos_wg_tohuwabohu.Tasks

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentCreateTaskBinding
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBLoader
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*


class CreateTaskFragment : Fragment() {
    val TAG = "[CREATE TASK FRAGMENT]"
    val dbWriter = DBWriter.getInstance()
    val dataHandler = DataHandler.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater!!.inflate(R.layout.fragment_create_task,container, false)
        val createTaskButton:Button = view.findViewById(R.id.button_create_task)
        val nameOfTask:EditText = view.findViewById(R.id.nameOfTaskToCreate)
        val pointsOfTask:EditText = view.findViewById(R.id.pointsOfTaskToCreate)
        val frequencyOfTask:EditText = view.findViewById(R.id.frequencyOfTaskToCreate)
        createTaskButton.setOnClickListener{view ->
            Log.d(TAG,"Clicked")
            Log.d(TAG,nameOfTask.text.toString())
            Log.d(TAG,pointsOfTask.text.toString())
            Log.d(TAG,frequencyOfTask.text.toString())
            if(TextUtils.isEmpty(nameOfTask.text.toString().trim{it <= ' '})
                || TextUtils.isEmpty(pointsOfTask.text.toString().trim{it <= ' '})
                || TextUtils.isEmpty(frequencyOfTask.text.toString().trim{it <= ' '})){
//                Toast.makeText(
//                        context = activity,
//                        "Hilfe",
//                        Toast.LENGTH_SHORT
//                    ).show()
                Log.d(TAG, "Nicht alles eingegeben")
                return@setOnClickListener
                }
            val frequency: Int = Integer.parseInt(frequencyOfTask.text.toString())
            val points: Int = Integer.parseInt(pointsOfTask.text.toString())

            dbWriter.createTask(frequency,nameOfTask.text.toString(),points, getCompleter())
        }
        return view
    }


    /**
     * finds the new completer, which has the lowest coin_count
     * @return roommate with the lowest coin_count
     */
    fun getCompleter(): Roommate? {
        val roommateList = dataHandler.roommateList
        var worstMate: Roommate? = null
        var worstCount: Long = -1
        Log.d(TAG, roommateList.size.toString())
        roommateList.forEach{ mate ->
            if(worstCount== (-1).toLong()){
                worstCount = mate.value.coin_count!!
            }
            if (mate.value.coin_count!! <= worstCount) {
                worstMate = dataHandler.getRoommate(mate.key)
                worstCount = mate.value.coin_count!!
            }
        }
        return worstMate
    }
}