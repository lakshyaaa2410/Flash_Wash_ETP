package com.example.flashwashapp

open class User {
    var name: String? = null
    var email: String? = null
    var pass: String? = null
    var image: String? = null
    var phone:String?=null
    var role: String?=null

    // Add a no-argument constructor
    constructor()

    constructor(name: String, email: String, pass: String, image: String , phone:String ,role:String) {
        this.name = name
        this.email = email
        this.pass = pass
        this.image = image
        this.phone = phone
        this.role = role
    }

}
