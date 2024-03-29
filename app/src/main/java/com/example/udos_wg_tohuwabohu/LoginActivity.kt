package com.example.udos_wg_tohuwabohu

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.udos_wg_tohuwabohu.databinding.ActivityLoginBinding
import com.example.udos_wg_tohuwabohu.dataclasses.ConnectionCheck
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    val TAG = "[Login Activity]"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if(!ConnectionCheck.getInstance().check(this)){
            val intent = Intent(this@LoginActivity, NoConnectionActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
            return
        }
        // check if user is logged in
        val user = FirebaseAuth.getInstance().currentUser
        if( user != null){
            val intent = Intent(this@LoginActivity, LonelyPageActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra(
                "user_id",
                user.uid
            )
            intent.putExtra(
                "email_id",
                user.email)
            startActivity(intent)
            finish()
        }

        // go to register activity
        binding.linkGotoRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        // on click "login"
        binding.buttonLogin.setOnClickListener {
            when {
                // show errors if textfields are empty
                TextUtils.isEmpty(binding.textLoginEmail.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Bitte Email eingeben.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(binding.textLoginPassword.text.toString().trim { it <= ' ' }) -> {
                    Toast.makeText(
                        this@LoginActivity,
                        "Bitte Passwort eingeben.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else -> {
                    // get email and password
                    val email: String = binding.textfieldLoginEmail.text.toString().trim { it <= ' ' }
                    val password: String =
                        binding.textfieldLoginPassword.text.toString().trim { it <= ' ' }
                    // create firebase user
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "You are logged in successfully.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                // go to main activity with uid and email
                                val intent = Intent(this@LoginActivity, LonelyPageActivity::class.java)
                                intent.flags =
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                intent.putExtra(
                                    "user_id",
                                    FirebaseAuth.getInstance().currentUser!!.uid
                                )
                                intent.putExtra("email_id", email)
                                startActivity(intent)
                                finish()
                            } else {
                                // if registration was not successful then show error message
                                Toast.makeText(
                                    this@LoginActivity,
                                    task.exception!!.message.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                                Log.d(TAG,"Login failed")
                                Log.d(TAG,task.exception!!.message.toString(),)
                            }
                        }
                }
            }
        }
    }
}