package com.example.udos_wg_tohuwabohu.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.databinding.FragmentCalendarBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.google.firebase.Timestamp
import java.text.DateFormatSymbols
import java.util.*


class CalendarFragment : Fragment() {
    private lateinit var composeView: ComposeView
    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentCalendarBinding

    //Get Calendar data from Data Handler
    private var calendarData = DataHandler.getInstance().getCalendar()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        // This property is only valid between onCreateView and onDestroyView.
        val v: View = _binding.root
        // Dispose of the Composition when the view's LifecycleOwner
        // is destroyed
        //setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)

        composeView = _binding.composeView
        composeView.setContent {
            // In  Compose world
            calendarData?.let { FullCalendar(it) }
            CalendarFAB()
            //MaterialDatePicker.Builder.datePicker().setTitleText("Select a date")
            //    .build().show(supportFragmentManager, "DATE_PICKER")
        }
        return v
    }

    fun setMainActivity(mainActivity: MainActivity) {
        Log.d("Calendar SETTTTTTTTTTTTTTTTTTTTTTTTTTTTTT", "; ")
        this.mainActivity = mainActivity
    }


// Inflate the layout for this fragment
//return inflater.inflate(R.layout.fragment_calendar, container, false)

    @Composable
    fun CalendarCard(date: String, time: String, cardText: String) {
        UdosTheme {
            Card(
                colors = UdoCardTheme(), modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(colors = UdoDateCardTheme()) {
                        Text(
                            text = date.padStart(2, "0".single()),
                            style = MaterialTheme.typography.displaySmall,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(5.dp),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text(
                            text = "Ab $time Uhr",
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(5.dp)
                        )
                    }
                }
                Text(
                    text = cardText,
                    fontSize = 24.sp,
                    modifier = Modifier.padding(35.dp, 5.dp, 5.dp, 10.dp)
                )
            }
        }
    }

    @Composable
    fun FullCalendar(calendarData: ArrayList<HashMap<String, Timestamp>>) {
        val sortedCalendarData =
            calendarData.sortedWith(compareBy { it.get(key = it.keys.first()) })
        var currentMonth = 100
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(5.dp)
        ) {
            sortedCalendarData.forEach { appointment: MutableMap<String, Timestamp> ->
                val month = appointment.values.first().toDate().month
                if (currentMonth != month) {
                    currentMonth = month
                    Text(
                        text = DateFormatSymbols.getInstance().months[month],
                        textAlign = TextAlign.End,
                        color = UdoWhite
                    )
                    Divider(
                        thickness = 2.dp,
                        color = UdoWhite,
                        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 5.dp)
                    )
                }
                Log.d("Calendar Date:", appointment.values.first().toDate().toString())
                CalendarCard(
                    date = appointment.values.first().toDate().date.toString(),
                    time = appointment.values.first()
                        .toDate().hours.toString() + ":" + appointment.values.first()
                        .toDate().minutes.toString().padStart(2, "0".single()),
                    cardText = appointment.keys.first()
                )
            }
        }
    }

    @Composable
    fun CalendarFAB() {

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
            modifier = Modifier.padding(10.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    mainActivity.showCalendarAddFragment()
                },
                modifier = Modifier
                    .requiredHeight(60.dp)
                    .requiredWidth(60.dp),
                shape = CircleShape,
                containerColor = UdoOrange
            ) { Text("+", color = UdoDarkBlue, fontSize = 30.sp) }

        }
    }
}

















