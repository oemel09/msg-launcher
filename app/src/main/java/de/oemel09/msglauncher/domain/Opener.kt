package de.oemel09.msglauncher.domain

import android.net.Uri
import android.provider.ContactsContract

data class Opener(val messengerSpecificId: String, internal val mimeType: String) {
    internal val contactUri: Uri =
        Uri.withAppendedPath(ContactsContract.Data.CONTENT_URI, messengerSpecificId)
}
