package upc.binome8.petapp

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import upc.binome8.petapp.data.ActivityDefault
import upc.binome8.petapp.data.PetsDB

class ParamViewModel(application: Application) : AndroidViewModel(application) {
    private val dao by lazy { PetsDB.getDatabase(application).ParamActivityDAO()}
    var activitydefault = dao.getAll()

    var selected = ActivityDefault(espece = "",description = "")
    var demandeModifier = mutableStateOf(false)
    var demandeAjout = mutableStateOf(false)

    fun add(espece: String, description: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insert(ActivityDefault(espece = espece, description = description))
        }
    }

    fun deleteActivity(activity: ActivityDefault) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(activity)
        }
    }
    fun updateActivity(espece: String,newdescription: String,id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateActivity(espece,newdescription,id)
        }
    }
    
}
