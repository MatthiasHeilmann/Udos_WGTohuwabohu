package com.example.udos_wg_tohuwabohu.dataclasses

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class WG(val docID: String, var name: String?,var entryCode:Long?, var contactPerson: DocumentReference?, var shoppingList: HashMap<String,Boolean>?, var calendar: ArrayList<HashMap<String,Timestamp>>?){
    constructor(vals: DocumentSnapshot) : this(
        vals.id,null, null, null, null,null
    )
    {
        update(vals)
    }
    fun update(vals: DocumentSnapshot){
        this.name = vals.getString("bezeichnung")
        this.entryCode = vals["entrycode"] as Long
        this.contactPerson = vals.getDocumentReference("ansprechpartner")
        this.shoppingList = vals["einkaufsliste"] as HashMap<String, Boolean>
        try{
            this.calendar = vals["calendar"] as ArrayList<HashMap<String, Timestamp>>
        }catch (e:Exception){

        }
        val TAG = "########"
        Log.d(TAG,this.name.toString())
        Log.d(TAG,this.contactPerson.toString())
    }
}
