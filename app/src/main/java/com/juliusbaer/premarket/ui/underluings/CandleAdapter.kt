package com.juliusbaer.premarket.ui.underluings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.CandleStickModel
import com.juliusbaer.premarket.ui.customViews.CandleStickChart
import com.juliusbaer.premarket.ui.customViews.DividerView
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import kotlinx.android.synthetic.main.market_card_candle.view.*

class CandleAdapter(private val listener: OnCandleClickListener) : RecyclerView.Adapter<CandleAdapter.ViewHolder>() {
    var zeroPercentPosition: Int = 50

    var min: Int = 0
        private set
    var max: Int = 0
        private set

    var items = listOf<CandleStickModel>()
        private set

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].productId.toLong()
    }

    fun submitList(newItems: List<CandleStickModel>, newMin: Int? = null, newMax: Int? = null, changedId: Int? = null) {
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].productId == newItems[newItemPosition].productId
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return min == newMin
                        && max == newMax
                        && items[oldItemPosition].productId != changedId
                        && items[oldItemPosition] == newItems[newItemPosition]
            }
            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
        })
        items = newItems
        newMax?.let { max = newMax }
        newMin?.let { min = newMin }
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context)
                .inflate(R.layout.market_card_candle, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.txtTitle
        private val txtPercentCard: TextView = itemView.txtProcent
        private val bid: TextView = itemView.txtRatio
        private val candleGraph: CandleStickChart = itemView.candleStickChart
        private val grid: DividerView = itemView.grid

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    listener.onClick(items[adapterPosition])
                }
            }
        }

        fun bind(item: CandleStickModel) {
            grid.selectedPos = zeroPercentPosition.toFloat()

            name.text = item.title

            txtPercentCard.text = item.priceChangePct.formatPercent(itemView.resources)

            val colorResId = when {
                item.priceChangePct < 0 -> R.color.rouge
                item.priceChangePct > 0 -> R.color.blueGreen
                else -> R.color.grey_1
            }
            txtPercentCard.setTextColor(ContextCompat.getColor(itemView.context, colorResId))

            bid.text = item.lastTraded.format(2)
            candleGraph.setData(item, min, max)
        }
    }

    interface OnCandleClickListener {
        fun onClick(item: CandleStickModel)
    }
}