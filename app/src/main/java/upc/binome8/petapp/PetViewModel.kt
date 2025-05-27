package upc.binome8.petapp

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import upc.binome8.petapp.data.Pet
import upc.binome8.petapp.data.PetsDB

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val dao by lazy { PetsDB.getDatabase(application).petsDAO()}
    var pets = dao.getAll()
    var petClick = mutableStateOf(false)
    var petInfoClick = Pet(0,"Buddy","Chien")
    var param = mutableStateOf(false)
    var demandeSupprimer = mutableStateOf(false)
    private var erreurIns= mutableStateOf(false)

    private val listNom = application.resources.getStringArray(R.array.pet_nom)
    private val listEspece = application.resources.getStringArray(R.array.pet_espece)
    var selected = mutableListOf<Pet>()

    fun addPet(nom: String, espece: String) {
        viewModelScope.launch(Dispatchers.IO) {
            if(-1L==dao.insert(Pet(nom = nom, espece = espece)))
                erreurIns.value=true
        }
    }

    fun deletePet(pet: Pet) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.delete(pet)
        }
    }

    fun remplirPets() {
        for (i in listNom.indices)
            addPet(nom = listNom[i], espece = listEspece[i])
    }
}
