package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBLoader
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.Task
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

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
        Log.d("[TasksFragment]",tasksData.toString() )
        var _binding: FragmentTasksBinding? = null
        // This property is only valid between onCreateView and onDestroyView.
        val v: View = inflater.inflate(R.layout.fragment_tasks, container, false)
        // Dispose of the Composition when the view's LifecycleOwner
        // is destroyed
        //setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            // In  Compose world
            tasksData?.let { FullTasks(it) }
        }
        return v
    }

    @Composable
    fun TaskCard(taskTitle:String, dueDate: Pair<String,Color>, frequency:String, roommate: Roommate?,taskKey:String){
            Card(colors = UdoCardTheme(), modifier = Modifier
//                .requiredHeight(height = 100.dp)
//                .requiredWidthIn(300.dp, 300.dp)
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
                        Button(onClick = {
                                val myTask: Task? = tasksData?.get(taskKey)
                                myTask?.let { checkTask(it) }
                                if (myTask != null) {
                                    myTask.points?.let { givePoints(roommate, it) }
                                }
//                                createTaskTest()
                                Log.d(TAG,"Button clicked")
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
    fun getTaskFrequency(frequency: Int) : String{
        if(frequency == 1) return "Täglich"
        if(frequency == 7) return "Wöchentlich"
        return "Alle " + frequency + " Tage"
    }

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
            taskYear>currentYear -> return Pair("Am " + taskDate + " fällig",UdoRed)
            taskYear<currentYear -> return Pair("Am " + taskDate + " fällig",UdoGray)
            taskMonth>currentMonth -> return Pair("Am " + taskDate + " fällig", UdoRed)
            taskMonth<currentMonth -> return Pair("Am " + taskDate + " fällig", UdoGray)
            taskDay==currentDay -> return Pair("Heute fällig", UdoRed)
            taskDay==currentDay+1 -> return Pair("Morgen fällig", UdoOrange)
            taskDay==currentDay-1 -> return Pair("Gestern fällig", UdoRed)
            taskDay<currentDay -> return Pair("Am " + taskDate + " fällig", UdoRed)
        }
        return Pair("Am " + taskDate + " fällig", UdoGray)
    }
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
        Column {
            tasksData?.forEach { task ->
                if(task.value.name == null || task.value.dueDate == null || task.value.frequency==null) return@forEach
                Log.d(TAG, task.key)
                Log.d(TAG, task.value.docId)
//                Log.d(TAG, task.value.name)
                val roommate: Roommate = dataHandler.getRoommate(task.value.roommate!!.id)
//                if()

                TaskCard(
                    taskTitle = task.value.name!!,
                    dueDate = getTaskDate(task.value.dueDate!!),
                    frequency = getTaskFrequency(task.value.frequency!!),
                    roommate = roommate,
                    taskKey = task.key
                )
            }
        }
    }

    /**
     * @return Roommate with the worst coin_count
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
    fun createTaskTest(){createTask(Date(),7,"Klo putzen",6)}
    fun createTask(frist: Date, frequencyInDays: Int, name: String, points: Int){
        val wgDocRef = dataHandler.wg?.let {
            myFirestore.collection(DBLoader.Collection.WG.toString()).document(
                it.docID)
        }
        val completerDocRef = getCompleter()?.let {
            myFirestore.collection("Mitbewohner").document(
                it.docID)
        }
        val myTask: MutableMap<String, Any> = HashMap()
        myTask["bezeichnung"] = name
        myTask["completed"] = false
        myTask["frequenz"] = frequencyInDays
        myTask["frist"] = frist
        myTask["punkte"] = points
        myTask["wg_id"] = wgDocRef!!
        myTask["erlediger"] = completerDocRef!!
        myFirestore.collection(DBLoader.Collection.Task.toString())
            .add(myTask)
    }
    fun checkTask(task: Task) {
        val newCompleter = getCompleter()
        var newDate = Date()
        val c = Calendar.getInstance()
        c.time = newDate
        task.frequency?.let { c.add(Calendar.DATE, it) }
        newDate = c.time
        if (newCompleter != null) {
            val newCompleterRef = myFirestore.collection("Mitbewohner").document(newCompleter.docID)
            myFirestore.collection(DBLoader.Collection.Task.toString()).document(task.docId).update(mapOf("frist" to newDate,"erlediger" to newCompleterRef))
        }
    }
    fun deleteTask(name: String){
        //TODO Aufgabe löschen
    }
    fun givePoints(roommate: Roommate?, points: Int){
        if (roommate != null) {
            val newPoints = roommate.coin_count?.plus(points)
            myFirestore.collection(roommateCollection).document(roommate.docID).update("coin_count",newPoints)
        }
    }
}