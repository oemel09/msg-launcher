package de.oemel09.msglauncher.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.oemel09.msglauncher.R
import de.oemel09.msglauncher.ui.ItemTouchHelperCallback
import de.oemel09.msglauncher.ui.OnItemDragListener

class SettingsFragment : Fragment(), OnItemDragListener {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var messengerAdapter: MessengerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_settings, container, false)

        messengerAdapter = MessengerAdapter(requireContext(), null)
        val rvMessengers = root.findViewById<RecyclerView>(R.id.settings_rv_messengers)
        rvMessengers.layoutManager = LinearLayoutManager(context)
        rvMessengers.adapter = messengerAdapter
        val dragDirs = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val touchHelper = ItemTouchHelper(ItemTouchHelperCallback(dragDirs, 0, this))
        touchHelper.attachToRecyclerView(rvMessengers)
        settingsViewModel.messengerLiveData.observe(viewLifecycleOwner, {
            messengerAdapter.updateMessengers(it)
        })

        val switchOpenContactsPage =
            root.findViewById<SwitchCompat>(R.id.settings_switch_show_open_contacts_page)
        settingsViewModel.showOpenContactsPageLiveData.observe(viewLifecycleOwner, {
            switchOpenContactsPage.isChecked = it
        })
        switchOpenContactsPage.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            settingsViewModel.setShowOpenContactsPage(b)
        }

        return root
    }

    override fun onItemDismiss(position: Int) {}

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        settingsViewModel.changePosition(fromPosition, toPosition)
        messengerAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    override fun onPause() {
        super.onPause()
        settingsViewModel.saveMessengers()
    }
}
