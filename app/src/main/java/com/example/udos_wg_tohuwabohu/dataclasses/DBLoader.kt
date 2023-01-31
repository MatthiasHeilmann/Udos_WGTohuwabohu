package com.example.udos_wg_tohuwabohu.dataclasses

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.startActivity
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.LonelyPageActivity
import com.example.udos_wg_tohuwabohu.MainActivity
import com.example.udos_wg_tohuwabohu.NoConnectionActivity
import com.example.udos_wg_tohuwabohu.Tasks.TasksFragment
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.asDeferred

class DBLoader private constructor() {
    companion object {
        private var instance: DBLoader? = null;

        fun getInstance(): DBLoader = instance ?: synchronized(this) {
            instance ?: DBLoader().also { instance = it }
        }
    }

    private var mainActivity: MainActivity? = null
    private val db = Firebase.firestore
    private val dataHandler = DataHandler.getInstance()
    private val TAG = "[DBLoader]"
    private var initializedTask = false
    private var initializedCalendar = false
    private var initializedFinance = false


    /**
     * Fetches needed data for the currently logged in user and it's wg.
     * Loaded object are then attached to the `dataHandler` singleton.
     */
    fun loadDatabase(userID: String) = runBlocking<Unit> {
        Log.d(TAG, "Loading database...")
        // Wait for the load to finish
        val wgRef = async { loadUserData(userID) }.await()
        Log.d(TAG, "Loaded User Data...")
        val anRef = async { loadWGData(wgRef) }.await()
        Log.d(TAG, "Loaded WG Data...")
        async { loadContactPeronData(anRef) }.await()
        Log.d(TAG, "Loaded ContactPerson Data...")
        async { loadRoommatesData(wgRef) }.await()
        Log.d(TAG, "Loaded Roommates Data...")
        async { loadChatFilesData(wgRef) }.await()
        Log.d(TAG, "Loaded Chat Files Data...")
        async { loadFinanceData(wgRef) }.await()
        Log.d(TAG, "Loaded Finance Data...")
        async { loadTaskData(wgRef) }.await()
        Log.d(TAG, "Loaded Task Data...")

        Log.d(TAG, "Loading Database successful")

        // Then add snapshotListener
        roommateSnapshotListener()
        chatSnapshotListener()
        financeSnapshotListener()
        wgSnapshotListener()
        contactPersonSnapshotListener()
        addTaskCollectionSnapshotListener()
        Log.d(TAG, "Added all Snapshot Listeners!")
    }

    /**
     * Fetches data for the currently logged in user form the firestore `db`.
     * Calls `loadWGData, loadRoommatesData` and `loadTaskData` with that information
     * @param {String} userID user id from the logged in user
     */
    private suspend fun loadUserData(userID: String): DocumentReference {
        val wgRef: DocumentReference?
        var userRes: DocumentSnapshot? = null
        try {
            userRes = db.collection(Collections.Roommate.call).document(userID)
                .get()
                .asDeferred().await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }

        Log.d(TAG, "Current User is: ${userRes!!.id} => ${userRes.data}")
        dataHandler.user = Roommate(userRes);
        // wg reference
        wgRef = userRes["wg_id"] as DocumentReference
        return wgRef
    }

    /**
     * Fetches data for the current user's WG from firestore `db`.
     * Call `loadContactPeronData` with that information
     */
    private suspend fun loadWGData(wgRef: DocumentReference): DocumentReference {
        val wg: WG
        var wgRes: DocumentSnapshot? = null
        try {
            wgRes = db.collection(Collections.WG.call).document(wgRef.id)
                .get()
                .asDeferred().await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }

        wg = WG(wgRes!!)

        dataHandler.wg = mutableStateListOf(wg)

        // ansprechpartner reference
        val anRef = wgRes.get("ansprechpartner") as DocumentReference
        return anRef
    }

    private suspend fun loadContactPeronData(anRef: DocumentReference) {
        val an: ContactPerson?
        var anRes: DocumentSnapshot? = null
        try {
            Log.d("[*******]",anRef.id)
            anRes = db.collection(Collections.ContactPerson.call)
                .document(anRef.id)
                .get()
                .asDeferred()
                .await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }
        an = ContactPerson(anRes!!)
        dataHandler.contactPerson = an
    }

    private suspend fun loadFinanceData(wgRef: DocumentReference){
        var financeRes: QuerySnapshot? = null
        try {
            financeRes = db.collection(Collections.WG.call)
                .document(wgRef.id).collection(Collections.FINANCES.call)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get().asDeferred().await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }
        try {
            financeRes?.forEach { cost ->
                dataHandler.addFinanceEntry(FinanceEntry(cost))
            }
        }catch (e: Exception){
            Log.d(TAG,"couldnt find finances. Not created yet?")
        }
    }

    private suspend fun loadChatFilesData(wgRef: DocumentReference) {
        var chatRes: QuerySnapshot? = null
        try {
            chatRes = db.collection(Collections.WG.call)
                .document(wgRef.id).collection(Collections.CHAT.call)
                .orderBy("timestamp").limit(200)
                .get().asDeferred().await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }
        try {
            chatRes?.forEach { msg ->
                dataHandler.addChatMessage(ChatMessage(msg))
            }
        }catch(e:Exception){
            Log.d(TAG,"couldnt find chatfiles. Not created yet?")
        }
    }

    private suspend fun loadRoommatesData(wgRef: DocumentReference) {
        var rmRes: QuerySnapshot? = null
        // Find all mitbewohner for this wg
        try {
            rmRes = db.collection(Collections.Roommate.call)
                .whereEqualTo("wg_id", wgRef)
                .get()
                .asDeferred().await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }

        rmRes!!.forEach { rm ->
            dataHandler.addRoommate(Roommate(rm))
        }
    }

    private suspend fun loadTaskData(wgRef: DocumentReference) {
        var tasksRes: QuerySnapshot? = null
        try {
            tasksRes = db.collection(Collections.WG.call)
                .document(wgRef.id)
                .collection("tasks")
                .get()
                .asDeferred().await()
        } catch (e: Exception) {
            dataLoadError()
            e.printStackTrace()
        }
        try {
            tasksRes!!.forEach { task ->
                dataHandler.addTask(Task(task))
            }
        }catch (e: Exception){
            Log.d(TAG,"couldnt find tasks. Not created yet?")
        }

    }

    private fun roommateSnapshotListener() {
        for (roommate in dataHandler.roommateList.values.toList()) {
            db.collection(Collections.Roommate.call).document(roommate.docID)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    // What happens if the database document gets changed
                    documentSnapshot?.let {
                        dataHandler.addRoommate(Roommate(it))
                        if (dataHandler.getRoommate(roommate.docID)
                                ?.equals(dataHandler.user!!.docID) == true
                        ) {
                            dataHandler.user!!.update(it)
                            mainActivity!!.reloadHomeFragment()
                        }
                        Log.d(
                            TAG,
                            "RoommateList updated ${dataHandler.roommateList.toString()}"
                        )
                    }
                }
            Log.d(
                TAG,
                "added snapshotlistener to Roommate: ${roommate.docID}"
            )
        }
    }

    /**
     * Listens to the whole collection tasks
     * adds new tasks to dataHandler
     * deletes deleted tasks from dataHandler
     */
    private fun addTaskCollectionSnapshotListener(){
            db.collection(Collections.WG.call)
                .document(dataHandler.wg.first().docID)
                .collection(Collections.Task.call)
                // Listener for collection
                .addSnapshotListener{ snapshots,e ->
                    if(e != null){
                        Log.d(TAG,"listen:error",e)
                        return@addSnapshotListener
                    }
                    for(dc in snapshots!!.documentChanges){
                        // for new documents
                        if(dc.type == DocumentChange.Type.ADDED){
                            Log.d("DC TYPE", dc.type.toString())
                            Log.d("TAG","NEW TASK IN COLLECTION: " + dc.document.id)

                            try{
                                // get new document as DocumentSnapshot and add to dataHandler
                                dataHandler.wg.first().let { it1 ->
                                    db.collection("wg")
                                        .document(it1.docID)
                                        .collection("tasks")
                                        .document(dc.document.id)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            dataHandler.addTask(Task(document))
                                            mainActivity?.reloadTaskFragment()
                                            initializedTask=true
                                        }
                                }
                            }catch (e: Exception){
                                Log.d(TAG, "Error getting new task")
                                dataLoadError()
                            }
                            try{
                                if(initializedTask){
                                    mainActivity?.setAlarmTask(dataHandler.getTask(dc.document.id), "new")
                                }
                            }catch(e: Exception){
                                Log.d(TAG, "Error getting new task, but that's not an issue")
                            }
                            // for deleted documents
                        }else if(dc.type == DocumentChange.Type.REMOVED){
                            Log.d(TAG,"TASK FROM COLLECTION REMOVED: " + dc.document.id)
                            dataHandler.taskList.remove(dc.document.id)
                            mainActivity?.reloadTaskFragment()
                        }else if(dc.type == DocumentChange.Type.MODIFIED) {
                            try{
                                Log.d(TAG,"Updating task....")
                                // get new document as DocumentSnapshot and add to dataHandler
                                dataHandler.wg.first().let { it1 ->
                                    db.collection("wg")
                                        .document(it1.docID)
                                        .collection("tasks")
                                        .document(dc.document.id)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            dataHandler.getTask(document.id).update(document)
                                            mainActivity?.setAlarmTask(dataHandler.getTask(document.id), "completed")
                                            mainActivity?.reloadTaskFragment()
                                        }
                                }
                            }catch (e: Exception){
                                Log.d(TAG, "Error updating new task")
                                dataLoadError()
                            }
                        }
                    }
                }

    }

    private fun chatSnapshotListener() {
        dataHandler.wg.first().let {
            db.collection(Collections.WG.call)
                .document(it.docID).collection(Collections.CHAT.call)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    for(dc in querySnapshot!!.documentChanges){
                        if(dc.type == DocumentChange.Type.ADDED){
                            val chatmsg = ChatMessage(dc.document)
                            dataHandler.addChatMessage(chatmsg)
                            mainActivity!!.setAlarmChat(chatmsg)
                        }
                    }
                    // What happens if the database document gets changed
                    /*querySnapshot?.let {
                        it.forEach { msg ->
                            Log.d("QuerySnapshot", ChatMessage(msg).toString())
                            val chatmsg = ChatMessage(msg)
                            dataHandler.addChatMessage(chatmsg)
                            mainActivity!!.setAlarmChat(chatmsg)
                        }
                        Log.d(
                            TAG,
                            "chat updated $it"
                        )
                    }*/
                }
        }
        Log.d(TAG, "Added snapshotlistener to chat")
    }

    private fun financeSnapshotListener() {
        dataHandler.wg.first().let {
            db.collection(Collections.WG.call)
                .document(it.docID).collection(Collections.FINANCES.call)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    for(dc in querySnapshot!!.documentChanges) {
                        // for new documents
                        if (dc.type == DocumentChange.Type.ADDED) {
                            dataHandler.addFinanceEntry(FinanceEntry(dc.document))
                            if(initializedFinance) {
                                mainActivity?.setAlarmFinance(FinanceEntry(dc.document))
                            }
                        }
                    }
                    initializedFinance=true
                    // What happens if the database document gets changed

                }

        }
        Log.d(TAG, "Added snapshotlistener to chat")
    }

    private fun wgSnapshotListener() {
        dataHandler.wg.first().let {
            db.collection(Collections.WG.call).document(it.docID)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    // What happens if the database document gets changed
                    documentSnapshot?.let {
                        dataHandler.wg.first().shoppingList?.clear()
                        dataHandler.wg.first().update(it)
                        mainActivity!!.reloadHomeFragment()
                        mainActivity!!.reloadCalendarFragment()
                        dataHandler.wg.first().calendar?.forEach{ appointment ->
                            if(System.currentTimeMillis() < appointment.values.first().seconds*1000) {
                                mainActivity!!.setAlarmCalendar(appointment)
                            }
                        }

                        Log.d(
                            TAG,
                            "wg updated ${dataHandler.wg}"
                        )
                    }
                }

        }
        Log.d(
            TAG,
            "added snapshotlistener to wg: ${dataHandler.wg}"
        )

    }

    private fun contactPersonSnapshotListener() {
        db.collection(Collections.ContactPerson.call).document(dataHandler.contactPerson!!.docID)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                // What happens if the database document gets changed
                documentSnapshot?.let {
                    dataHandler.contactPerson?.update(it)
                    mainActivity!!.reloadHomeFragment()
                    Log.d(
                        TAG,
                        "contactPerson updated ${dataHandler.contactPerson.toString()}"
                    )
                }
            }
        Log.d(
            TAG,
            "added snapshotlistener to contactPerson: ${dataHandler.contactPerson.toString()}"
        )
    }

    fun setMainActivity(mainActivity: MainActivity) {
        this.mainActivity = mainActivity
    }

    private fun dataLoadError(){
        Toast.makeText(mainActivity,
            "Es gab einen Fehler beim Laden der Inhalte. Bitte versuche es erneut.",
            Toast.LENGTH_LONG)
            .show()
    }



}

