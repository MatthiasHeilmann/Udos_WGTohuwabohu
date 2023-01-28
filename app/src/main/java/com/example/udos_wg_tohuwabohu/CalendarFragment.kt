package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.TimePicker
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import com.example.udos_wg_tohuwabohu.databinding.FragmentCalendarBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.google.android.material.internal.ContextUtils.getActivity
import com.google.firebase.Timestamp
import java.sql.Time
import java.text.DateFormatSymbols
import java.util.Date


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [CalendarFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CalendarFragment : Fragment() {
    lateinit var composeView: ComposeView

    //Get Calendar data from Data Handler
    var calendarData = DataHandler.getInstance().getCalendar()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            CalendarFAB()
            //MaterialDatePicker.Builder.datePicker().setTitleText("Select a date")
            //    .build().show(supportFragmentManager, "DATE_PICKER")
        }
        return v
    }
}


        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_calendar, container, false)

@OptIn(ExperimentalUnitApi::class)
@Composable
fun CalendarCard(date: String, time: String, shape: Shape, cardText: String){
    UdosTheme {
        Card(colors= UdoCardTheme(),modifier = Modifier.fillMaxWidth().padding(5.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Card(colors = UdoDateCardTheme()) {
                    Text(text = date.padStart(2,"0".single()),style = MaterialTheme.typography.displaySmall)
                }
                Box(modifier= Modifier.fillMaxWidth().padding(5.dp), contentAlignment= Alignment.CenterEnd){
                    Text(text= " " + time + " Uhr", textAlign = TextAlign.End)
                }
            }
            Text(text =" "+ cardText, fontSize = 24.sp)
        }
    }
}

@Composable
fun FullCalendar(calendarData: MutableList<MutableMap<String, Timestamp>>){
    var sortedCalendarData = calendarData.sortedWith(compareBy { it.get(key= it.keys.first()) })
    var currentMonth = 100
    Column (modifier = Modifier
            .verticalScroll(rememberScrollState()).padding(5.dp)){
        sortedCalendarData.forEach { appointment: MutableMap<String, Timestamp> ->
            if(currentMonth != appointment.values.first().toDate().month){
                currentMonth = appointment.values.first().toDate().month
                Text(text= DateFormatSymbols.getInstance().months[appointment.values.first().toDate().month], textAlign = TextAlign.End)
                Divider(thickness= 2.dp,color= UdoDarkGray)
            }
            Log.d("Calendar Date:", appointment.values.first().toDate().toString())
            CalendarCard(
                date  = appointment.values.first().toDate().date.toString(),
                time  = appointment.values.first().toDate().hours.toString()+":"+appointment.values.first().toDate().minutes.toString().padStart(2,"0".single()),
                shape = MaterialTheme.shapes.large,
                cardText = appointment.keys.first()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun CalendarPopup(onDismiss: (Boolean) -> Unit) {
    var descriptionText by remember { mutableStateOf("Hello") }
    var datePickerActive by rememberSaveable { mutableStateOf(false) }
    var timePickerActive by rememberSaveable { mutableStateOf(false) }
    var dateChanged by rememberSaveable { mutableStateOf(false) }
    var timeChanged by rememberSaveable { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    var dateChosen by rememberSaveable { mutableStateOf(java.util.Date(0)) }
    var timeChosen by rememberSaveable { mutableStateOf(java.sql.Time(0,0,0)) }

    if (timePickerActive) {
        Box(
            contentAlignment = Alignment.Center, // you apply alignment to all children
            modifier = Modifier.fillMaxSize()
        ) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { timePickerActive = false },
                properties = UdoPopupProperties()
            ) {
                showTimePicker(timeChosen, onTimeChange = {
                    timeChosen = it
                    timeChanged = true
                }, onDismiss = { timePickerActive = it })
            }
        }
    }
    else if (datePickerActive) {
        Box(
            contentAlignment = Alignment.Center, // you apply alignment to all children
            modifier = Modifier.fillMaxSize()
        ) {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { datePickerActive = false },
                properties = UdoPopupProperties()
            ) {
                showDatePicker(dateChosen, onDateChange = {
                    dateChosen = it
                    dateChanged = true
                }, onDismiss = { datePickerActive = it })
            }
        }
    } else {
        Card(
            colors = UdoPopupCardTheme(),
            modifier = Modifier
                .requiredHeight(height = 450.dp)
                .requiredWidth(width = 300.dp)
                .padding(2.dp),
            border = BorderStroke(2.dp, UdoDarkBlue)
        ) {
            Column(modifier = Modifier.padding(5.dp),) {
                Text(text = "Termin Hinzufügen", style = MaterialTheme.typography.popupLabel)
                Text(text = "Termin Beschreibung", style = MaterialTheme.typography.popupLabel)
                OutlinedTextField(
                    value = descriptionText,
                    onValueChange = { text: String -> descriptionText = text },
                    modifier = Modifier
                        .requiredHeight(height = 100.dp)
                        .requiredWidth(width = 300.dp),
                    enabled = true,
                    readOnly = false,
                    colors = UdoPopupTextfieldColors(),
                    keyboardOptions = UdoKeyboardOptions(),
                    keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                )
                Text(text = "Termin Start", style = MaterialTheme.typography.popupLabel)
                Row(modifier = Modifier.padding(5.dp)) {

                    Button(onClick = { datePickerActive = true },
                        modifier = Modifier
                            .requiredWidth(200.dp)
                            .requiredHeight(50.dp)
                            .padding(5.dp)
                    ) {
                        Text(
                            "  Datum Auswählen  ",
                            color = UdoWhite,
                            fontSize = 15.sp
                        )
                    }
                    Card(colors = UdoUnfocusableCardTheme(),
                        modifier = Modifier
                            .requiredWidth(width = 100.dp)
                            .requiredHeight(height = 50.dp)
                            .padding(5.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if(dateChanged) {
                                Text(
                                    text = dateChosen.date.toString() + "." + (dateChosen.month + 1).toString() + "." + dateChosen.year.toString(),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                Row(modifier = Modifier.padding(5.dp)) {
                    Button(onClick = { timePickerActive = true },
                        modifier = Modifier
                            .requiredWidth(200.dp)
                            .requiredHeight(50.dp)
                            .padding(4.dp)
                    ) {
                        Text(
                            "Zeitpunkt Auswählen",
                            color = UdoWhite,
                            fontSize = 15.sp,
                        textAlign= TextAlign.Center)
                    }
                    Card(colors = UdoUnfocusableCardTheme(),
                        modifier = Modifier
                            .requiredWidth(width = 100.dp)
                            .requiredHeight(height = 50.dp)
                            .padding(5.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if(timeChanged) {
                                Text(
                                    text = timeChosen.hours.toString() + ":" + (timeChosen.minutes).toString()
                                        .padStart(2, "0".single()),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }
                    }
                }
                Button(onClick = { val validChange=validateCalendarEntry(descriptionText,dateChosen, timeChosen)
                                 if(validChange){ onDismiss(false)}},
                    modifier = Modifier
                        .requiredWidth(290.dp)
                        .requiredHeight(75.dp)
                        .padding(5.dp)){
                            Text(text= "Termin Speichern")
                        }
            }


            //DatePickerDialog(LocalContext.current)
        }
    }
}


@Composable
fun showDatePicker(dateVar: java.util.Date, onDateChange: (java.util.Date) -> Unit, onDismiss: (Boolean) -> Unit) {
    //var dateChosen by remember { mutableStateOf(Timestamp(0,0)) }
    Card(
        colors = UdoPopupCardTheme(),
        modifier = Modifier
            .requiredHeight(height = 450.dp)
            .requiredWidth(width = 300.dp)
            .padding(30.dp),
        border = BorderStroke(2.dp, UdoDarkBlue)
    ) {
        AndroidView(
            { CalendarView(it) },
            modifier = Modifier
                .wrapContentWidth()
                .background(UdoWhite, shape = RoundedCornerShape(4.dp)),
            update = { views ->
                views.setOnDateChangeListener { calendarView, i, i2, i3 ->
                    //var tempTimeVar = Timestamp(java.util.Date(i,i2+1,i3+1))
                    onDateChange(java.util.Date(i,i2,i3))
                }
            }
        )
        Button(onClick = { onDismiss(false) },
            modifier = Modifier
                .requiredWidth(190.dp)
                .requiredHeight(50.dp)
                .padding(5.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                "Speichern",
                color = UdoWhite,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun showTimePicker(timeVar: java.sql.Time, onTimeChange: (java.sql.Time) -> Unit, onDismiss: (Boolean) -> Unit) {
    Card(
        colors = UdoPopupCardTheme(),
        modifier = Modifier
            .requiredHeight(height = 500.dp)
            .requiredWidth(width = 300.dp)
            .padding(30.dp),
        border = BorderStroke(2.dp, UdoDarkBlue)
    ) {
        AndroidView(
            { var mTimePicker=TimePicker(it)
            mTimePicker.setIs24HourView(true)
                return@AndroidView mTimePicker
            },
            modifier = Modifier
                .wrapContentWidth()
                .background(UdoWhite, shape = RoundedCornerShape(4.dp)),
            update = { views ->
                views.setOnTimeChangedListener { timePicker, hour, minute ->
                    onTimeChange(java.sql.Time(hour, minute, 0))
                }
            }
        )
        Button(onClick = { onDismiss(false) },
            modifier = Modifier
                .requiredWidth(190.dp)
                .requiredHeight(50.dp)
                .padding(5.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Text(
                "Speichern",
                color = UdoWhite,
                fontSize = 15.sp
            )
        }
    }
}

@Composable
fun CalendarFAB() {
    var popupActive by remember { mutableStateOf(false) }
    if (popupActive) {
        UdosTheme {
            Popup(
                alignment = Alignment.Center,
                onDismissRequest = { popupActive = false },
                properties = UdoPopupProperties()
            ) {
                CalendarPopup(onDismiss={popupActive=it})
            }
        }
    } else {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.Bottom,
        ) {
            FloatingActionButton(
                onClick = { popupActive = true },
                modifier = Modifier
                    .requiredHeight(75.dp)
                    .requiredWidth(75.dp),
                shape = CircleShape,
                containerColor = Color(0xff30475e)
            ) { Text("+", color = UdoWhite, fontSize = 30.sp) }
        }
    }
}

fun validateCalendarEntry(message: String, date: java.util.Date, time: Time): Boolean{
    Log.w( "validateCalendarEntry: ",message+ date.date.toString()+" "+date.month.toString()+" "+date.year.toString()+" "+time.hours.toString()+" "+time.minutes.toString()+" "+time.seconds.toString() )
    val summedDate= Date(date.year,date.month,date.date,time.hours,time.minutes)
    val summedTimestamp= Timestamp(summedDate)
    if ((summedTimestamp.seconds>Timestamp.now().seconds) and (message!="")){
        var dbw = DBWriter.getInstance()
        dbw.createCalendarEntry(message, summedTimestamp)
        return true
    }
    else{
    Log.w("Validation Error","Please enter a description and a time in the future!")
        return false
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



















