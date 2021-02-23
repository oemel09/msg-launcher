package de.oemel09.msglauncher.domain.contacts

data class Contact(
    var lookup: String,
    var name: String,
    var customMessenger: String?,
    var isListed: Boolean,
    var priority: Int
) {

    constructor(lookup: String, name: String) : this(lookup, name, null, false, 0)
}
