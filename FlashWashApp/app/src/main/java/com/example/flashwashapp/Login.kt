package com.example.flashwashapp
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class Login : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var password: EditText
    private lateinit var register: TextView
    private lateinit var loginButton: Button
    private lateinit var email: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        email = findViewById(R.id.emailId)
        password = findViewById(R.id.password)
        loginButton = findViewById(R.id.loginButton)
        register = findViewById(R.id.signupText)
        firebaseAuth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            // Check if the device is connected to a network
            if (!isNetworkConnected()) {
                // Prompt the user to turn on Wi-Fi
                showCustomToast("Please Turn On Wi-Fi To Proceed")
                return@setOnClickListener
            }

            val userEmail = email.text.toString()
            val pass = password.text.toString()

            if (userEmail.isNotEmpty() && pass.isNotEmpty()) {
                // Check if the email contains "@" symbol
                if (!userEmail.contains("@")) {
                    // Email is invalid, display a toast message
                    Toast.makeText(this, "Invalid email address.", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Fields are not empty and email is valid, proceed with sign-in
                firebaseAuth.signInWithEmailAndPassword(userEmail, pass)
                    .addOnCompleteListener { loginTask ->
                        try {
                            if (loginTask.isSuccessful) {
                                // Successfully logged in, fetch user data from the database
                                fetchUserDataFromDatabase(userEmail, pass)
                            } else {
                                throw loginTask.exception ?: Exception("Unknown error")
                            }
                        } catch (e: Exception) {
                            handleLoginError(e)
                        }
                    }
            } else {
                // One or more fields are empty, display a toast message
                Toast.makeText(this, "All fields are required.", Toast.LENGTH_SHORT).show()
            }
        }

        register.setOnClickListener {
            val intent: Intent = Intent(this, SignUp::class.java)
            startActivity(intent)
        }
        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
        email.setText(sharedPreferences.getString("username", ""))
        password.setText(sharedPreferences.getString("password", ""))
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun fetchUserDataFromDatabase(userEmail: String, pass: String) {
        val sanitizedEm = userEmail.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(sanitizedEm)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    if (user != null) {
                        val userName = user.name
                        val userImage = user.image
                        val userRole = user.role

                        val editor = sharedPreferences.edit()
                        editor.putString("username", userEmail)
                        editor.putString("password", pass)
                        editor.putString("userName", userName)
                        editor.putString("userImage", userImage)
                        editor.apply()

                        val targetActivity = when (userRole) {
                            "washer" -> WasherActivity::class.java
                            else -> MainActivity::class.java // Default interface for unknown roles
                        }

                        // Start the MainActivity and pass the user data
                        val intent = Intent(applicationContext , targetActivity)
                        intent.putExtra("USER_ID", userEmail)
                        intent.putExtra("userName", userName)
                        intent.putExtra("userImage", userImage)
                        startActivity(intent)
                    } else {
                        handleLoginError(Exception("Failed to deserialize user data"))
                    }
                } else {
                    handleLoginError(Exception("DataSnapshot does not exist"))
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                handleLoginError(Exception("Database Error: ${databaseError.message}"))
            }
        })
    }

    private fun handleLoginError(exception: Exception) {
        runOnUiThread {
            Toast.makeText(this, "Login Error: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showCustomToast(message: String) {
        val inflater = layoutInflater
        val layout: View = inflater.inflate(R.layout.custom_toast, findViewById(R.id.custom_toast_layout))

        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.CENTER_VERTICAL or Gravity.BOTTOM, 0, 100)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = layout
        toast.show()
    }
}
