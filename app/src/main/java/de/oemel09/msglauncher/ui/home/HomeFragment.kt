package de.oemel09.msglauncher.ui.home

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import de.oemel09.msglauncher.R
import de.oemel09.msglauncher.domain.Opener
import de.oemel09.msglauncher.domain.messengers.MESSENGER_ID_AUTO
import de.oemel09.msglauncher.domain.messengers.Messenger
import de.oemel09.msglauncher.domain.messengers.MessengerManager
import de.oemel09.msglauncher.ui.ItemTouchHelperCallback
import de.oemel09.msglauncher.ui.OnItemDragListener
import de.oemel09.msglauncher.ui.settings.MessengerAdapter

private const val PERMISSION_REQUEST_READ_CONTACTS = 1305
private const val START_SEARCH_DELAY = 250

class HomeFragment : Fragment(), OnItemDragListener, ContactAdapter.OnContactClickListener {

    private lateinit var homeViewModel: HomeViewModel

    private lateinit var rootLayout: View
    private lateinit var contactAdapter: ContactAdapter
    private lateinit var messengerManager: MessengerManager
    private lateinit var customMessengerDialog: Dialog
    private val searchStartHandler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        rootLayout = requireActivity().findViewById(R.id.main_container)
        contactAdapter = ContactAdapter(requireContext(), this)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val rvContacts = root.findViewById<RecyclerView>(R.id.home_rv_contacts)
        rvContacts.layoutManager = LinearLayoutManager(context)
        rvContacts.adapter = contactAdapter
        val dragDirs = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        val swipeDirs = ItemTouchHelper.LEFT
        val touchHelper = ItemTouchHelper(ItemTouchHelperCallback(dragDirs, swipeDirs, this))
        touchHelper.attachToRecyclerView(rvContacts)
        homeViewModel.getContacts().observe(viewLifecycleOwner, {
            contactAdapter.updateContacts(it)
        })

        val etSearch = root.findViewById<TextInputEditText>(R.id.home_tiet_search_contacts)
        etSearch.addTextChangedListener(searchWatcher)
        etSearch.setOnEditorActionListener { _: TextView, actionId: Int, _: KeyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val imm =
                    requireContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
                true
            }
            false
        }

        messengerManager = MessengerManager(requireContext())

        startReadingContacts()
        return root
    }

    private val searchWatcher = object : TextWatcher {
        override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
            searchStartHandler.removeCallbacksAndMessages(null)
            if (text!!.length >= 2) {
                searchStartHandler.postDelayed({
                    homeViewModel.loadContacts(
                        text.toString().trim(),
                        object : HomeViewModel.LoadContactListener {
                            override fun onContactsLoaded(oldSize: Int, newSize: Int) {
                                contactAdapter.isSearchResult = true
                                contactAdapter.notifyItemRangeRemoved(0, oldSize)
                                contactAdapter.notifyItemRangeInserted(0, newSize)
                            }
                        })
                }, START_SEARCH_DELAY.toLong())
            } else if (text.isEmpty()) {
                contactAdapter.isSearchResult = false
                readContacts()
            }
        }

        override fun afterTextChanged(s: Editable?) {}
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(
                    rootLayout,
                    R.string.read_contacts_permission_granted,
                    Snackbar.LENGTH_SHORT
                ).show()
                readContacts()
            } else {
                Snackbar.make(
                    rootLayout,
                    R.string.read_contacts_permission_denied,
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startReadingContacts() {
        if (isReadContactsPermissionGiven()) {
            readContacts()
        } else {
            requestReadContactsPermission()
        }
    }

    private fun readContacts() {
        homeViewModel.loadContacts(null, object : HomeViewModel.LoadContactListener {
            override fun onContactsLoaded(oldSize: Int, newSize: Int) {
                contactAdapter.notifyItemRangeRemoved(0, oldSize)
                contactAdapter.notifyItemRangeRemoved(oldSize, newSize)
            }
        })
    }

    private fun isReadContactsPermissionGiven(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestReadContactsPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
            Snackbar.make(
                rootLayout, R.string.read_contacts_access_required,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.ok) {
                showReadContactsPermissionRequest()
            }.show()
        } else {
            showReadContactsPermissionRequest()
        }
    }

    private fun showReadContactsPermissionRequest() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_CONTACTS),
            PERMISSION_REQUEST_READ_CONTACTS
        )
    }

    override fun onContactClick(position: Int) {
        val contact = homeViewModel.getContact(position)
        val opener = messengerManager.getOpener(contact)
        if (opener == null) {
            Snackbar.make(
                rootLayout,
                getString(R.string.home_no_messenger_found),
                Snackbar.LENGTH_SHORT
            )
                .show()
        } else {
            openMessenger(opener)
        }
    }

    override fun onAddContactClick(position: Int) {
        homeViewModel.addItem(position)
        contactAdapter.notifyItemChanged(position)
        Snackbar.make(
            rootLayout,
            getString(R.string.home_contact_added_to_list, homeViewModel.getContact(position).name),
            Snackbar.LENGTH_SHORT
        ).show()
    }

    override fun onOpenContactPageClick(position: Int) {
        val contact = homeViewModel.getContact(position)
        val contactUri = messengerManager.getContactUri(contact)
        if (contactUri == null) {
            Snackbar.make(
                rootLayout,
                getString(R.string.home_contact_not_found),
                Snackbar.LENGTH_SHORT
            )
                .show()
        } else {
            openContactPage(contactUri)
        }
    }

    override fun onMessengerIconClick(position: Int) {
        val builder = AlertDialog.Builder(requireContext())
        val contactPosition = position
        val selectedContact = homeViewModel.getContact(position)
        builder.setTitle(
            getString(
                R.string.home_select_custom_messenger_for_user,
                selectedContact.name
            )
        )
        val contentView = layoutInflater.inflate(R.layout.messenger_list_dialog, null)
        val recyclerView =
            contentView.findViewById<RecyclerView>(R.id.messenger_list_dialog_rv_messengers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val messengers = messengerManager.getAllApplicableMessengers(selectedContact) as MutableList
        val autoSelectMessenger =
            Messenger(MESSENGER_ID_AUTO, getString(R.string.home_select_messenger_automatically), 0)
        messengers.add(0, autoSelectMessenger)

        val messengerAdapter =
            MessengerAdapter(requireContext(), object : MessengerAdapter.OnMessengerClickListener {
                override fun onMessengerClick(position: Int) {
                    customMessengerDialog.dismiss()
                    homeViewModel.updateCustomMessenger(contactPosition, messengers[position])
                    contactAdapter.notifyItemChanged(contactPosition)
                }
            })
        messengerAdapter.updateMessengers(messengers)
        recyclerView.adapter = messengerAdapter
        builder.setView(contentView)

        customMessengerDialog = builder.create()
        customMessengerDialog.show()
    }

    override fun onItemDismiss(position: Int) {
        homeViewModel.removeItem(position)
        contactAdapter.notifyItemRemoved(position)
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int) {
        homeViewModel.moveItem(fromPosition, toPosition)
        contactAdapter.notifyItemMoved(fromPosition, toPosition)
    }

    private fun openMessenger(opener: Opener) {
        val messengerIntent = Intent(Intent.ACTION_VIEW)
        messengerIntent.setDataAndType(opener.contactUri, opener.mimeType)
        messengerIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(messengerIntent)
    }

    private fun openContactPage(contactUri: Uri) {
        val messengerIntent = Intent(Intent.ACTION_VIEW, contactUri)
        startActivity(messengerIntent)
    }
}
