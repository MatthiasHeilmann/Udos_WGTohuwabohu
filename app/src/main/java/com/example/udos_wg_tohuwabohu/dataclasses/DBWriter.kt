package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class DBWriter private constructor() {

    companion object {
        private var instance: DBWriter? = null;

        fun getInstance(): DBWriter = instance ?: synchronized(this) {
            instance ?: DBWriter().also { instance = it }
        }
    }

    private val db = Firebase.firestore
    private val dataHandler = DataHandler.getInstance()
    private val TAG = "[MainActivity]"


}