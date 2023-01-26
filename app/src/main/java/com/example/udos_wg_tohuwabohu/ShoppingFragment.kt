package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udos_wg_tohuwabohu.dataclasses.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*
import kotlin.collections.HashMap


class ShoppingFragment : Fragment() {


    lateinit var composeView: ComposeView
    private val TAG: String = "[SHOPPING FRAGMENT]"
    private val db = Firebase.firestore
    val dbWriter = DBWriter.getInstance()
    val dh = DataHandler.getInstance()

    var shoppingList = dh.wg?.shoppingList
    var roomateList = dh.roommateList

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val v: View = inflater.inflate(R.layout.fragment_shopping, container, false)

        val addItemButton:FloatingActionButton = v.findViewById(R.id.button_add_item)
        val deleteItemsButton:FloatingActionButton = v.findViewById(R.id.button_delete_items)
        val createInvoiceButton:FloatingActionButton = v.findViewById(R.id.button_create_invoice)
        val entryField:EditText = v.findViewById(R.id.addItemEntryField)


         // Button Item hinzufügen
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

                Toast.makeText(
                    requireActivity(),
                    "${entryItem} hinzugefügt.",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG,"${entryItem} hinzugefügt.")
            }
        }

        // Button Items löschen
        deleteItemsButton.setOnClickListener { v ->
            Log.d(TAG,"Items gelöscht")
            deleteItems(dh.wg?.shoppingList)
        }

        // Button Rechnung erstellen
        createInvoiceButton.setOnClickListener { v ->
            Log.d(TAG,"Rechnung erstellt.")
            // createBill()
        }

        // compose Komponente
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            dh.wg?.shoppingList?.let {showShoppingList(shoppingList = (it))}
        }
        return v
    }

    // TODO: delete checked Items
    fun deleteItems(shoppingList: SnapshotStateMap<String, Boolean>?){

        shoppingList?.forEach{ item ->
            if (item.value == true) {
                db.collection("wg")
                    .document(dh.wg!!.docID)
                    .update(
                        mapOf(
                            "einkaufsliste.${item.key}" to FieldValue.delete(),
                        )
                    )
                Log.d(TAG,"${item.key} gelöscht." )
            }
        }
        // todo: update shoppingList var
        // todo: update view
    }

    // TODO: createBill
    // TODO: update view after adding/deleting items
    // TODO: fix height of compose element


    @Composable
    fun createItemRow(item: Map.Entry<String, Boolean>) {
        var checkedState = remember { mutableStateOf(false) }

        Row {
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = {
                    checkedState.value = it;
                    /*checkShoppinglistItem(item, checkedState)*/
                    dbWriter.checkShoppinglistItem(item, checkedState)
                },
                Modifier.size(30.dp),
            )
            Text(
                modifier = Modifier
                    .clickable {
                        checkedState.value = !checkedState.value;
                        /*checkShoppinglistItem(item, checkedState)*/
                        dbWriter.checkShoppinglistItem(item, checkedState)
                    },
                text = item.key + " " + item.value.toString(),
                style = TextStyle(
                    color = Color.White,
                    fontSize = 20.sp,
                )
            )
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    fun showShoppingList(shoppingList: SnapshotStateMap<String, Boolean>) {

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

// TODO: create bill with checked items
/*fun createBill() {
    // bill button -> click -> open popup ->
        // every item with item.value = true will be shown
            // (optional: can add more items manually)
        // entryfield for price
        // every roommmate from users wg will be shown (user included)
            // checkable boxes
        // send button ->
            // ?? checked users (excluding self) will get notification
            // notification: confirm/refuse
                // confirm:
                // refuse:
            // on finished confirmation ->
                // user.balance + price
                // every checked roommate -> balance - (price/#ofBegünstigt)
        // cancel button -> close popup
}*/


// ---------------------------------------------------------
// OLD, now in dbwriter
/*  fun addItem(item: String) {
      db.collection("wg")
          .document(dh.wg!!.docID)
          .update(mapOf(
              "einkaufsliste.${item}" to false,
          ))

      // update shoppingList var
      // refresh view
  }

  fun checkShoppinglistItem(item: Map.Entry<String, Boolean>, checkedState: MutableState<Boolean>){
      db.collection("wg")
          .document(dh.wg!!.docID)
          .update(mapOf(
              "einkaufsliste.${item.key}" to checkedState.value,
          ));
      Log.d("[SHOPPING FRAGMENT]",item.key + " geändert zu " + checkedState.value);

      // update shoppingList var
  }*/