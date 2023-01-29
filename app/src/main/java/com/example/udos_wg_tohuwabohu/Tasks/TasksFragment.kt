package com.example.udos_wg_tohuwabohu.Tasks

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*
import java.util.*

/**
 *
 */
class TasksFragment : Fragment() {
    lateinit var composeView: ComposeView
    private val TAG: String = "[TASKS FRAGMENT]"
    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()
    private var tasksData = dataHandler.getTasks()
    private lateinit var mainActivity: MainActivity
    lateinit var _binding:FragmentTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(layoutInflater)
        val v: View = _binding.root
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            tasksData?.let { FullTasks(it) }
            TasksFAB()
        }
        return v
    }
    @Composable
    fun TaskCard(taskTitle:String, dueDate: Pair<String,Color>, frequency:String, roommate: Roommate?,taskKey:String){
        val myTask: Task? = tasksData?.get(taskKey)
        val showCheckDialog = remember { mutableStateOf(false) }
        val showDeleteDialog = remember { mutableStateOf(false) }
        if(showCheckDialog.value){
            CheckConfirmationDialog(showCheckDialog = showCheckDialog.value,
                onDismiss = {showCheckDialog.value = it},
                myTask = myTask)
        }
        if(showDeleteDialog.value){
            DeleteConfirmationDialog(showDeleteDialog = showDeleteDialog.value,
                onDismiss = {showDeleteDialog.value = it},
                myTask = myTask)
        }
            Card(colors = UdoCardTheme(), modifier = Modifier
                .padding(5.dp)
                .pointerInput(Unit){
                    detectTapGestures(
                        onLongPress = {
                            showDeleteDialog.value = true
                        }
                    )
                })
            {
                Row(modifier = Modifier.padding(10.dp)){
                    Column{
                        Text(text = taskTitle, color = dueDate.second)
                        Text(text = dueDate.first, color = dueDate.second)
                        Text(text = frequency)
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    Column(horizontalAlignment = Alignment.End)
                        {
                        if (roommate != null||roommate?.username!=null) {
                            roommate.username?.let { Text(text = it) }
                        }else{
                            Text(text = "Unbekannter Benutzer")
                        }
                        /** button to check the task */
                        Button(onClick = {
                            showCheckDialog.value = true
                        },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = UdoLightBlue, containerColor = UdoWhite),
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
    private fun getTaskFrequency(frequency: Int) : String{
        if(frequency == 1) return "Täglich"
        if(frequency == 7) return "Wöchentlich"
        if(frequency % 7==0) return "Alle "+ frequency/7 + " Wochen"
        return "Alle $frequency Tage"
    }

    /**
     * returns the date of a task in the correct format with the priority color
     */
    private fun getTaskDate(date: Date): Pair<String,Color>{
        val c = Calendar.getInstance()
        val currentDay = c.get(Calendar.DATE)
        val currentMonth = c.get(Calendar.MONTH)+1
        val currentYear = c.get(Calendar.YEAR)
        val taskDay = date.date
        val taskMonth = date.month+1
        val taskYear = date.year+1900
        
        val taskDate = formatNumber(taskDay)+"."+formatNumber(taskMonth)+"."+taskYear.toString()
        // sorry for that
        when{
            // future
            taskYear>currentYear -> return Pair("Am " + taskDate + " fällig", UdoWhite)
            taskMonth>currentMonth -> return Pair("Am " + taskDate + " fällig", UdoWhite)
            taskDay==currentDay+1 -> return Pair("Morgen fällig", UdoOrange)
            // past
            taskYear<currentYear -> return Pair("War am " + taskDate + " fällig", UdoRed)
            taskMonth<currentMonth -> return Pair("War am " + taskDate + " fällig", UdoRed)
            taskDay==currentDay-1 -> return Pair("War gestern fällig", UdoRed)
            taskDay<currentDay -> return Pair("War am " + taskDate + " fällig", UdoRed)
            // today
            taskDay==currentDay -> return Pair("Heute fällig", UdoRed)
        }
        return Pair("Am " + taskDate + " fällig", UdoWhite)
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
        val sortedTaskList = unSortedTaskList.sortedWith(compareBy { it.dueDate })
        Column (modifier = Modifier
            .verticalScroll(scrollState)
        ){
            _binding.taskEmptyText.visibility = View.VISIBLE
            sortedTaskList.forEach { task ->
                if(_binding.taskEmptyText.visibility == View.VISIBLE) _binding.taskEmptyText.visibility = View.INVISIBLE

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
    @Composable
    fun CheckConfirmationDialog(showCheckDialog: Boolean,
                          onDismiss: (Boolean) -> Unit,
                                myTask: Task?){
        if (showCheckDialog) {
            AlertDialog(
                onDismissRequest = {onDismiss(false)},
                dismissButton = {
                    TextButton(onClick = {onDismiss(false)})
                    { Text(text = "Abbrechen", color = UdoRed) }
                },
                confirmButton = {
                    TextButton(onClick = {
                        if(myTask!=null){
                           dbWriter.checkTask(myTask)
                            myTask.points?.let {dbWriter.givePoints(dataHandler.user, it) }
                        }else{
                            Log.d(TAG,"No task to check")
                        }
                        onDismiss(false)
                    }
                        , shape = RoundedCornerShape(4.dp))
                    { Text(text = "Ich bin sicher") }
                },
                title = { Text(text = "Bestätigen") },
                text = { Text(text = "Sicher, dass du die Aufgabe erledigt hast?") }
            )
        }
    }
    @Composable
    fun DeleteConfirmationDialog(showDeleteDialog: Boolean,
                                onDismiss: (Boolean) -> Unit,
                                myTask: Task?){
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = {onDismiss(false)},
                dismissButton = {
                    TextButton(onClick = {onDismiss(false)})
                    { Text(text = "Abbrechen") }
                },
                confirmButton = {
                    TextButton(onClick = {
                        Log.d(TAG,"LÖSCHEN")
                        if(myTask!=null){
                           dbWriter.deleteTask(myTask.docId)
                        }else{
                            Log.d(TAG,"No task to delete")
                        }
                        onDismiss(false)
                    }
                        , shape = RoundedCornerShape(4.dp))
                    { Text(text = "Ja, sicher!", color = UdoRed) }
                },
                title = { Text(text = "Wirklich löschen?") },
                text = { Text(text = "Bist du sicher, dass du die Aufgabe löschen möchtest?") }
            )
        }
    }
    @Composable
    fun TasksFAB() {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.padding(10.dp)
        ) {
            FloatingActionButton(
                onClick = {
                  mainActivity.openCreateTaskFragment()
                          },
                modifier = Modifier
                    .requiredHeight(60.dp)
                    .requiredWidth(60.dp),
                shape = CircleShape,
                containerColor = UdoOrange
            ) { Text("+", color = UdoDarkBlue, fontSize = 30.sp) }
        }
    }
    fun setMainActivity(mainActivity: MainActivity){
        this.mainActivity=mainActivity
    }
}