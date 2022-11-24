package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date

class Task(val docId: String, val name: String, val frequency: Int?, val dueDate: Date?, val points: Int?, val roommate: DocumentReference?, val wg: DocumentReference?) {
    constructor(vals: DocumentSnapshot): this(
        vals.id,
        vals["bezeichnung"].toString(),
        vals.getLong("frequenz")?.toInt(),
        vals.getTimestamp("frist")?.toDate(),
        vals.getLong("punkte")?.toInt(),
        vals.getDocumentReference("erlediger"),
        vals.getDocumentReference("wgID")
    )
}