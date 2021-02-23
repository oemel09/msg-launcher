package de.oemel09.msglauncher.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import de.oemel09.msglauncher.domain.messengers.Messenger
import de.oemel09.msglauncher.domain.messengers.MessengerManager
import java.util.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val messengerManager: MessengerManager = MessengerManager(application)
    private val messengers = messengerManager.getMessengers()

    val messengerLiveData = MutableLiveData<List<Messenger>>().apply {
        value = messengers
    }

    fun changePosition(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition..toPosition) {
                Collections.swap(messengers, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition step -1) {
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
        messengerManager.saveMessengers(messengers)
    }
}
