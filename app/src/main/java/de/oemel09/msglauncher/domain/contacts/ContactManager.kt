package de.oemel09.msglauncher.domain.contacts

import android.content.Context
import android.util.Log

private const val CONTACTS = "CONTACTS"
private const val ADDRESS_BOOK_ALREADY_QUERIED = "address_book_already_queried"
private val TAG = ContactManager::class.simpleName

class ContactManager(context: Context) {

    private val addressBook = AddressBook(context)
    private val contactDB = ContactDB(context)
    private val prefs = context.getSharedPreferences(CONTACTS, Context.MODE_PRIVATE)

    internal fun loadListedContacts(): List<Contact> {
        var contacts = contactDB.loadListedContacts()
        if (!prefs.getBoolean(ADDRESS_BOOK_ALREADY_QUERIED, false) && contacts.isEmpty()) {
            contacts = addressBook.loadListedContacts()
            contacts.forEach { c ->
                contactDB.insertContact(c)
            }
            prefs.edit().putBoolean(ADDRESS_BOOK_ALREADY_QUERIED, true).apply()
        }
        return contacts
    }

    internal fun loadContacts(filter: String): List<Contact> {
        var contacts = contactDB.loadContacts(filter)
        if (contacts.isEmpty()) {
            contacts = addressBook.loadContacts(filter)
        }
        return contacts
    }

    internal fun hideContact(contact: Contact) {
        contact.isListed = false
        contactDB.updateContactIsListed(contact)
    }

    internal fun swapContactPriority(contact1: Contact, contact2: Contact) {
        contactDB.updateContactPriority(contact1)
        contactDB.updateContactPriority(contact2)
    }

    internal fun updateDb() {
        object : Thread(Runnable {
            val startTime = System.currentTimeMillis()
            addressBook.loadContacts(null).forEach {
                contactDB.insertContact(it)
            }
            val updateTime = System.currentTimeMillis() - startTime
            Log.i(TAG, "update took $updateTime ms")
        }) {}.start()
    }
}
