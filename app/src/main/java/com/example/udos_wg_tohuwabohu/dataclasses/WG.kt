package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class WG(val docID: String, val name: String?, val contactPerson: DocumentReference?, val shoppingList: HashMap<String,Boolean>?){
    constructor(vals: DocumentSnapshot) : this(
        vals.id,
        vals.getString("bezeichnung"),
        vals.getDocumentReference("ansprechpartner"),
        vals["einkaufsliste"] as HashMap<String, Boolean>
    )
}
