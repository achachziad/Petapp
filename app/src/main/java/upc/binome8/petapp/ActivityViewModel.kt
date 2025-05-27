package upc.binome8.petapp

import android.app.Application

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import upc.binome8.petapp.data.Activity
import upc.binome8.petapp.data.PetsDB

class ActivityViewModel(application: Application) : AndroidViewModel(application) {
   private val activityDao by lazy { PetsDB.getDatabase(application).activityDAO() }
   //private val listNom = application.resources.getStringArray(R.array.pet_activities)
   var demandeAjout = mutableStateOf(false)
   var demandeModifier = mutableStateOf(false)
   var selected = mutableListOf<Activity>()

   fun getActivity(id: Int) = activityDao.getActivity(id)

   fun addActivity(id: Int, description: String,notif: Boolean, recurrence: String, horaire: String,callbackGeneratedId: (Long) -> Unit ) {
      viewModelScope.launch(Dispatchers.IO) {
         val generatedId = ( activityDao.insert(Activity(idPet = id, description = description, notif = notif, recurrence = recurrence, horaire = horaire)) )
         // Ici on récupère l'id renvoyé par le DAO pour l'utiliser dans les notifications
         callbackGeneratedId(generatedId)
      }
   }

   fun updateActivity(old: String, new: String, id: Int, newNotif: Boolean, newHoraire: String, newRecurrence: String) {
      viewModelScope.launch(Dispatchers.IO) {
         activityDao.updateActivity(oldDescription = old, newDescription = new, newNotif = newNotif, id = id, newHoraire = newHoraire,newRecurrence = newRecurrence)
      }
   }

   fun deleteActivity(activity: Activity) {
      viewModelScope.launch(Dispatchers.IO) {
         activityDao.delete(activity)
      }
   }

   private val defaultActivityDao by lazy {PetsDB.getDatabase(application).ParamActivityDAO()}
   fun getActivityDefault(espece : String ) = defaultActivityDao.getActivityDefault(espece)
}