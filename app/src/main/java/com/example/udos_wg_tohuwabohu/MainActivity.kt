package com.example.udos_wg_tohuwabohu

import android.content.ContentValues.TAG
import android.content.Intent
import android.content.IntentSender
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.replace
import com.example.udos_wg_tohuwabohu.databinding.ActivityMainBinding
import com.example.udos_wg_tohuwabohu.dataclasses.Ansprechpartner
import com.example.udos_wg_tohuwabohu.dataclasses.Mitbewohner
import com.example.udos_wg_tohuwabohu.dataclasses.WG
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    val db = Firebase.firestore
    val TAG = "[MainActivity]"


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

        Log.d(TAG, "connecting to database...")
        db.collection("mitbewohner").document(userID.toString())
            .get()
            .addOnSuccessListener { result ->
                Log.d(TAG,"found results")
                Log.d(TAG, "Mein Mitbewohner: ")
                Log.d(TAG, "${result.id} => ${result.data}")

                // wg reference
                val wg = result.data?.get("wgID")
                val docRef: DocumentReference = wg as DocumentReference

                db.collection("wg").document(docRef.id)
                    .get()
                    .addOnSuccessListener { result2 ->
                        Log.d(TAG, "Meine WG: ")
                        Log.d(TAG, "${result2.id} => ${result2.data}")
                        // ansprechpartner reference
                        val ansprechpartner = result2["ansprechpartner"]
                        val docRef2: DocumentReference = ansprechpartner as DocumentReference

                        db.collection("ansprechpartner").document(docRef2.id)
                            .get()
                            .addOnSuccessListener { result3 ->
                                val myAnsprechpartner: Ansprechpartner = Ansprechpartner(
                                    docRef2.id,
                                    result3["vorname"].toString(),
                                    result3["nachname"].toString(),
                                    result3["email"].toString(),
                                    result3["IBAN"].toString(),
                                    result3["tel_nr"].toString()
                                )
                                val myWG: WG = WG(
                                    docRef.id,
                                    result2["bezeichnung"].toString(),
                                    myAnsprechpartner,
                                    result2["einkaufsliste"] as Map<String, Boolean>
                                )
                                val mySelf: Mitbewohner = Mitbewohner(
                                    result.id,
                                    result["emailID"].toString(),
                                    result["vorname"].toString(),
                                    result["nachname"].toString(),
                                    result["username"].toString(),
                                    result["coin_count"] as Long,
                                    result["guteNudel_count"] as Long,
                                    result["kontostand"] as Double,
                                    myWG
                                )
                                Log.d(TAG, mySelf.toString())
                                Log.d(TAG, myWG.toString())
                                Log.d(TAG, myAnsprechpartner.toString())
                            }
                            .addOnFailureListener { exception ->
                                Log.w(TAG, "Error getting Ansprechpartner Object.", exception)
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.w(TAG, "Error getting WG Object.", exception)
                    }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting Mitbewohner Object.", exception)
            }
    }

    private fun replaceFragment(fragment : Fragment){
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_layout,fragment)
        fragmentTransaction.commit()
    }
}