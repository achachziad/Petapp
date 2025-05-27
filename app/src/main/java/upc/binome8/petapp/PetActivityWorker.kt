package upc.binome8.petapp

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import upc.binome8.petapp.NotificationUtils.createNotif

class PetActivityWorker(val context: Context, params: WorkerParameters):Worker(context, params) {
    override fun doWork(): Result {
        val petId = inputData.getInt("petId", -1)
        val petName = inputData.getString("petName") ?: "Pet"
        val petSpecie = inputData.getString("espece") ?: "Espece"
        val description = inputData.getString("description") ?: "Activité inconnue"

        Log.i("PetActivityWorker - Activity informations","$petId,$petName,$description")

        createNotif(context,petId,petName,petSpecie,description)
        return Result.success()

    }
}