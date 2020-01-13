package com.juliusbaer.premarket.ui.more

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import kotlinx.android.synthetic.main.item_more.view.*

class MoreAdapter(private val items: List<ItemMenu>, private val listener: (pos: Int) -> Unit): RecyclerView.Adapter<MoreAdapter.ViewHolder>() {
    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_more, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        private val text: TextView = itemView.text

        init {
            itemView.setOnClickListener {
                if (adapterPosition >= 0) {
                    listener(adapterPosition)
                }
            }
        }

        fun bind(itemMenu: ItemMenu) {
            val compoundDrawables = text.compoundDrawablesRelative
            text.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    itemView.context.getDrawable(itemMenu.imageRes),
                    compoundDrawables[1],
                    compoundDrawables[2],
                    compoundDrawables[3])
            text.setText(itemMenu.title)
        }
    }
}