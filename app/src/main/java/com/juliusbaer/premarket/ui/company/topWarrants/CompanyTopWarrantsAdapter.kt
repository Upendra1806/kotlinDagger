package com.juliusbaer.premarket.ui.company.topWarrants

import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.ui.adapters.WarrantViewHolder
import com.juliusbaer.premarket.ui.adapters.WarrantsOnClickListener

class CompanyTopWarrantsAdapter(private val listener: WarrantsOnClickListener) : ListAdapter<WarrantModel, WarrantViewHolder>(
        AsyncDifferConfig.Builder(DiffCallback())
                .setBackgroundThreadExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .build()
) {
    override fun onBindViewHolder(holder: WarrantViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) holder.bind(item, listener) else holder.clear()
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id?.toLong() ?: position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarrantViewHolder =
            WarrantViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_warrant, parent, false))


    fun updateItem(update: ProductUpdateModel): Boolean {
        for (i in 0 until itemCount) {
            val model = getItem(i)
            if (model?.id == update.id) {
                model.updateFromSocketModel(update)
                notifyItemChanged(i)
                return true
            }
        }
        return false
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