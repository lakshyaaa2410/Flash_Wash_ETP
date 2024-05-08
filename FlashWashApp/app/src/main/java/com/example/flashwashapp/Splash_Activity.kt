package com.example.flashwashapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class Splash_Activity : AppCompatActivity() {

    private lateinit var imageViewSplash: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        imageViewSplash = findViewById(R.id.imageViewSplash)

        // Start animation
        imageViewSplash.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate))

        // Handler to delay activity transition
        Handler().postDelayed({
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }, 2500) // 5 seconds delay
    }
}
