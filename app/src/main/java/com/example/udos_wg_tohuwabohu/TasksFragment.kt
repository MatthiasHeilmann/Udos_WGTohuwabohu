package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.udos_wg_tohuwabohu.databinding.FragmentCalendarBinding
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Task
import com.google.firebase.Timestamp
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
    fun TaskCard(i: String, shape: Shape, cardText: String){
        UdosTheme {
            Card(colors= UdoCardTheme(),modifier = Modifier.requiredHeight(height = 80.dp)) {
                Row {
                    Card(colors = UdoDateCardTheme()) {
                        Text(text = i,style = MaterialTheme.typography.displayMedium)
                    }
                    Text(text = cardText, style = MaterialTheme.typography.displayMedium) }
            }
        }
    }

    @Preview
    @Composable
    fun PreviewTaskCard(){
        TaskCard(i = "  8  ", shape = MaterialTheme.shapes.large, cardText = "Task Test")
    }

    @Composable
    fun FullTasks(taskData: HashMap<String, Task>){
        Column {
//            taskData.forEach { appointment: HashMap<String, Timestamp> ->
//                TaskCard(
//                    i = appointment.values.first().toDate().day.toString(),
//                    shape = MaterialTheme.shapes.large,
//                    cardText = appointment.keys.first()
//                )
//            }
            tasksData?.forEach { task ->
                TaskCard(
                    i = task.key,
                    shape = MaterialTheme.shapes.large,
                    cardText = task.value.name ?: "default"
                )
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