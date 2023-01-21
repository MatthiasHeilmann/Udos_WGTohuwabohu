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

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TasksFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TasksFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var composeView: ComposeView
    val TAG: String = "[TASKS FRAGMENT]"
    var tasksData = DataHandler.getInstance().getTasks()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
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
//        UdosTheme {
//            Card(colors= UdoCardTheme(),modifier = Modifier.requiredHeight(height = 80.dp)) {
//                Row {
//                    Card(colors = UdoDateCardTheme()) {
//                        Text(text = i,style = MaterialTheme.typography.displayMedium)
//                    }
//                    Text(text = cardText, style = MaterialTheme.typography.displayMedium) }
//            }
//        }
//        UdosTheme {
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment TasksFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            TasksFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
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