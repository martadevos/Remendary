package com.example.remendary.usecases.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import com.example.remendary.R
import com.example.remendary.usecases.home.MainActivity
import com.example.remendary.util.Extensions.toast
import com.example.remendary.util.FirebaseUtils.firebaseAuth

class SignInActivity : AppCompatActivity() {
    lateinit var signInEmail: String
    lateinit var signInPassword: String
    lateinit var signInInputsArray: Array<EditText>
    lateinit var etSignInEmail : EditText
    lateinit var etSignInPassword : EditText
    lateinit var btnCreateAccount2 : Button
    lateinit var btnSignIn : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        etSignInEmail = this.findViewById(R.id.etSignInEmail)
        etSignInPassword = this.findViewById(R.id.etSignInPassword)
        btnCreateAccount2 = this.findViewById(R.id.btnCreateAccount2)
        btnSignIn = this.findViewById(R.id.btnSignIn)

        signInInputsArray = arrayOf(etSignInEmail, etSignInPassword)
        btnCreateAccount2.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
            finish()
        }

        btnSignIn.setOnClickListener {
            signInUser()
        }
    }

    /**
     * Function that checks whether the email and password edit texts are empty or not.
     *
     * @return Boolean. Returns true if they are not empty and false if they are.
     */
    private fun notEmpty(): Boolean = signInEmail.isNotEmpty() && signInPassword.isNotEmpty()

    /**
     * Procedure that calls the notEmpty function to check if the fields are not empty and
     * if returned true signs in with firebase and shows a message showing if it was successful
     * or not; if fields are empty it displays an error message in the inputs edit texts when
     * they are focused
     */
    private fun signInUser() {
        signInEmail = etSignInEmail.text.toString().trim()
        signInPassword = etSignInPassword.text.toString().trim()

        if (notEmpty()) {
            firebaseAuth.signInWithEmailAndPassword(signInEmail, signInPassword)
                .addOnCompleteListener { signIn ->
                    if (signIn.isSuccessful) {
                        startActivity(Intent(this, MainActivity::class.java))
                        toast("signed in successfully")
                        finish()
                    } else {
                        toast("sign in failed")
                    }
                }
        } else {
            signInInputsArray.forEach { input ->
                if (input.text.toString().trim().isEmpty()) {
                    input.error = "${input.hint} is required"
                }
            }
        }
    }
}