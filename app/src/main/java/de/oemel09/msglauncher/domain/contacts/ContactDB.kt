package de.oemel09.msglauncher.domain.contacts

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_VERSION = 1
private const val DATABASE_NAME = "contacts_db"

private const val TABLE_NAME = "contacts"
private const val COLUMN_LOOKUP = "lookup"
private const val COLUMN_NAME = "name"
private const val COLUMN_CUSTOM_MESSENGER = "custom_messenger"
private const val COLUMN_IS_LISTED = "is_listed"
private const val COLUMN_PRIORITY = "priority"
const val CREATE_TABLE = ("CREATE TABLE " + TABLE_NAME + "("
        + COLUMN_LOOKUP + " TEXT PRIMARY KEY,"
        + COLUMN_NAME + " TEXT,"
        + COLUMN_CUSTOM_MESSENGER + " TEXT DEFAULT NULL,"
        + COLUMN_IS_LISTED + " INTEGER DEFAULT 0,"
        + COLUMN_PRIORITY + " INTEGER"
        + ")")

class ContactDB(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        db!!.execSQL(CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    internal fun insertContact(contact: Contact) {
        val db = writableDatabase
        val values = ContentValues()
        values.put(COLUMN_LOOKUP, contact.lookup)
        values.put(COLUMN_NAME, contact.name)
        values.put(COLUMN_CUSTOM_MESSENGER, contact.customMessenger)
        values.put(COLUMN_IS_LISTED, contact.isListed)
        values.put(COLUMN_PRIORITY, contact.priority)

        try {
            db.insertOrThrow(TABLE_NAME, null, values)
        } catch (ignore: SQLException) {
        }
        db.close()
    }

    internal fun loadListedContacts(): List<Contact> {
        val db = readableDatabase

        val selection = "$COLUMN_IS_LISTED = ?"
        val selectionArgs = arrayOf("1")
        val cursor =
            db.query(TABLE_NAME, null, selection, selectionArgs, null, null, COLUMN_PRIORITY, null)
        val contacts = extractContacts(cursor)
        cursor.close()
        return contacts
    }

    internal fun loadContacts(filter: String?): List<Contact> {
        val db = readableDatabase

        var selection: String? = null
        var selectionArgs = emptyArray<String>()
        if (filter != null && filter.isNotEmpty()) {
            selection = "$COLUMN_NAME LIKE ?"
            selectionArgs = arrayOf("% $filter %")
        }
        val cursor = db.query(TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val contacts = extractContacts(cursor)
        cursor.close()
        return contacts
    }

    private fun extractContacts(cursor: Cursor?): List<Contact> {
        val contacts = mutableListOf<Contact>()
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val contact = Contact(
                        cursor.getString(cursor.getColumnIndex(COLUMN_LOOKUP)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_NAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_CUSTOM_MESSENGER)),
                        cursor.getInt(cursor.getColumnIndex(COLUMN_IS_LISTED)) > 0,
                        cursor.getInt(cursor.getColumnIndex(COLUMN_PRIORITY)),
                    )
                    contacts.add(contact)
                } while (cursor.moveToNext())
            }
            cursor.close()
        }
        return contacts
    }

    internal fun updateContactIsListed(contact: Contact) {
        val values = ContentValues()
        values.put(COLUMN_IS_LISTED, contact.isListed)
        update(values, contact.lookup)
    }

    internal fun updateContactPriority(contact: Contact) {
        val values = ContentValues()
        values.put(COLUMN_PRIORITY, contact.priority)
        update(values, contact.lookup)
    }

    private fun update(values: ContentValues, lookup: String) {
        val db = writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_LOOKUP = ?", arrayOf(lookup))
    }
}
