package com.example.udos_wg_tohuwabohu.Finances

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.TextFieldColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udos_wg_tohuwabohu.*
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentFinanceAddBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler

class FinanceAddFragment : Fragment() {
    private lateinit var mainActivity: MainActivity
    private lateinit var _binding: FragmentFinanceAddBinding
    private lateinit var composeView: ComposeView
    val dataHandler = DataHandler.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFinanceAddBinding.inflate(inflater, container, false)
        val view = _binding.root

        _binding.cancelCreateButton.setOnClickListener {
            mainActivity.showFinanceFragment()
        }

        _binding.buttonCreateFinance.setOnClickListener {
            // TODO create finance
            mainActivity.showFinanceFragment()
        }

        composeView = view.findViewById(R.id.compose_view)
        composeView.setContent {
            TestDropdown()
        }
        return view
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TestDropdown() {
        val listItems = dataHandler.roommateList.values.map { r -> r.username ?: "unknown" }
        val listCheckedNames = remember { mutableStateListOf<String>() }
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
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Create an Outlined Text Field
                // with icon and not expanded
                OutlinedTextField(
                    value = listCheckedNames.joinToString("\n"),
                    onValueChange = { },
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .onFocusChanged { f ->
                            expanded = (f.isFocused && f.hasFocus)
                        }
                        .padding(64.dp,10.dp),
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
                    listItems.forEachIndexed { index, name ->
                        DropdownMenuItem(
                            onClick = {
                                checkedList[index] = !checkedList[index]
                                if (checkedList[index])
                                    listCheckedNames.add(name)
                                else
                                    listCheckedNames.remove(name)
                            },
                            modifier = Modifier.background(UdoLightBlue),
                            text = {
                                Row() {
                                    Checkbox(
                                        checked = checkedList[index],
                                        onCheckedChange = {
                                            checkedList[index] = it
                                            if (it)
                                                listCheckedNames.add(name)
                                            else
                                                listCheckedNames.remove(name)
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
                                                    listCheckedNames.add(name)
                                                else
                                                    listCheckedNames.remove(name)
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
    }

    @Preview
    @Composable
    fun FinanceAddPreview() {
        TestDropdown()
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }
}