package upc.binome8.petapp

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType.Companion.PrimaryNotEditable
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import upc.binome8.petapp.data.Espece
import androidx.compose.ui.platform.LocalContext

class AddPetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AddPetScreen()
        }
    }
}

@Composable
fun AddPetScreen(){
    var nom by remember { mutableStateOf("")}
    var espece by remember { mutableStateOf("")}
    val context = LocalContext.current
    val maxLength = 24

    Column{
        TextField(
            value = nom,
            onValueChange = { if (it.length <= maxLength) nom = it },
            label = { Text("Nom") },
            keyboardOptions = KeyboardOptions.Default
        )
        DropDownPetSpecies( onSpeciesSelected = { espece = it } )
        Row {
            Button(onClick = { add(context, nom, espece) }) { Text("Ajouter") }
            Button(onClick = { cancel(context) }) { Text("Annuler") }
        }
    }
}

fun add(c: Context, nom:String, espece: String){
    val a = c as Activity
    val iii  = Intent()
    iii.putExtra("nom", nom)
    iii.putExtra("espece",espece)
    a.setResult(RESULT_OK, iii)
    a.finish()
}

fun cancel(c: Context){
    val a = c as Activity
    a.setResult(RESULT_CANCELED)
    a.finish()
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDownPetSpecies( onSpeciesSelected: (String) -> Unit ){
    val choix = Espece.entries.toTypedArray()
    var expanded by remember { mutableStateOf(false) }
    var choisi by remember { mutableStateOf(choix[0]) }
    remember { onSpeciesSelected(choisi.toString()) }

    Column{
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {expanded = !expanded }
        ) {
            TextField(
                modifier = Modifier.menuAnchor(PrimaryNotEditable, true),
                readOnly = true,
                value = choisi.toString(),
                onValueChange = { },
                label = { Text("Choix") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                choix.forEach{ c ->
                    DropdownMenuItem(
                        onClick = {
                            choisi = c
                            expanded = false
                            onSpeciesSelected(c.toString()) },
                        text = { Text(c.toString()) }
                    )
                }
            }
        }
    }
}
