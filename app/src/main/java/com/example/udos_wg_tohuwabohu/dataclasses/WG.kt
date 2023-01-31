package com.example.udos_wg_tohuwabohu.dataclasses


import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class WG(
    val docID: String,
    var name: String?,
    var contactPerson: DocumentReference?,
    var shoppingList: SnapshotStateMap<String, Boolean>?,
    var calendar: ArrayList<HashMap<String, Timestamp>>?,
    var entryCode: Long?
) {
    constructor(vals: DocumentSnapshot) : this(
        vals.id, null, null, mutableStateMapOf(), null, null
    ) {
        update(vals)
    }

    fun update(vals: DocumentSnapshot) {
        this.name = vals.getString("bezeichnung")
        this.entryCode = vals["entrycode"] as Long
        this.contactPerson = vals.getDocumentReference("ansprechpartner")
        (vals["einkaufsliste"] as HashMap<String, Boolean>).forEach { item ->
            shoppingList?.set(item.key, item.value)
        }
        this.calendar = vals["calendar"] as ArrayList<HashMap<String, Timestamp>>

    }
}










