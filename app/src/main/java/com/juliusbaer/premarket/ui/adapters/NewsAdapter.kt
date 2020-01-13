package com.juliusbaer.premarket.ui.adapters

import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.AsyncDifferConfig
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.models.serverModels.NewsModel
import kotlinx.android.synthetic.main.item_news.view.*
import java.text.SimpleDateFormat
import java.util.*

class NewsAdapter(val listener: NewsClickListener) : ListAdapter<NewsModel, NewsAdapter.ViewHolder>(AsyncDifferConfig.Builder(DiffCallback())
        .setBackgroundThreadExecutor(AsyncTask.THREAD_POOL_EXECUTOR)
        .build()) {
    companion object {
        private val sdf = SimpleDateFormat("d\nMMM", Locale.ENGLISH)
    }

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtDate: TextView = itemView.txtDate
        private val txtNews: TextView = itemView.txtNews
        private val del: ImageView = itemView.del
        private val newsCard: ConstraintLayout = itemView.newsCard

        init {
            del.setOnClickListener {
                if (adapterPosition >= 0) {
                    listener.onDeleteItem(getItem(adapterPosition).id)
                }
            }
            newsCard.setOnClickListener {
                if (adapterPosition >= 0) {
                    listener.onItemClick(getItem(adapterPosition).id)
                }
            }
        }

        fun bind(newsModel: NewsModel) {
            txtNews.typeface = if (newsModel.isRead == true) {
                ResourcesCompat.getFont(itemView.context, R.font.verlag_light)
            } else {
                ResourcesCompat.getFont(itemView.context, R.font.verlag_bold)
            }
            txtDate.text = (sdf.format(newsModel.publishDate * 1000L))
            txtNews.text = newsModel.headLine
        }
    }

    interface NewsClickListener {
        fun onDeleteItem(id: Int)
        fun onItemClick(id: Int)
    }

    private class DiffCallback : DiffUtil.ItemCallback<NewsModel>() {
        override fun areItemsTheSame(oldItem: NewsModel, newItem: NewsModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NewsModel, newItem: NewsModel): Boolean {
            return oldItem == newItem
        }
    }
}