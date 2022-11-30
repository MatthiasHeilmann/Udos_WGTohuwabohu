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
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

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
    private fun loadDatabase(userID: String){
        Log.d(TAG, "connecting to database...")
        // TODO wait for the load to finish
        loadUserData(userID)

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
    fun loadUserData(userID: String){
        db.collection(Collection.Roommate.call).document(userID)
            .get()
            .addOnSuccessListener { userRes ->
                Log.d(TAG,"Current User is: ${userRes.id} => ${userRes.data}")
                dataHandler.user = Roommate(userRes);
                // wg reference
                val wgRef = userRes["wg_id"] as DocumentReference
                loadWGData(wgRef)
                loadRoommatesData(wgRef)
                loadTaskData(wgRef)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting Roommate Object.", exception)
            }
    }

    /**
     * Fetches data for the current user's WG from firestore `db`.
     * Call `loadContactPeronData` with that information
     */
    fun loadWGData(wgRef: DocumentReference){
        var wg: WG? = null
        db.collection(Collection.WG.call).document(wgRef.id)
            .get()
            .addOnSuccessListener { wgRes ->

                wg = WG(wgRes)
                dataHandler.wg = wg

                // ansprechpartner reference
                val anRef = wgRes["ansprechpartner"] as DocumentReference
                println("Realted to WG: $wg")
                loadContactPeronData(anRef)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting WG Object.", exception)
            }
    }

    fun loadContactPeronData(anRef: DocumentReference){
        var an: ContactPerson?

        db.collection(Collection.ContactPerson.call).document(anRef.id)
            .get()
            .addOnSuccessListener { anRes ->
                an = ContactPerson(anRes)
                dataHandler.contactPerson = an
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting Contact Person Object.", exception)
            }
    }

    fun loadRoommatesData(wgRef: DocumentReference){
        // Find all mitbewohner for this wg
        db.collection(Collection.Roommate.call)
            .whereEqualTo("wg_id", wgRef)
            .get()
            .addOnSuccessListener { rmRes ->
                rmRes.forEach { rm ->
                    dataHandler.addRoommate(Roommate(rm))
                }
            }
            .addOnFailureListener{ e ->
                Log.w(TAG, "Error getting Roommate objects.", e)
            }
    }

    fun loadTaskData(wgRef: DocumentReference){
        // Find all tasks for this wg with all roommates
        db.collection(Collection.Task.call)
            .whereEqualTo("wg_id", wgRef)
            .get()
            .addOnSuccessListener { afRes ->
                afRes.forEach { af ->
                    // Get assigned mitbewohner
                    var mId = (af["erlediger"] as DocumentReference).id
                    val m = dataHandler.getRoommate(mId)

                    dataHandler.addTask(Task(af))
                }
            }
            .addOnFailureListener{ e->
                Log.w(TAG, "Error getting Tasks objects.", e)
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