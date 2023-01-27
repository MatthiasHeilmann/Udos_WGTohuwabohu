package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date
import kotlin.reflect.typeOf

class ChatMessage(
    val docID: String,
    var message: String?,
    var timestamp: Date?,
    var user: DocumentReference?
) {
    constructor(vals: DocumentSnapshot) : this(
        vals.id, null, null, null
    ) {
        update(vals)
    }

    fun update(vals: DocumentSnapshot) {
        this.message = vals.getString("message")
        this.timestamp = vals.getTimestamp("timestamp")?.toDate()
        this.user = vals.getDocumentReference("user")
    }

    override fun equals(other: Any?): Boolean {
        return when (other) {
            null            -> false
            is ChatMessage  -> other.docID.equals(this.docID)
            else            -> super.equals(other)
        }
    }
}