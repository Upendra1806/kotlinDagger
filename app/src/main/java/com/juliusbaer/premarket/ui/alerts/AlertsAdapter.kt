package com.juliusbaer.premarket.ui.alerts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.AlertsModel

import kotlinx.android.synthetic.main.setting_card.view.*

class AlertsAdapter(private val listener: AlertsClickListener) : RecyclerView.Adapter<AlertsAdapter.ViewHolder>() {
    private var items = listOf<AlertsModel>()

    val type: Int = 1
    val footer: Int = 2

    fun submitList(items: List<AlertsModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return NormalHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.setting_card, parent, false))
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        try {
            when (holder) {
                is NormalHolder -> holder.bind(items[position], listener)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    inner class NormalHolder(itemView: View) : ViewHolder(itemView) {
        private val trash: ImageView = itemView.imgTrash
        private var imgSettings = itemView.iClick
        private val title: TextView = itemView.txtTitle

        init {
            imgSettings.setOnClickListener {
                if (adapterPosition >= 0) {
                    listener.onItemClick(items[adapterPosition])
                }
            }
        }

        fun bind(model: AlertsModel, listener: AlertsClickListener) {
            title.text = model.productName
            trash.setOnClickListener { listener.onDeleteAlert(model.productId) }
        }
    }

    open class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        open fun bind(newsModel: AlertsModel) {
        }
    }

    interface AlertsClickListener {
        fun onItemClick(item: AlertsModel)
        fun onDeleteAlert(productId: Int)
    }
}