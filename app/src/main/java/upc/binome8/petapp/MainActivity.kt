package upc.binome8.petapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material.icons.sharp.RestoreFromTrash
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import androidx.work.WorkManager
import upc.binome8.petapp.NotificationUtils.createChannel
import upc.binome8.petapp.data.Pet

const val CHANNEL_ID = "Pet App Channel"


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WorkManager.getInstance(this).cancelAllWork()
        createChannel(this)
        setContent {
            AppScreen()
        }
    }
}


@Composable
fun AppScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            Modifier.padding(innerPadding)
        ) {
            composable("home") { Screen() }
            composable("param") { ParamScreen() }
        }
    }
}

@Composable
fun Screen(model: PetViewModel = viewModel()){
    // Si l'utilisateur a appuyé sur un des animaux on lance l'activité correspondante
    if(model.petClick.value){
        model.petClick.value = false
        val context = LocalContext.current
        val intent = Intent(context, ActivityShow::class.java)
        intent.putExtra("id",model.petInfoClick.idPet)
        intent.putExtra("nom",model.petInfoClick.nom)
        intent.putExtra("espece",model.petInfoClick.espece)
        context.startActivity(intent)
    }
    // Si l'utilisateur a appuyé sur l'onglet paramètre on lance l'activité correspondante
    if(model.param.value){
        model.param.value
        val context = LocalContext.current
        val intent = Intent(context, ParamActivty::class.java)
        context.startActivity(intent)
    }

    val annuler = {model.demandeSupprimer.value = false}
    if(model.demandeSupprimer.value){
        DialogueSupprimer(
            supprimer = {
                for(i in model.selected) {
                    (model::deletePet)(i)
                }
                model.selected.clear()
                annuler()},
            annuler = annuler
        )
    }
    Column{
        // On affiche la liste des animaux
        ShowPetList()
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween){
        AddButton(model::addPet)
        IconButton(onClick = {model.demandeSupprimer.value = true}, modifier = Modifier.size(100.dp)) { Icon(Icons.Sharp.RestoreFromTrash, contentDescription = "Delete") }

        }
    }
}

@Composable
/*Demande a l'utilisateur si l'animal doit etre supprimer plusieur peuvent etre choisie */
fun DialogueSupprimer(supprimer: () -> Unit, annuler: () -> Unit) = AlertDialog(
    confirmButton = {
        IconButton(onClick = supprimer) { Icon(Icons.Sharp.Check, contentDescription = "close") }
    },
    dismissButton = {
        IconButton(onClick = annuler) { Icon(Icons.Sharp.Clear, contentDescription = "close") }
    },
    onDismissRequest = annuler,
    title = { Text("Supprimer les animaux sélectionnés ?") },
)

@Composable
fun ShowPetList(model: PetViewModel = viewModel() ){
    // On prérempli la db pour l'exemple
    LaunchedEffect(Unit) {
        model.remplirPets()
    }

    // On récupère les animaux ajoutés
    val pets by model.pets.collectAsState(listOf())

    // Pour chaque animal de la liste on affiche sa card avec les infos
    LazyColumn(modifier = Modifier.fillMaxHeight(0.8f)){
        items(pets){
            PetCard(it.idPet,it.nom, it.espece,model)
        }
    }
}

@Composable
fun PetCard(id: Int, nom: String, espece: String, model: PetViewModel) {
    var isSelect by remember { mutableStateOf(false) }
    Card(
        shape = RoundedCornerShape(8.dp),
        onClick = {
            model.petClick.value = true
            model.petInfoClick = Pet(id, nom, espece)
            Log.i("animal sélectionné", "$id $nom $espece")
        },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF98FB98)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = id.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = nom,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = espece,
                fontSize = 16.sp,
                color = Color.Gray
            )
            Checkbox(
                onCheckedChange = {
                    if(isSelect){
                        model.selected.remove(Pet(id, nom, espece))
                        isSelect = !(isSelect)

                    }else{
                        model.selected.add(Pet(id, nom, espece))
                        isSelect = !(isSelect)

                    }
                    Log.i("Checkbox state changed", "Pet: $nom, Selected: ${isSelect}, List : ${model.selected}")
                },
                checked = isSelect

            )
        }
    }
}
@Composable
fun ParamScreen() {
    EcranParametre()
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("home"){ launchSingleTop = true } },
            icon = {
                Icon(Icons.Default.Home, contentDescription = "Home")
            },
        )
        NavigationBarItem(
            selected = false,
            onClick = { navController.navigate("param"){ launchSingleTop = true } },
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            },
        )
    }
}

@Composable
fun AddButton( onAddPet: (String, String) -> Unit){
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {  if( it.resultCode == Activity.RESULT_OK ){
        val intent = it.data
        val nom = intent?.getStringExtra("nom") ?: ""
        val espece = intent?.getStringExtra("espece") ?: ""
        onAddPet(nom, espece)
    }}

    Button(onClick = {
        val iii = Intent(context, AddPetActivity::class.java)
        launcher.launch(iii)
    }) { Text("Ajouter un animal",fontSize = 20.sp) }
}