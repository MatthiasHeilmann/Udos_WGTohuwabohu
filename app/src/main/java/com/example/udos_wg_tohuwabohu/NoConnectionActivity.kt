package com.example.udos_wg_tohuwabohu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.core.os.postDelayed
import com.example.udos_wg_tohuwabohu.databinding.ActivityNoConnectionBinding
import com.example.udos_wg_tohuwabohu.dataclasses.ConnectionCheck

class NoConnectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityNoConnectionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoConnectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRetry.setOnClickListener{
            retry()
        }
    }
    var handler: Handler = Handler()
    var runnable: Runnable? = null
    var delay = 5000
    override fun onResume() {
        handler.postDelayed(Runnable {
            handler.postDelayed(runnable!!, delay.toLong())
            retry()
           Log.d("[CONN]","hit")
        }.also { runnable = it }, delay.toLong())
        super.onResume()
    }
    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(runnable!!)
    }
    fun retry(){
        if(ConnectionCheck.getInstance().checkConnection(this)){
            onPause()
            val intent = Intent(this@NoConnectionActivity, LoginActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}