package com.example.udos_wg_tohuwabohu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.example.udos_wg_tohuwabohu.databinding.ActivityRegisterBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val TAG = "[Register Activity]"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // go to back to login
        binding.linkGotoLogin.setOnClickListener{
            onBackPressed()
        }
        // on click "register"
        binding.buttonRegister.setOnClickListener{
            when{
                TextUtils.isEmpty(binding.textfieldFirstName.text.toString().trim{it <= ' '}) ||
                TextUtils.isEmpty(binding.textfieldLastName.text.toString().trim{it <= ' '}) ||
                TextUtils.isEmpty(binding.textfieldUserName.text.toString().trim{it <= ' '}) -> {
                    Toast.makeText(this@RegisterActivity,
                    "Bitte gib alle Informationen an!",
                    Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"Input error")
                }
                !isEmailValid(binding.textfieldRegisterEmail.text.toString().trim{it <= ' '}) -> {
                    Toast.makeText(this@RegisterActivity,
                        "Bitte gib eine gültige Emailadresse an!",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"Input error")
                }
                binding.textfieldRegisterPassword.text.toString().trim{it <= ' '}.length<6 -> {
                    Toast.makeText(this@RegisterActivity,
                        "Dein Passwort ist zu kurz!",
                        Toast.LENGTH_SHORT).show()
                    Log.d(TAG,"Input error")
                }
                else -> {
                    // get email and password
                    val email : String = binding.textfieldRegisterEmail.text.toString().trim{it<=' '}
                    val password : String = binding.textfieldRegisterPassword.text.toString().trim{it<=' '}
                    val firstName : String = binding.textfieldFirstName.text.toString().trim{it<=' '}
                    val lastName : String = binding.textfieldLastName.text.toString().trim{it<=' '}
                    val userName : String = binding.textfieldUserName.text.toString().trim{it<=' '}

                    // create firebase user
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (task.isSuccessful){
                                    // if registration was successful show success message
                                    val firebaseUser: FirebaseUser = task.result!!.user!!
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "Registrierung erfolgreich!",
                                        Toast.LENGTH_SHORT
                                        ).show()
                                    Log.d(TAG,"Successfully registered")
                                    // go to main activity with uid and email
                                    val intent = Intent(this@RegisterActivity,LonelyPageActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("user_id",firebaseUser.uid)
                                    intent.putExtra("email_id",firebaseUser.email)

                                    saveUsertoDatabase(firstName, lastName, userName, email)

                                    startActivity(intent)
                                    finish()
                                }else{
                                    // if registration was not successful then show error message
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                        ).show()
                                    Log.d(TAG,"Error at registration")
                                    Log.d(TAG,task.exception!!.message.toString())
                                }
                            }
                        )
                }
            }
        }
    }
    // upon registry create user in collection "mitbewohner"
    fun saveUsertoDatabase(firstname: String, lastname: String, username: String, email: String){
        val db = FirebaseFirestore.getInstance()
        val emptyWG = db.collection("wg").document("EmptyWG")
        val user: MutableMap<String, Any> = HashMap()
        user["vorname"] = firstname
        user["nachname"] = lastname
        user["username"] = username
        user["emailID"] = email
        user["wg_id"] = emptyWG
        user["coin_count"] = 0
        user["guteNudel_count"] = 0
        user["kontostand"] = 0

        db.collection("mitbewohner")
            .document(Firebase.auth.currentUser!!.uid)
            .set(user)
    }

    val EMAIL_REGEX = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})";
    fun isEmailValid(email: String): Boolean {
        return EMAIL_REGEX.toRegex().matches(email);
    }
}