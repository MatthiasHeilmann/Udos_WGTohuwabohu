package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.Task
import com.google.android.material.tabs.TabLayout.TabGravity
import java.util.*

/**
 * A simple [Fragment] subclass.
 * Use the [TasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TasksFragment : Fragment() {
    lateinit var composeView: ComposeView
    val TAG: String = "[TASKS FRAGMENT]"
    var tasksData = DataHandler.getInstance().getTasks()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("[TasksFragment]",tasksData.toString() )
        var _binding: FragmentTasksBinding? = null
        // This property is only valid between onCreateView and onDestroyView.
        var v: View = inflater.inflate(R.layout.fragment_tasks, container, false)
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
    fun TaskCard(taskTitle:String, dueDate: String, frequency:String, roommate: Roommate?){
            Card(colors = UdoCardTheme(), modifier = Modifier
//                .requiredHeight(height = 100.dp)
//                .requiredWidthIn(300.dp, 300.dp)
                .padding(5.dp)){
                Row(modifier = Modifier.padding(10.dp)){
                    Column{
                        Text(text = taskTitle)
                        Text(text = dueDate)
                        Text(text = frequency)
                    }
                    Spacer(modifier = Modifier.weight(1.0f))
                    Column(horizontalAlignment = Alignment.End)
                        {
                        Text(text = "Matthias Heilmann")
                        Button(onClick = {
                             // TODO button function
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

    @Preview
    @Composable
    fun PreviewTaskCard(){
        TaskCard("Müll rausbringen","23.01.2023","Wöchentlich",null)
    }

    @Composable
    fun FullTasks(taskData: HashMap<String, Task>){
        Column {
            tasksData?.forEach { task ->
                task.value.name?.let {
                    TaskCard(
                        taskTitle = it,
                        dueDate = task.value.dueDate.toString(),
                        frequency = task.value.frequency.toString(),
                        roommate = null
                    )
                }
            }
        }
    }

    fun getErlediger(){
        //TODO Wer muss die nächste Aufgabe erledigen? brauchen hierfür eine Regel
        //evtl. der, der am wenigesten guteNudel punkte hat oder gerade am wenigsten aufgaben hat?
    }
    fun createTask(frist: Date, frequencyInDays: Int, name: String, points: Int){
        //TODO neue Task erstellen
        //getErlediger()
    }
    fun checkTask(name: String){
        //TODO Aufgabe abhaken
        //getErlediger()
        //frist -> today + frequenz
    }
    fun deleteTask(name: String){
        //TODO Aufgabe löschen
    }
}