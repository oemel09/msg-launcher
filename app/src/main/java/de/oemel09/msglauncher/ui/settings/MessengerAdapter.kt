package de.oemel09.msglauncher.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.oemel09.msglauncher.R
import de.oemel09.msglauncher.domain.messengers.Messenger

class MessengerAdapter(private val context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var messengers: List<Messenger>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(context).inflate(R.layout.messenger_item, parent, false)
        return ViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as ViewHolder
        val messenger = messengers[position]
        viewHolder.tvName.text = messenger.name
        val icon = context.packageManager.getApplicationIcon(messenger.id)
        viewHolder.ivIcon.setImageDrawable(icon)
    }

    override fun getItemCount(): Int {
        return messengers.size
    }

    fun updateMessengers(messengers: List<Messenger>) {
        this.messengers = messengers
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal val ivIcon: ImageView = itemView.findViewById(R.id.iv_messenger_icon)
        internal val tvName: TextView = itemView.findViewById(R.id.tv_messenger_name)
    }
}
