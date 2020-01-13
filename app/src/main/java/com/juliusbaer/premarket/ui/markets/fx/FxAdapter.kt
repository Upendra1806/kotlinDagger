package com.juliusbaer.premarket.ui.markets.fx

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.ui.fragments.extentions.round
import com.juliusbaer.premarket.utils.FxMetals
import kotlinx.android.synthetic.main.item_currency.view.*
import kotlin.math.absoluteValue

class FxAdapter(val listener: (item: FxModel) -> Unit) : RecyclerView.Adapter<FxAdapter.ViewHolder>() {
    private var max: Double = 0.0

    private var items = listOf<FxModel>()

    init {
        setHasStableIds(true)
    }

    fun submitList(newItems: List<FxModel>, updateId: Int? = null) {
        val newMax = newItems.maxBy { it.priceChangePct.absoluteValue }?.priceChangePct?.absoluteValue
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return (newMax == null || max == newMax)
                        && items[oldItemPosition].id != updateId
                        && items[oldItemPosition] == newItems[newItemPosition]
            }

            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return if (updateId != null) true else null
            }
        })
        items = newItems
        newMax?.let { max = it }
        diff.dispatchUpdatesTo(this)
    }


    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty() && payloads[0] == true) {
            holder.bindUpdate(items[position])
            return
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.name
        private val lastTraded: TextView = itemView.lastTraded
        private val priceLayout: CardView = itemView.priceLayout

        private val positivePercent: TextView = itemView.positivePercent
        private val positivePercentLine: View = itemView.positivePercentLine
        private val positivePercentLineIndicator: View = itemView.positivePercentLineIndicator
        private val negativePercent: TextView = itemView.negativePercent
        private val negativePercentLine: View = itemView.negativePercentLine
        private val negativePercentLineIndicator: View = itemView.negativePercentLineIndicator

        init {
            itemView.setOnClickListener {
                if (adapterPosition >= 0) {
                    listener(items[adapterPosition])
                }
            }
        }

        fun bind(item: FxModel) {
            name.text = item.title
            FxMetals.values().firstOrNull {
                item.ticker?.startsWith(it.prefix, ignoreCase = true) == true
            }?.let {
                name.setCompoundDrawablesRelativeWithIntrinsicBounds(it.iconResId, 0, 0, 0)
            }
            bindUpdate(item)
        }

        fun bindUpdate(item: FxModel) {
            lastTraded.text = (item.lastTraded ?: 0.0).format(item.precision)

            val priceChangePct = item.priceChangePct
            val priceChangePctRounded = priceChangePct.times(100).round(2)
            when {
                priceChangePctRounded > 0 -> {
                    priceLayout.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.observatory))
                    lastTraded.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))

                    positivePercent.text = priceChangePct.formatPercent(itemView.resources)
                    setPositiveNegativeVisibility(isPositiveVisible = true, isNegativeVisible = false)

                    val params = positivePercentLineIndicator.layoutParams as LinearLayout.LayoutParams
                    params.weight = if (max > 0) (priceChangePct / max).toFloat() else 0f
                    positivePercentLineIndicator.layoutParams = params
                }
                priceChangePctRounded < 0 -> {
                    priceLayout.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.mandarianOrange))
                    lastTraded.setTextColor(ContextCompat.getColor(itemView.context, R.color.white))

                    negativePercent.text = priceChangePct.formatPercent(itemView.resources)
                    setPositiveNegativeVisibility(isPositiveVisible = false, isNegativeVisible = true)

                    val params = negativePercentLineIndicator.layoutParams as LinearLayout.LayoutParams
                    params.weight = if (max > 0) (priceChangePct.absoluteValue / max).toFloat() else 0f
                    negativePercentLineIndicator.layoutParams = params
                }
                else -> {
                    priceLayout.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.background))
                    lastTraded.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_grey_darker))
                    setPositiveNegativeVisibility(isPositiveVisible = false, isNegativeVisible = false)
                }
            }
        }

        private fun setPositiveNegativeVisibility(isPositiveVisible: Boolean, isNegativeVisible: Boolean) {
            positivePercent.isVisible = isPositiveVisible
            positivePercentLine.isVisible = isPositiveVisible

            negativePercent.isVisible = isNegativeVisible
            negativePercentLine.isVisible = isNegativeVisible
        }
    }

    fun updateItem(update: ProductUpdateModel): Boolean {
        val pos = items.indexOfFirst { it.id == update.id }
        return if (pos >= 0) {
            val model = items[pos]
            model.updateFromSocketModel(update)

            submitList(items, model.id)
            true
        } else {
            false
        }
    }
}

