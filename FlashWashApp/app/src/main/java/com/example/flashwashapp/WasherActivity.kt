package com.example.flashwashapp

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DecimalFormat

class WasherActivity : AppCompatActivity() {
    private lateinit var userId: String
    private lateinit var nameTextView: TextView
    private lateinit var specializationValueTextView: TextView
    private lateinit var licenseNumberValueTextView: TextView
    private lateinit var aboutValueTextView: TextView
    private lateinit var pricingValueTextView: TextView
    private lateinit var ratingValueTextView: TextView
    private lateinit var locationValueTextView: TextView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var profile : CircleImageView
    private lateinit var experienceValueTextView :TextView

    private lateinit var update :Button
    private var imgLink :String = ""
    private lateinit var sharedPreferences: SharedPreferences
    val decimalFormat = DecimalFormat("#.#")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_washer)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)

        // Initialize TextViews
        nameTextView = findViewById(R.id.nameValueTextView)
        specializationValueTextView = findViewById(R.id.specializationValueTextView)
        licenseNumberValueTextView = findViewById(R.id.licenseNumberValueTextView)
        aboutValueTextView = findViewById(R.id.aboutValueTextView)
        pricingValueTextView = findViewById(R.id.pricingValueTextView)
        ratingValueTextView = findViewById(R.id.ratingValueTextView)
        locationValueTextView = findViewById(R.id.locationValueTextView)
        profile = findViewById(R.id.profile)
        update = findViewById(R.id.updateButton)
        experienceValueTextView = findViewById(R.id.experienceValueTextView)

        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)

        userId = sharedPreferences.getString("username", "").toString()

        val sanitizedEm = sanitizeUserId(userId)
        Log.e("Sanitised Mail --->", "${sanitizedEm}")

        databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(sanitizedEm)
        fetchDataFromFirebase()

        update.setOnClickListener{
            var intent = Intent(this,updateDetails::class.java)
            intent.putExtra("specialization",specializationValueTextView.text.toString())
            intent.putExtra("name",nameTextView.text.toString())
            intent.putExtra("pricing",licenseNumberValueTextView.text.toString())
            intent.putExtra("about",aboutValueTextView.text.toString())
            intent.putExtra("rating",pricingValueTextView.text.toString())
            intent.putExtra("location",locationValueTextView.text.toString())
            intent.putExtra("sanitizedEm",sanitizedEm.toString())
            intent.putExtra("image",imgLink)
            intent.putExtra("experience",experienceValueTextView.text.toString())
            startActivity(intent)
        }
    }

    private fun fetchDataFromFirebase() {
        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val user = dataSnapshot.getValue(Washer::class.java)
                user?.let {
                    // Display user details in the welcome activity

                    nameTextView.text =  it.name
                    specializationValueTextView.text = it.specialization
                    licenseNumberValueTextView.text = it.licenseNumber
                    aboutValueTextView.text = it.about
                    pricingValueTextView.text = it.pricing.toString()
                    ratingValueTextView.text = decimalFormat.format(it.rating)
                    experienceValueTextView.text = it.experience

                    val parts = it.location.split(",")

                    // Find locality and admin information
                    var locality = ""
                    var admin = ""
                    for (part in parts) {
                        if (part.trim().startsWith("locality")) {
                            locality = part.split("=")[1]
                        } else if (part.trim().startsWith("admin")) {
                            admin = part.split("=")[1]
                        }
                    }

                    locationValueTextView.text = "$locality, $admin"


                    if(it.image != null){
                        imgLink = it.image.toString()
                        Glide.with(applicationContext).load(it.image)
                            .apply(
                                RequestOptions()
                                    .error(R.drawable.user)
                                    .placeholder(R.drawable.user)
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                            ).into(profile)
                    }
                    updateUI(user)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("Firebase", "Error fetching data from Firebase: ${databaseError.message}")
                // Handle error (e.g., display a message to the user)
            }
        })
    }
    private fun sanitizeUserId(userId: String): String {
        return userId.replace(".", "_")
            .replace("#", "_")
            .replace("$", "_")
            .replace("[", "_")
            .replace("]", "_").toString().toLowerCase()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.washer_menu_list, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {

                return true
            }
            R.id.action_appointment -> {
                val intent = Intent(this,ViewAppointments::class.java)
                startActivity(intent)
                return true
            }
            R.id.action_logout -> {
                val intent = Intent(this,Login::class.java)
                startActivity(intent)
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        Log.d("On Resume -------> ", "onResume: ")
        fetchDataFromFirebase()
        super.onResume()
    }
    private fun updateUI(user: Washer) {
        // Update UI elements with data from Firebase
        nameTextView.text =  user.name
        specializationValueTextView.text = user.specialization
        licenseNumberValueTextView.text = user.licenseNumber

        aboutValueTextView.text = user.about
        pricingValueTextView.text = user.pricing.toString()
        ratingValueTextView.text = user.rating.toString()
        locationValueTextView.text = user.location

        if (user.image != null) {
            // Load image using Glide or any other image loading library
            Glide.with(applicationContext).load(user.image)
                .apply(
                    RequestOptions()
                        .error(R.drawable.user)
                        .placeholder(R.drawable.user)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                ).into(profile)
        }
    }
}