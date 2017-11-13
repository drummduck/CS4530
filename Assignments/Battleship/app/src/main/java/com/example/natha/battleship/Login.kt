package com.example.natha.battleship

import android.content.ContentValues.TAG
import android.graphics.Color
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
    lateinit var forgotPassword : TextView
    lateinit var auth : FirebaseAuth
    lateinit var currentUser : FirebaseUser
    lateinit var task : Task<AuthResult>
    var triedSignIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        emailField = findViewById(R.id.email)
        passwordField = findViewById(R.id.password)
        errorField = findViewById(R.id.errorMessage)
        loginButton = findViewById(R.id.loginRegister)
        forgotPassword = findViewById(R.id.forgot)

        loginButton.setOnClickListener(clickListener)
        forgotPassword.setOnClickListener(clickListener)
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser != null)
        {
            currentUser = auth.currentUser!!
            //start activity, give user info, this is where it begins to suck
        }
    }

    val clickListener = View.OnClickListener { view ->

        when(view.id)
        {
            R.id.loginRegister ->
            {
                if(passwordField.text.contains(("^(?=.*?[A-Z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}$").toRegex()) && !passwordField.text.isEmpty() && !loginRegister.text.isEmpty()) {
                    auth.signInWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString()).addOnCompleteListener(onCompleteListener)
                    if (triedSignIn) {
                        auth.createUserWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString()).addOnCompleteListener(onCompleteListener)
                        triedSignIn = false
                    }
                }
                else
                {
                    errorField.setBackgroundColor(Color.RED)
                    if(passwordField.text.isEmpty() || loginRegister.text.isEmpty())
                        errorField.setText("Password and Email fields must not be empty")
                    else
                        errorField.setText("Password requires 1 or more capital letters, digits, and non-alphanumeric symbols and must be at least 8 characters")
                }
            }

            R.id.forgot ->
            {
                auth.signOut()
            }
        }
    }

    val onCompleteListener = OnCompleteListener<AuthResult>{ task ->

        if(task.isSuccessful)
        {
            if(triedSignIn){}//Do verification?
            //Go to game list
        }

        else
        {
            if(triedSignIn)Toast.makeText(this, "Authentication failed.",
                    Toast.LENGTH_SHORT).show()
            triedSignIn = true
        }
    }
}


