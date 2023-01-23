package com.example.udos_wg_tohuwabohu.Tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBLoader
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 */
class TasksFragment : Fragment() {
    lateinit var composeView: ComposeView
    private val TAG: String = "[TASKS FRAGMENT]"
    private val dataHandler = DataHandler.getInstance()
    private var tasksData = dataHandler.getTasks()
    private val myFirestore = Firebase.firestore
    private val roommateCollection = DBLoader.Collection.Roommate.toString()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        var _binding: FragmentTasksBinding? = FragmentTasksBinding.inflate(layoutInflater)
        val v: View = inflater.inflate(R.layout.fragment_tasks, container, false)
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            tasksData?.let { FullTasks(it) }
        }
        return v
    }
    @Composable
    fun TaskCard(taskTitle:String, dueDate: Pair<String,Color>, frequency:String, roommate: Roommate?,taskKey:String){
            Card(colors = UdoCardTheme(), modifier = Modifier
                .padding(5.dp)){
                Row(modifier = Modifier.padding(10.dp)){
                    Column{
                        Text(text = taskTitle, color = dueDate.second)
                        Text(text = dueDate.first, color = dueDate.second)
                        Text(text = frequency)
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    Column(horizontalAlignment = Alignment.End)
                        {
                        if (roommate != null) {
                            roommate.username?.let { Text(text = it) }
                        }
                        /** button to check the task */
                        Button(onClick = {
                                val myTask: Task? = tasksData?.get(taskKey)
                                myTask?.let { checkTask(it) }
                                if (myTask != null) {
                                    myTask.points?.let { givePoints(roommate, it) }
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UdoLightBlue, containerColor = UdoGray),
                            modifier = Modifier.absolutePadding(4.dp),
                            shape = RoundedCornerShape(5)
                        ) {
                            Text(text = "Erledigt")
                        }
                    }
                }


            }
    }

    /**
     * returns the frequency of the task in the correct format
     */
    fun getTaskFrequency(frequency: Int) : String{
        if(frequency == 1) return "Täglich"
        if(frequency == 7) return "Wöchentlich"
        return "Alle " + frequency + " Tage"
    }

    /**
     * returns the date of a task in the correct format with the priority color
     */
    fun getTaskDate(date: Date): Pair<String,Color>{
        val c = Calendar.getInstance()
        val currentDay = c.get(Calendar.DATE)
        val currentMonth = c.get(Calendar.MONTH)+1
        val currentYear = c.get(Calendar.YEAR)
        val taskDay = date.date
        val taskMonth = date.month+1
        val taskYear = date.year+1900
        
        val taskDate = formatNumber(taskDay)+"."+formatNumber(taskMonth)+"."+taskYear.toString()
        when{
            taskYear<currentYear -> return Pair("Am " + taskDate + " fällig", UdoRed)
            taskYear>currentYear -> return Pair("Am " + taskDate + " fällig", UdoGray)
            taskMonth<currentMonth -> return Pair("Am " + taskDate + " fällig", UdoRed)
            taskMonth>currentMonth -> return Pair("Am " + taskDate + " fällig", UdoGray)
            taskDay==currentDay -> return Pair("Heute fällig", UdoRed)
            taskDay==currentDay+1 -> return Pair("Morgen fällig", UdoOrange)
            taskDay==currentDay-1 -> return Pair("Gestern fällig", UdoRed)
            taskDay<currentDay -> return Pair("Am " + taskDate + " fällig", UdoRed)
        }
        return Pair("Am " + taskDate + " fällig", UdoGray)
    }

    /**
     * formats date numbers
     */
    private fun formatNumber(n: Int): String {
        return if (n > 9) "" + n else "0" + n
    }

    @Preview
    @Composable
    fun PreviewTaskCard(){
        TaskCard("Müll rausbringen",Pair("Morgen fällig", UdoOrange),"Wöchentlich",null,"ABCS")
    }

    @Composable
    fun FullTasks(taskData: HashMap<String, Task>){
        val scrollState = rememberScrollState()
        val unSortedTaskList: ArrayList<Task> = ArrayList()
        taskData.forEach{task ->
            unSortedTaskList.add(task.value)
        }
        var sortedTaskList = unSortedTaskList.sortedWith(compareBy { it.dueDate })
        Column (modifier = Modifier
            .verticalScroll(rememberScrollState())
        ){
            sortedTaskList.forEach { task ->
                if(task.name == null || task.dueDate == null || task.frequency==null) return@forEach
                val roommate: Roommate? = dataHandler.getRoommate(task.roommate!!.id)

                TaskCard(
                    taskTitle = task.name!!,
                    dueDate = getTaskDate(task.dueDate!!),
                    frequency = getTaskFrequency(task.frequency!!),
                    roommate = roommate,
                    taskKey = task.docId
                )
            }
        }
    }

    /**
     * finds the new completer, which has the lowest coin_count
     * @return roommate with the lowest coin_count
     */
    fun getCompleter(): Roommate? {
        val roommateList = dataHandler.roommateList
        var worstMate: Roommate? = null
        var worstCount: Long = 0
        roommateList.forEach{ mate ->
            if (mate.value.coin_count!! <= worstCount) {
                worstMate = dataHandler.getRoommate(mate.key)
                worstCount = mate.value.coin_count!!
            }
        }
        return worstMate
    }
//    fun createTaskTest(){createTask(Date(),7,"Klo putzen",6)}

    /**
     * checks the task in the database,
     * sets the duedate to today + frequency,
     * sets new completer to completer given by getCompleter()
     */
    fun checkTask(task: Task) {
        val newCompleter = getCompleter()
        var newDate = Date()
        val c = Calendar.getInstance()
        c.time = newDate
        task.frequency?.let { c.add(Calendar.DATE, it) }
        newDate = c.time
        if (newCompleter != null) {
            val newCompleterRef = myFirestore.collection("mitbewohner").document(newCompleter.docID)
            myFirestore.collection("wg")
                .document(dataHandler.wg!!.docID)
                .collection("tasks")
                .document(task.docId)
                .update(mapOf(
                    "frist" to newDate,
                    "erlediger" to newCompleterRef
                ))
        }
    }

    /**
     * deletes a task in the database
     */
    fun deleteTask(docId: String){
        myFirestore.collection(DBLoader.Collection.Task.toString()).document(docId).delete()
    }

    /**
     * gives the roommate who checks the task the points in the database
     */
    fun givePoints(roommate: Roommate?, points: Int){
        if (roommate != null) {
            val newPoints = roommate.coin_count?.plus(points)
            myFirestore.collection(roommateCollection).document(roommate.docID).update("coin_count",newPoints)
        }
    }
}