package upc.binome8.petapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Pet::class,Activity::class,ActivityDefault::class], version = 16, exportSchema = false)
abstract class PetsDB : RoomDatabase() {
    abstract fun petsDAO(): PetsDAO
    abstract  fun activityDAO(): ActivityDAO
    abstract  fun ParamActivityDAO() : ParamActivityDAO
    companion object {
        @Volatile
        private var instance: PetsDB? = null
        fun getDatabase(context: Context): PetsDB {
            if (instance != null) return instance!!
            val db = Room.databaseBuilder(context.applicationContext, PetsDB::class.java, "pets.db")
                .fallbackToDestructiveMigration().build()
            instance = db
            return db
        }

    }
}