package com.example.udos_wg_tohuwabohu

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore

class MainActivity : AppCompatActivity() {

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

    private fun loadDatabase(userID: String){
        Log.d(TAG, "connecting to database...")
        db.collection("mitbewohner").document(userID)
            .get()
            .addOnSuccessListener { userRes ->
                Log.d(TAG,"found results")
                Log.d(TAG, "Mein Mitbewohner: ")
                Log.d(TAG, "${userRes.id} => ${userRes.data}")

                // wg reference
                val wgRef = userRes["wg_id"] as DocumentReference
                var wg: WG? = null
                db.collection("wg").document(wgRef.id)
                    .get()
                    .addOnSuccessListener { wgRes ->

                        // ansprechpartner reference
                        val anRef = wgRes["ansprechpartner"] as DocumentReference
                        var an: ContactPerson? = null

                        db.collection("ansprechpartner").document(anRef.id)
                            .get()
                            .addOnSuccessListener { anRes ->
                                an = ContactPerson(anRes)
                                wg = WG(wgRes)

                                // Find all mitbewohner for this wg
                                db.collection("mitbewohner")
                                    .whereEqualTo("wg_id", wgRef)
                                    .get()
                                    .addOnSuccessListener { mRes ->
                                        mRes.forEach { m ->
                                            dataHandler.addRoommate(Roommate(m))
                                        }

                                        // Find all tasks for this wg with all mitbewohner
                                        println("Searching for all tasks")
                                        db.collection("aufgaben")
                                            .whereEqualTo("wg_id", wgRef)
                                            .get()
                                            .addOnSuccessListener { afRes ->
                                                println("got an answer for ${"/" + wgRef.path}")
                                                println(afRes)
                                                afRes.forEach { af ->
                                                    println("Got a task")
                                                    println(af)
                                                    println("Found erlediger id: ${af["erlediger"]}")
                                                    // Get assigned mitbewohner
                                                    var mId = (af["erlediger"] as DocumentReference).id
                                                    val m = dataHandler.getRoommate(mId)

                                                    dataHandler.addTask(Task(af))
                                                }
                                                /*
                                                    All Mitbewohner and Tasks for this WG are loaded here
                                                    TODO: Versuche die scheiße synchron zu handeln damit nicht alles verschachtelt ist
                                                    TODO: Alternativ: Implementiere realtime updates
                                                 */
                                                val mySelf: Roommate = Roommate(userRes)
                                                dataHandler.wg = wg
                                                dataHandler.user = mySelf
                                            }
                                            .addOnFailureListener{ e->
                                                Log.w(TAG, "Error getting Tasks objects.", e)
                                            }
                                    }
                                    .addOnFailureListener{ e ->
                                        Log.w(TAG, "Error getting Mitbewohner objects.", e)
                                    }
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting Ansprechpartner Object.", exception)
                            }
                        println(wg)
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting WG Object.", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting Mitbewohner Object.", exception)
            }
    }
}