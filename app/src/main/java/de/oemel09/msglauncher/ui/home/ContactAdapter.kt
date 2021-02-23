package de.oemel09.msglauncher.ui.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.oemel09.msglauncher.R
import de.oemel09.msglauncher.domain.contacts.Contact

class ContactAdapter(
    private val context: Context,
    private val onContactClickListener: OnContactClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var contacts: List<Contact>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(context).inflate(R.layout.contact_item, parent, false)
        return ViewHolder(rootView, onContactClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val contact = contacts[position]
        val viewHolder = holder as ViewHolder
        viewHolder.tvName.text = contact.name
    }

    override fun getItemCount(): Int {
        return contacts.size
    }

    fun updateContacts(contacts: List<Contact>) {
        this.contacts = contacts
    }

    class ViewHolder(itemView: View, private val onContactClickListener: OnContactClickListener) :
        RecyclerView.ViewHolder(
            itemView
        ), View.OnClickListener {

        internal val tvName = itemView.findViewById<TextView>(R.id.tv_contact_name)

        init {
            itemView.findViewById<View>(R.id.contact_item_root).setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            onContactClickListener.onClick(adapterPosition)
        }

    }

    interface OnContactClickListener {
        fun onClick(position: Int)
    }
}
