package com.example.udos_wg_tohuwabohu.Finances

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import com.example.udos_wg_tohuwabohu.R
import com.example.udos_wg_tohuwabohu.databinding.FragmentFinanceAddBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler

class FinanceAddFragment : Fragment() {
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
        _binding = FragmentFinanceAddBinding.inflate(inflater,container,false)
        val view = _binding.root

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
                        .fillMaxWidth(),
                    label = { Text("Schnorrer") },
                    trailingIcon = {
                        Icon(icon, "contentDescription",
                            Modifier.clickable { expanded = !expanded })
                    }
                )

                // Create a drop-down menu with list of cities,
                // when clicked, set the Text Field text as the city selected
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                ) {
                    listItems.forEachIndexed { index, name ->
                        println("making for: $name ($index)")
                        DropdownMenuItem(
                            onClick = {
                                checkedList[index] = !checkedList[index]
                                if (checkedList[index])
                                    listCheckedNames.add(name)
                                else
                                    listCheckedNames.remove(name)
                            },
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
                                    )
                                    Text(
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
                                        text = name
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}