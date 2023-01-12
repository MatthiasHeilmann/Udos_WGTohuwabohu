package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.sql.Timestamp
import java.util.Date

class ChatMessage(val docId: String, var message: String?, var timestamp: Date?, var user: DocumentReference?) {
    constructor(vals: DocumentSnapshot): this(
        vals.id, null, null, null
    )
    {
        update(vals)
    }
    fun update(vals: DocumentSnapshot){
        this.message = vals.getString("message")
        this.timestamp = vals.getTimestamp("timestamp")?.toDate()
        this.user = vals.getDocumentReference("user")
    }
}