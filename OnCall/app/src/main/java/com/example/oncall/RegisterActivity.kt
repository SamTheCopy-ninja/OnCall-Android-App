package com.example.oncall

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.widget.Toast
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    // declare variables
    private lateinit var username: EditText
    private lateinit var  password: EditText
    private lateinit var  register: Button
    private lateinit var  signIn: TextView

    // create Firebase authentication object
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // initialize variables
        username = findViewById<EditText>(R.id.regEmail)
        password = findViewById<EditText>(R.id.regPassword)
        register = findViewById<Button>(R.id.regBtn)
        signIn = findViewById<TextView>(R.id.loginRedirect)

        // Initialising auth object
        auth = Firebase.auth

        // Clicking on register button
        register.setOnClickListener{
            signUpUser()
        }

        // Redirect to login
        signIn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Function to register a user via Firebase
    private fun signUpUser(){
        // Get user input
        val user = username.text.toString()
        val passWrd = password.text.toString()

        // If statement to check if user entered details
        if (user.isBlank() || passWrd.isBlank()){
            Toast.makeText(this, "Username and password can not be blank", Toast.LENGTH_SHORT).show()
            return
        }

        // If all credential are correct call createUserWithEmailAndPassword
        // using auth object and pass the email and pass in it.
        auth.createUserWithEmailAndPassword(user, passWrd).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, "Successfully Singed Up", Toast.LENGTH_SHORT).show()
                var intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Singed Up Failed!", Toast.LENGTH_SHORT).show()
            }
        }

    }
}