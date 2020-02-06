package com.juliusbaer.premarket.ui.watchlist

import android.content.Context
import android.widget.Filter
import android.widget.Filterable
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.utils.ArrayAdapterNormalized

class WatchListSearchAdapter(context: Context, resource: Int, objects: List<WatchListSearchModel>) :
        ArrayAdapterNormalized<WatchListSearchModel>(context, resource, objects),Filterable {

    var allData = objects
    var filterList: List<WatchListSearchModel>? = objects


    override fun getItemId(position: Int): Long {
        return getItem(position)!!.id.toLong()
    }

    override val emptyItem = WatchListSearchModel(-1, context.getString(R.string.no_results), "", null)
    override fun isEnabled(position: Int): Boolean {
        return getItem(position)!!.id > 0
    }

    /*override fun getFilter(): Filter {

        return object : Filter() {
            override fun performFiltering(constraint: CharSequence): FilterResults {
                val filterResults = FilterResults()
                if (constraint == null || constraint.length == 0) {
                    filterResults.count = allData.size
                    filterResults.values = allData
                } else {
                    val resultsModel: MutableList<WatchListSearchModel> = ArrayList()
                    val searchStr = constraint.toString().toLowerCase()
                    for (item in allData) {
                        if (item.title.contains(searchStr)) {
                            resultsModel.add(item)
                        }
                        filterResults.count = resultsModel.size
                        filterResults.values = resultsModel
                    }
                }
                return filterResults
            }

            override fun publishResults(constraint: CharSequence, results: FilterResults) {
                filterList = results.values as List<WatchListSearchModel>?
                notifyDataSetChanged()
            }
        }
    }*/
}