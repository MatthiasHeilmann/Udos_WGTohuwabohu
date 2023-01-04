package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.udos_wg_tohuwabohu.databinding.FragmentCalendarBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.google.firebase.Timestamp


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null
    lateinit var composeView: ComposeView
    //Get Calendar data from Data Handler
    var calendarData = DataHandler.getInstance().getCalendar()

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
        var _binding: FragmentCalendarBinding? = null
        // This property is only valid between onCreateView and onDestroyView.
        var v: View = inflater.inflate(R.layout.fragment_calendar, container, false)
        // Dispose of the Composition when the view's LifecycleOwner
        // is destroyed
        //setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            // In  Compose world
            calendarData?.let { FullCalendar(it) }
            }
        return v
        }



        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_calendar, container, false)


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment calendarFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            CalendarFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}

    @Composable
    fun CalendarCard(i: String, shape: Shape, cardText: String){
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
    fun PreviewCalendarCard(){
                CalendarCard(i = "  8  ", shape = MaterialTheme.shapes.large, cardText = " Test")
    }

    @Composable
    fun FullCalendar(calendarData: ArrayList<HashMap<String, Timestamp>>){
        Column {
            calendarData.forEach { appointment: HashMap<String, Timestamp> ->
                CalendarCard(
                    i = appointment.values.first().toDate().day.toString(),
                    shape = MaterialTheme.shapes.large,
                    cardText = appointment.keys.first()
                )
            }
        }
}

/*@Composable
fun UdosTheme(
    colors: Colors,
    typography: Typography = MaterialTheme.typography,
    shapes: Shapes = MaterialTheme.shapes,
    content: @Composable () -> Unit
): Unit {
    CalendarCard(i = "Hello World!")
}
*/




















