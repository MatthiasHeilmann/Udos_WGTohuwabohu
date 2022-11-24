package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class Roommate(val docID: String, val email: String, val forename: String, val surname: String, var username: String, val coin_count: Long, val guteNudel_count: Long, val balance: Double, val wg: String?){
    constructor(vals: DocumentSnapshot): this(
        vals.id,
        vals["emailID"].toString(),
        vals["vorname"].toString(),
        vals["nachname"].toString(),
        vals["username"].toString(),
        vals["coin_count"] as Long,
        vals["guteNudel_count"] as Long,
        vals["kontostand"] as Double,
        vals.getDocumentReference("wg_id")?.id
    )
    fun update(vals: DocumentSnapshot){
        vals["emailID"].toString()
        vals["vorname"].toString()
        vals["nachname"].toString()
        vals["username"].toString()
        vals["coin_count"] as Long
        vals["guteNudel_count"] as Long
        vals["kontostand"] as Double
        vals.getDocumentReference("wg_id")
    }
}
