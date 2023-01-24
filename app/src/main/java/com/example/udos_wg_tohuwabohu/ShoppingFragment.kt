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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

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

    val dataHandler = DataHandler.getInstance()
    var shoppingList = DataHandler.getInstance().wg?.shoppingList


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
        Log.d(TAG,shoppingList.toString() )
        /*var _binding: FragmentTasksBinding? = FragmentTasksBinding.inflate(layoutInflater)*/

        val addItemButton:FloatingActionButton = v.findViewById(R.id.button_add_item)
        val deleteItemsButton:FloatingActionButton = v.findViewById(R.id.button_delete_items)
        val createInvoiceButton:FloatingActionButton = v.findViewById(R.id.button_create_invoice)

        val entryField:EditText = v.findViewById(R.id.addItemEntryField)

        // Button Item hinzufügen
        addItemButton.setOnClickListener { v ->
            Log.d(TAG,"Button geklickt")
            if(TextUtils.isEmpty(entryField.text.toString().trim{it <= ' '})
                ){
                Log.d(TAG, "Eingabefeld war leer")
                Toast.makeText(
                    requireActivity(),
                    "Bitte Artikel eintragen.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // addItem(entryField, false)
        }

        // Button Items löschen
        deleteItemsButton.setOnClickListener { v ->
            Log.d(TAG,"Items gelöscht")

            // deleteItems()
        }

        // Button Items löschen
        createInvoiceButton.setOnClickListener { v ->
            Log.d(TAG,"Rechnung erstellt.")

            // deleteItems()
        }




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

    // TODO: add Item
    /*fun addItem(name: String, checked: Boolean){

        db.collection("wg")
            .document(dataHandler.wg!!.docID)
            .add(item)

        // update list
    }*/

    // TODO: delete Items
    /*fun deleteItems(){
        // Frage: "markierte Items löschen?" ja/nein
        // ja -> every item with item.value = true will be deleted from db

        // update list
    }*/

    // TODO: Invoice
    /*fun createInvoice(){

        // every item with item.value = true will be deleted from db

        // update list
    }*/

}


@Composable
fun itemCheckBox() {
    val checkedState = remember { mutableStateOf(false) }
    Checkbox(
        checked = checkedState.value,
        onCheckedChange = { checkedState.value = it },
        Modifier.size(30.dp)
    )
}

@Composable
fun showShoppingList(shoppingList: HashMap<String,Boolean>){
    Column(modifier = Modifier
        .fillMaxSize()
    ){
        shoppingList.forEach { item ->
            Row {
                itemCheckBox();
                Text(
                    text = item.key + " " + item.value.toString(),
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        background = Color.LightGray
                    )
                );
            }
          }
    }

    // TODO: add checkbox behaviour
    // click on row -> checked

    /*fun checkShoppinglistItem(item: String){
        // make items checkable
        // click -> box gets checked ->
            // item.value = true
    }*/

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
}