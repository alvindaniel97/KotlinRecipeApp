package com.alvin.kotlinrecipeapp.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.alvin.kotlinrecipeapp.R
import com.alvin.kotlinrecipeapp.models.User
import com.alvin.kotlinrecipeapp.utils.Constants
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : BaseActivity() {
    private val mFireStore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        tvRegisterNow.setOnClickListener {
            val registerIntent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(registerIntent)
        }

        btnLogin.setOnClickListener {
            loginUser()
        }

        tvForgotPassword.setOnClickListener {
            val forgotPasswordIntent =
                Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(forgotPasswordIntent)
        }
    }

    fun validateLoginDetails(): Boolean {
        return when {
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errEmail), true)
                false
            }
            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errPassword), true)
                false
            }
            etPassword.text.toString().length <= 5 -> {
                showSnackBar(resources.getString(R.string.errPasswordLength), true)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches() -> {
                showSnackBar(resources.getString(R.string.errValidEmail), true)
                false
            }
            else -> {
                true
            }
        }
    }

    fun loginUser() {
        if (validateLoginDetails()) {
            showProgressDialog()
            val email: String = etEmail.text.toString().trim { it <= ' ' }
            val password: String = etPassword.text.toString().trim { it <= ' ' }

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->

                    if (task.isSuccessful) {
                        val currentUser = FirebaseAuth.getInstance().currentUser
                        var currentUserId = ""
                        if (currentUser != null) {
                            currentUserId = currentUser.uid
                        }
                        mFireStore.collection(Constants.USERS).document(currentUserId).get()
                            .addOnSuccessListener { document ->
                                val user = document.toObject(User::class.java)!!

                                // Save user info to sharedPref
                                val sharedPreferences =
                                    getSharedPreferences("userProfile", Context.MODE_PRIVATE)
                                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                editor.putString("userId", user.id)
                                editor.putString("firstName", user.firstname)
                                editor.putString("lastName", user.lastName)
                                editor.putString("email", user.email)
                                editor.apply()
                                Toast.makeText(
                                    this,
                                    "Welcome ${user.firstname} ${user.lastName}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                                startActivity(
                                    Intent(
                                        this@LoginActivity,
                                        BottomNavigationActivity::class.java
                                    )
                                )


                            }.addOnFailureListener { e ->
                                Log.i("Failed to fetch user", e.toString())
                            }
                    } else {
                        hideProgressDialog()
                        showSnackBar(task.exception!!.message.toString(), true)
                    }
                }.addOnFailureListener { e ->
                    showSnackBar("User not found", true)
                }
        }
    }
}