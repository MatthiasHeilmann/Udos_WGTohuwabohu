package com.example.udos_wg_tohuwabohu.dataclasses


import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.MutableState
import com.example.udos_wg_tohuwabohu.MainActivity
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class DBWriter private constructor() {

    companion object {
        private var instance: DBWriter? = null

        fun getInstance(): DBWriter = instance ?: synchronized(this) {
            instance ?: DBWriter().also { instance = it }
        }
    }

    private val db = Firebase.firestore
    private val dataHandler = DataHandler.getInstance()
    private val TAG = "[MainActivity]"
    private val emptyWG = db.collection(Collections.WG.call).document("EmptyWG")
    private var mainActivity: MainActivity? = null
    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }

    fun createFinanceEntry(description: String, price: Double, moucherIDs: List<String>) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return

        val userDocRef = dataHandler.user?.let {
            db.collection("Mitbewohner").document(
                it.docID
            )
        } ?: return

        val moucherDocRefs = moucherIDs.map {
            db.collection("Mitbewohner").document(it)
        }

        // Update roommates with new balance
        updateBalance(dataHandler.user, price)
        moucherIDs.forEach { moucherID ->
            updateBalance(dataHandler.getRoommate(moucherID), price / moucherIDs.size * -1)
        }

        // Upload finance entry to database
        val feMap: MutableMap<String, Any> = HashMap()
        feMap["bezeichnung"] = description
        feMap["goenner"] = userDocRef
        feMap["preis"] = price
        feMap["schnorrer"] = moucherDocRefs
        feMap["timestamp"] = Date()


        dataHandler.wg.first().let {
            db.collection("wg")
                .document(it.docID)
                .collection("finanzen")
                .add(feMap)
        }
    }

    /**
     * creates a new task for the wg in the database
     * gets the completer with getCompleter()
     */
    fun createTask(frequencyInDays: Int, name: String, points: Int, completer: Roommate?) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        // first duedate
        var newDate = Date()
        val c = Calendar.getInstance()
        c.time = newDate
        c.add(Calendar.DATE, frequencyInDays)
        newDate = c.time

        val wgDocRef = dataHandler.wg.let {
            it.first().let { it1 ->
                db.collection(Collections.WG.toString()).document(
                    it1.docID
                )
            }
        }

        val completerDocRef = completer?.let {
            db.collection("Mitbewohner").document(
                it.docID
            )
        } ?: return

        val myTask: MutableMap<String, Any> = HashMap()
        myTask["bezeichnung"] = name
        myTask["completed"] = false
        myTask["frequenz"] = frequencyInDays
        myTask["frist"] = newDate
        myTask["punkte"] = points
        myTask["wg_id"] = wgDocRef
        myTask["erlediger"] = completerDocRef
        dataHandler.wg.first().let {
            db.collection("wg")
                .document(it.docID)
                .collection("tasks")
                .add(myTask)
        }
    }

    fun createChatMessage(message: String, timestamp: Date, user: Roommate?) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        val userDocRef = user?.let {
            db.collection("Mitbewohner").document(
                it.docID
            )
        } ?: return

        val cmMap: MutableMap<String, Any> = HashMap()
        cmMap["message"] = message
        cmMap["timestamp"] = timestamp
        cmMap["user"] = userDocRef

        dataHandler.wg.first().let {
            db.collection("wg")
                .document(it.docID)
                .collection("chat_files")
                .add(cmMap)
        }
    }

    fun createCalendarEntry(description: String, timestamp: Timestamp) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        val ceMap: MutableMap<String, Timestamp> = HashMap()
        ceMap[description] = timestamp

        dataHandler.wg.first().let {
            db.collection("wg")
                .document(it.docID)
                .update("calendar", FieldValue.arrayUnion(ceMap))
        }
    }

    fun updateWgData(
        name: String,
        contactSurname: String,
        contactFirstname: String,
        contactEmail: String,
        contactPhone: String,
        contactIBAN: String
    ) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        val wgName: MutableMap<String, Any> = HashMap()
        wgName["bezeichnung"] = name
        db.collection(Collections.WG.call)
            .document(dataHandler.wg.first().docID)
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

    fun leaveWG(mainActivity: MainActivity) {
        if (!ConnectionCheck.getInstance().check(mainActivity)) return
        val userData: MutableMap<String, Any> = HashMap()
        userData["wg_id"] = emptyWG
        userData["kontostand"] = 0
        userData["coin_count"] = 0
        userData["guteNudel_count"] = 0
        db.collection("mitbewohner")
            .document(dataHandler.user!!.docID)
            .update(userData)
            .addOnSuccessListener {
                mainActivity.restartApp()
            }
            .addOnFailureListener {
                Toast.makeText(
                    mainActivity,
                    "Es ist ein Fehler aufgetreten. Bitte versuche es erneut.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * finds the new completer, which has the lowest coin_count
     * @return roommate with the lowest coin_count
     */
    private fun getCompleter(): Roommate? {
        val roommateList = dataHandler.roommateList
        var worstMate: Roommate? = null
        var worstCount: Long = Long.MAX_VALUE
        roommateList.forEach { mate ->
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
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        val newCompleter = getCompleter()
        var newDate = Date()
        val c = Calendar.getInstance()
        c.time = newDate
        task.frequency?.let { c.add(Calendar.DATE, it) }
        newDate = c.time
        if (newCompleter != null) {
            val newCompleterRef = db.collection("mitbewohner").document(newCompleter.docID)
            dataHandler.wg.first().let {
                db.collection("wg")
                    .document(it.docID)
                    .collection("tasks")
                    .document(task.docId)
                    .update(
                        mapOf(
                            "frist" to newDate,
                            "erlediger" to newCompleterRef
                        )
                    )
            }
        }
    }

    /**
     * deletes a task in the database
     */
    fun deleteTask(docId: String) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        dataHandler.wg.first().let {
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
    fun givePoints(roommate: Roommate?, points: Int) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return
        if (roommate != null) {
            val newPoints = (roommate.coin_count ?: 0).plus(points)
            db.collection(Collections.Roommate.call).document(roommate.docID)
                .update("coin_count", newPoints)
        }
    }

    /**
     * gives the roommate who checks the task the points in the database
     */
    private fun updateBalance(roommate: Roommate?, price: Double) {
        if (!ConnectionCheck.getInstance().check(mainActivity!!)) return

        println("Update shit $roommate, $price")

        if (roommate != null) {
            val newPoints = (roommate.balance ?: 0.0).plus(price)
            db.collection(Collections.Roommate.call).document(roommate.docID)
                .update("kontostand", newPoints)
        }
    }

    fun addItemToShoppingList(item: String) {
        db.collection("wg")
            .document(dataHandler.wg.first().docID)
            .update(
                mapOf(
                    "einkaufsliste.${item}" to false,
                )
            )
    }

    fun checkShoppinglistItem(
        item: Map.Entry<String, Boolean>,
        checkedState: MutableState<Boolean>
    ) {
        db.collection("wg")
            .document(dataHandler.wg.first().docID)
            .update(
                mapOf(
                    "einkaufsliste.${item.key}" to checkedState.value,
                )
            )
        Log.d("[SHOPPING FRAGMENT]", item.key + " geändert zu " + checkedState.value)
    }
}