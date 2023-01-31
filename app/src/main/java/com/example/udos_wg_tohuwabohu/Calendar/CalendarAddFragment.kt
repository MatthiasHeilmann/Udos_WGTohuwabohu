package com.example.udos_wg_tohuwabohu.Calendar

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentFinanceAddBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import java.sql.Time
import java.util.*

class CalendarAddFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentFinanceAddBinding
    private lateinit var composeView: ComposeView
    private val dataHandler = DataHandler.getInstance()
    private val dbWriter = DBWriter.getInstance()

    private val listCheckedNames = mutableStateMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceAddBinding.inflate(inflater, container, false)
        val view = _binding.root

        val createFinanceButton: Button = _binding.buttonCreateFinance
        val cancelButton: Button = _binding.cancelCreateButton
        val priceText: EditText = _binding.entryPrice
        val descriptionText: EditText = _binding.entryDescription

        createFinanceButton.setOnClickListener {
            val description = descriptionText.text.toString()
            val price = priceText.text.toString().toDouble()
            val moucherIDs = listCheckedNames.keys.toTypedArray()

            dbWriter.createFinanceEntry(description, price, moucherIDs.toList())
            mainActivity.showFinanceFragment()
        }

        cancelButton.setOnClickListener {
            mainActivity.showFinanceFragment()
        }

        composeView = view.findViewById(R.id.compose_view)
        composeView.setContent {
            // Date and Time picker
        }
        return view
    }

    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun DateAndTimePicker(){
        var datePickerActive by rememberSaveable { mutableStateOf(false) }
        var timePickerActive by rememberSaveable { mutableStateOf(false) }
        var dateChanged by rememberSaveable { mutableStateOf(false) }
        var timeChanged by rememberSaveable { mutableStateOf(false) }
        var dateChosen by rememberSaveable { mutableStateOf(Date(0)) }
        var timeChosen by rememberSaveable { mutableStateOf(Time(0, 0, 0)) }

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
        } else if (datePickerActive) {
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
        }

        Row(modifier = Modifier.padding(5.dp)) {

            Button(
                onClick = { datePickerActive = true },
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
            Card(
                colors = UdoUnfocusableCardTheme(),
                modifier = Modifier
                    .requiredWidth(width = 100.dp)
                    .requiredHeight(height = 50.dp)
                    .padding(5.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (dateChanged) {
                        Text(
                            text = "${dateChosen.date}.${dateChosen.month + 1}.${dateChosen.year}",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Row(modifier = Modifier.padding(5.dp)) {
            Button(
                onClick = { timePickerActive = true },
                modifier = Modifier
                    .requiredWidth(200.dp)
                    .requiredHeight(50.dp)
                    .padding(4.dp)
            ) {
                Text(
                    "Zeitpunkt Auswählen",
                    color = UdoWhite,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center
                )
            }
            Card(
                colors = UdoUnfocusableCardTheme(),
                modifier = Modifier
                    .requiredWidth(width = 100.dp)
                    .requiredHeight(height = 50.dp)
                    .padding(5.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (timeChanged) {
                        Text(
                            text = timeChosen.hours.toString() + ":"
                                    + (timeChosen.minutes).toString()
                                .padStart(2, "0".single()),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }
}