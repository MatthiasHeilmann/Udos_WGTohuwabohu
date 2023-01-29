package com.example.udos_wg_tohuwabohu.dataclasses

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import android.widget.Toast
import com.example.udos_wg_tohuwabohu.MainActivity
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
    val EmptyWG = db.collection(Collections.WG.call).document("EmptyWG")
    private var mainActivity: MainActivity? = null
    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }
    /**
     * creates a new task for the wg in the database
     * gets the completer with getCompleter()
     */
    fun createTask(frequencyInDays: Int, name: String, points: Int, completer: Roommate?){
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
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
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
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
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
        val ceMap: MutableMap<String, Timestamp> = HashMap()
        ceMap[description] = timestamp

        dataHandler.wg.first().let {
            db.collection("wg")
                .document(it.docID)
                .update("calendar",FieldValue.arrayUnion(ceMap))
        }
    }
    fun updateWgData(name: String,contactSurname:String,contactFirstname:String,contactEmail:String,contactPhone:String,contactIBAN:String){
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
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

    fun leaveWG(mainActivity: MainActivity){
        if(!ConnectionCheck.getInstance().check(mainActivity)) return
        db.collection("mitbewohner")
            .document(dataHandler.user!!.docID)
            .update("wg_id",EmptyWG)
            .addOnSuccessListener {
                mainActivity.restartApp()
            }
            .addOnFailureListener{
                Toast.makeText(mainActivity,"Es ist ein Fehler aufgetreten. Bitte versuche es erneut.",Toast.LENGTH_SHORT).show()
            }
    }

    /**
     * finds the new completer, which has the lowest coin_count
     * @return roommate with the lowest coin_count
     */
    fun getCompleter(): Roommate? {
        val roommateList = dataHandler.roommateList
        var worstMate: Roommate? = null
        var worstCount: Long = Long.MAX_VALUE
        roommateList.forEach{ mate ->
            if (mate.value.coin_count!! <= worstCount) {
                worstMate = dataHandler.getRoommate(mate.key)
                worstCount = mate.value.coin_count!!
            }
        }
        return worstMate
    }
    /**
     * checks the task in the database,
     * sets the duedate to today + frequency,
     * sets new completer to completer given by getCompleter()
     */
    fun checkTask(task: Task) {
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
        val newCompleter = getCompleter()
        var newDate = Date()
        val c = Calendar.getInstance()
        c.time = newDate
        task.frequency?.let { c.add(Calendar.DATE, it) }
        newDate = c.time
        if (newCompleter != null) {
            val newCompleterRef = db.collection("mitbewohner").document(newCompleter.docID)
            dataHandler.wg!!.first().let {
                db.collection("wg")
                    .document(it.docID)
                    .collection("tasks")
                    .document(task.docId)
                    .update(mapOf(
                        "frist" to newDate,
                        "erlediger" to newCompleterRef
                    ))
            }
        }
    }

    /**
     * deletes a task in the database
     */
    fun deleteTask(docId: String){
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
        dataHandler.wg!!.first().let {
            db.collection("wg")
                .document(it.docID)
                .collection("tasks")
                .document(docId)
                .delete()
        }
    }

    /**
     * gives the roommate who checks the task the points in the database
     */
    fun givePoints(roommate: Roommate?, points: Int){
        if(!ConnectionCheck.getInstance().check(mainActivity!!)) return
        if (roommate != null) {
            val newPoints = roommate.coin_count?.plus(points)
            db.collection(Collections.Roommate.call).document(roommate.docID).update("coin_count",newPoints)
        }
    }
}