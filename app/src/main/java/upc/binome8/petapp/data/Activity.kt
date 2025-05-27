package upc.binome8.petapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Data Class pour représenter les activités ajoutables aux animaux
@Entity
data class Activity(
    @PrimaryKey (autoGenerate = true) val idActivity: Int = 0,
    val idPet : Int,
    val description: String,
    val notif : Boolean,
    val recurrence : String,
    val horaire : String
    )