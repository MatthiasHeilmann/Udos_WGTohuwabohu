package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class WG(val docID: String, var name: String?, var contactPerson: DocumentReference?, var shoppingList: HashMap<String,Boolean>?){
    constructor(vals: DocumentSnapshot) : this(
        vals.id,null, null, null
    )
    {
        update(vals)
    }
    fun update(vals: DocumentSnapshot){
        this.name = vals.getString("bezeichnung")
        this.contactPerson = vals.getDocumentReference("ansprechpartner")
        this.shoppingList = vals["einkaufsliste"] as HashMap<String, Boolean>
    }
}
