package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class WG(val docID: String, val name: String?, val contactPerson: String?, val shoppingList: HashMap<String,Boolean>?){
    constructor(vals: DocumentSnapshot) : this(
        vals.id,
        vals.getString("bezeichnung"),
        vals.getDocumentReference("ansprechpartner")?.id,
        vals["einkaufsliste"] as HashMap<String, Boolean>
    )
}
