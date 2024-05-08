package com.example.flashwashapp

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.locatemyadvocate.R
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.random.Random

class SignUp : AppCompatActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var nameEditText: EditText
    private lateinit var loginText: TextView
    private lateinit var roleSwitch :Switch
    private lateinit var emailEditText: EditText
    private lateinit var phoneEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var registerButton: Button
    private lateinit var db: FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    lateinit var circleImage: CircleImageView
    lateinit var linkImg: String
    lateinit var role :String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.primary)))
        window?.statusBarColor = ContextCompat.getColor(this, R.color.primary)


        nameEditText = findViewById(R.id.editTextName)
        emailEditText = findViewById(R.id.editTextEmail)
        phoneEditText = findViewById(R.id.editTextPhone)
        passwordEditText = findViewById(R.id.editTextPassword)
        confirmPasswordEditText = findViewById(R.id.editTextConfirmPassword)
        registerButton = findViewById(R.id.buttonRegister)
        firebaseAuth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        circleImage = findViewById(R.id.profile)
        linkImg=""
        role="client"
        loginText = findViewById(R.id.loginText)
        roleSwitch = findViewById(R.id.roleSwitch)


        circleImage.setOnClickListener{
            intent = Intent()
            intent.setType("image/*")
            intent.setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent,"Choose the image"),1)

        }

        roleSwitch.setOnCheckedChangeListener { _, isChecked ->
            role = if (isChecked) "washer" else "client"
        }

        registerButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = confirmPasswordEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && name.isNotEmpty() &&
                confirmPassword.isNotEmpty() && linkImg.isNotEmpty() && phone.isNotEmpty()
            ) {
                if (phone.length < 10) {
                    Toast.makeText(this, "Phone number must be at least 10 digits", Toast.LENGTH_SHORT).show()
                } else {
                    firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                        if (it.isSuccessful) {
                            Toast.makeText(this, "User created", Toast.LENGTH_SHORT).show()
                            realtimeDatabase(name, email, password, phone, role)
                            var i: Intent = Intent(this, Login::class.java)
                            startActivity(i)
                        } else {
                            try {
                                throw it.getException()!!
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "Invalid Credentials Check your email", Toast.LENGTH_SHORT).show()
                            } catch (e: FirebaseAuthWeakPasswordException) {
                                Toast.makeText(this, "Weak Paasword", Toast.LENGTH_SHORT).show()
                            } catch (e: FirebaseAuthUserCollisionException) {
                                Toast.makeText(this, "Some error! Please try again" + e.toString(), Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(
                                    this,
                                    it.exception.toString(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Empty Fields are not allowed", Toast.LENGTH_SHORT).show()
            }
        }


        loginText.setOnClickListener{
            val intent :Intent = Intent(this,Login::class.java)
            startActivity(intent)
        }
    }
    fun realtimeDatabase(nam: String, em: String, pass: String,phone:String,role:String) {
        Toast.makeText(this, linkImg, Toast.LENGTH_LONG).show()
        val sanitizedEm = em.replace(".", "_").replace("#", "_").replace("$", "_").replace("[", "_").replace("]", "_")
        val reference = FirebaseDatabase.getInstance().getReference("Users").child(sanitizedEm)
        // Check the role and create an instance of the corresponding model
        val users = if (role == "washer") {
            val specialization = "" // Add specialization as needed
            val pricing = "" // Add pricing as needed
            val about = ""
            val licenseNum = Random.nextInt(1, 101)
            val rating = Random.nextDouble(0.1, 5.0)
            val location =""
            val experience =""
            Washer(specialization, pricing, about ,licenseNum.toInt() ,rating.toFloat() ,location , experience ,nam, em, pass, linkImg, phone, role )
        } else {
            User(nam, em, pass, linkImg, phone ,role)
        }
        reference.setValue(users).addOnCompleteListener { task ->
            try {
                if (task.isSuccessful) {
                    Toast.makeText(this, "Your data saved", Toast.LENGTH_SHORT).show()
                } else {
                    throw task.exception ?: Exception("Unknown error")
                }
            } catch (e: Exception) {
                if (e is FirebaseException) {
                    // Handle Firebase-related exceptions
                    Log.e("Firebase Error -------->",e.message.toString())

                    Toast.makeText(this, "Firebase Error: ${e.message}", Toast.LENGTH_SHORT).show()
                } else {
                    // Handle other exceptions
                    Log.e("Error  ------>",e.message.toString())
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode==1 && data!=null && resultCode== RESULT_OK && data.getData()!=null){
            Glide.with(this).load(data.data).into(circleImage)
            onImageAdd(data.data)
        }
    }


    fun onImageAdd(imageUri: Uri?) {
        if (imageUri != null) {
            // Set the desired filename for the uploaded image
            storageReference =
                FirebaseStorage.getInstance().reference.child("Images/"+System.currentTimeMillis()+".jpg")

            storageReference.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot? ->
                    storageReference.downloadUrl
                        .addOnSuccessListener { downloadUrl: Uri ->
                            linkImg = downloadUrl.toString()
                        }
                        .addOnFailureListener { e: Exception? ->
                        }
                }
                .addOnFailureListener { e: Exception? ->
                    // Handle any errors if the image upload fails
                }
        }
    }
}