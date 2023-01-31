package com.example.udos_wg_tohuwabohu.Finances

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentFinanceAddBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBWriter
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler

class FinanceAddFragment : Fragment() {
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
            MoucherDropdown()
        }
        return view
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MoucherDropdown() {
        val listItems = dataHandler.roommateList.values.map { r ->
            (r.docID) to (r.username ?: "unknown")
        }.toMap()
        val checkedList = remember { listItems.map { false }.toMutableStateList() }
        var expanded by remember { mutableStateOf(false) }

        val focusRequester = remember { FocusRequester() }
        val focusManager = LocalFocusManager.current

        // Up Icon when expanded and down icon when collapsed
        val icon = if (expanded)
            Icons.Filled.KeyboardArrowUp
        else
            Icons.Filled.KeyboardArrowDown
        Column(Modifier.padding(20.dp)) {
            OutlinedTextField(
                value = listCheckedNames.values.joinToString("\n"),
                onValueChange = { },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester)
                    .onFocusChanged { f ->
                        expanded = (f.isFocused && f.hasFocus)
                    }
                    .padding(horizontal = 64.dp, vertical = 10.dp),
                label = { Text("Schnorrer") },
                trailingIcon = {
                    Icon(
                        icon, "contentDescription",
                        Modifier.clickable { expanded = !expanded },
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

            // Create a drop-down menu
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    focusManager.clearFocus(true)
                    expanded = false
                },
                modifier = Modifier.background(UdoLightBlue)
            ) {
                listItems.entries.forEachIndexed { index, (docId, name) ->
                    DropdownMenuItem(
                        onClick = {
                            checkedList[index] = !checkedList[index]
                            if (checkedList[index])
                                listCheckedNames.put(docId, name)
                            else
                                listCheckedNames.remove(docId)
                        },
                        modifier = Modifier.background(UdoLightBlue),
                        text = {
                            Row() {
                                Checkbox(
                                    checked = checkedList[index],
                                    onCheckedChange = {
                                        checkedList[index] = it
                                        if (it)
                                            listCheckedNames.put(docId, name)
                                        else
                                            listCheckedNames.remove(docId)
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = UdoOrange
                                    )
                                )
                                Text(
                                    text = name,
                                    modifier = Modifier
                                        .offset(y = 15.dp)
                                        .fillMaxWidth()
                                        .clickable {
                                            checkedList[index] = !checkedList[index]
                                            if (checkedList[index])
                                                listCheckedNames.put(docId, name)
                                            else
                                                listCheckedNames.remove(docId)
                                        },
                                    color = UdoWhite
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }

    @Preview
    @Composable
    fun FinanceAddPreview() {
        MoucherDropdown()
    }
}