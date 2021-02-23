package de.oemel09.msglauncher.ui.home

import android.annotation.SuppressLint
import android.app.Application
import android.os.AsyncTask
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.oemel09.msglauncher.domain.contacts.Contact
import de.oemel09.msglauncher.domain.contacts.ContactManager
import java.util.*

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val contactLiveData = MutableLiveData<List<Contact>>()
    private val contacts = mutableListOf<Contact>()
    private val contactManager = ContactManager(getApplication())

    init {
        contactLiveData.value = contacts
    }

    internal fun getContacts(): MutableLiveData<List<Contact>> {
        return contactLiveData
    }

    @SuppressLint("StaticFieldLeak")
    internal fun loadContacts(filter: String?, loadContactListener: LoadContactListener) {
        object : AsyncTask<Unit, Unit, List<Contact>>() {

            override fun doInBackground(vararg units: Unit?): List<Contact> {
                return if (filter == null) {
                    contactManager.loadListedContacts()
                } else {
                    contactManager.loadContacts(filter)
                }
            }

            override fun onPostExecute(c: List<Contact>?) {
                val oldSize = contacts.size
                contacts.clear()
                contacts.addAll(c!!)
                loadContactListener.onContactsLoaded(oldSize, c.size)
            }
        }.execute()
    }

    internal fun removeItem(position: Int) {
        val contact = contacts.removeAt(position)
        contactManager.hideContact(contact)
    }

    internal fun moveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                val contact1 = contacts[i]
                val contact2 = contacts[i + 1]
                val tmpPriority: Int = contact1.priority
                contact1.priority = contact2.priority
                contact2.priority = tmpPriority
                contactManager.swapContactPriority(contact1, contact2)
                Collections.swap(contacts, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                val contact1 = contacts[i]
                val contact2 = contacts[i - 1]
                val tmpPriority: Int = contact1.priority
                contact1.priority = contact2.priority
                contact2.priority = tmpPriority
                contactManager.swapContactPriority(contact1, contact2)
                Collections.swap(contacts, i, i - 1)
            }
        }
    }

    fun getContact(position: Int): Contact {
        return contacts[position]
    }

    internal fun saveContacts() {
        contactManager.updateDb()
    }

    public interface LoadContactListener {
        fun onContactsLoaded(oldSize: Int, newSize: Int)
    }
}