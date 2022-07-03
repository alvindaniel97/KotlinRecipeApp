package com.alvin.kotlinrecipeapp.activities

import android.os.Bundle
import android.widget.Toast
import com.alvin.kotlinrecipeapp.R
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPasswordActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        setupActionBar()

        btnRecoverPassword.setOnClickListener {
            val email: String = etForgotPasswordEmail.text.toString().trim { it <= ' ' }
            if (email.isEmpty()) {
                showSnackBar(resources.getString(R.string.forgot_password_empty), false)
            } else {
                showProgressDialog()
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        hideProgressDialog()
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this@ForgotPasswordActivity, "Recovery email is sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()
                        }
                    }
            }
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(tbForgotPassword)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeActionContentDescription(R.drawable.ic_baseline_arrow_back_24)
        }
        tbForgotPassword.setNavigationOnClickListener { onBackPressed() }
    }
}