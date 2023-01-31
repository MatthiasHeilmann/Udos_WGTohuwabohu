package com.example.udos_wg_tohuwabohu.Calendar

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.drawable.Icon
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.util.Xml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TimePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role.Companion.Image
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Popup
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentAddCalendarBinding
import com.example.udos_wg_tohuwabohu.databinding.FragmentFinanceAddBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.google.android.material.internal.ThemeEnforcement.obtainStyledAttributes
import com.google.firebase.Timestamp
import org.xmlpull.v1.XmlPullParser
import java.sql.Time
import java.util.*

class CalendarAddFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentAddCalendarBinding
    private lateinit var composeView: ComposeView
    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()

    private var dateChosen = mutableStateOf(Date().apply { year += 1900 })
    private var timeChosen = mutableStateOf(Time(Date().time))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddCalendarBinding.inflate(inflater, container, false)
        val view = _binding.root

        val cancelButton: Button = _binding.cancelCreateButton
        val createAppointmentButton: Button = _binding.buttonCreateAppointment
        val descriptionText: EditText = _binding.nameOfTaskToCreate

        createAppointmentButton.setOnClickListener {
            val description = descriptionText.text.toString()
            val date = dateChosen.value.apply { year -= 1900 }
            val time = timeChosen.value

            val summedDate = Date(date.year, date.month, date.date, time.hours, time.minutes)
            val summedTimestamp = Timestamp(summedDate)

            val validEntries = validateCalendarEntry(description, summedTimestamp)

            if (validEntries)
                dbWriter.createCalendarEntry(description, summedTimestamp)
            else {
                Log.w("Validation Error", "Please enter a description and a time in the future!")
            }

            mainActivity.showCalendarFragment()
        }

        cancelButton.setOnClickListener {
            mainActivity.showCalendarFragment()
        }

        composeView = view.findViewById(R.id.compose_view)
        composeView.setContent {
            DateAndTimePicker()
        }
        return view
    }

    @Composable
    fun DateAndTimePicker() {
        val datePickerActive = rememberSaveable { mutableStateOf(false) }
        val timePickerActive = rememberSaveable { mutableStateOf(false) }
        var dateChanged by rememberSaveable { mutableStateOf(false) }
        var timeChanged by rememberSaveable { mutableStateOf(false) }

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        if (timePickerActive.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = {
                        focusManager.clearFocus(true)
                        timePickerActive.value = false
                    },
                    properties = UdoPopupProperties()
                ) {
                    showTimePicker(
                        onTimeChange = {
                            timeChosen.value = it
                            timeChanged = true
                        },
                        onDismiss = {
                            focusManager.clearFocus(true)
                            timePickerActive.value = it
                        }
                    )
                }
            }
        } else if (datePickerActive.value) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Popup(
                    alignment = Alignment.Center,
                    onDismissRequest = {
                        focusManager.clearFocus(true)
                        datePickerActive.value = false
                    },
                    properties = UdoPopupProperties()
                ) {
                    showDatePicker(
                        onDateChange = {
                            dateChosen.value = it
                            dateChanged = true
                        },
                        onDismiss = { datePickerActive.value = it })
                }
            }
        }

        Column {
            Box(modifier = Modifier.padding(5.dp)) {
                OutlinedTextFieldWithPopup(
                    value = formatDate(dateChosen.value),
                    label = "Datum",
                    popupBinding = datePickerActive,
                    focusRequester = focusRequester,
                    painter = painterResource(R.drawable.ic_baseline_calendar_month_24)
                )
            }

            Box(modifier = Modifier.padding(5.dp)) {
                OutlinedTextFieldWithPopup(
                    value = formatNumber(timeChosen.value.hours) +
                            ":" + formatNumber(timeChosen.value.minutes),
                    label = "Zeitpunkt",
                    popupBinding = timePickerActive,
                    focusRequester = focusRequester,
                    painter = painterResource(R.drawable.ic_baseline_access_time_24)
                )
            }
        }

    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun OutlinedTextFieldWithPopup(
        value: String,
        label: String,
        popupBinding: MutableState<Boolean>,
        focusRequester: FocusRequester,
        painter: Painter
    ) {

        OutlinedTextField(
            value = value,
            onValueChange = { },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { f ->
                    popupBinding.value = (f.isFocused && f.hasFocus)
                }
                .padding(horizontal = 64.dp, vertical = 10.dp),
            label = { Text(label) },
            trailingIcon = {
                Icon(
                    painter,
                    "$label auswählen",
                    Modifier.clickable { popupBinding.value = !popupBinding.value },
                    tint = UdoWhite
                )
            },
            textStyle = TextStyle(
                color = UdoWhite,
                fontSize = 20.sp
            ),
            colors = TextFieldDefaults.textFieldColors(
                focusedLabelColor = UdoWhite,
                focusedIndicatorColor = UdoWhite,
                unfocusedLabelColor = UdoWhite,
                unfocusedIndicatorColor = UdoWhite,
                containerColor = Color.Transparent
            )
        )
    }

    @Composable
    fun showDatePicker(
        onDateChange: (Date) -> Unit,
        onDismiss: (Boolean) -> Unit
    ) {
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
                    .background(Color.Transparent, shape = RoundedCornerShape(4.dp)),
                update = { views ->
                    views.setOnDateChangeListener { _, i, i2, i3 ->
                        onDateChange(Date(i, i2, i3))
                    }
                }
            )
            Button(
                onClick = { onDismiss(false) },
                modifier = Modifier
                    .requiredWidth(190.dp)
                    .requiredHeight(50.dp)
                    .padding(5.dp)
                    .align(Alignment.CenterHorizontally),
                colors = UdoPopupButtonColors()
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
    fun showTimePicker(
        onTimeChange: (Time) -> Unit,
        onDismiss: (Boolean) -> Unit
    ) {
        Card(
            colors = UdoPopupCardTheme(),
            modifier = Modifier
                .requiredHeight(height = 500.dp)
                .requiredWidth(width = 300.dp)
                .padding(30.dp),
            border = BorderStroke(2.dp, UdoDarkBlue)
        ) {
            AndroidView(
                {

                    val mTimePicker = TimePicker(
                        it
                    )
                    mTimePicker.setIs24HourView(true)
                    return@AndroidView mTimePicker
                },
                modifier = Modifier
                    .wrapContentWidth()
                    .background(Color.Transparent, shape = RoundedCornerShape(4.dp)),
                update = { views ->
                    views.setOnTimeChangedListener { timePicker, hour, minute ->
                        onTimeChange(Time(hour, minute, 0))
                    }
                }
            )
            Button(
                onClick = { onDismiss(false) },
                modifier = Modifier
                    .requiredWidth(190.dp)
                    .requiredHeight(50.dp)
                    .padding(5.dp)
                    .align(Alignment.CenterHorizontally),
                colors = UdoPopupButtonColors()
            ) {
                Text(
                    "Speichern",
                    color = UdoWhite,
                    fontSize = 15.sp
                )
            }
        }
    }

    private fun formatDate(date: Date): String {
        return "${formatNumber(date.date)}.${formatNumber(date.month + 1)}." +
                formatNumber(date.year)
    }

    private fun formatNumber(n: Int): String {
        return if (n > 9) "" + n else "0" + n
    }

    private fun validateCalendarEntry(message: String, summedTimestamp: Timestamp): Boolean {
        return (summedTimestamp.seconds > Timestamp.now().seconds) and (message != "")
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }

    @Preview
    @Composable
    fun test() {
        DateAndTimePicker()
    }
}