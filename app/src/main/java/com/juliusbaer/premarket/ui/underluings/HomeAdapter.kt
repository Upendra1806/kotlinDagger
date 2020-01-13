package com.juliusbaer.premarket.ui.underluings

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.IUserStorage
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.ui.fragments.extentions.round
import kotlinx.android.synthetic.main.market_card_square.view.*
import java.util.*
import kotlin.math.abs


class HomeAdapter(private val storage: IUserStorage,
                  private val listener: OnItemClickListener) : RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    var items = listOf<UnderlyingModel>()
        private set
    private var max: Double = 0.0
    private var min: Double = 0.0

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return items[position].id.toLong()
    }

    fun submitList(newItems: List<UnderlyingModel>, changedId: Int? = null) {
        val newMax = newItems.maxBy { it.priceChangePct }?.priceChangePct ?: 0.0
        val newMin = newItems.minBy { it.priceChangePct }?.priceChangePct ?: 0.0
        val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return items[oldItemPosition].id == newItems[newItemPosition].id
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return min == newMin
                        && max == newMax
                        && items[oldItemPosition].id != changedId
                        && items[oldItemPosition] == newItems[newItemPosition]
            }

            override fun getOldListSize() = items.size
            override fun getNewListSize() = newItems.size
        })
        min = newMin
        max = newMax
        items = newItems
        diff.dispatchUpdatesTo(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.market_card_square, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.txtName
        private val txtPercentCard: TextView = itemView.txtPercent1
        private val bid: TextView = itemView.txtBid
        private var background: ConstraintLayout = itemView.cardView2

        init {
            val gradientDrawable = GradientDrawable()
            gradientDrawable.shape = GradientDrawable.RECTANGLE
            gradientDrawable.cornerRadius = itemView.resources.getDimension(R.dimen.underlying_square_radius)
            background.background = gradientDrawable

            itemView.setOnClickListener {
                if (adapterPosition >= 0) {
                    listener.onClick(items[adapterPosition])
                }
            }
        }

        fun bind(item: UnderlyingModel) {
            calculateAlpha(item)

            name.text = item.title
            txtPercentCard.text = item.priceChangePct.formatPercent(itemView.resources, false)
            bid.text = item.lastTraded?.format(2)
        }

        private fun calculateAlpha(item: UnderlyingModel) {
            val formattedPrice = item.priceChangePct.times(100).round(2)
            when {
                formattedPrice > 0 -> {
                    (background.background as GradientDrawable).setColor(ContextCompat.getColor(itemView.context, R.color.observatory))

                    val alpha = ((0.6 + (1 - 0.6) * abs(item.priceChangePct / max))).toFloat()
                    background.alpha = alpha

                    bid.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                    txtPercentCard.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                }
                formattedPrice < 0 -> {
                    (background.background as GradientDrawable).setColor(ContextCompat.getColor(itemView.context, R.color.negativeBgColor))

                    val alpha = ((0.6 + (1 - 0.6) * abs(item.priceChangePct / min))).toFloat()
                    background.alpha = alpha

                    bid.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                    txtPercentCard.setTextColor(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                }
                else -> {
                    (background.background as GradientDrawable).setColor(ContextCompat.getColor(itemView.context, R.color.white))
                    background.alpha = 1f

                    bid.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    name.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                    txtPercentCard.setTextColor(ContextCompat.getColor(itemView.context, R.color.black))
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onClick(model: UnderlyingModel)
    }

    fun updateItem(socketModel: ProductUpdateModel): Boolean {
        val oldIndex = items.indexOfFirst { it.id == socketModel.id }
        return if (oldIndex >= 0 && !items[oldIndex].isEqual(socketModel)) {
            if (items[oldIndex].priceChangePct != socketModel.priceChangePct && !storage.getAlphabetic()) {
                val newList = ArrayList(items)
                newList[oldIndex].updateFromSocketModel(socketModel)

                if (!storage.getAlphabetic()) {
                    newList.sortWith(compareByDescending<UnderlyingModel> { it.priceChangePct }.thenBy { it.title.toLowerCase() })
                }
                submitList(newList, newList[oldIndex].id)
            } else {
                (items as MutableList)[oldIndex].updateFromSocketModel(socketModel)
                notifyItemChanged(oldIndex)
            }
            true
        } else {
            false
        }
    }
}