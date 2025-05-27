package upc.binome8.petapp.data

// Enum pour représenter les espèces par défaut
enum class Espece(val label: String) {
    CHIEN("Chien"),
    CHAT("Chat"),
    HAMSTER("Hamster"),
    POISSON("Poisson"),
    LAPIN("Lapin"),
    CHEVAL("Cheval");

    override fun toString(): String {
        return label
    }
}