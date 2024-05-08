package com.example.flashwashapp

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener

class Client_booked_appointment : AppCompatActivity() {
    private lateinit var bookingRecyclerView: RecyclerView
    private lateinit var database: DatabaseReference
    private lateinit var adapter: AppointmentClientAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private var clientId : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_client_booked_appointment)

        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        bookingRecyclerView = findViewById(R.id.bookingRv)

        database = FirebaseDatabase.getInstance().reference.child("appointments")

        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
        clientId = sharedPreferences.getString("username", "").toString()

        adapter = AppointmentClientAdapter(emptyList(),database)
        bookingRecyclerView.adapter = adapter

        val layoutManager = LinearLayoutManager(this)
        bookingRecyclerView.layoutManager = layoutManager

        fetchAppointmentsForWasher(clientId ?: "")
    }

    private fun fetchAppointmentsForWasher(clientId: String) {
        val query: Query = database.orderByChild("clientId").equalTo(clientId)
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
                    adapter.appointments = appointmentsList
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}