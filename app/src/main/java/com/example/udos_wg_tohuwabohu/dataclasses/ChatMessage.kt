package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date
import kotlin.reflect.typeOf

class ChatMessage(val docID: String, var message: String?, var timestamp: Date?, var user: DocumentReference?) {
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

    override fun equals(other: Any?): Boolean {
        return if(other is ChatMessage) equals(other as ChatMessage)
        else super.equals(other)
    }

    fun equals(other: ChatMessage?): Boolean {
        if (other != null) {
            return this.docID == other.docID
        }
        return false
    }
}