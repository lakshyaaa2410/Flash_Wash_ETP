package com.example.flashwashapp

import com.google.firebase.database.Exclude
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class AppointmentModel(
    var appointmentId: String? = null,
    var washerId: String? = null,
    var clientId: String? = null,
    var date: String? = null,
    var isConfirmed: Boolean? = null
) {
    @Exclude
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "appointmentId" to appointmentId,
            "washerId" to washerId,
            "clientId" to clientId,
            "date" to date,
            "isConfirmed" to isConfirmed
        )
    }
}