
package com.example.udos_wg_tohuwabohu

import android.nfc.Tag
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.children
import androidx.core.view.get
import com.example.udos_wg_tohuwabohu.databinding.FragmentCalendarBinding
import com.example.udos_wg_tohuwabohu.databinding.FragmentShoppingBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.ArrayList


class ShoppingFragment : Fragment() {


    lateinit var composeView: ComposeView
    private lateinit var _binding:FragmentShoppingBinding
    private val TAG: String = "[SHOPPING FRAGMENT]"
    private val db = Firebase.firestore
    private val dbWriter = DBWriter.getInstance()
    private val dh = DataHandler.getInstance()
    private var shoppingList = dh.wg.first().shoppingList


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShoppingBinding.inflate(inflater,container,false)
        val v: View = _binding.root

        val addItemButton:FloatingActionButton = _binding.buttonAddItem
        val deleteItemsButton:FloatingActionButton = _binding.buttonDeleteItems
        val entryField:EditText = _binding.addItemEntryField



        // add items button
        addItemButton.setOnClickListener { v ->
            // test logs
            Log.d(TAG,"Button geklickt")
            Log.d(TAG,shoppingList.toString() )

            if(TextUtils.isEmpty(entryField.text.toString().trim{it <= ' '})
                ){
                Log.d(TAG, "Eingabefeld war leer")
                Toast.makeText(
                    requireActivity(),
                    "Bitte Artikel eintragen.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {
                val entryItem = entryField.text.toString()
                /*addItem(entryItem)*/
                dbWriter.addItemToShoppingList(entryItem)
                _binding.addItemEntryField.text.clear()
                Toast.makeText(
                    requireActivity(),
                    "${entryItem} hinzugefügt.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG,"${entryItem} hinzugefügt.")
            }
        }

        // delete items button
        deleteItemsButton.setOnClickListener { v ->
            Log.d(TAG,"Items gelöscht")
            deleteItems(dh.wg.first().shoppingList)
        }


        // compose component
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            dh.wg.first().shoppingList?.let {showShoppingList(shoppingList = (it))}
        }
        return v
    }

    fun deleteItems(shoppingList: SnapshotStateMap<String, Boolean>?){
        shoppingList?.forEach{ item ->
            if (item.value) {
                db.collection("wg")
                    .document(dh.wg.first().docID)
                    .update(
                        mapOf(
                            "einkaufsliste.${item.key}" to FieldValue.delete(),
                        )
                    )
                Log.d(TAG,"${item.key} gelöscht." )
            }
        }
        // sets all checkboxes to false, otherwise the boxes at the indices of the deleted boxes would stay checked
        boxStates.forEach{state ->
            state.value = false
        }
    }

    // holds the mutable states of the checkboxes
    val boxStates: ArrayList<MutableState<Boolean>> = ArrayList()

    @Composable
    fun createItemRow(item: Map.Entry<String, Boolean>) {
        val checkedState = remember { mutableStateOf(false) }
        boxStates.add(checkedState)
        Row (modifier = Modifier.padding(45.dp,5.dp,45.dp,1.dp)){
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = {
                    checkedState.value = it;
                    /*checkShoppinglistItem(item, checkedState)*/
                    dbWriter.checkShoppinglistItem(item, checkedState)
                },
                Modifier.size(30.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = UdoOrange
                )
            )
            Text(
                modifier = Modifier
                    .clickable {
                        checkedState.value = !checkedState.value;
                        /*checkShoppinglistItem(item, checkedState)*/
                        dbWriter.checkShoppinglistItem(item, checkedState)
                    }.padding(start = 10.dp),
                text = item.key,
                style = TextStyle(
                    color = UdoWhite,
                    fontSize = 20.sp,
                )
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun showShoppingList(shoppingList: SnapshotStateMap<String, Boolean>) {

        // list with items
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(
                        state = rememberScrollState(),
                        enabled = true,
                    ),
            ) {
                shoppingList.forEach { item ->
                    createItemRow(item);
                }
            }

    }

}

