package upc.binome8.petapp.data


// Enum pour les récurrences des notifications, tout les jours, toutes les semaines, mois ou unique
enum class Recurrence(val label: String) {
    QUOTIDIEN("Quotidien"),
    HEBDOMADAIRE("Hebdomadaire"),
    MENSUEL("Mensuel"),
    UNIQUE("Unique");

    override fun toString(): String {
        return label
    }
}