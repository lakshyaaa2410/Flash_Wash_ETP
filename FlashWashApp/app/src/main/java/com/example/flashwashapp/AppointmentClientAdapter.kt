package com.example.flashwashapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AppointmentClientAdapter(
    var appointments: List<AppointmentModel>,
    private val database: DatabaseReference
) : RecyclerView.Adapter<AppointmentClientAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val washerNameTextView: TextView = itemView.findViewById(R.id.washerNameTextView)
        val washerContactTextView: TextView = itemView.findViewById(R.id.washerContactTextView)
        val cancelAppointmentButton: Button = itemView.findViewById(R.id.cancelAppointmentButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.client_booking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        // Set appointment details
        if(appointment.isConfirmed == true){
            holder.statusTextView.text = "Status: Confirmed"
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_green_dark))
        }else{
            holder.statusTextView.text = "Status: Pending"
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.holo_blue_light))
        }
        holder.dateTextView.text = "Date: ${appointment.date}"

        fetchwasherDetails(holder, appointment.washerId ?: "")

        // Handle cancel appointment button click
        holder.cancelAppointmentButton.setOnClickListener {
            // Implement cancel appointment logic here
        }
    }

    private fun fetchwasherDetails(holder: ViewHolder, washerId: String) {

        val sanitizedEm = washerId.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
        val washerRef = FirebaseDatabase.getInstance().reference.child("Users").child(sanitizedEm)

        washerRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val washerName = dataSnapshot.child("name").value.toString()
                    val washerContact = dataSnapshot.child("phone").value.toString()
                    holder.washerNameTextView.text = "Washer Name: $washerName"
                    holder.washerContactTextView.text = "Washer Contact: +91-$washerContact"

                } else {
                    // Handle case where washer data does not exist
                    holder.washerNameTextView.text = "Washer Name: Not found"
                    holder.washerContactTextView.text = "Washer Contact: Not found"

                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
                // For simplicity, showing a Toast message here. You can replace it with a more robust error handling mechanism.
                holder.washerNameTextView.text = "Washer Name: Error"
                holder.washerContactTextView.text = "Washer Contact: Error"
            }
        })
    }

    override fun getItemCount(): Int {
        return appointments.size
    }
}
