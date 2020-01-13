package com.juliusbaer.premarket.ui.adapters

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.ui.fragments.extentions.format
import com.juliusbaer.premarket.ui.fragments.extentions.formatDate
import com.juliusbaer.premarket.ui.fragments.extentions.formatPercent
import com.juliusbaer.premarket.utils.Constants
import kotlinx.android.synthetic.main.item_warrant.view.*

class WarrantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val txtName: TextView = itemView.txtNameWarrants
    private val txtPutCall: TextView = itemView.txtPutCall
    private val txtBid: TextView = itemView.txtBid
    private val txtAsk: TextView = itemView.txtAsk
    private val txtPercents: TextView = itemView.txtDate
    private val warrantCard: ConstraintLayout = itemView.warrantsCard
    private val imgStar: ImageView = itemView.imgStar
    private val txtExpiry: TextView = itemView.txtExpiry
    private val txtStrike: TextView = itemView.txtStrike

    fun bind(item: WarrantModel, listener: WarrantsOnClickListener) {
        txtName.text = item.title
        txtPutCall.text = item.strikeType
        txtBid.text = item.priceBid.format(2)
        txtAsk.text = item.priceAsk.format(2)
        txtExpiry.text = item.exerciseDate?.formatDate(Constants.DATE_ONLY_FORMAT) ?: ""
        txtStrike.text = if (!item.strikeType.isNullOrEmpty()) item.strikeLevel?.format(2) else ""
        imgStar.setImageResource(if (item.isTop == true) R.drawable.ic_star_warrant_fill else R.drawable.ic_star_warrant)
        txtPercents.setTextColor(ContextCompat.getColor(itemView.context, if (item.priceChangePct < 0) R.color.rouge else R.color.blueGreen))
        txtPercents.text = item.priceChangePct.formatPercent(itemView.resources)

        warrantCard.setOnClickListener {
            listener.onClick(item)
        }
    }

    fun clear() {
        txtName.text = ""
        txtPutCall.text = ""
        txtBid.text = ""
        txtAsk.text = ""
        txtExpiry.text = ""
        txtStrike.text = ""
        imgStar.setImageResource(R.drawable.ic_star_warrant)
        txtPercents.text = ""
        warrantCard.setOnClickListener(null)
    }
}