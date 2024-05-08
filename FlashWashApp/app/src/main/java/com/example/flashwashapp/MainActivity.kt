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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    lateinit var userName: TextView
    private lateinit var rv: RecyclerView
    private lateinit var databaseReference: DatabaseReference
    private lateinit var adapter: WasherAdapter
    private val mainList = ArrayList<Washer>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var allButton: Button
    private lateinit var fourWheelerButton: Button
    private lateinit var twoWheelerButton: Button
    private lateinit var dryCleaningButton: Button
    private lateinit var interiorWashButton: Button
    private lateinit var truckWashButton: Button
    private lateinit var partsRepairButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.setBackgroundDrawable(
            ColorDrawable(
                ContextCompat.getColor(
                    this,
                    R.color.primary
                )
            )
        )
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)


        allButton = findViewById(R.id.all)
        fourWheelerButton = findViewById(R.id.fourWheel)
        twoWheelerButton = findViewById(R.id.twoWheel)
        dryCleaningButton = findViewById(R.id.dryClean)
        interiorWashButton = findViewById(R.id.interiorWash)
        truckWashButton = findViewById(R.id.truckWash)
        partsRepairButton = findViewById(R.id.partsRepair)

        allButton.setOnClickListener { filterBySpecialization("") } // Empty string for all
        fourWheelerButton.setOnClickListener { filterBySpecialization("Four Wheeler") }
        twoWheelerButton.setOnClickListener { filterBySpecialization("Two Wheeler") }
        dryCleaningButton.setOnClickListener { filterBySpecialization("Dry Cleaning") }
        interiorWashButton.setOnClickListener { filterBySpecialization("Interior") }
        truckWashButton.setOnClickListener { filterBySpecialization("Truck Wash") }
        partsRepairButton.setOnClickListener { filterBySpecialization("Parts Repair") }

        sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE)
        userName = findViewById(R.id.userName)
        userName.setText("Welcome, " + sharedPreferences.getString("username", ""))

        rv = findViewById(R.id.washerRecyclerView)
        adapter = WasherAdapter(mainList)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        rv.layoutManager = layoutManager
        rv.itemAnimator = DefaultItemAnimator()
        rv.adapter = adapter // Set the previously initialized adapter here

        databaseReference = FirebaseDatabase.getInstance().getReference("Users")
        loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_profile -> {
                return true
            }
            R.id.action_appointment -> {
                val intent = Intent(this,Client_booked_appointment::class.java)
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
    private fun filterBySpecialization(specialization: String) {
        if (specialization.isEmpty()) {
            // If the filter is empty, load all data
            loadData()
        } else {
            // Filter the mainList based on the specialization
            val filteredList = mainList.filter { it.specialization == specialization }
            adapter.updateList(filteredList) // Update RecyclerView with filtered list
        }
    }

    private fun loadData() {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mainList.clear() // Clear the list to avoid duplicates

                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(Washer::class.java)
                    if (user?.role == "washer") {
                        mainList.add(user)
                    }
                }
                adapter.notifyDataSetChanged() // Notify the adapter of the data change
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle any errors here
                Log.e("Firebase", "Data retrieval failed: $error")
            }
        })
    }
}