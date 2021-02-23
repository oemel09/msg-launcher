package de.oemel09.msglauncher.domain.messengers

class Messenger internal constructor(var id: String, var name: String, var priority: Int) : Comparable<Messenger> {

    override fun compareTo(other: Messenger): Int {
        // sort in descending order
        return other.priority.compareTo(priority)
    }
}
