package com.example.flashwashapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class WasherAdapter(private var washerList: List<Washer>) : RecyclerView.Adapter<WasherAdapter.WasherViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WasherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.washer_items, parent, false)
        return WasherViewHolder(view)
    }

    fun updateList(newList: List<Washer>) {
        washerList = newList
        notifyDataSetChanged()
    }
    override fun onBindViewHolder(holder: WasherViewHolder, position: Int) {
        val currentWasher = washerList[position]

        holder.nameTextView.text = currentWasher.name
        holder.specializationTextView.text = currentWasher.specialization
        holder.pricingTextView.text = "Pricing: Rs. ${currentWasher.pricing}"
        holder.locationTextView.text = currentWasher.location
        holder.ratingsRatingBar.rating = currentWasher.rating
        holder.editTextPhone.text = currentWasher.phone

        // Load profile picture using Glide
        Glide.with(holder.itemView.context)
            .load(currentWasher.image)
            .placeholder(R.drawable.user) // Placeholder image
            .error(R.drawable.user) // Error image
            .into(holder.profilePicImageView)
        // Set click listener for the card
        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, WasherBook::class.java)
            intent.putExtra("specialization",currentWasher.specialization)
            intent.putExtra("name",currentWasher.name)
            intent.putExtra("licenseNumber",currentWasher.licenseNumber)
            intent.putExtra("about",currentWasher.about)
            intent.putExtra("pricing",currentWasher.pricing)
            intent.putExtra("location",currentWasher.location)
            intent.putExtra("email",currentWasher.email)
            intent.putExtra("phone",currentWasher.phone)
            intent.putExtra("image",currentWasher.image)
            intent.putExtra("experience",currentWasher.experience)
            intent.putExtra("rating",currentWasher.rating)
            context.startActivity(intent)
        }
    }


    override fun getItemCount() = washerList.size

    inner class WasherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profilePicImageView: ImageView = itemView.findViewById(R.id.profilePicImageView)
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val ratingsRatingBar: RatingBar = itemView.findViewById(R.id.ratingsRatingBar)
        val specializationTextView: TextView = itemView.findViewById(R.id.specializationTextView)
        val pricingTextView: TextView = itemView.findViewById(R.id.pricingTextView)
        val locationTextView: TextView = itemView.findViewById(R.id.locationTextView)
        val editTextPhone:TextView = itemView.findViewById(R.id.editTextPhone)
    }
}
