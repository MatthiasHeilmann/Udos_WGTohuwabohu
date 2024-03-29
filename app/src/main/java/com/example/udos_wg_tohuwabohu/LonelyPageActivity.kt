package com.example.udos_wg_tohuwabohu

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.udos_wg_tohuwabohu.databinding.ActivityLonelypageBinding
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.Collections
import com.example.udos_wg_tohuwabohu.dataclasses.ContactPerson
import com.example.udos_wg_tohuwabohu.dataclasses.Roommate
import com.example.udos_wg_tohuwabohu.dataclasses.WG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.asDeferred
import java.sql.Timestamp
import kotlin.random.Random

class LonelyPageActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLonelypageBinding
    val db = Firebase.firestore
    val TAG = "[LonelyPageActivity]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //This is just so our current version of the MainActivity does not immediately crash
        val email = intent.getStringExtra("email_id")
        db.collection("mitbewohner").document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val wg_id = document.get("wg_id")
                Log.d(TAG, "onCreate Lonelypage: wg_id: $wg_id")
                if (document.get("wg_id") != db.collection("wg").document("EmptyWG")) {
                    val intent = createMainActivityIntent(this@LonelyPageActivity, email!!)
                    startActivity(intent)
                    return@addOnSuccessListener
                }

            }
            .addOnFailureListener {
                displayDatabaseError(this@LonelyPageActivity)
            }
        binding = ActivityLonelypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonLogoutLonelypage.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Log.d(TAG,"User logged out")
            startActivity(Intent(this@LonelyPageActivity, LoginActivity::class.java))
            finish()
        }

        binding.buttonCreateWg.setOnClickListener {
            try {
                createWG(email!!)
            } catch (e: Exception) {
                Toast.makeText(
                    this@LonelyPageActivity,
                    "Es ist ein Fehler aufgetreten. Bitte versuche es nocheinmal",
                    Toast.LENGTH_SHORT
                ).show()
                e.printStackTrace()
            }
        }

        binding.buttonJoinWg.setOnClickListener {
            when {
                // show errors if textfields are empty
                TextUtils.isEmpty(
                    binding.textfieldWgEntryCode.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LonelyPageActivity,
                        "Bitte gib einen Beitritts-Code ein.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d(TAG,"Invalid code")
                }
                else -> {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val WgEntryCode: Int = binding.textfieldWgEntryCode.text.toString().trim { it <= ' ' }.toInt()
                    //Make database query matching entry code
                    try{
                        db.collection("wg")
                            .whereEqualTo("entrycode", WgEntryCode)
                            .get()
                            //Update User Profile and start Main Activity
                            .addOnSuccessListener { WG_documents ->
                                Log.d(TAG,"Found WG")
                                WG_documents.forEach {
                                    db.collection("mitbewohner").document(uid)
                                        .update("wg_id",db.collection("wg").document(it.id))
                                        .addOnSuccessListener {
                                            Log.d(TAG,"Joined WG")
                                            val intent= createMainActivityIntent(this@LonelyPageActivity, email!!)
                                            startActivity(intent)
                                        }
                                }
                            }
                    }catch (e:Exception){
                        Toast.makeText(this@LonelyPageActivity,
                            "Es gab einen Fehler beim Beitreten der WG. Stelle sicher, dass der Beitrittscode korrekt ist.",
                            Toast.LENGTH_LONG)
                            .show()
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    private fun createWG(email: String) = runBlocking<Unit> {
        try {
            val newWG = async { db.collection("wg").document() }.await()
            val newAnsprechpartner = async { db.collection("ansprechpartner").document() }.await()
            async {
                newAnsprechpartner.set(
                    hashMapOf(
                        "IBAN" to "Noch nicht festgelegt",
                        "email" to "Noch nicht festgelegt",
                        "nachname" to "",
                        "tel_nr" to "Noch nicht festgelegt",
                        "vorname" to "Noch nicht festgelegt"
                    )
                )
            }.await()
            Log.d(TAG,"ContactPerson created")
            async {
                newWG.set(
                    hashMapOf(
                        "ansprechpartner" to newAnsprechpartner,
                        "bezeichnung" to "Neue WG",
                        "einkaufsliste" to emptyMap<String, Int>(),
                        "calendar" to emptyList<MutableMap<String, com.google.firebase.Timestamp>>(),
                        "entrycode" to createEntryCode()
                    )
                )
            }.await()
            Log.d(TAG,"WG created")
            async {
                db.collection("mitbewohner")
                    .document(Firebase.auth.currentUser!!.uid)
                    .update("wg_id", newWG)
            }.await()
            Log.d(TAG,"Roommate updated")
            val intent = createMainActivityIntent(this@LonelyPageActivity, email!!)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(TAG, e.toString())
            Toast.makeText(this@LonelyPageActivity,"Es gab einen Fehler beim Erstellen der WG. Bitte versuche es erneut.",Toast.LENGTH_LONG).show()
        }
    }

    private fun createEntryCode(): Int {
        val wgEntryCode = Random.nextInt(100000, 999999)
        return wgEntryCode
    }

    private fun displayDatabaseError(context: Context) {
        Toast.makeText(
            context,
            "Database Query Failed, Please Try Again",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createMainActivityIntent(context: Context, email: String): Intent {
        val intent = Intent(context, MainActivity::class.java)
        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(
            "user_id",
            FirebaseAuth.getInstance().currentUser!!.uid
        )
        intent.putExtra("email_id", email)
        return intent
    }
}