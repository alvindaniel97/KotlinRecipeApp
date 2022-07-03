package com.alvin.kotlinrecipeapp.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.WindowInsets
import android.view.WindowManager
import com.alvin.kotlinrecipeapp.R
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }

        Handler().postDelayed({
            val currentUser = FirebaseAuth.getInstance().currentUser
            if(currentUser != null){
                val mIntent = Intent(this@SplashActivity, BottomNavigationActivity::class.java)
                startActivity(mIntent)
                finish()
            }else {
                val mIntent = Intent(this@SplashActivity, LoginActivity::class.java)
                startActivity(mIntent)
                finish()
            }
        }, 1500)
    }
}