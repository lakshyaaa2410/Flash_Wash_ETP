package com.example.flashwashapp

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.util.Locale

class updateDetails : AppCompatActivity() {
    private lateinit var nameEditText: TextView
    private lateinit var specializationEditText: EditText
    private lateinit var licenseNumberEditText: EditText
    private lateinit var aboutEditText: EditText
    private lateinit var casesWonEditText: EditText
    private lateinit var locationEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var profile : CircleImageView
    private lateinit var experienceEditText:EditText

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var name:String=""
    private var specialization:String=""
    private var licenseNumber:String=""
    private var about:String=""
    private var pricing:String=""
    private var location:String=""
    private var experience:String=""


    private var email:String=""

    private lateinit var dbRef: DatabaseReference
    lateinit var washer: Washer

    companion object {
        const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_details)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initializing views
        nameEditText = findViewById(R.id.nameEditText)
        specializationEditText = findViewById(R.id.specializationEditText)
        licenseNumberEditText = findViewById(R.id.licenseNumberEditText)
        aboutEditText = findViewById(R.id.aboutEditText)
        casesWonEditText = findViewById(R.id.casesWonEditText)
        locationEditText = findViewById(R.id.locationEditText)
        updateButton = findViewById(R.id.updateButton)
        profile = findViewById(R.id.profile)
        experienceEditText = findViewById(R.id.experienceEditText)

        // Assigning retrieved values to TextViews
        specializationEditText.setText(intent.getStringExtra("specialization"))
        nameEditText.setText(intent.getStringExtra("name"))
        licenseNumberEditText.setText(intent.getStringExtra("licenseNumber"))
        aboutEditText.setText(intent.getStringExtra("about"))
        casesWonEditText.setText(intent.getStringExtra("pricing"))
        locationEditText.setText(intent.getStringExtra("location"))
        email = intent.getStringExtra("sanitizedEm").toString()
        experienceEditText.setText(intent.getStringExtra("experience"))

        washer = Washer()

        // Fetch current location after views are initialized
        fetchCurrentLocation()

        Glide.with(applicationContext).load(intent.getStringExtra("image"))
            .apply(
                RequestOptions()
                    .error(R.drawable.user)
                    .placeholder(R.drawable.user)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            ).into(profile)

        // Initialize click listener for update button
        updateButton.setOnClickListener {
            specialization = specializationEditText.text.toString()
            licenseNumber = licenseNumberEditText.text.toString()
            about = aboutEditText.text.toString()
            pricing = casesWonEditText.text.toString()
            location = locationEditText.text.toString()
            experience = experienceEditText.text.toString()
            updateUserDetails(name, specialization, licenseNumber, about, pricing, location , experience)
        }
    }

    private fun updateUserDetails(name:String,  specialization:String ,licenseNumber:String,about:String,pricing:String , location: String ,experience:String){
        dbRef = FirebaseDatabase.getInstance().getReference("Users")

        val updates = HashMap<String, Any>()
        updates["specialization"] = specialization
        updates["licenseNumber"] = licenseNumber
        updates["about"] = about
        updates["pricing"] = pricing.toInt()
        updates["location"] = location
        updates["experience"] = experience

        dbRef.child(email).updateChildren(updates).addOnCompleteListener{
            Toast.makeText(this,"Updated Successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(this,WasherActivity::class.java)
            startActivity(intent)
        }.addOnFailureListener{ error ->
            Toast.makeText(this,"Deleting Error ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }

    private fun fetchCurrentLocation() {
        // Check location permissions
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
        } else {
            // Permission has already been granted
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        val geocoder = Geocoder(this, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(location.latitude, location.longitude, 1)!!
                        // Assign location coordinates to the locationEditText field
                        locationEditText.setText(list[0].getAddressLine(0).toString())
                    } else {
                        // Location is null
                        Toast.makeText(
                            this,
                            "Unable to retrieve current location",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle failure
                    Toast.makeText(
                        this,
                        "Failed to retrieve current location: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission granted, fetch the current location
                    fetchCurrentLocation()
                } else {
                    // Permission denied
                    Toast.makeText(
                        this,
                        "Permission denied. Unable to retrieve current location",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}
