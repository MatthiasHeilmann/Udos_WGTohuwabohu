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

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

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
                // show errors if textfields are empty
                TextUtils.isEmpty(binding.textfieldRegisterEmail.text.toString().trim{it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter Email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                TextUtils.isEmpty(binding.textfieldRegisterPassword.text.toString().trim{it <= ' '}) -> {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please enter Password.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else -> {
                    // get email and password
                    val email : String = binding.textfieldRegisterEmail.text.toString().trim{it<=' '}
                    val password : String = binding.textfieldRegisterPassword.text.toString().trim{it<=' '}
                    // create firebase user
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (task.isSuccessful){
                                    // if registration was successful show success message
                                    val firebaseUser: FirebaseUser = task.result!!.user!!
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        "You are registered successfully.",
                                        Toast.LENGTH_SHORT
                                        ).show()
                                    // go to main activity with uid and email
                                    val intent = Intent(this@RegisterActivity,MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("user_id",firebaseUser.uid)
                                    intent.putExtra("email_id",firebaseUser.email)
                                    startActivity(intent)
                                    finish()
                                }else{
                                    // if registration was not successful then show error message
                                    Toast.makeText(
                                        this@RegisterActivity,
                                        task.exception!!.message.toString(),
                                        Toast.LENGTH_SHORT
                                        ).show()
                                }
                            }
                        )
                }
            }
        }



    }
}