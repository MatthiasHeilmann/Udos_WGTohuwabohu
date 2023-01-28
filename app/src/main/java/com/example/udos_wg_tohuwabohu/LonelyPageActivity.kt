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
        //TODO prevent no internet connection
        db.collection("mitbewohner").document(Firebase.auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document ->
                val wg_id = document.get("wg_id")
                Log.d(TAG, "onCreate von Lonelypage: wg_id: $wg_id")
                if (document.get("wg_id") != db.collection("wg").document("EmptyWG")) {
                    var intent = createMainActivityIntent(this@LonelyPageActivity, email!!)
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
                        "Please enter your code to join a WG.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val uid = FirebaseAuth.getInstance().currentUser!!.uid
                    val WgEntryCode: Int =
                        binding.textfieldWgEntryCode.text.toString().trim { it <= ' ' }.toInt()
                    joinWG(WgEntryCode, uid, email!!)
                }
            }
        }
    }

    fun createWG(email: String) = runBlocking<Unit> {
        try {
            val newWG = async { db.collection("wg").document() }.await()
            val newAnsprechpartner = async { db.collection("ansprechpartner").document() }.await()
            async {
                newAnsprechpartner.set(
                    hashMapOf(
                        "IBAN" to "tbd",
                        "email" to "tbd",
                        "nachname" to "",
                        "tel_nr" to "tbd",
                        "vorname" to "tbd"
                    )
                )
            }.await()
            async {
                newWG.set(
                    hashMapOf(
                        "ansprechpartner" to newAnsprechpartner,
                        "bezeichnung" to "Neue WG",
                        "einkaufsliste" to emptyMap<String, Int>(),
//                        "calendar" to MutableList<MutableMap<String,com.google.firebase.Timestamp>>(0,),
                        "calendar" to emptyList<MutableMap<String, com.google.firebase.Timestamp>>(),
                        "entrycode" to createEntryCode()
                    )
                )
            }.await()
            async {
                db.collection("mitbewohner")
                    .document(Firebase.auth.currentUser!!.uid)
                    .update("wg_id", newWG)
            }.await()
            val intent = createMainActivityIntent(this@LonelyPageActivity, email!!)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("[LONELY PAGE]", "[#############################]")
            Log.d("[LONELY PAGE]", e.toString())
        }
    }

    fun joinWG(entryCode: Int, uid: String, email: String) = runBlocking<Unit> {
        val wg = async { db.collection("wg").whereEqualTo("entrycode", entryCode).get() }.await()
        if (wg.result.isEmpty) {
            Toast.makeText(
                this@LonelyPageActivity,
                "Ungültiger Code. Bitte überprüfe deine Eingabe.",
                Toast.LENGTH_SHORT
            ).show()
            return@runBlocking
        }
        async {
            wg.addOnSuccessListener { documents ->
                for (document in documents) {
                    val wgRef = db.collection("wg").document(document.id).get()
                    db.collection("mitbewohner").document(uid).update("wg_id", wgRef)
                }
            }
        }.await()
        val intent = createMainActivityIntent(this@LonelyPageActivity, email)
        startActivity(intent)
    }

    fun createEntryCode(): Int {
        val wgEntryCode = Random.nextInt(100000, 999999)
//        if(codeList.contains(wgEntryCode.toLong())){
//            return createEntryCode(codeList)
//        }
        return wgEntryCode
    }

    fun displayDatabaseError(context: Context) {
        Toast.makeText(
            context,
            "Database Query Failed, Please Try Again",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun createMainActivityIntent(context: Context, email: String): Intent {
        var intent = Intent(context, MainActivity::class.java)
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