package de.oemel09.msglauncher.ui.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.oemel09.msglauncher.R
import de.oemel09.msglauncher.domain.messengers.MESSENGER_ID_AUTO
import de.oemel09.msglauncher.domain.messengers.Messenger

class MessengerAdapter(
    private val context: Context,
    private val onMessengerClickListener: OnMessengerClickListener?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private lateinit var messengers: List<Messenger>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val rootView = LayoutInflater.from(context).inflate(R.layout.messenger_item, parent, false)
        return MessengerViewHolder(rootView, onMessengerClickListener)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder = holder as MessengerViewHolder
        val messenger = messengers[position]
        viewHolder.tvName.text = messenger.name
        if (messenger.id == MESSENGER_ID_AUTO) {
            viewHolder.ivIcon.setImageResource(R.drawable.ic_automatic)
        } else {
            val icon = context.packageManager.getApplicationIcon(messenger.id)
            viewHolder.ivIcon.setImageDrawable(icon)
        }
    }

    override fun getItemCount(): Int {
        return messengers.size
    }

    fun updateMessengers(messengers: List<Messenger>) {
        this.messengers = messengers
    }

    class MessengerViewHolder(
        itemView: View,
        private val onMessengerClickListener: OnMessengerClickListener?
    ) : RecyclerView.ViewHolder(itemView) {
        internal val ivIcon =
            itemView.findViewById<ImageView>(R.id.messenger_item_iv_messenger_icon)
        internal val tvName = itemView.findViewById<TextView>(R.id.messenger_tv_messenger_name)

        init {
            itemView.setOnClickListener {
                onMessengerClickListener?.onMessengerClick(adapterPosition)
            }
        }
    }

    interface OnMessengerClickListener {
        fun onMessengerClick(position: Int)
    }
}
