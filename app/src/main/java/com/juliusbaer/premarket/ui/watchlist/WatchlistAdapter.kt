package com.juliusbaer.premarket.ui.watchlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.daimajia.swipe.SwipeLayout
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.ProductUpdateModel
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.ui.fragments.extentions.round
import com.juliusbaer.premarket.utils.FxMetals
import kotlinx.android.synthetic.main.item_watchlist.view.*
import kotlinx.android.synthetic.main.item_watchlist_header_bid_ask.view.*

class WatchlistAdapter(val listener: OnExpandableClickListener) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        private const val HEADER_UNDERLYING = 10
        private const val ITEM_UNDERLYING = 11

        private const val HEADER_WARRANT = 20
        private const val ITEM_WARRANT = 21

        private const val HEADER_INDEX = 30
        private const val ITEM_INDEX = 31

        private const val HEADER_FX = 40
        private const val ITEM_FX = 41

        private const val HEADER_METALS = 50
        private const val ITEM_METALS = 51

        private val headerTypes = listOf(HEADER_UNDERLYING, HEADER_WARRANT, HEADER_INDEX, HEADER_FX, HEADER_METALS)
    }

    interface OnExpandableClickListener {
        fun onDelete(id: Int)
        fun onSetAlert(item: WatchListItem)
        fun onItemClick(item: WatchListItem)
    }

    var groups = listOf<List<WatchListItem>>()
        private set

    fun submitList(groupsUpd: List<List<WatchListItem>>) {
        groups = groupsUpd

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return groups.sumBy { it.size } + groups.count { it.isNotEmpty() }
    }

    override fun getItemViewType(position: Int): Int {
        var absPos = 0
        for (i in groups.indices) {
            val groupSize = groups[i].size
            if (groupSize > 0) {
                if (position == absPos) {
                    return headerTypes[i]
                } else {
                    if (position > absPos && position <= absPos + groupSize) {
                        return headerTypes[i] + 1
                    }
                    absPos += groupSize
                }
                absPos++
            }
        }
        return position
    }

    private fun getItem(position: Int): WatchListItem? {
        var absPos = 0
        for (group in groups) {
            if (group.isNotEmpty()) {
                ++absPos
                if (position >= absPos && position < absPos + group.size) {
                    return group[position - absPos]
                }
                absPos += group.size
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            HEADER_UNDERLYING -> BidAskHeaderViewHolder(inflater.inflate(R.layout.item_watchlist_header_bid_ask, parent, false), R.string.underlying)
            HEADER_INDEX -> HeaderViewHolder(inflater.inflate(R.layout.item_watchlist_header, parent, false), R.string.index)
            HEADER_WARRANT -> BidAskHeaderViewHolder(inflater.inflate(R.layout.item_watchlist_header_bid_ask, parent, false), R.string.warrant)
            HEADER_FX -> FxHeaderViewHolder(inflater.inflate(R.layout.item_watchlist_header_bid_ask, parent, false), R.string.fx)
            HEADER_METALS -> FxHeaderViewHolder(inflater.inflate(R.layout.item_watchlist_header_bid_ask, parent, false), R.string.precious_metals)
            ITEM_UNDERLYING -> UnderlyingViewHolder(inflater.inflate(R.layout.item_watchlist, parent, false))
            ITEM_INDEX -> IndexViewHolder(inflater.inflate(R.layout.item_watchlist, parent, false))
            ITEM_WARRANT -> WarrantViewHolder(inflater.inflate(R.layout.item_watchlist, parent, false))
            ITEM_FX -> FxViewHolder(inflater.inflate(R.layout.item_watchlist, parent, false))
            ITEM_METALS -> MetalViewHolder(inflater.inflate(R.layout.item_watchlist, parent, false))
            else -> throw IllegalStateException("Unknown viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is UnderlyingViewHolder -> holder.bind(getItem(position) as WatchListItem.Underlying)
            is IndexViewHolder -> holder.bind(getItem(position) as WatchListItem.Index)
            is WarrantViewHolder -> holder.bind(getItem(position) as WatchListItem.Warrant)
            is FxViewHolder -> holder.bind(getItem(position) as WatchListItem.Fx)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            if (payloads[0] == true) {
                when (holder) {
                    is UnderlyingViewHolder -> {
                        holder.bindUpdate(getItem(position) as WatchListItem.Underlying)
                        return
                    }
                    is IndexViewHolder -> {
                        holder.bindUpdate(getItem(position) as WatchListItem.Index)
                        return
                    }
                    is WarrantViewHolder -> {
                        holder.bindUpdate(getItem(position) as WatchListItem.Warrant)
                        return
                    }
                    is FxViewHolder -> {
                        holder.bindUpdate(getItem(position) as WatchListItem.Fx)
                        return
                    }
                }
            }
        }
        super.onBindViewHolder(holder, position, payloads)
    }

    fun updateItem(productModel: ProductUpdateModel): WatchListItem? {
        var absPos = 0
        for (group in groups) {
            if (group.isNotEmpty()) {
                ++absPos
                val pos = group.indexOfFirst { it.id == productModel.id }
                if (pos >= 0) {
                    val item = group[pos]
                    when (item) {
                        is WatchListItem.Underlying -> item.model.updateFromSocketModel(productModel)
                        is WatchListItem.Index -> item.model.updateFromSocketModel(productModel)
                        is WatchListItem.Warrant -> item.model.updateFromSocketModel(productModel)
                        is WatchListItem.Fx -> item.model.updateFromSocketModel(productModel)
                    }
                    notifyItemChanged(absPos + pos, true)

                    return item
                } else {
                    absPos += group.size
                }
            }
        }
        return null
    }

    private abstract inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        protected val isNotification: ImageView = itemView.isNotification
        protected val name: TextView = itemView.txtName
        protected val priceChange: TextView = itemView.txtPriceChange
        protected val value1: TextView = itemView.value1
        protected val value2: TextView = itemView.value2
        protected val price: TextView = itemView.txtPrice
        private val swipe = itemView.swipe

        init {
            itemView.items.setOnClickListener {
                if (adapterPosition >= 0) {
                    getItem(adapterPosition)?.let { listener.onItemClick(it) }
                }
            }
            swipe.addDrag(SwipeLayout.DragEdge.Left, swipe.bottom_wrapper)
            swipe.addSwipeListener(object : SwipeLayout.SwipeListener {
                override fun onOpen(layout: SwipeLayout?) {
                    itemView.items.setOnClickListener { swipe.close() }
                    itemView.del.setOnClickListener {
                        if (adapterPosition >= 0) {
                            getItem(adapterPosition)?.let { listener.onDelete(it.id) }
                        }
                    }
                    itemView.alarm.setOnClickListener {
                        if (adapterPosition >= 0) {
                            getItem(adapterPosition)?.let { listener.onSetAlert(it) }
                        }
                    }
                }

                override fun onUpdate(layout: SwipeLayout?, leftOffset: Int, topOffset: Int) {
                }

                override fun onStartOpen(layout: SwipeLayout?) {
                }

                override fun onStartClose(layout: SwipeLayout?) {
                }

                override fun onHandRelease(layout: SwipeLayout?, xvel: Float, yvel: Float) {
                }

                override fun onClose(layout: SwipeLayout?) {
                    itemView.items.setOnClickListener {
                        if (adapterPosition >= 0) {
                            getItem(adapterPosition)?.let { listener.onItemClick(it) }
                        }
                    }
                }

            })
            isNotification.setColorFilter(ContextCompat.getColor(itemView.context, R.color.rouge))
        }

        protected fun setPriceChange(priceChangePct: Double) {
            priceChange.text = priceChangePct.formatPercent(itemView.resources)
            val priceChangePctRounded = priceChangePct.times(100).round(2)
            val color = when {
                priceChangePctRounded < 0 -> R.color.rouge
                priceChangePctRounded > 0 -> R.color.blueGreen
                else -> R.color.text_grey_darker
            }
            priceChange.setTextColor(ContextCompat.getColor(itemView.context, color))
        }
    }

    private inner class WarrantViewHolder(itemView: View) : ViewHolder(itemView) {
        private val txtPutCall: TextView = itemView.txtPutCall

        fun bind(item: WatchListItem.Warrant) {
            isNotification.isVisible = item.notificationReceived == true
            name.text = item.title
            if (!item.model.strikeType.isNullOrEmpty()) {
                txtPutCall.text = item.model.strikeType
                txtPutCall.isVisible = true
            } else {
                txtPutCall.isVisible = false
            }
            bindUpdate(item)
        }

        fun bindUpdate(item: WatchListItem.Warrant) {
            value2.text = item.model.priceAsk.format(2)
            value1.text = item.model.priceBid.format(2)
            price.text = item.model.lastTraded?.format(2)

            setPriceChange(item.model.priceChangePct)
        }
    }

    private inner class UnderlyingViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(item: WatchListItem.Underlying) {
            isNotification.isVisible = item.notificationReceived == true
            name.text = item.title

            bindUpdate(item)
        }

        fun bindUpdate(item: WatchListItem.Underlying) {
            value1.text = item.model.priceBid.format(2)
            value2.text = item.model.priceAsk.format(2)
            price.text = item.model.lastTraded?.format(2)

            setPriceChange(item.model.priceChangePct)
        }
    }

    private open inner class FxViewHolder(itemView: View) : ViewHolder(itemView) {
        @CallSuper
        open fun bind(item: WatchListItem.Fx) {
            isNotification.isVisible = item.notificationReceived == true
            name.text = item.title

            bindUpdate(item)
        }

        fun bindUpdate(item: WatchListItem.Fx) {
            value1.text = item.model.maxLastTraded?.format(item.model.precision)
            value2.text = item.model.minLastTraded?.format(item.model.precision)
            price.text = item.model.lastTraded?.format(item.model.precision)

            setPriceChange(item.model.priceChangePct)
        }
    }

    private inner class MetalViewHolder(itemView: View) : FxViewHolder(itemView) {
        override fun bind(item: WatchListItem.Fx) {
            super.bind(item)

            FxMetals.values().firstOrNull {
                item.ticker?.startsWith(it.prefix, ignoreCase = true) == true
            }?.let {
                name.setCompoundDrawablesRelativeWithIntrinsicBounds(it.iconResId, 0, 0, 0)
            }
        }
    }

    private inner class IndexViewHolder(itemView: View) : ViewHolder(itemView) {
        fun bind(item: WatchListItem.Index) {
            isNotification.isVisible = item.notificationReceived == true
            name.text = item.title

            bindUpdate(item)
        }

        fun bindUpdate(item: WatchListItem.Index) {
            price.text = item.model.lastTraded?.format(2)

            setPriceChange(item.model.priceChangePct)
        }
    }

    private open inner class HeaderViewHolder(itemView: View, @StringRes titleResId: Int) : RecyclerView.ViewHolder(itemView) {
        private val textGroup = itemView.textGroup

        init {
            textGroup.setText(titleResId)
        }
    }

    private open inner class BidAskHeaderViewHolder(itemView: View, @StringRes titleResId: Int) : HeaderViewHolder(itemView, titleResId) {
        protected val bid: TextView = itemView.bid
        protected val ask: TextView = itemView.ask
    }

    private inner class FxHeaderViewHolder(itemView: View, @StringRes titleResId: Int) : BidAskHeaderViewHolder(itemView, titleResId) {
        init {
            bid.setText(R.string.high)
            ask.setText(R.string.low)
        }
    }
}