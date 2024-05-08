package com.example.flashwashapp
class Washer : User {
    var specialization: String = ""
    var licenseNumber: String = ""
    var about: String = ""
    var pricing: Int = 0
    var rating: Float = 0.0f
    var location: String = ""
    var experience : String =""

    constructor(
        specialization: String,
        licenseNumber: String,
        about: String,
        pricing: Int,
        rating: Float,
        location: String,
        experience :String,
        name: String,
        email: String,
        pass: String,
        image: String,
        phone: String,
        role: String
    ) : super(name, email, pass, image, phone, role) {
        this.specialization = specialization
        this.licenseNumber = licenseNumber
        this.about = about
        this.pricing = pricing
        this.rating = rating
        this.location = location
        this.experience = experience
    }

    constructor() : super() {
        // No-argument constructor
    }
}
