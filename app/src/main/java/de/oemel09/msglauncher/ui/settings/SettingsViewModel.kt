package de.oemel09.msglauncher.ui.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.oemel09.msglauncher.domain.messengers.Messenger
import de.oemel09.msglauncher.domain.messengers.MessengerManager
import java.util.*

const val PREFS_APPEARANCE = "APPEARANCE"
const val PREFS_SHOW_OPEN_CONTACT_PAGE = "showOpenContactPage"

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val messengerManager: MessengerManager = MessengerManager(application)
    private val messengers = messengerManager.getAllMessengers()

    private val prefs = application.getSharedPreferences(PREFS_APPEARANCE, Context.MODE_PRIVATE)
    private var showOpenContactsPage = prefs.getBoolean(PREFS_SHOW_OPEN_CONTACT_PAGE, false)

    val messengerLiveData = MutableLiveData<List<Messenger>>().apply {
        value = messengers
    }
    val showOpenContactsPageLiveData = MutableLiveData<Boolean>().apply {
        value = showOpenContactsPage
    }

    fun changePosition(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(messengers, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(messengers, i, i - 1)
            }
        }

        val from = messengers[fromPosition]
        val to = messengers[toPosition]
        val tmpPriority = to.priority
        to.priority = from.priority
        from.priority = tmpPriority
    }

    fun saveMessengers() {
        messengerManager.saveMessengers()
    }

    fun setShowOpenContactsPage(showOpenContactsPage: Boolean) {
        prefs.edit().putBoolean(PREFS_SHOW_OPEN_CONTACT_PAGE, showOpenContactsPage).apply()
        showOpenContactsPageLiveData.postValue(showOpenContactsPage)
    }
}
