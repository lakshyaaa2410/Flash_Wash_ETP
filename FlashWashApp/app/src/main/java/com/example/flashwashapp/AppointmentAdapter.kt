package com.example.flashwashapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DatabaseReference


class AppointmentAdapter(
    var appointments: List<AppointmentModel>,
    private val database: DatabaseReference
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appointmentIdTextView: TextView = itemView.findViewById(R.id.appointmentIdTextView)
        val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        val clientNameTextView: TextView = itemView.findViewById(R.id.clientNameTextView)
        val clientContactTextView: TextView = itemView.findViewById(R.id.clientContactTextView)
        val confirmButton: Button = itemView.findViewById(R.id.confirmButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.appointment_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appointment = appointments[position]
        holder.appointmentIdTextView.text = "#Id: ${appointment.appointmentId}"
        holder.dateTextView.text = "Date: ${appointment.date}"
        val clientId: String = appointment.clientId.toString()
        val sanitizedEm = clientId.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")

        // Fetch client details from the database
        database.child("Users").child(sanitizedEm ?: "").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val clientName = snapshot.child("name").value.toString()
                val clientContact = snapshot.child("phone").value.toString()
                holder.clientNameTextView.text = "Client Name: $clientName"
                holder.clientContactTextView.text = "Contact Info: $clientContact"
            }
        }.addOnFailureListener {
            // Handle database fetch failure
        }


        if (appointment.isConfirmed == true) {
            holder.confirmButton.text = "Booking Confirmed"
            holder.confirmButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
            holder.confirmButton.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white)) // Set text color to white
            holder.confirmButton.isEnabled = false // Disable the button
        } else {
            holder.confirmButton.text = "Confirm Appointment"
            holder.confirmButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.primary))
            holder.confirmButton.isEnabled = true // Enable the button
        }
        // Handle confirm button click
        holder.confirmButton.setOnClickListener {
            val appointmentId = appointment.appointmentId
            if (appointmentId != null) {
                confirmAppointment(holder.itemView.context,holder,appointmentId)
            }
        }
    }

    private fun confirmAppointment(context: Context, holder: ViewHolder, appointmentId: String) {
        // Update appointment status in the database to confirmed
        database.child("appointments").child(appointmentId).child("confirmed").setValue(true)
            .addOnSuccessListener {
                // Handle success
                holder.confirmButton.text = "Booking Confirmed"
                holder.confirmButton.setBackgroundColor(ContextCompat.getColor(holder.itemView.context, R.color.gray))
                holder.confirmButton.setTextColor(ContextCompat.getColor(holder.itemView.context, android.R.color.white)) // Set text color to white
                holder.confirmButton.isEnabled = false // Disable the button
                Toast.makeText(context, "Appointment confirmed", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                // Handle failure
                Toast.makeText(context, "Appointment confirmation failed", Toast.LENGTH_SHORT).show()
            }
    }


    override fun getItemCount(): Int {
        return appointments.size
    }
}
