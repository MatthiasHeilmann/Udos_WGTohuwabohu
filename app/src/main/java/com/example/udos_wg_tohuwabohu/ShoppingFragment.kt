package com.example.udos_wg_tohuwabohu

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.example.udos_wg_tohuwabohu.databinding.FragmentTasksBinding
import com.example.udos_wg_tohuwabohu.dataclasses.DBLoader
import com.example.udos_wg_tohuwabohu.dataclasses.DataHandler
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.Task
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.HashMap

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
    /*private val db = Firebase.firestore*/

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
        Log.d(TAG,shoppingList.toString() )
        /*var _binding: FragmentTasksBinding? = FragmentTasksBinding.inflate(layoutInflater)*/
        val v: View = inflater.inflate(R.layout.fragment_shopping, container, false)
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


    }

@Composable
fun showShoppingList(shoppingList: HashMap<String,Boolean>){
    Column {
        shoppingList.forEach { item ->
            Text(text = item.key + " " + item.value.toString());
          }
    }


    // TODO: add item to shoppingList
    /*@Composable
    fun addItem(item: String, checked: Boolean){
        // open popup with entry field
            // save button -> submit to db, close popup and update list in fragment
            // cancel button -> close popup
    }*/

    // TODO: check item in shoppingList (set false)
    /*fun checkShoppinglistItem(item: String){
        // make items checkable
        // click -> box gets checked ->
            // item.value = true
    }*/

    // TODO: delete item(s) from shoppingList
    /*fun deleteItem() {
        // trash button -> click ->
            // every item with item.value = true will be deleted from db
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
                    // every checked roommate+user -> balance - (price/#ofBegünstigt)
            // cancel button -> close popup
    }*/
}