package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

class Aufgabe(val docId: String, val bezeichnung: String, val frequenz: Int?, val frist: Date?, val punkte: Int?, val erlediger: Mitbewohner?, val wg: WG?) {
    constructor(vals: DocumentSnapshot, erlediger: Mitbewohner?, wg: WG?): this(
        vals.id,
        vals["bezeichnung"].toString(),
        vals.getLong("frequenz")?.toInt(),
        vals.getTimestamp("frist")?.toDate(),
        vals.getLong("punkte")?.toInt(),
        erlediger,
        wg
    )
}