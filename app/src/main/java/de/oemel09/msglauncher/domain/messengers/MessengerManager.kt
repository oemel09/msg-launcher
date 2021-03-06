package de.oemel09.msglauncher.domain.messengers

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import de.oemel09.msglauncher.domain.Opener
import de.oemel09.msglauncher.domain.contacts.Contact
import java.util.*

const val MESSENGER_ID_AUTO = "AUTOMATICALLY_CHOOSE_BEST"
const val MESSENGER_NAME_THREEMA = "Threema"
const val MESSENGER_ID_THREEMA = "ch.threema.app"
const val MESSENGER_ID_WHATSAPP = "com.whatsapp"
const val MESSENGER_NAME_WHATSAPP = "WhatsApp"
const val MESSENGER_ID_TELEGRAM = "org.telegram.messenger"
const val MESSENGER_NAME_TELEGRAM = "Telegram"
const val MESSENGER_ID_SIGNAL = "org.thoughtcrime.securesms"
const val MESSENGER_NAME_SIGNAL = "Signal"

// used by Threema, Telegram and WhatsApp
const val MIME_TYPE_PROFILE = "%.profile"

// used by Signal
const val MIME_TYPE_CONTACT = "%.contact"

// for shared prefs
private const val PREFS_MESSENGERS = "MESSENGERS"

class MessengerManager(private val context: Context) {

    private var messengers: List<Messenger> = loadMessengers()

    private fun loadMessengers(): List<Messenger> {
        var messengers: MutableList<Messenger>?
        val sharedPreferences = context.getSharedPreferences(PREFS_MESSENGERS, Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString(PREFS_MESSENGERS, null)
        val type = object : TypeToken<List<Messenger?>?>() {}.type
        messengers = gson.fromJson(json, type)
        if (messengers == null) {
            // default messenger order
            messengers = ArrayList()
            val p = context.packageManager
            for (m in supportedMessengers) {
                if (isPackageInstalled(m.id, p)) {
                    messengers.add(m)
                }
            }
        }
        messengers.sort()
        return messengers
    }

    private fun isPackageInstalled(packageName: String, packageManager: PackageManager): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun saveMessengers() {
        val sharedPreferences = context.getSharedPreferences(PREFS_MESSENGERS, Context.MODE_PRIVATE)
        sharedPreferences.edit().putString(PREFS_MESSENGERS, Gson().toJson(messengers)).apply()
    }

    private val supportedMessengers: Array<Messenger>
        get() = arrayOf(
            Messenger(MESSENGER_ID_THREEMA, MESSENGER_NAME_THREEMA, 40),
            Messenger(MESSENGER_ID_SIGNAL, MESSENGER_NAME_SIGNAL, 30),
            Messenger(MESSENGER_ID_TELEGRAM, MESSENGER_NAME_TELEGRAM, 20),
            Messenger(MESSENGER_ID_WHATSAPP, MESSENGER_NAME_WHATSAPP, 10)
        )

    internal fun getAllMessengers(): List<Messenger> {
        return messengers
    }

    internal fun getAllApplicableMessengers(contact: Contact): List<Messenger> {
        return messengers.filter {
            createOpener(contact.lookup, it.id) != null
        }
    }

    internal fun getContactUri(contact: Contact): Uri? {
        var contactUri: Uri? = null
        val projection = arrayOf(ContactsContract.RawContacts._ID)
        val selection = ContactsContract.Data.LOOKUP_KEY + " = ?"
        val selectionArgs = arrayOf(contact.lookup)
        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null && cursor.count > 0) {
            if (cursor.moveToNext()) {
                contactUri =
                    ContactsContract.Contacts.getLookupUri(cursor.getLong(0), contact.lookup)
            }
            cursor.close()
        }
        return contactUri
    }

    internal fun getOpener(contact: Contact): Opener? {
        if (contact.customMessenger == null) {
            for (m in messengers) {
                val opener = createOpener(contact.lookup, m.id)
                if (opener != null) {
                    return opener
                }
            }
            return null
        } else {
            return createOpener(contact.lookup, contact.customMessenger!!)
        }
    }

    private fun createOpener(lookup: String, messengerId: String): Opener? {
        var opener: Opener? = null
        val projection = arrayOf(ContactsContract.RawContacts._ID, ContactsContract.Data.MIMETYPE)
        val selection = (ContactsContract.Data.LOOKUP_KEY + " = ? AND "
                + ContactsContract.RawContacts.ACCOUNT_TYPE + " IN (?) AND ("
                + ContactsContract.Data.MIMETYPE + " LIKE ? OR "
                + ContactsContract.Data.MIMETYPE + " LIKE ?)")
        val selectionArgs = arrayOf(lookup, messengerId, MIME_TYPE_PROFILE, MIME_TYPE_CONTACT)
        val cursor = context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )
        if (cursor != null && cursor.count > 0) {
            val hasMessenger = cursor.moveToNext()
            if (hasMessenger) {
                opener = Opener(cursor.getString(0), cursor.getString(1))
            }
            cursor.close()
        }
        return opener
    }
}
