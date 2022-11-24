package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

class Task(val docId: String, var name: String?, var frequency: Int?, var dueDate: Date?, var points: Int?, var roommate: DocumentReference?, var wg: DocumentReference?) {
    constructor(vals: DocumentSnapshot): this(
        vals.id, null, null, null, null, null, null
    )
    {
        update(vals)
    }
    fun update(vals: DocumentSnapshot){
        this.name = vals["bezeichnung"].toString()
        this.frequency = vals.getLong("frequenz")?.toInt()
        this.dueDate = vals.getTimestamp("frist")?.toDate()
        this.points = vals.getLong("punkte")?.toInt()
        this.roommate = vals.getDocumentReference("erlediger")
        this.wg = vals.getDocumentReference("wg_id")
    }
}