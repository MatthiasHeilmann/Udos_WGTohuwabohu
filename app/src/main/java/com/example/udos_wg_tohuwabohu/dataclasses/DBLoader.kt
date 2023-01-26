package com.example.udos_wg_tohuwabohu.dataclasses

import android.util.Log
import android.widget.Toast
import com.example.udos_wg_tohuwabohu.MainActivity
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
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

    /**
     * Fetches needed data for the currently logged in user and it's wg.
     * Loaded object are then attached to the `dataHandler` singleton.
     */
    fun loadDatabase(userID: String) = runBlocking<Unit> {
        Log.d(TAG, "Loading database...")
        // Wait for the load to finish
        val wgRef = async { loadUserData(userID) }.await()
        val anRef = async { loadWGData(wgRef) }.await()
        async { loadChatFilesData(wgRef) }.await()
        async { loadContactPeronData(anRef) }.await()
        async { loadRoommatesData(wgRef) }.await()
        async { loadTaskData(wgRef) }.await()

        Log.d(TAG, "#################################################")
        dataHandler.getChat().forEach { m ->
            Log.d(
                TAG,
                "${dataHandler.getRoommate(m.user!!.id)?.username} said: ${m.message} at ${m.timestamp}"
            )
        }

        Log.d(TAG, "Loading Database succesfull")

        // Then add snapshotListener
        roommateSnapshotListener()
        chatSnapshotListener()
        wgSnapshotListener()
        contactPersonSnapshotListener()
        addTaskCollectionSnapshotListener()
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
            // TODO handle excpetion: Give toast and shutdown app
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
            // Todo handle exception: give toast and shutdown
            e.printStackTrace()
        }

        wg = WG(wgRes!!)

        dataHandler.wg = wg

        // ansprechpartner reference
        val anRef = wgRes.get("ansprechpartner") as DocumentReference
        return anRef
    }

    private suspend fun loadContactPeronData(anRef: DocumentReference) {
        val an: ContactPerson?
        var anRes: DocumentSnapshot? = null
        try {

            anRes = db.collection(Collections.ContactPerson.call).document(anRef.id)
                .get()
                .asDeferred().await()
        } catch (e: Exception) {
            // TODO handle exception: Give toast and create default contact person
            e.printStackTrace()
        }
        an = ContactPerson(anRes!!)
        dataHandler.contactPerson = an
    }

    private suspend fun loadChatFilesData(wgRef: DocumentReference) {
        var chatRes: QuerySnapshot? = null
        try {
            chatRes = db.collection(Collections.WG.call)
                .document(wgRef.id).collection(Collections.CHAT.call)
                .orderBy("timestamp").limit(50)
                .get().asDeferred().await()
        } catch (e: Exception) {
            // TODO handle exception: Give toast and empty chat object
            e.printStackTrace()
        }
        chatRes?.forEach { msg ->
            dataHandler.addChatMessage(ChatMessage(msg))
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
            // TODO handle exception: Give toast and shutdown app
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
            // TODO handle exception: Give toast and shutdown app
            e.printStackTrace()
        }

        tasksRes!!.forEach { task ->
            dataHandler.addTask(Task(task))
        }


    }

    private fun roommateSnapshotListener() {
        for (roommate in dataHandler.roommateList.values) {
            db.collection(Collections.Roommate.call).document(roommate.docID)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    // What happens if the database document gets changed
                    documentSnapshot?.let {
                        dataHandler.getRoommate(roommate.docID)?.update(it)
                        if (dataHandler.getRoommate(roommate.docID)
                                ?.equals(dataHandler.user!!.docID) == true
                        ) {
                            dataHandler.user!!.update(it)
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

    private fun taskSnapshotListener() {
        for (task in dataHandler.taskList.values) {
            db.collection(Collections.Task.call).document(task.docId)
                .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    // What happens if the database document gets changed
                    documentSnapshot?.let {
                        dataHandler.getTask(task.docId).update(it)
                        Log.d(
                            TAG,
                            "task updated ${dataHandler.getTask(it.id)}"
                        )
                    }
                }
            Log.d(
                TAG,
                "added snapshotlistener to task: ${task.docId}"
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
            .document(dataHandler.wg!!.docID)
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
                        Log.d("TAG","NEW TASK IN COLLECTION: " + dc.document.id)
                        try{
                            // get new document as DocumentSnapshot and add to dataHandler
                            db.collection("wg")
                                .document(dataHandler.wg!!.docID)
                                .collection("tasks")
                                .document(dc.document.id)
                                .get()
                                .addOnSuccessListener { document ->
                                    dataHandler.addTask(Task(document))
                                    //TODO refresh fragment if active
                                }
                        }catch (e: Exception){
                            Log.d(TAG, "Error getting new task")
                            //TODO Exception
                        }
                        // for deleted documents
                    }else if(dc.type == DocumentChange.Type.REMOVED){
                        Log.d(TAG,"TASK FROM COLLECTION REMOVED: " + dc.document.id)
                        dataHandler.taskList.remove(dc.document.id)
                        //TODO refresh fragment if active
                    }else if(dc.type == DocumentChange.Type.MODIFIED) {
                        try{
                            // get new document as DocumentSnapshot and add to dataHandler
                            db.collection("wg")
                                .document(dataHandler.wg!!.docID)
                                .collection("tasks")
                                .document(dc.document.id)
                                .get()
                                .addOnSuccessListener { document ->
                                    dataHandler.getTask(document.id).update(document)
                                    //TODO refresh fragment if active
                                }
                        }catch (e: Exception){
                            Log.d(TAG, "Error updating new task")
                            //TODO Exception
                        }
                    }
                }
            }
    }

    private fun chatSnapshotListener() {
        db.collection(Collections.WG.call)
            .document(dataHandler.wg!!.docID).collection(Collections.CHAT.call)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                // What happens if the database document gets changed
                querySnapshot?.let {
                    it.forEach { msg ->
                        dataHandler.addChatMessage(ChatMessage(msg))
                    }
                    Log.d(
                        TAG,
                        "chat updated $it"
                    )
                }
            }
        Log.d(TAG, "Added snapshotlistener to chat")
    }

    private fun wgSnapshotListener() {
        db.collection(Collections.WG.call).document(dataHandler.wg!!.docID)
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(mainActivity, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                // What happens if the database document gets changed
                documentSnapshot?.let {
                    dataHandler.wg?.update(it)
                    Log.d(
                        TAG,
                        "wg updated ${dataHandler.wg.toString()}"
                    )
                }
            }
        Log.d(
            TAG,
            "added snapshotlistener to wg: ${dataHandler.wg.toString()}"
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
}