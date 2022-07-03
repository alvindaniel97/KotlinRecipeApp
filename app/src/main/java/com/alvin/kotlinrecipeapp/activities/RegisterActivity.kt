package com.alvin.kotlinrecipeapp.activities

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.alvin.kotlinrecipeapp.R
import com.alvin.kotlinrecipeapp.models.User
import com.alvin.kotlinrecipeapp.utils.Constants
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : BaseActivity() {
    private val mFireStore = FirebaseFirestore.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setupActionBar()

        tvLoginNow.setOnClickListener {
            onBackPressed()
        }

        btnRegister.setOnClickListener {
            userRegistration()
        }
    }

    private fun setupActionBar() {
        setSupportActionBar(tbRegister)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeActionContentDescription(R.drawable.ic_baseline_arrow_back_24)
        }
        tbRegister.setNavigationOnClickListener { onBackPressed() }
    }

    fun userRegistration() {
        if (validateRegisterDetails()) {
            showProgressDialog()
            val email: String = etEmail.text.toString().trim { it <= ' ' }
            val password: String = etPassword.text.toString().trim { it <= ' ' }

            // Create user in firebase with email and password
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(OnCompleteListener<AuthResult> { task ->
                    // Add user info to FireStore
                    if (task.isSuccessful) {
                        val firebaseUser: FirebaseUser = task.result!!.user!!

                        val user = User(
                            firebaseUser.uid,
                            etFirstName.text.toString().trim { it <= ' ' },
                            etLastName.text.toString().trim { it <= ' ' },
                            etEmail.text.toString().trim { it <= ' ' },
                        )

                        mFireStore.collection(Constants.USERS).document(user.id)
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener {
                                hideProgressDialog()
                                Toast.makeText(
                                    this@RegisterActivity,
                                    resources.getString(R.string.registerSuccess),
                                    Toast.LENGTH_SHORT
                                ).show()
                                FirebaseAuth.getInstance().signOut()
                                finish()
                            }.addOnFailureListener { e ->
                                hideProgressDialog()
                                Log.i("Error registering user", e.toString())
                            }

                    } else {
                        hideProgressDialog()
                        showSnackBar(task.exception!!.message.toString(), true)
                    }
                })
        }
    }

    fun validateRegisterDetails(): Boolean {
        return when {
            TextUtils.isEmpty(etFirstName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errFirstName), true)
                false
            }
            TextUtils.isEmpty(etLastName.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errLastName), true)
                false
            }
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errEmail), true)
                false
            }
            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errPassword), true)
                false
            }
            TextUtils.isEmpty(etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBar(resources.getString(R.string.errConfirmPassword), true)
                false
            }
            etPassword.text.toString().trim { it <= ' ' } != etConfirmPassword.text.toString()
                .trim { it <= ' ' } -> {
                showSnackBar(resources.getString(R.string.errPasswordMismatch), true)
                false
            }
            etPassword.text.toString().length <= 5 -> {
                showSnackBar(resources.getString(R.string.errPasswordLength), true)
                false
            }
            etConfirmPassword.text.toString().length <= 5 -> {
                showSnackBar(resources.getString(R.string.errConfirmPasswordLength), true)
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(etEmail.text.toString()).matches() -> {
                showSnackBar(resources.getString(R.string.errValidEmail), true)
                false
            }
            else -> {
                //showSnackBar(resources.getString(R.string.registerSuccess), false)
                true
            }
        }
    }


}