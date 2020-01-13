package com.juliusbaer.premarket.ui.mostActive

import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import kotlinx.android.synthetic.main.most_active_card.view.*

class MostActiveAdapter(val listener: OnItemClick) : ListAdapter<WarrantModel, MostActiveAdapter.ViewHolder>(
        AsyncDifferConfig.Builder(DiffCallback())
                .setBackgroundThreadExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .build()) {
    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.most_active_card, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), listener)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mostItem = itemView.mostItem
        private val title = itemView.txtTitle
        private val txtPosition = itemView.txtPosition
        private val txtLastTraded = itemView.txtAsk

        fun bind(item: WarrantModel, listener: OnItemClick) {
            title.text = item.title
            txtPosition.text = (adapterPosition + 1).toString()
            txtLastTraded.text = item.tradedVolume?.toString()?:""
            mostItem.setOnClickListener { listener.onClick(item) }
        }
    }

    interface OnItemClick {
        fun onClick(item: WarrantModel)
    }

    private class DiffCallback : DiffUtil.ItemCallback<WarrantModel>() {
        override fun areItemsTheSame(oldItem: WarrantModel, newItem: WarrantModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WarrantModel, newItem: WarrantModel): Boolean {
            return oldItem == newItem
        }
    }
}

