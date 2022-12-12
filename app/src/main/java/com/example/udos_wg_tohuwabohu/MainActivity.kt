package com.example.udos_wg_tohuwabohu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.asDeferred

class MainActivity : AppCompatActivity() {
    enum class Collection(val call: String){
        Roommate("mitbewohner"),
        WG("wg"),
        ContactPerson("ansprechtpartner"),
        Task("aufgabe");
        override fun toString(): String {
            return call
        }
    }

    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore
    val TAG = "[MainActivity]"
    val dataHandler = DataHandler.getInstance();

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get user values and show
        val userID = intent.getStringExtra("user_id")
        val emailID = intent.getStringExtra("email_id")
        binding.textUserID.text = "User ID: $userID"
        binding.textUserEmail.text = "Email: $emailID"

        binding.buttonLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this@MainActivity,LoginActivity::class.java))
            finish()
        }
        replaceFragment(ChatFragment())
        binding.textToolbar.text = "Chat"
        // navigation
        binding.bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId){
                R.id.nav_chat -> {
                    replaceFragment(ChatFragment())
                    binding.textToolbar.text = "Chat"
                }
                R.id.nav_finance -> {
                    replaceFragment(FinanceFragment())
                    binding.textToolbar.text = "Finanzen"
                }
                R.id.nav_task -> {
                    replaceFragment(TasksFragment())
                    binding.textToolbar.text = "Aufgaben"
                }
                R.id.nav_shopping -> {
                    replaceFragment(ShoppingFragment())
                    binding.textToolbar.text = "Einkaufsliste"
                }
                R.id.nav_calender -> {
                    replaceFragment(CalendarFragment())
                    binding.textToolbar.text = "Kalender"
                }
                else -> {

                }
            }
            true
        }
        // database
        loadDatabase(userID!!)

    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }

    /**
     * Fetches needed data for the currently logged in user and it's wg.
     * Loaded object are then attached to the `dataHandler` singleton.
     */
    private fun loadDatabase(userID: String) = runBlocking<Unit> {
        Log.d(TAG, "Loading database...")
        // Wait for the load to finish
        val wgRef = async { loadUserData(userID) }.await()
        val anRef = async { loadWGData(wgRef) }.await()
        async { loadContactPeronData(anRef) }.await()
        async { loadRoommatesData(wgRef) }.await()
        async { loadTaskData(wgRef) }.await()

        Log.d(TAG, "Loading Database succesfull")

        // Then add snapshotListener
        roommateSnapshotListener()
        taskSnapshotListener()
        wgSnapshotListener()
        contactPersonSnapshotListener()
    }

    /**
     * Fetches data for the currently logged in user form the firestore `db`.
     * Calls `loadWGData, loadRoommatesData` and `loadTaskData` with that information
     * @param {String} userID user id from the logged in user
     */
    suspend fun loadUserData(userID: String): DocumentReference {
        val wgRef: DocumentReference?
        var userRes: DocumentSnapshot? = null
        try{
            userRes = db.collection(Collection.Roommate.call).document(userID)
                .get()
                .asDeferred().await()
        }catch(e: Exception) {
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
    suspend fun loadWGData(wgRef: DocumentReference): DocumentReference{
        var wg: WG
        var wgRes: DocumentSnapshot? = null
        try {
            wgRes = db.collection(Collection.WG.call).document(wgRef.id)
                .get()
                .asDeferred().await()
        }catch (e: Exception){
            // Todo handle exception: give toast and shutdown
            e.printStackTrace()
        }

        wg = WG(wgRes!!)
        dataHandler.wg = wg

        // ansprechpartner reference
        val anRef = wgRes.get("ansprechpartner") as DocumentReference
        return anRef
    }

    suspend fun loadContactPeronData(anRef: DocumentReference){
        var an: ContactPerson?
        var anRes: DocumentSnapshot? = null
        try {

            anRes = db.collection(Collection.ContactPerson.call).document(anRef.id)
                .get()
                .asDeferred().await()
        }catch (e: Exception){
            // TODO handle exception: Give toast and create default contact person
            e.printStackTrace()
        }
        an = ContactPerson(anRes!!)
        dataHandler.contactPerson = an
    }

    suspend fun loadRoommatesData(wgRef: DocumentReference){
        var rmRes: QuerySnapshot? = null
        // Find all mitbewohner for this wg
        try {
            rmRes = db.collection(Collection.Roommate.call)
                .whereEqualTo("wg_id", wgRef)
                .get()
                .asDeferred().await()
        }catch (e: Exception){
            // TODO handle exception: Give toast and shutdown app
            e.printStackTrace()
        }

        rmRes!!.forEach { rm ->
            dataHandler.addRoommate(Roommate(rm))
        }
    }

    suspend fun loadTaskData(wgRef: DocumentReference){
        var tasksRes: QuerySnapshot? = null
        try {

            // Find all tasks for this wg with all roommates
            tasksRes = db.collection(Collection.Task.call)
                .whereEqualTo("wg_id", wgRef)
                .get()
                .asDeferred().await()
        }catch (e: Exception){
            // TODO handle exception: Give toast and shutdown app
            e.printStackTrace()
        }

        tasksRes!!.forEach { task ->
            dataHandler.addTask(Task(task))
        }


    }

    fun roommateSnapshotListener(){
        for (roommate in dataHandler.roommateList.values) {
            db.collection("mitbewohner").document(roommate.docID)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    // What happens if the database document gets changed
                    querySnapshot?.let {
                        dataHandler.getRoommate(roommate.docID).update(querySnapshot)
                        if(dataHandler.getRoommate(roommate.docID).equals(dataHandler.user!!.docID)){
                            dataHandler.user!!.update(querySnapshot)
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
    fun taskSnapshotListener(){
        for (task in dataHandler.taskList.values) {
            db.collection("aufgaben").document(task.docId)
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    firebaseFirestoreException?.let {
                        Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                        return@addSnapshotListener
                    }
                    // What happens if the database document gets changed
                    querySnapshot?.let {
                        dataHandler.getTask(task.docId).update(querySnapshot)
                        Log.d(
                            TAG,
                            "taskList updated ${dataHandler.taskList.toString()}"
                        )
                    }
                }
            Log.d(
                TAG,
                "added snapshotlistener to task: ${task.docId}"
            )
        }
    }
    fun wgSnapshotListener(){
        db.collection("wg").document(dataHandler.wg!!.docID)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                // What happens if the database document gets changed
                querySnapshot?.let {
                    dataHandler.wg?.update(querySnapshot)
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
    fun contactPersonSnapshotListener(){
        db.collection("ansprechpartner").document(dataHandler.contactPerson!!.docID)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Toast.makeText(this, it.message, Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                // What happens if the database document gets changed
                querySnapshot?.let {
                    dataHandler.contactPerson?.update(querySnapshot)
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
}