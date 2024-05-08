package com.example.flashwashapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ViewAppointments : AppCompatActivity() {

    private lateinit var appointmentsRecyclerView: RecyclerView
    private lateinit var appointmentAdapter: AppointmentAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var sharedPreferences: SharedPreferences


    private var washerId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_appointments)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        appointmentsRecyclerView = findViewById(R.id.appointmentsRecyclerView)
        appointmentsRecyclerView.layoutManager = LinearLayoutManager(this)
        appointmentAdapter = AppointmentAdapter(emptyList(), database)
        appointmentsRecyclerView.adapter = appointmentAdapter

        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
        washerId = sharedPreferences.getString("username", "").toString()

        fetchAppointmentsForwasher(washerId ?: "")
    }

    private fun fetchAppointmentsForwasher(washerId: String) {

        val query: Query = database.child("appointments").orderByChild("washerId").equalTo(washerId)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val appointmentsList = mutableListOf<AppointmentModel>()

                if (dataSnapshot.exists()) {
                    for (appointmentSnapshot in dataSnapshot.children) {
                        val appointment = appointmentSnapshot.getValue(AppointmentModel::class.java)
                        appointment?.let {
                            appointmentsList.add(it)
                        }
                    }
                    appointmentAdapter.appointments = appointmentsList
                    appointmentAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}
