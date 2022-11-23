package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

data class Mitbewohner(val userID: String, val emailID: String, val vorname: String, val nachname: String, val username: String, val coin_count: Long, val guteNudel_count: Long, val kontostand: Double, val wg: WG? ){
    constructor(vals: DocumentSnapshot, wg: WG?): this(
        vals.id,
        vals["emailID"].toString(),
        vals["vorname"].toString(),
        vals["nachname"].toString(),
        vals["username"].toString(),
        vals["coin_count"] as Long,
        vals["guteNudel_count"] as Long,
        vals["kontostand"] as Double,
        wg
    )
}
