package com.example.natha.battleship

import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.login.*


/**
 * Created by Natha on 11/12/2017.
 */
class Login : AppCompatActivity() {
    lateinit var emailField : EditText
    lateinit var passwordField : EditText
    lateinit var errorField : TextView
    lateinit var loginButton : Button
    lateinit var registerButton : Button
    lateinit var cancel : TextView
    lateinit var title : TextView
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var task : Task<AuthResult>
    var handler = Handler()
    var loginState = LoginState.STARTED



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        errorField = findViewById(R.id.errorMessage)
        loginButton = findViewById(R.id.login)
        registerButton = findViewById(R.id.register)
        cancel = findViewById(R.id.cancel)
        title = findViewById(R.id.title)

        loginButton.setOnClickListener(clickListener)
        registerButton.setOnClickListener(clickListener)
        cancel.setOnClickListener(clickListener)
        auth = FirebaseAuth.getInstance()
        if(auth != null && auth.currentUser != null)
        {
            Log.e("LOGIN", "Current user is logged in")
            currentUser = auth.currentUser!!
        }
        if(auth != null && auth.currentUser != null && currentUser.isEmailVerified)
        {
            Log.e("LOGIN", "Current user exists and has email verified ")
            intent = Intent(applicationContext, Game::class.java)
            intent.putExtra("userEmail", currentUser.email)
            intent.putExtra("userUID", currentUser.uid)
            startActivity(intent)
            finish()
        }
    }

    val clickListener = View.OnClickListener { view ->

        when(view.id)
        {
            R.id.login ->
            {
                loginState = LoginState.SIGNING_IN
                if(passwordField.text.contains(("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#:?!@\$%^&*-]).{8,}$").toRegex()) && !passwordField.text.isEmpty() && !emailField.text.isEmpty()) {
                    Log.e("LOGIN", "Attempting to sign in first")
                    auth.signInWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString()).addOnCompleteListener(onCompleteListener)
                }
                else
                {
                    loginState = LoginState.STARTED
                    Log.e("LOGIN", "Fields are either empty or the password doesnt meet the criteria")
                    errorField.setBackgroundColor(Color.RED)
                    if(passwordField.text.isEmpty() || emailField.text.isEmpty())
                        errorField.setText("Password and Email fields must not be empty")
                    else
                        errorField.setText("Password requires 1 or more capital letters, digits, and non-alphanumeric symbols and must be at least 8 characters")
                }
            }

            R.id.register ->
            {
                loginState = LoginState.CREATING
                if(passwordField.text.contains(("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#:?!@\$%^&*-]).{8,}$").toRegex()) && !passwordField.text.isEmpty() && !emailField.text.isEmpty()) {
                    Log.e("LOGIN", "Attempting to create user")
                    auth.createUserWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString()).addOnCompleteListener(onCompleteListener)
                }
                else
                {
                    loginState = LoginState.STARTED
                    Log.e("LOGIN", "Fields are either empty or the password doesnt meet the criteria")
                    errorField.setBackgroundColor(Color.RED)
                    if(passwordField.text.isEmpty() || emailField.text.isEmpty())
                        errorField.setText("Password and Email fields must not be empty")
                    else
                        errorField.setText("Password requires 1 or more capital letters, digits, and non-alphanumeric symbols and must be at least 8 characters")
                }
            }

            R.id.cancel ->
            {
                emailField.text.clear()
                emailField.hint = "Email:"
                emailField.gravity = Gravity.LEFT
                emailField.isFocusable = true
                emailField.isFocusableInTouchMode = true
                emailField.isClickable = true
                emailField.isEnabled = true
                passwordField.text.clear()
                passwordField.hint = "Password:"
                cancel.visibility = View.INVISIBLE
                errorField.setBackgroundColor(android.R.attr.editTextBackground)
                errorField.text = ""
                errorField.visibility = View.VISIBLE
                passwordField.visibility = View.VISIBLE
                title.visibility = View.VISIBLE
                loginButton.visibility = View.VISIBLE
                registerButton.visibility = View.VISIBLE
            }
        }
    }

    val onCompleteListener = OnCompleteListener<AuthResult>{ task ->

        if(task.isSuccessful) {
            Log.e("Login", "login or creation")
            auth = FirebaseAuth.getInstance()
            if (auth != null && auth.currentUser != null) {
                Log.e("LOGIN", "User exists on login or creation")
                currentUser = auth.currentUser!!
            }

            if (loginState == LoginState.SIGNING_IN) {
                if (currentUser.isEmailVerified) {
                    Log.e("LOGIN", "Email is verified on sign in, take to main screen")
                    intent = Intent(applicationContext, Game::class.java)
                    intent.putExtra("userEmail", currentUser!!.email)
                    intent.putExtra("userUID", currentUser!!.uid)
                    startActivity(intent)
                    finish()
                } else {
                    emailField.setText("Email sent out, please verify your email.")
                    emailField.isFocusableInTouchMode = false
                    emailField.gravity = Gravity.CENTER
                    emailField.isFocusable = false
                    emailField.isClickable = false
                    emailField.isEnabled = false;
                    cancel.visibility = View.VISIBLE
                    errorField.visibility = View.INVISIBLE
                    loginButton.visibility = View.INVISIBLE
                    registerButton.visibility = View.INVISIBLE
                    passwordField.visibility = View.INVISIBLE
                    title.visibility = View.INVISIBLE
                    sendVerificationEmail()
                    var timer = 0
                    for(i in 1..301) {
                        Handler().postDelayed(Runnable {

                            if(cancel.visibility == View.INVISIBLE) handler.removeCallbacks(null)

                            else if (timer >= 300 && cancel.visibility == View.VISIBLE) {
                                Log.e("LOGIN", "Times up, kicking back to login screen")
                                emailField.text.clear()
                                emailField.hint = "Email:"
                                emailField.gravity = Gravity.LEFT
                                emailField.isFocusable = true
                                emailField.isFocusableInTouchMode = true
                                emailField.isClickable = true
                                emailField.isEnabled = true
                                passwordField.text.clear()
                                passwordField.hint = "Password:"
                                cancel.visibility = View.INVISIBLE
                                errorField.setBackgroundColor(Color.RED)
                                errorField.setText("Email verifcation not completed, check email or internet connection.")
                                errorField.visibility = View.VISIBLE
                                passwordField.visibility = View.VISIBLE
                                title.visibility = View.VISIBLE
                                loginButton.visibility = View.VISIBLE
                                registerButton.visibility = View.VISIBLE

                                auth.signOut()
                            } else {
                                Log.e("LOGIN", "Timer is at: " + timer)
                                if (currentUser.isEmailVerified) {
                                    Log.e("LOGIN", "Email is verified, take to main screen")
                                    intent = Intent(applicationContext, Game::class.java)
                                    intent.putExtra("userEmail", currentUser!!.email)
                                    intent.putExtra("userUID", currentUser!!.uid)
                                    startActivity(intent)
                                    finish()
                                }
                            }
                            timer++
                        }, i.toLong() * 1000)
                    }
                }
            } else if (loginState == LoginState.CREATING) {
                emailField.setText("Email sent out, please verify your email.")
                emailField.isFocusable = false
                emailField.isFocusableInTouchMode = false
                emailField.isClickable = false
                emailField.isEnabled = false
                emailField.gravity = Gravity.CENTER
                cancel.visibility = View.VISIBLE
                errorField.visibility = View.INVISIBLE
                loginButton.visibility = View.INVISIBLE
                registerButton.visibility = View.INVISIBLE
                passwordField.visibility = View.INVISIBLE
                title.visibility = View.INVISIBLE
                var timer = 0
                for(i in 1..301) {
                    handler.postDelayed(Runnable {

                        if(cancel.visibility == View.INVISIBLE) handler.removeCallbacks(null)

                        else if (timer >= 300 && cancel.visibility == View.VISIBLE) {
                            Log.e("LOGIN", "Times up, kicking back to login screen")
                            emailField.text.clear()
                            emailField.hint = "Email:"
                            emailField.gravity = Gravity.LEFT
                            emailField.isFocusable = true
                            emailField.isFocusableInTouchMode = true
                            emailField.isClickable = true
                            emailField.isEnabled = true
                            passwordField.text.clear()
                            passwordField.hint = "Password:"
                            cancel.visibility = View.INVISIBLE
                            errorField.setBackgroundColor(Color.RED)
                            errorField.setText("Email verifcation not completed, check email or internet connection.")
                            errorField.visibility = View.VISIBLE
                            passwordField.visibility = View.VISIBLE
                            title.visibility = View.VISIBLE
                            loginButton.visibility = View.VISIBLE
                            registerButton.visibility = View.VISIBLE

                            auth.signOut()
                        } else {
                            Log.e("LOGIN", "Timer is at: " + timer)
                            if (currentUser.isEmailVerified) {
                                Log.e("LOGIN", "Email is verified, take to main screen")
                                intent = Intent(applicationContext, Game::class.java)
                                intent.putExtra("userEmail", currentUser!!.email)
                                intent.putExtra("userUID", currentUser!!.uid)
                                startActivity(intent)
                                finish()
                            }
                        }
                        timer++
                    }, i.toLong() * 1000)
                }
            }
        }

        else
        {
            auth.signOut()
            errorField.text = "Authentication failed"
            errorField.setBackgroundColor(Color.RED)
            if(loginState == LoginState.SIGNING_IN) errorField.text = errorField.text.toString() + ", user may not exist or check internet settings."
            else if(loginState == LoginState.CREATING)  errorField.text = errorField.text.toString() + ", check internet settings."
        }
    }

    private fun sendVerificationEmail()
    {
        Log.e("LOGIN", "Sending verification email")
        loginState = LoginState.VERIFYING
        if(currentUser != null)currentUser.sendEmailVerification().addOnCompleteListener(OnCompleteListener() { task ->

            if(!task.isSuccessful && cancel.visibility == View.INVISIBLE) {
                errorField.text = "Email verification failed to send, check your internet settings."
                emailField.text.clear()
                emailField.hint = "Email:"
                emailField.gravity = Gravity.LEFT
                emailField.isFocusable = true
                emailField.isFocusableInTouchMode = true
                emailField.isClickable = true
                emailField.isEnabled = true
                passwordField.text.clear()
                passwordField.hint = "Password:"
                cancel.visibility = View.VISIBLE
                errorField.visibility = View.VISIBLE
                errorField.setBackgroundColor(android.R.attr.editTextBackground)
                passwordField.visibility = View.VISIBLE
                title.visibility = View.VISIBLE
                loginButton.visibility = View.VISIBLE
                registerButton.visibility = View.VISIBLE

            }

            else Log.e("LOGIN", "EMAIL SUCCEEDED ON SEND")
        })
    }

//    public override fun onSaveInstanceState(savedInstanceState: Bundle?) {
//        if(savedInstanceState !is Bundle) return
//        Log.e("SAVEDINSTANCESTATE", "IN SAVED INSTANCE STATE BEFORE")
//        savedInstanceState.putString("loginState", fileName)
//        super.onSaveInstanceState(savedInstanceState)
//    }
}


enum class LoginState
{
    STARTED, SIGNING_IN, CREATING, VERIFYING, DONE
}