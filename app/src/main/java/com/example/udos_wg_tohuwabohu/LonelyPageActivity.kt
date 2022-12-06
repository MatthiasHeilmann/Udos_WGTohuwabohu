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
import com.example.udos_wg_tohuwabohu.dataclasses.WG
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.snapshots
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlin.random.Random

class LonelyPageActivity: AppCompatActivity() {
    private lateinit var binding: ActivityLonelypageBinding
    val db = Firebase.firestore
    val TAG = "[LonelyPageActivity]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //This is just so our current version of the MainActivity does not immediately crash
        val email = intent.getStringExtra("email_id")

        db.collection("mitbewohner").document(Firebase.auth.currentUser!!.uid )
            .get()
            .addOnSuccessListener { document ->
                val wg_id= document.get("wg_id")
                Log.d(TAG, "onCreate von Lonelypage: wg_id: $wg_id")
                if(document.get("wg_id") != db.collection("wg").document("EmptyWG")){
                    var intent= createMainActivityIntent(this@LonelyPageActivity, email!!)
                    startActivity(intent)
                }
                else{
                    binding = ActivityLonelypageBinding.inflate(layoutInflater)
                    setContentView(binding.root)

                    binding.buttonLogoutLonelypage.setOnClickListener {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this@LonelyPageActivity, LoginActivity::class.java))
                        finish()
                    }

                    binding.buttonCreateWg.setOnClickListener {
                        val newWG =db.collection("wg").document()
                        val newAnsprechpartner = db.collection("ansprechpartner").document()
                        newAnsprechpartner.set(hashMapOf(
                            "IBAN" to "",
                            "email" to "",
                            "nachname" to "",
                            "tel_nr" to "",
                            "vorname" to "")).addOnSuccessListener {
                                newWG.set(hashMapOf(
                                "ansprechpartner" to newAnsprechpartner,
                                "bezeichnung" to "",
                                "einkaufsliste" to emptyMap<String, Int>(),
                                "entrycode" to createEntryCode())
                            ).addOnSuccessListener { db.collection("mitbewohner").document(Firebase.auth.currentUser!!.uid )
                                .update("wg_id", newWG)
                                .addOnSuccessListener {
                                    var intent= createMainActivityIntent(this@LonelyPageActivity, email!!)
                                    startActivity(intent) }
                                .addOnFailureListener {
                                    displayDatabaseError(this@LonelyPageActivity)
                                } }
                                .addOnFailureListener {
                                    displayDatabaseError(this@LonelyPageActivity)
                                } }


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
                                val WgEntryCode: Int = binding.textfieldWgEntryCode.text.toString().trim { it <= ' ' }.toInt()
                                //Make database query matching entry code
                                db.collection("wg")
                                    .whereEqualTo("entrycode", WgEntryCode)
                                    .get()
                                    //Update User Profile and start Main Activity
                                    .addOnSuccessListener { WG_documents ->
                                        WG_documents.forEach {
                                            db.collection("mitbewohner").document(uid)
                                                .update("wg_id",db.collection("wg").document(it.id))
                                                .addOnSuccessListener {
                                                    var intent= createMainActivityIntent(this@LonelyPageActivity, email!!)
                                                    startActivity(intent)
                                                }
                                                .addOnFailureListener {
                                                    displayDatabaseError(this@LonelyPageActivity)
                                                }
                                        }
                                    }
                                    .addOnFailureListener {
                                        displayDatabaseError(this@LonelyPageActivity)
                                    }
                            }
                        }
                    }

                }
            }
            .addOnFailureListener {
                displayDatabaseError(this@LonelyPageActivity)
            }








}
    fun createEntryCode(): Int {
        var wgEntryCode = Random.nextInt(100000, 999999)
        /*var duplicate = true
        while (duplicate) {
            wgEntryCode = Random.nextInt(100000, 999999)
            db.collection("wg")
                .whereEqualTo("entrycode", wgEntryCode)
                .get()
                .addOnSuccessListener { result ->
                    Log.d(TAG, "createEntryCode: LOOPING")
                    for (document in result) {
                        if (!document.exists()) {
                            duplicate = false
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "Error getting documents: ", exception)
                }
        }*/
        return wgEntryCode
    }
}
    fun displayDatabaseError(context: Context) {
        Toast.makeText(
            context,
            "Database Query Failed, Please Try Again",
            Toast.LENGTH_SHORT
        ).show()
    }

    fun createMainActivityIntent(context: Context, email: String): Intent{
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