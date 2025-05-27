package upc.binome8.petapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Data Class de la représentation des animaux avec un nom et une espèce ( enum )
@Entity
data class Pet(
    @PrimaryKey(autoGenerate = true) val idPet: Int = 0,
    val nom : String,
    val espece: String,
)