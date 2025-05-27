package upc.binome8.petapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Data Class pour représenter les activités par défaut des animaux
@Entity
data class ActivityDefault(
    @PrimaryKey (autoGenerate = true) val idActivityDefault : Int = 0,
    val espece : String,
    val description : String
)