package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.util.*

class FinanceEntry(
    val docID: String,
    var description: String?,
    var price: Double?,
    var timestamp: Date?,
    var benefactor: DocumentReference?,
    var moucherList: ArrayList<DocumentReference>?
) {
    constructor(vals: DocumentSnapshot) : this(
        vals.id, null, null, null, null, ArrayList<DocumentReference>()
    ) {
        update(vals)
    }

    fun update(vals: DocumentSnapshot) {
        this.description = vals.getString("bezeichnung")
        this.price = vals.getDouble("preis")
        this.timestamp = vals.getTimestamp("timestamp")?.toDate()
        this.benefactor = vals.getDocumentReference("goenner")
        this.moucherList?.addAll(vals.get("schnorrer") as ArrayList<DocumentReference>)
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null -> false
            is FinanceEntry -> other.docID == this.docID
            else -> super.equals(other)
        }
    }
}