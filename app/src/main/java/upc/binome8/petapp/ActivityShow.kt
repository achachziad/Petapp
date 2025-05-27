package upc.binome8.petapp

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.DownloadManager
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Mode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.SubdirectoryArrowLeft
import androidx.compose.material.icons.sharp.Check
import androidx.compose.material.icons.sharp.Clear
import androidx.compose.material.icons.sharp.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.WorkManager
import upc.binome8.petapp.NotificationUtils.scheduleNotification
import upc.binome8.petapp.data.Activity
import upc.binome8.petapp.data.Recurrence
import java.io.File
import java.util.Locale

class ActivityShow : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // On récupère les infos du intent, id, nom et espèce de l'animal
            val petId = intent.getIntExtra("id",0)
            val petName = intent.getStringExtra("nom")
            val petSpecie = intent.getStringExtra("espece")

            Log.i("ActivityShow - SetContent", "Pet Informations: $petId, $petName, $petSpecie")

            EcranParametre(petId,petName,petSpecie)
        }
    }
}

@Composable
fun AnimalImageCard(context: Context, species: String) {
    // Définir l'URL selon l'espèce
    val imageUrl = when (species.lowercase()) {
        "chien" -> "https://media.istockphoto.com/id/149325740/fr/photo/chien-chiot-berger-belge-5-mois-debout.jpg?s=612x612&w=0&k=20&c=Ojzz0iiA64jubk0DCCyvHDroxLOHU59wHZVEdHnn-MU="
        "lapin" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/e/ed/BRACHYLAGUS_IDAHOENSIS.jpg/200px-BRACHYLAGUS_IDAHOENSIS.jpg"
        "chat" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/2/25/Chat_roux_%C3%A0_pelage_court..jpg/300px-Chat_roux_%C3%A0_pelage_court..jpg"
        "poisson" -> "https://commons.wikimedia.org/wiki/File:Synchiropus_splendidus_2_Luc_Viatour.jpg?uselang=fr"
        "hamster" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/6/65/D_hamster.jpg/220px-D_hamster.jpg"
        "cheval" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f7/Mangalarga_Marchador_Conforma%C3%A7%C3%A3o.jpg/240px-Mangalarga_Marchador_Conforma%C3%A7%C3%A3o.jpg"
        else -> "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f8/Question_mark_alternate.svg/langfr-90px-Question_mark_alternate.svg.png"
    }

    // Nom du fichier téléchargé
    val fileName = "$species.jpg"

    // Chemin du fichier
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(storageDir, fileName)

    // Téléchargement de l'image si elle n'existe pas
    if (!imageFile.exists()) {
        downloadImageWithDownloadManager(context, imageUrl, fileName)
    }

    // Chargement de l'image en Bitmap
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }

    LaunchedEffect(imageFile) {
        if (imageFile.exists()) {
            bitmap.value = BitmapFactory.decodeFile(imageFile.absolutePath)
        }
    }

    // Affichage dans la Card
    Card(
        modifier = Modifier
            .padding(16.dp)
            .size(150.dp),
        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
    ) {
        if (bitmap.value != null) {
            Image(
                bitmap = bitmap.value!!.asImageBitmap(),
                contentDescription = "Image de l'animal",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Placeholder pendant le téléchargement
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chargement...")
            }
        }
    }
}

fun downloadImageWithDownloadManager(context: Context, imageUrl: String, fileName: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val imageFile = File(storageDir, fileName)

    if (!imageFile.exists()) {
        val request = DownloadManager.Request(Uri.parse(imageUrl))
            .setTitle("Téléchargement de l'image")
            .setDescription("Téléchargement de $fileName")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationUri(Uri.fromFile(imageFile))

        downloadManager.enqueue(request)
    }
}

@Composable
fun EcranParametre(petId: Int, petName: String?, petSpecie: String?, model: ActivityViewModel = viewModel() ) {
    val context = LocalContext.current

    // Si une demande d'ajout d'activité est demandé par l'utilisateur
    if(model.demandeAjout.value){
        DialogueActivity(
            context = context,
            isEditing = false,
            nom = petName,
            onConfirm = { description, notif, recurrence, horaire ->
                // A la confirmation on ajoute une activité avec les données entrées
                model.addActivity(petId, description, notif, recurrence.toString(), horaire) { generatedId ->
                    Log.i("ActivityID", "ID nouvelle activitée: $generatedId")

                    // Puis si une notification a été demandé on appelle la fonction qui les gèrent
                    if (notif) {
                        scheduleNotification(
                            context = context,
                            activityId = generatedId,
                            petId = petId,
                            petName = petName,
                            espece = petSpecie,
                            description = description,
                            recurrence = recurrence,
                            horaire = horaire
                        )
                    }

                }
            },
            annuler = { model.demandeAjout.value = false }
        )
    }

    // Si une demande de modification d'activité est demandée par l'utilisateur
    if (model.demandeModifier.value) {
        DialogueActivity(
            context = context,
            isEditing = true,
            nom = petName,
            initialActivity = model.selected.first(),
            onConfirm = { description, notif, recurrence, horaire ->
                // A la confirmation on update l'activité
                model.updateActivity(
                    old = model.selected.first().description,
                    new = description,
                    newNotif = notif,
                    id = petId,
                    newRecurrence = recurrence.toString(),
                    newHoraire = horaire
                )

                // On récupère l'id de l'activité déjà existante
                val activityId = model.selected.first().idActivity

                // Si à la fin de l'edit on a notif true, on renvoie une demande de worker, si le worker existe déjà il
                // est remplacé grâce à la policy CANCEL_AND_REENQUEUE
                if (notif) {
                    scheduleNotification(
                        context = context,
                        activityId = activityId.toLong(),
                        petId = petId,
                        petName = petName,
                        espece = petSpecie,
                        description = description,
                        recurrence = recurrence,
                        horaire = horaire
                    )
                } else {
                    //sinon on essaye de le supprimer dans le cas où il existe
                    WorkManager.getInstance(context).cancelUniqueWork("Pet activity $activityId")
                }

            },
            annuler = { model.demandeModifier.value = false }
        )
    }

    Scaffold(topBar = { MyTopAppBar(petName) }) {
        it.calculateBottomPadding()
    }
    val stop = {val intent = Intent()
        val a = context as android.app.Activity
        a.setResult(RESULT_OK,intent)
        a.finish()
    }

    // On récupère les activités de l'animal dans la db et les activités par défaut des paramètres
    val petActivities by model.getActivity(petId).collectAsState(listOf())
    val activitiesDefault by model.getActivityDefault(petSpecie.toString()).collectAsState(listOf())

    Column(modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // On affiche l'image par défaut de l'animal
        AnimalImageCard(context = LocalContext.current, species = petSpecie.toString())

        // On affiche pour chaque activités sa card
        LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)){

            items(activitiesDefault){item ->
                DefaultActivityCard(item.description)
            }
            items(petActivities){
                ActivityCard(it.idActivity, it.idPet, it.description, it.notif, it.recurrence, it.horaire)
            }

        }
        Row {
            IconButton(onClick = { model.demandeAjout.value = true }) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "ADD"
                )
            }
            IconButton(onClick = stop) {
                Icon(
                    Icons.Filled.SubdirectoryArrowLeft,
                    contentDescription = "Return"
                )
            }
        }
    }
}

@Composable
fun DefaultActivityCard(description: String) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFADD8E6)),
    ) {
        Column {
            Text(
                text = description,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// Dialogue d'ajout / edition des activités
@SuppressLint("InlinedApi")
@Composable
fun DialogueActivity(
    context: Context,
    isEditing: Boolean,
    nom: String? = null,
    initialActivity: Activity? = null,
    onConfirm: (description: String, notif: Boolean, recurrence: Recurrence, horaire: String) -> Unit,
    annuler: () -> Unit,
){
    var activityDesc by remember { mutableStateOf(initialActivity?.description ?: "") }
    var notif by remember { mutableStateOf(initialActivity?.notif ?: false) }
    var recurrence by remember { mutableStateOf(initialActivity?.recurrence?.let { Recurrence.valueOf(it.uppercase()) } ?: Recurrence.QUOTIDIEN) }
    var horaire by remember { mutableStateOf("12:00") }
    val maxLength = 100

    // On prépare un launcher pour demander les permissions de notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d("Permissions: ", if (isGranted) "Granted" else "Denied")
    }

    AlertDialog(
        text = {
            Column {
                OutlinedTextField(
                    value = activityDesc,
                    onValueChange = {  if (it.length <= maxLength) activityDesc = it },
                    label = { Text("Description") },
                    keyboardOptions = KeyboardOptions.Default
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notification")
                    Spacer(modifier = Modifier.width(10.dp))
                    Switch(
                        checked = notif,
                        onCheckedChange = { isChecked ->
                            // Si le "slider" des notifications est utilisé on lance la demande de permission
                            notif = isChecked
                            if (isChecked) {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    )
                }
                // Puis on affiche le menu drop down pour choisir la récurrence des notifications
                if (notif) {
                    DropDownNotificationSchedule( onScheduleSelected = { recurrence = it })

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        TimePicker(
                            context = context,
                            onTimeSelected = { selectedTime ->
                                horaire = selectedTime
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(horaire)
                    }
                }
            }
        },

        // Confirm
        confirmButton = {
            IconButton(onClick = {
                onConfirm(activityDesc, notif, recurrence, horaire)
                annuler()
            }) {
                Icon(Icons.Sharp.Check, contentDescription = "Confirm")
            }
        },
        // Cancel
        dismissButton = {
            IconButton(onClick = annuler) {
                Icon(Icons.Sharp.Clear, contentDescription = "Cancel")
            }
        },
        title = {
            // Si on est en mode édition on affiche le titre edition sinon ajout
            Text(if (isEditing) "Modifier l'activité" else "Ajouter une activité à $nom")
        },
        onDismissRequest = annuler
    )
}

@Composable
fun TimePicker(context: Context, onTimeSelected: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Button(onClick = { showDialog = true } /* , modifier = Modifier.fillMaxWidth() */) {
        Text("Choisir un horaire")
    }

    if (showDialog) {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                onTimeSelected(selectedTime)
                showDialog = false
            },
            12,
            0,
            true
        ).show()
    }
}


// Fonction pour afficher le menu drop down qui permet de choisir la récurrence des notifications
@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownNotificationSchedule( onScheduleSelected: (Recurrence) -> Unit ){
    val choix = Recurrence.entries.toTypedArray()
    var expanded by remember { mutableStateOf(false)}
    var choisi by remember { mutableStateOf(choix[0])}
    remember { onScheduleSelected(choisi)}

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        TextField(
            value = choisi.label,
            onValueChange = { },
            label = { Text("Récurrence des notifications") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            readOnly = true,
            modifier = Modifier.menuAnchor(PrimaryNotEditable, true)
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            choix.forEach { c ->
                DropdownMenuItem(
                    onClick = {
                        choisi = c
                        expanded = false
                        onScheduleSelected(c)
                    },
                    text = { Text(c.label) }
                )
            }
        }
    }
}

@Composable
fun ActivityCard(activityId: Int, id: Int, description: String, notif: Boolean, recurrence: String, horaire: String, model : ActivityViewModel = viewModel()) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF98FB98)),
    ) {
        Column{
            Text(
                text = description,
                modifier = Modifier.padding(16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    model.deleteActivity(Activity( activityId ,idPet = id, description = description, notif, recurrence, horaire))
                    WorkManager.getInstance(context).cancelUniqueWork("Pet activity $activityId")
                }) {
                    Icon(
                        Icons.Sharp.Delete,
                        contentDescription = "Delete"
                    )
                }
                IconButton(
                    onClick = {
                        model.demandeModifier.value = true
                        model.selected.clear()
                        model.selected.add(Activity(idActivity = activityId, idPet = id, description = description, notif, recurrence, horaire))
                        Log.i("ACTIVITY CARD", "Activity SELECTED: $description")
                    }) {
                    Icon(
                        Icons.Filled.Mode,
                        contentDescription = "Edit"
                    )
                }
                Icon(
                    imageVector = if (notif) Icons.Filled.Notifications else Icons.Filled.NotificationsOff,
                    contentDescription = if (notif) "Notification activée" else "Notification désactivée"
                )
                if (notif) {
                    Row {
                        Text(
                            text = recurrence
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = horaire
                        )
                    }
                }
                }
            }

        }
    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyTopAppBar(nom: String?) = CenterAlignedTopAppBar(
    title = { Text("$nom", style = MaterialTheme.typography.titleLarge) }
)


