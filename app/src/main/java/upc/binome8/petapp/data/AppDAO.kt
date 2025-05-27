package upc.binome8.petapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PetsDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pet: Pet): Long

    @Query("SELECT * FROM pet")
    fun getAll(): Flow<List<Pet>>

    @Query("DELETE FROM pet")
    suspend fun clearTable()

    @Delete
    suspend fun deletePets(pet: List<Pet>)

    @Delete
    suspend fun delete(pet: Pet)

}

@Dao
interface ActivityDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun  insert(activity : Activity): Long

    @Query("UPDATE Activity SET description = :newDescription, notif = :newNotif, recurrence = :newRecurrence, horaire = :newHoraire WHERE idPet =:id AND description = :oldDescription")
    suspend fun  updateActivity(
        oldDescription: String,
        newDescription: String,
        newNotif: Boolean,
        id: Int,
        newRecurrence: String,
        newHoraire: String
    )

    @Query("Select * From activity Where idPet=:id")
    fun getActivity(id :Int) : Flow<List<Activity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertActivities(author: List<Activity>)

    @Delete
    suspend fun delete(activity: Activity)

}

@Dao
interface ParamActivityDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(param : ActivityDefault)

    @Query("SELECT * FROM ActivityDefault")
    fun getAll(): Flow<List<ActivityDefault>>

    @Query("UPDATE ActivityDefault SET description = :newDescription WHERE espece = :espece AND idActivityDefault = :idActivityDefault")
    suspend fun  updateActivity(espece: String, newDescription: String, idActivityDefault: Int)

    @Query("SELECT * FROM ActivityDefault WHERE espece = :espece")
    fun getActivityDefault(espece : String): Flow<List<ActivityDefault>>

    @Delete
    suspend fun delete(activity: ActivityDefault)

}