package com.juliusbaer.premarket.ui.markets.indices

import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatDate
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.utils.Constants
import com.juliusbaer.premarket.utils.Constants.DATE_FORMAT
import kotlinx.android.synthetic.main.markets_card.view.*
import kotlin.math.abs

class MarketsIndicesAdapter(val listener: OnItemClick) : ListAdapter<IndexModel, MarketsIndicesAdapter.ViewHolder>(
        AsyncDifferConfig.Builder(DiffCallback())
                .setBackgroundThreadExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
                .build()) {

    private var max: Double = 0.0
    private var min: Double = 0.0

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun submitList(list: List<IndexModel>?) {
        max = list?.maxBy { it.priceChangePct }?.priceChangePct?.times(100) ?: 1.0
        min = list?.minBy { it.priceChangePct }?.priceChangePct?.times(100) ?: 1.0

        super.submitList(list)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.markets_card, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position), listener, max, min)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtAsk: TextView = itemView.txtAsk1
        private val txtDateTime: TextView = itemView.txtDateTime
        private val txtDateTime2: TextView = itemView.txtDateTime2
        private val txtPercents: TextView = itemView.txtPercent1
        private val txtName: TextView = itemView.txtNameMarket
        var background: CardView = itemView.cardView2

        fun bind(index: IndexModel, listener: OnItemClick, max: Double, min: Double) {
            //Removed the condition to check for hasDetails
            itemView.setOnClickListener { listener.onClick(index) }

            txtPercents.text = index.priceChangePct.formatPercent(itemView.resources)
            calculateAlpha(index.priceChangePct.times(100), max, min)
            txtName.text = index.marketsTitle
            txtAsk.text = index.lastTraded?.format(2)
            txtDateTime2.text = index.date?.formatDate(DATE_FORMAT)?.replaceBefore(" ", "") ?: ""
            txtDateTime.text = index.date?.formatDate(Constants.DATE_ONLY_FORMAT) ?: ""
        }

        private fun calculateAlpha(priceChangePct: Double, max: Double, min: Double) {

            when {
                priceChangePct > 0 -> {
                    background.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.observatory))
                    val alpha = ((0.6 + (1 - 0.6) * abs(priceChangePct / min))).toFloat()
                    background.alpha = alpha
                    val textColor = ContextCompat.getColor(itemView.context, R.color.white)
                    txtAsk.setTextColor(textColor)
                    txtName.setTextColor(textColor)
                    txtPercents.setTextColor(textColor)
                    txtDateTime.setTextColor(textColor)
                }
                priceChangePct < 0 -> {
                    val alpha = ((0.6 + (1 - 0.6) * abs(priceChangePct / min))).toFloat()
                    background.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.negativeBgColor))
                    background.alpha = alpha
                    val textColor = ContextCompat.getColor(itemView.context, R.color.white)
                    txtAsk.setTextColor(textColor)
                    txtName.setTextColor(textColor)
                    txtPercents.setTextColor(textColor)
                    txtDateTime.setTextColor(textColor)
                }
                else -> {
                    background.alpha = 1f
                    background.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.white))
                    val textColor = ContextCompat.getColor(itemView.context, R.color.black)
                    txtAsk.setTextColor(textColor)
                    txtName.setTextColor(textColor)
                    txtPercents.setTextColor(textColor)
                    txtDateTime.setTextColor(textColor)
                }
            }
        }
    }

    fun updateItem(update: ProductUpdateModel): Boolean {
        for (i in 0 until itemCount) {
            val model = getItem(i)
            if (model.id == update.id) {
                model.updateFromSocketModel(update)

                notifyItemChanged(i)
                return true
            }
        }
        return false
    }

    interface OnItemClick {
        fun onClick(index: IndexModel)
    }

    private class DiffCallback : DiffUtil.ItemCallback<IndexModel>() {
        override fun areItemsTheSame(oldItem: IndexModel, newItem: IndexModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: IndexModel, newItem: IndexModel): Boolean {
            return oldItem == newItem
        }
    }
}

