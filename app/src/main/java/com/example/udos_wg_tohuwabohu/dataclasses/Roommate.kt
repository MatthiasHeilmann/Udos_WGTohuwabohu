package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot

data class Roommate(val docID: String, var email: String?, var forename: String?, var surname: String?, var username: String?, var coin_count: Long?, var guteNudel_count: Long?, var balance: Double?, var wg: DocumentReference? ){
    constructor(vals: DocumentSnapshot): this(
        vals.id, null, null, null,null,null,null,null,null
    )
    {
        update(vals)
    }
    fun update(vals: DocumentSnapshot){
        this.email = vals["emailID"].toString()
        this.forename = vals["vorname"].toString()
        this.surname = vals["nachname"].toString()
        this.username = vals["username"].toString()
        this.coin_count = vals["coin_count"] as Long
        this.guteNudel_count = vals["guteNudel_count"] as Long
        this.balance = vals["kontostand"] as Double
        this.wg = vals.getDocumentReference("wg_id")
    }
}
