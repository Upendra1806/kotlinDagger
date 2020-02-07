package com.juliusbaer.premarket.ui.watchlist

import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.FxModel
import com.juliusbaer.premarket.models.serverModels.IndexModel
import com.juliusbaer.premarket.models.serverModels.UnderlyingModel
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.utils.ArrayAdapterNormalized
import com.juliusbaer.premarket.utils.Constants

class WatchListSearchAdapter(context: Context, resource: Int, objects: List<WatchListSearchModel>) :
        ArrayAdapterNormalized<WatchListSearchModel>(context, resource, objects), Filterable {

    var allData = objects
    var filterList: List<WatchListSearchModel>? = objects


    override fun getItemId(position: Int): Long {
        return getItem(position)!!.id.toLong()
    }

    override val emptyItem = WatchListSearchModel(-1, context.getString(R.string.no_results), "", null)
    override fun isEnabled(position: Int): Boolean {
        return getItem(position)!!.id > 0
    }

    override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()
                if (constraint == null || constraint.length == 0) {
                    filterResults.count = allData.size
                    filterResults.values = allData
                } else {
                    val resultsModel: MutableList<WatchListSearchModel> = ArrayList()
                    val searchStr = constraint.toString().toLowerCase()
                    for (item in allData) {

                        when (item.type) {
                            Constants.ElementType.INDEX -> {
                                val model = item.item as IndexModel
                                if (item.title.toLowerCase().contains(searchStr) ||
                                        model.valor.toLowerCase().contains(searchStr)
                                        || model.ticker!!.toLowerCase().contains(searchStr)
                                        || model.isin!!.toLowerCase().contains(searchStr))
                                    resultsModel.add(item)
                            }
                            Constants.ElementType.UNDERLYING -> {
                                val model = item.item as UnderlyingModel
                                if (item.title.toLowerCase().contains(searchStr) ||
                                        model.valor.toLowerCase().contains(searchStr)
                                        || model.ticker!!.toLowerCase().contains(searchStr)
                                        || model.isin!!.toLowerCase().contains(searchStr))
                                    resultsModel.add(item)
                            }
                            Constants.ElementType.WARRANTS -> {
                                val model = item.item as WarrantModel
                                if (item.title.toLowerCase().contains(searchStr) ||
                                        model.valor!!.toLowerCase().contains(searchStr)
                                        || model.ticker!!.toLowerCase().contains(searchStr)
                                        || model.isin!!.toLowerCase().contains(searchStr))
                                    resultsModel.add(item)
                            }
                            Constants.ElementType.FX -> {
                                val model = item.item as FxModel
                                if (item.title.toLowerCase().contains(searchStr) ||
                                        model.valor!!.toLowerCase().contains(searchStr)
                                        || model.ticker!!.toLowerCase().contains(searchStr)
                                        || model.name!!.toLowerCase().contains(searchStr))
                                    resultsModel.add(item)
                            }
                            Constants.ElementType.CURRENCY -> {
                                val model = item.item as FxModel
                                if (item.title.toLowerCase().contains(searchStr) ||
                                        model.valor!!.toLowerCase().contains(searchStr)
                                        || model.ticker!!.toLowerCase().contains(searchStr)
                                        || model.name!!.toLowerCase().contains(searchStr))
                                    resultsModel.add(item)
                            }
                        }
                        filterResults.count = resultsModel.size
                        filterResults.values = resultsModel
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filterList = results!!.values as List<WatchListSearchModel>?
                filterList?.let {
                    clear()
                    addAll(it)
                }
                notifyDataSetChanged()
            }
        }
    }
}