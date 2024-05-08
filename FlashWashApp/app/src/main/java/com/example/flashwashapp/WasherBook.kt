package com.example.flashwashapp
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.database.FirebaseDatabase
import java.util.Calendar
import java.util.UUID

class WasherBook : AppCompatActivity() {
    private lateinit var profilePicImageView: ImageView
    private lateinit var fullNameTextView: TextView
    private lateinit var specializationTextView: TextView
    private lateinit var yearsOfExperienceTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneTextView: TextView
    private lateinit var bioTextView: TextView
    private lateinit var bookAppointmentButton: Button
    private lateinit var getDirectionsButton: Button
    private lateinit var sharedPreferences: SharedPreferences
    private val database = FirebaseDatabase.getInstance().reference

    private var selectedDate: String =""

    private var userId:String=""
    private var washerId : String =""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_washer_book)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Initialize views

        profilePicImageView = findViewById(R.id.profilePicImageView)
        fullNameTextView = findViewById(R.id.fullNameTextView)
        specializationTextView = findViewById(R.id.specializationTextView)
        yearsOfExperienceTextView = findViewById(R.id.yearsOfExperienceTextView)
        emailTextView = findViewById(R.id.emailTextView)
        phoneTextView = findViewById(R.id.phoneTextView)

        bioTextView = findViewById(R.id.bioTextView)
        bookAppointmentButton = findViewById(R.id.bookAppointmentButton)
        getDirectionsButton = findViewById(R.id.getDirectionsButton)

        Glide.with(this)
            .load(intent.getStringExtra("image"))
            .placeholder(R.drawable.user) // Placeholder image
            .error(R.drawable.user) // Error image
            .into(profilePicImageView)

        fullNameTextView.text = intent.getStringExtra("name")
        specializationTextView.text = intent.getStringExtra("specialization")
        yearsOfExperienceTextView.text = "Years of Experience: ${intent.getStringExtra("experience")}"
        emailTextView.text = intent.getStringExtra("email")
        phoneTextView.text ="+91-" + intent.getStringExtra("phone")
        bioTextView.text = intent.getStringExtra("about")

        bookAppointmentButton.setOnClickListener {
            // Implement booking appointment logic
            sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
            userId = sharedPreferences.getString("username", "").toString()
            washerId = intent.getStringExtra("email").toString()
            showDatePicker()
        }

        getDirectionsButton.setOnClickListener {
            // Implement getting directions logic
            openGoogleMaps(intent.getStringExtra("location").toString())
        }

        // Add onClick listeners
        emailTextView.setOnClickListener {
            // Open default mail app
            val emailIntent = Intent(Intent.ACTION_SENDTO)
            emailIntent.data = Uri.parse("mailto:${intent.getStringExtra("email")}")
            startActivity(emailIntent)
        }

        phoneTextView.setOnClickListener {
            // Open default calling app
            val phoneIntent = Intent(Intent.ACTION_DIAL)
            phoneIntent.data = Uri.parse("tel:${intent.getStringExtra("phone")}")
            startActivity(phoneIntent)
        }
    }

    private fun openGoogleMaps(location: String) {
        val gmmIntentUri = Uri.parse("geo:0,0?q=$location")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    private fun bookAppointment(userId:String,washerId:String, dateTime: String){
        // Generate a unique appointment ID
        val appointmentId = UUID.randomUUID().toString()

        // Create an instance of AppointmentModel with the appointment details
        val appointment = AppointmentModel(
            appointmentId,
            washerId,
            userId,
            dateTime,
            false // Set isConfirmed to false initially
        )

        // Push the appointment data to the database
        database.child("appointments").child(appointmentId).setValue(appointment)
            .addOnSuccessListener {
                Toast.makeText(this, "Appointment booked for: $dateTime", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to book appointment. Please try again.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showDatePicker() {
        // Get current date
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                bookAppointment(userId, washerId, selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }
}
