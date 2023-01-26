package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.PullRefreshState
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBLoader
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.Task
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

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ShoppingFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ShoppingFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var composeView: ComposeView
    private val TAG: String = "[SHOPPING FRAGMENT]"
    private val db = Firebase.firestore

    val dh = DataHandler.getInstance()
    var shoppingList = dh.wg?.shoppingList

    var checkboxesArrayList: ArrayList<String> = ArrayList()

    // testing
    // var roomateList = dh.roommateList



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
                Log.d(TAG,"${entryItem} hinzugefügt.")
                addItem(entryItem)
                Toast.makeText(
                    requireActivity(),
                    "${entryItem} hinzugefügt.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Button Items löschen
        deleteItemsButton.setOnClickListener { v ->
            Log.d(TAG,"Items gelöscht")
            // deleteItems()
        }

        // Button Rechnung erstellen
        createInvoiceButton.setOnClickListener { v ->
            Log.d(TAG,"Rechnung erstellt.")
            // createBill()
        }

        // compose Komponente
        composeView = v.findViewById(R.id.compose_view)
        composeView.setContent {
            shoppingList?.let {showShoppingList(shoppingList = (it))}
        }
        return v
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ShoppingFragment.
         */
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ShoppingFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun addItem(item: String) {
        db.collection("wg")
            .document(dh.wg!!.docID)
            .update(mapOf(
                "einkaufsliste.${item}" to false,
            ))
        // refresh view
    }

    fun checkShoppinglistItem(item: Map.Entry<String, Boolean>, checkedState: MutableState<Boolean>){
        db.collection("wg")
            .document(dh.wg!!.docID)
            .update(mapOf(
                "einkaufsliste.${item.key}" to checkedState.value,
            ));
        Log.d("[SHOPPING FRAGMENT]",item.key + " geändert zu " + checkedState.value);
    }

    // TODO: delete Items
    fun deleteItems(){
        // every item with item.value = true will be deleted from db
        // update list

        db.collection("wg")
            .document(dh.wg!!.docID)
            .update(mapOf(
                "einkaufsliste.banane" to FieldValue.delete(),
            ))
    }

    // TODO: Invoice
    // TODO: update view after adding/deleting items
    // TODO: fix scroll and height of compose element


    @Composable
    fun createItemRow(item: Map.Entry<String, Boolean>) {
        var checkedState = remember { mutableStateOf(false) }

        Row {
            Checkbox(
                checked = checkedState.value,
                onCheckedChange = {
                    checkedState.value = it;
                    checkShoppinglistItem(item, checkedState)
                },
                Modifier.size(30.dp),
            )
            Text(
                modifier = Modifier
                    .clickable {
                        checkedState.value = !checkedState.value;
                        checkShoppinglistItem(item, checkedState)
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
    fun showShoppingList(shoppingList: HashMap<String,Boolean>) {

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