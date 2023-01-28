package com.example.udos_wg_tohuwabohu.dataclasses

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
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
            it.first().let { it1 ->
                db.collection(Collections.WG.toString()).document(
                    it1.docID)
            }
        } ?: return

        val completerDocRef = completer?.let {
            db.collection("Mitbewohner").document(
                it.docID)
        } ?: return

        val myTask: MutableMap<String, Any> = HashMap()
        myTask["bezeichnung"] = name
        myTask["completed"] = false
        myTask["frequenz"] = frequencyInDays
        myTask["frist"] = newDate
        myTask["punkte"] = points
        myTask["wg_id"] = wgDocRef
        myTask["erlediger"] = completerDocRef
        dataHandler.wg!!.first().let {
            db.collection("wg")
                .document(it.docID)
                .collection("tasks")
                .add(myTask)
        }
    }

    fun createChatMessage(message: String, timestamp: Date, user: Roommate?){
        val userDocRef = user?.let {
            db.collection("Mitbewohner").document(
                it.docID)
        } ?: return

        val cmMap: MutableMap<String, Any> = HashMap()
        cmMap["message"] = message
        cmMap["timestamp"] = timestamp
        cmMap["user"] = userDocRef

        dataHandler.wg!!.first().let {
            db.collection("wg")
                .document(it.docID)
                .collection("chat_files")
                .add(cmMap)
        }
    }

    fun createCalendarEntry(description: String, timestamp: Timestamp){
        val ceMap: MutableMap<String, Timestamp> = HashMap()
        ceMap[description] = timestamp

        dataHandler.wg.first().let {
            db.collection("wg")
                .document(it.docID)
                .update("calendar",FieldValue.arrayUnion(ceMap))
        }
    }
    fun updateWgData(name: String,contactSurname:String,contactFirstname:String,contactEmail:String,contactPhone:String,contactIBAN:String){
        val wgName: MutableMap<String, Any> = HashMap()
        wgName["bezeichnung"] = name
        db.collection(Collections.WG.call)
            .document(dataHandler.wg!!.first().docID)
            .update(wgName)
        val contact: MutableMap<String, Any> = HashMap()
        contact["IBAN"] = contactIBAN
        contact["email"] = contactEmail
        contact["nachname"] = contactSurname
        contact["vorname"] = contactFirstname
        contact["tel_nr"] = contactPhone
        db.collection(Collections.ContactPerson.call)
            .document(dataHandler.contactPerson!!.docID)
            .update(contact)
    }

    fun leaveWG(){
        val EmptyWG = db.collection(Collections.WG.call).document("EmptyWG")
        db.collection(Collections.Roommate.call).document(dataHandler.user!!.docID).update("wg_id",EmptyWG)
    }
}