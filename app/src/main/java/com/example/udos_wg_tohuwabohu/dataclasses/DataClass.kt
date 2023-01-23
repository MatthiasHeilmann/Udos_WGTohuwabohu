package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentSnapshot

abstract class DataClass (var docId: String?){
    abstract fun update(vals: DocumentSnapshot);

    fun equals(other: DataClass?): Boolean {
        if (other != null) {
            return this.docId.equals(other.docId)
        }
        return false
    }
}