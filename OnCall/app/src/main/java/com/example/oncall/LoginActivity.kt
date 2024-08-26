package com.example.oncall

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent

class LoginActivity : AppCompatActivity() {
    // declare variables
    private lateinit var username: EditText
    private lateinit var  password: EditText
    private lateinit var  login: Button
    private lateinit var  signUp: TextView


    // Creating firebaseAuth object
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // initialize variables
        username = findViewById<EditText>(R.id.loginEmail)
        password = findViewById<EditText>(R.id.passwordLogin)
        login = findViewById<Button>(R.id.loginBtn)
        signUp = findViewById<TextView>(R.id.loginRedirect)



        // initialising Firebase auth object
        auth = FirebaseAuth.getInstance()

        // Navigate to home page after login
        login.setOnClickListener {
            login()
        }

        // Navigate to register page
        signUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Check login details
    private fun login() {
        // Get user input
        val user = username.text.toString()
        val passWrd = password.text.toString()

        // Calling signInWithEmailAndPassword(email, pass) function using Firebase auth object

        auth.signInWithEmailAndPassword(user, passWrd).addOnCompleteListener(this) {
            if (it.isSuccessful) {

                Toast.makeText(this, "Successfully Logged In", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()

            } else{
                Toast.makeText(this, "Log In failed ", Toast.LENGTH_SHORT).show()
                username.text.clear()
                password.text.clear()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }

        }
    }
}