package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

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

    /**
     * creates a new task for the wg in the database
     * gets the completer with getCompleter()
     */
    fun createTask(frequencyInDays: Int, name: String, points: Int, completer: Roommate?){
        // first duedate
        var newDate = Date()
        val c = Calendar.getInstance()
        c.time = newDate
        c.add(Calendar.DATE, frequencyInDays)
        newDate = c.time

        val wgDocRef = dataHandler.wg?.let {
            db.collection(Collections.WG.toString()).document(
                it.docID)
        }

        val completerDocRef = completer?.let {
            db.collection("Mitbewohner").document(
                it.docID)
        }

        val myTask: MutableMap<String, Any> = HashMap()
        myTask["bezeichnung"] = name
        myTask["completed"] = false
        myTask["frequenz"] = frequencyInDays
        myTask["frist"] = newDate
        myTask["punkte"] = points
        myTask["wg_id"] = wgDocRef!!
        myTask["erlediger"] = completerDocRef!!
        db.collection("wg")
            .document(dataHandler.wg!!.docID)
            .collection("tasks")
            .add(myTask)
    }
}