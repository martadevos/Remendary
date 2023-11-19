package com.example.remendary.usecases.login

import android.content.ContentValues.TAG
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import com.example.remendary.R
import com.example.remendary.model.domain.User
import com.example.remendary.usecases.home.MainActivity
import com.example.remendary.util.Extensions.toast
import com.example.remendary.util.FirebaseUtils.firebaseAuth
import com.example.remendary.util.FirebaseUtils.firebaseUser
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CreateAccountActivity : AppCompatActivity() {
    private lateinit var userEmail: String
    private lateinit var userPassword: String
    private lateinit var username: String
    private lateinit var createAccountInputsArray: Array<EditText>
    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var etUsername: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnCreateAccount: Button
    private lateinit var btnSignIn2: Button

    private val db = Firebase.firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        etUsername = this.findViewById(R.id.etUsername)
        etEmail = this.findViewById(R.id.etEmail)
        etPassword = this.findViewById(R.id.etPassword)
        etConfirmPassword = this.findViewById(R.id.etConfirmPassword)
        btnCreateAccount = this.findViewById(R.id.btnCreateAccount)
        btnSignIn2 = this.findViewById(R.id.btnSignIn2)

        createAccountInputsArray = arrayOf(etUsername, etEmail, etPassword, etConfirmPassword)
        btnCreateAccount.setOnClickListener {
            signIn()
        }

        btnSignIn2.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
            toast("please sign into your account")
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        val user: FirebaseUser? = firebaseUser
        user?.let {
            startActivity(Intent(this, MainActivity::class.java))
            toast("welcome back")
        }
    }

    /**
     * Function that checks whether inputs are empty or not.
     *
     * @return Boolean. Returns true if items are not empty and false if they are.
     */
    private fun notEmpty(): Boolean = etUsername.text.toString().trim().isNotEmpty() &&
            etEmail.text.toString().trim().isNotEmpty() &&
            etPassword.text.toString().trim().isNotEmpty() &&
            etConfirmPassword.text.toString().trim().isNotEmpty()

    /**
     * Function that calls the notEmpty function to check if the inputs are not empty and, if they are not,
     * checks if password and password confirmation are the same.
     *
     * @return Boolean. Returns true if inputs are not empty and passwords are the same, and false
     * in any other case.
     */
    private fun identicalPassword(): Boolean {
        var identical = false
        val exists = exists()
        //Items are not empty and passwords are the same
        if (notEmpty()) {
            if (!exists && etPassword.text.toString().trim() == etConfirmPassword.text.toString().trim()) {
                identical = true
            } else if (!exists) {
                etConfirmPassword.error = "Passwords are not matching"
            } else{
                etUsername.error = "This username already exists"
            }
        }
        //Items are empty
        else{
            //Shows an error message in each item edit text when it is focused
            createAccountInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
        return identical
    }

    /**
     * Procedure that checks if there is an user in the DB with the current username and returns a
     * boolean whit the result
     * @return Boolean. Returns true if there is a user with this username and false if there is not
     */
    private fun exists(): Boolean {
        var exists = false
        db.collection("users").document(username).get()
            .addOnSuccessListener { document ->
                exists = document.exists()
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
        return exists
    }

    /**
     * Procedure that calls the identicalPassword function and if returned true creates an user
     * in firebase and returns a message showing whether the creation was successful or not
     */
    private fun signIn() {
        username = etUsername.text.toString().trim()
        if (identicalPassword()) {
            userEmail = etEmail.text.toString().trim()
            userPassword = etPassword.text.toString().trim()

            //User creation
            firebaseAuth.createUserWithEmailAndPassword(userEmail, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseUser?.updateProfile(userProfileChangeRequest {
                            displayName = username
                        })
                        val userDb = hashMapOf(
                            "username" to username,
                            "email" to userEmail,
                        )
                        db.collection("users").document(username).set(userDb)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot added with ID: ${username}")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Error adding document", e)
                            }
                        toast("account created successfully!")
                        sendEmailVerification()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    } else {
                        toast("failed to Authenticate!")
                    }
                }

            firebaseUser!!.updateProfile(userProfileChangeRequest {
                displayName = "Jane Q. User"
            })
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                    }
                }
        }
    }

    /* send verification email to the new user. This will only
    *  work if the firebase user is not null.
    */

    private fun sendEmailVerification() {
        firebaseUser?.let {
            it.sendEmailVerification().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    toast("email sent to $userEmail")
                }
            }
        }
    }
}