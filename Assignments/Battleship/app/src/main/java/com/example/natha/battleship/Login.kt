package com.example.natha.battleship

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
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
    lateinit var loginButtons : LinearLayout
    lateinit var cancel : TextView
    lateinit var title : TextView
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    var handler = Handler()
    lateinit var loginState : LoginState



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
        loginButtons = findViewById(R.id.loginButtons)

        loginButton.setOnClickListener(clickListener)
        registerButton.setOnClickListener(clickListener)
        cancel.setOnClickListener(clickListener)
        auth = FirebaseAuth.getInstance()
        loginState = LoginState.STARTED

        if(checkConnection()) {
            if (auth != null && auth.currentUser != null) {
                if (checkConnection()) {
                    Log.e("LOGIN", "Current user is logged in")
                    currentUser = auth.currentUser!!
                }
            }
            if (auth != null && auth.currentUser != null && currentUser.isEmailVerified) {
                Log.e("LOGIN", "Current user exists and has email verified ")
                FirebaseDatabase.getInstance().reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (!dataSnapshot.hasChild(currentUser.uid)) {
                            var UserRef = FirebaseDatabase.getInstance().getReference()
                            UserRef.child("Users").child(currentUser.uid).setValue("")
                        }
                    }

                    override fun onCancelled(p0: DatabaseError?) {

                    }
                })
                intent = Intent(applicationContext, Game::class.java)
                loginState = LoginState.DONE
                startActivity(intent)
                finish()
            }
        }

        else  errorField.text = "Check internet settings"


    }

    val clickListener = View.OnClickListener { view ->

        when(view.id)
        {
            R.id.login ->
            {
                if(checkConnection()) {
                    loginState = LoginState.SIGNING_IN
                    if (passwordField.text.contains(("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#:?!@\$%^&*-]).{8,}$").toRegex()) && !passwordField.text.isEmpty() && !emailField.text.isEmpty()) {
                        Log.e("LOGIN", "Attempting to sign in first")
                        auth.signInWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString()).addOnCompleteListener(onCompleteListener)
                    } else {
                        loginState = LoginState.STARTED
                        Log.e("LOGIN", "Fields are either empty or the password doesnt meet the criteria")
                        errorField.setBackgroundColor(Color.RED)
                        if (passwordField.text.isEmpty() || emailField.text.isEmpty())
                            errorField.setText("Password and Email fields must not be empty")
                        else errorField.setText("Password requires 1 or more capital letters, digits, and non-alphanumeric symbols and must be at least 8 characters")
                    }
                }

                else errorField.text = "Check internet settings"
            }

            R.id.register ->
            {
                if(checkConnection()) {
                    loginState = LoginState.CREATING
                    if (passwordField.text.contains(("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#:?!@\$%^&*-]).{8,}$").toRegex()) && !passwordField.text.isEmpty() && !emailField.text.isEmpty()) {
                        Log.e("LOGIN", "Attempting to create user")
                        auth.createUserWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString()).addOnCompleteListener(onCompleteListener)
                    } else {
                        loginState = LoginState.STARTED
                        Log.e("LOGIN", "Fields are either empty or the password doesnt meet the criteria")
                        errorField.setBackgroundColor(Color.RED)
                        if (passwordField.text.isEmpty() || emailField.text.isEmpty())
                            errorField.setText("Password and Email fields must not be empty")
                        else
                            errorField.setText("Password requires 1 or more capital letters, digits, and non-alphanumeric symbols and must be at least 8 characters")
                    }
                }
                else errorField.text = "Check internet settings"

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
                loginButtons.visibility = View.INVISIBLE
                errorField.setBackgroundColor(android.R.attr.editTextBackground)
                errorField.text = ""
                errorField.visibility = View.VISIBLE
                passwordField.visibility = View.VISIBLE
                title.visibility = View.VISIBLE
                handler.removeCallbacksAndMessages(null)
                handler = Handler()
                loginState = LoginState.STARTED
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
                    FirebaseDatabase.getInstance().reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener
                    {
                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                            if(!dataSnapshot.hasChild(currentUser.uid))
                            {
                                var UserRef = FirebaseDatabase.getInstance().getReference()
                                UserRef.child("Users").child(currentUser.uid).setValue("")
                            }
                        }

                        override fun onCancelled(p0: DatabaseError?) {

                        }
                    })
                    intent = Intent(applicationContext, Game::class.java)
                    loginState = LoginState.DONE
                    startActivity(intent)
                    finish()
                } else {
                    emailField.setText("Email sent out, please verify your email.")
                    emailField.isFocusableInTouchMode = false
                    emailField.gravity = Gravity.CENTER
                    loginState = LoginState.VERIFYING
                    emailField.isFocusable = false
                    emailField.isClickable = false
                    emailField.isEnabled = false;
                    cancel.visibility = View.VISIBLE
                    errorField.visibility = View.INVISIBLE
                    loginButtons.visibility = View.VISIBLE
                    passwordField.visibility = View.INVISIBLE
                    title.visibility = View.INVISIBLE
                    sendVerificationEmail()
                    var timer = 0
                    for(i in 1..301) {
                        handler.postDelayed(Runnable {

                            if(timer % 5 == 0) currentUser.reload()

                            if(cancel.visibility == View.INVISIBLE)
                            {
                                handler.removeCallbacksAndMessages(null)
                                handler = Handler()
                            }

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
                                loginButtons.visibility = View.VISIBLE
                                handler.removeCallbacksAndMessages(null)
                                handler = Handler()
                                loginState = LoginState.STARTED

                                auth.signOut()
                            } else {
                                Log.e("LOGIN", "Timer is at: " + timer + " in sign-in")
                                if (currentUser.isEmailVerified) {
                                    Log.e("LOGIN", "Email is verified, take to main screen")
                                    intent = Intent(applicationContext, Game::class.java)
                                    handler.removeCallbacksAndMessages(null)
                                    handler = Handler()
                                    loginState = LoginState.DONE

                                    FirebaseDatabase.getInstance().reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener
                                    {
                                        override fun onDataChange(dataSnapshot: DataSnapshot) {
                                            if(!dataSnapshot.hasChild(currentUser.uid))
                                            {
                                                var UserRef = FirebaseDatabase.getInstance().getReference()
                                                UserRef.child("Users").child(currentUser.uid).setValue("")
                                            }
                                        }

                                        override fun onCancelled(p0: DatabaseError?) {

                                        }
                                    })

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
                loginButtons.visibility = View.INVISIBLE
                passwordField.visibility = View.INVISIBLE
                title.visibility = View.INVISIBLE
                loginState = LoginState.VERIFYING
                sendVerificationEmail()
                var timer = 0
                for(i in 1..301) {
                    handler.postDelayed(Runnable {

                        if(timer % 5 == 0) currentUser.reload()

                        if(cancel.visibility == View.INVISIBLE)
                        {
                            handler.removeCallbacksAndMessages(null)
                            handler = Handler()
                        }

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
                            loginButtons.visibility = View.VISIBLE
                            handler.removeCallbacksAndMessages(null)
                            handler = Handler()
                            loginState = LoginState.STARTED

                            auth.signOut()
                        }
                        else {
                            Log.e("LOGIN", "Timer is at: " + timer + " in creation")
                            if (currentUser.isEmailVerified) {
                                Log.e("LOGIN", "Email is verified, take to main screen")

                                FirebaseDatabase.getInstance().reference.child("Users").addListenerForSingleValueEvent(object : ValueEventListener
                                {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                                        if(!dataSnapshot.hasChild(currentUser.uid))
                                        {
                                            var UserRef = FirebaseDatabase.getInstance().getReference()
                                            UserRef.child("Users").child(currentUser.uid).setValue("")
                                        }
                                    }

                                    override fun onCancelled(p0: DatabaseError?) {

                                    }
                                })

                                loginState = LoginState.DONE
                                intent = Intent(applicationContext, Game::class.java)
                                handler.removeCallbacksAndMessages(null)
                                handler = Handler()
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
            else if(loginState == LoginState.CREATING)  errorField.text = errorField.text.toString() + ", user may already exist or check internet settings."
            loginState = LoginState.STARTED
        }
    }

    private fun sendVerificationEmail() {
        Log.e("LOGIN", "Sending verification email")
        loginState = LoginState.VERIFYING
        if (currentUser != null) currentUser.sendEmailVerification().addOnCompleteListener(OnCompleteListener() { task ->

            if (!task.isSuccessful && cancel.visibility == View.INVISIBLE) {
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
                loginButtons.visibility = View.INVISIBLE
                loginState = LoginState.STARTED
            } else Log.e("LOGIN", "EMAIL SUCCEEDED ON SEND")
        })
    }
    override fun onBackPressed() {
        if(loginState == LoginState.VERIFYING)
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
            loginButtons.visibility = View.VISIBLE
            handler.removeCallbacksAndMessages(null)
            handler = Handler()
            loginState = LoginState.STARTED
        }
        else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler = Handler()
    }

    fun checkConnection() : Boolean
    {
        var cm = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        var activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null) { // connected to the internet
            if (activeNetwork.getType() != ConnectivityManager.TYPE_WIFI || activeNetwork.getType() != ConnectivityManager.TYPE_MOBILE)
                return true
        }
        return true
    }
}

enum class LoginState
{
    STARTED, SIGNING_IN, CREATING, VERIFYING, DONE
}