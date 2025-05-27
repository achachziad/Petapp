    package upc.binome8.petapp

    import android.app.NotificationChannel
    import android.app.NotificationManager
    import android.app.PendingIntent
    import android.content.Context
    import android.content.Context.NOTIFICATION_SERVICE
    import android.content.Intent
    import android.util.Log
    import androidx.core.app.NotificationCompat
    import androidx.work.Data
    import androidx.work.ExistingPeriodicWorkPolicy
    import androidx.work.ExistingWorkPolicy
    import androidx.work.OneTimeWorkRequestBuilder
    import androidx.work.PeriodicWorkRequestBuilder
    import androidx.work.WorkManager
    import upc.binome8.petapp.data.Recurrence
    import java.util.Calendar
    import java.util.concurrent.TimeUnit


    object NotificationUtils {

        fun createChannel(c: Context) {
            val channel = NotificationChannel(CHANNEL_ID, "Pet App Channel", NotificationManager.IMPORTANCE_HIGH)
            channel.description = "Pet App Notifications Channel"
            val notificationManager = c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        fun createNotif(c: Context, petId: Int, petName: String, petSpecie: String,description: String){
            Log.i("createNotif:","$petId, $petName, $petSpecie, $description")

            val notificationManager = c.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val intent = Intent(c, ActivityShow::class.java).apply {
                putExtra("id", petId)
                putExtra("nom", petName)
                putExtra("espece", petSpecie)
            }

            val pending = PendingIntent.getActivity(c, petId, intent, PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(c, CHANNEL_ID).setSmallIcon(R.drawable.pets)
                .setContentTitle("N'oublie pas l'activité de $petName")
                .setContentText(description)
                .setContentIntent(pending)
                .setAutoCancel(true)
                .build()
            val notificationId = petId * 10 + System.currentTimeMillis().toInt()
            notificationManager.notify(notificationId, notification)
        }

        // Fonction pour gérer la demande d'envoi de notifications
        fun scheduleNotification(context: Context, activityId: Long, petId: Int, petName: String?, description: String, espece: String?, recurrence: Recurrence, horaire: String) {
            // On génère le delay à partir de l'horaire de récurrence ( journalier, hebdomadaire etc... ) demandé
            // par l'utilisateur
            val periodicDelay = when (recurrence) {
                Recurrence.QUOTIDIEN -> TimeUnit.DAYS.toMillis(1)
                Recurrence.HEBDOMADAIRE -> TimeUnit.DAYS.toMillis(7)
                Recurrence.MENSUEL -> TimeUnit.DAYS.toMillis(30)
                Recurrence.UNIQUE -> 0L // Pas de répétition pour une notification unique
            }

            // On créé un data builder pour passer des données dans l'intent et permettre à la notification d'envoyer sur
            // la page de l'animal
            val data = Data.Builder()
                .putInt("petId", petId)
                .putString("petName", petName)
                .putString("description", description)
                .putString("espece", espece)
                .build()

            Log.i("DataBuilder", "petId=$petId, petName=$petName, description=$description, espece=$espece, activity id:$activityId")

            // On génère un id unique pour le worker qui servira a récupérer et supprimer la notification si besoin
            val uniqueWorkName = "Pet activity $activityId"
            val initialDelay = calculInitialDelay(horaire)

            // Si la recurrence choisie est "unique" on envoie une requête OneTime, sinon Periodic
            if ( recurrence == Recurrence.UNIQUE ){
                val uniqueReq = OneTimeWorkRequestBuilder<PetActivityWorker>()
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context).enqueueUniqueWork(uniqueWorkName, ExistingWorkPolicy.REPLACE, uniqueReq)
            } else {
                val periodicReq =   PeriodicWorkRequestBuilder<PetActivityWorker>(periodicDelay, TimeUnit.MILLISECONDS)
                    .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .build()
                WorkManager.getInstance(context).enqueueUniquePeriodicWork(uniqueWorkName, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, periodicReq)
            }
        }

        // Fonction pour calculer l'initial delay en milisecondes entre le moment où la demande est envoyé et l'horaire choisi
        private fun calculInitialDelay(horaire: String): Long {

            val now = Calendar.getInstance()

            val h = horaire.split(":")[0].toInt()
            val m = horaire.split(":")[1].toInt()

            val target = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
            }

            if (target.before(now)) target.add(Calendar.DAY_OF_YEAR, 1)
            val initialDelay = target.timeInMillis - now.timeInMillis

            return initialDelay
        }

    }