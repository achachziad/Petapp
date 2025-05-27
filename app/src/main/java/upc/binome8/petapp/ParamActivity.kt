package upc.binome8.petapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.sharp.Add
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import upc.binome8.petapp.data.ActivityDefault
import upc.binome8.petapp.data.Espece

class ParamActivty : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EcranParametre()
        }
    }
}

@Composable
fun EcranParametre(model: ParamViewModel = viewModel()){
    if(model.demandeModifier.value){
        DialogueModifier(annuler = {model.demandeModifier.value = false},id = model.selected.idActivityDefault, espece = model.selected.espece,model = model)
    }
    if(model.demandeAjout.value){
        DialogueAdd(annuler = {model.demandeAjout.value = false},model = model)
    }
    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.fillMaxHeight(0.5f),
            Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ShowActivityList()
            Text("Ajouter une activité par defaut")
            IconButton(
                onClick = { model.demandeAjout.value = true },
                modifier = Modifier.size(100.dp)
            ) {
                Icon(
                    Icons.Sharp.Add, contentDescription = "Parametre"
                )
            }

        }
    }
}

@Composable
fun DialogueModifier(annuler : () -> Unit, id : Int, espece: String, model: ParamViewModel) {
    var activity by remember { mutableStateOf("") }

    AlertDialog(
        text = {
            Column {
                OutlinedTextField(value = activity, onValueChange = {activity = it})
            }
        },
        confirmButton = {
            IconButton(onClick = {model.updateActivity(espece,activity,id);annuler();}) { Icon(Icons.Sharp.Check, contentDescription = "close") }
        },
        dismissButton = {
            IconButton(onClick = annuler) { Icon(Icons.Sharp.Clear, contentDescription = "close") }
        }, title = {Text("Modifier une activité par défaut")},

        onDismissRequest = annuler
    )
}

@Composable
fun DialogueAdd(annuler: () -> Unit, model: ParamViewModel) {
    var activity by remember { mutableStateOf("") }
    var selectedEspece by remember { mutableStateOf(Espece.CHIEN) } // Espèce par défaut
    var expanded by remember { mutableStateOf(false) } // Pour gérer l'état du menu déroulant
    val maxLength = 100

    AlertDialog(
        text = {
            Column {
                // Champ de texte pour la description d'activité
                OutlinedTextField(
                    value = activity,
                    onValueChange = { if ( it.length < maxLength ) activity = it },
                    label = { Text("Description de l'activité") },
                    keyboardOptions = KeyboardOptions.Default
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Menu déroulant pour l'espèce
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Espèce: ${selectedEspece.label}")
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        Espece.entries.forEach { espece ->
                            DropdownMenuItem(text ={Text(espece.label)} ,onClick = {
                                selectedEspece = espece
                                expanded = false
                            })
                        }
                    }
                }
            }
        },
        confirmButton = {
            IconButton(
                onClick = {
                    // Appel de la fonction addActivity avec la description et l'espèce
                    model.add(description = activity, espece = selectedEspece.toString())
                    annuler()
                }
            ) {
                Icon(Icons.Sharp.Check, contentDescription = "Confirm")
            }
        },
        dismissButton = {
            IconButton(onClick = annuler) {
                Icon(Icons.Sharp.Clear, contentDescription = "Dismiss")
            }
        },
        title = { Text("Ajouter une activité par défaut") },
        onDismissRequest = annuler
    )
}

@Composable
fun ShowActivityList(model: ParamViewModel = viewModel() ){
    val db = model.activitydefault.collectAsState(listOf())
    val activity = db.value

    LazyColumn(modifier = Modifier.fillMaxHeight(0.8f)) {

        items(activity) { item ->
            ActivityDefaultCard(item.idActivityDefault, item.description, item.espece, model)
        }
    }
}

@Composable
fun ActivityDefaultCard(activityId: Int, description: String, espece: String, model : ParamViewModel ) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF98FB98)),
    ) {
        Column{
            Row {
                Text(
                    text = espece,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = description,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Row {
                IconButton(onClick = { model.deleteActivity(ActivityDefault( activityId ,espece,description = description))}) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete"
                    )
                }
                IconButton(onClick = {  model.demandeModifier.value = true;model.selected =
                    ActivityDefault( activityId ,espece, description = description)
                 }) {Icon(
                    Icons.Filled.Mode,
                    contentDescription = "Delete"
                )
                }
            }
        }
    }
}
