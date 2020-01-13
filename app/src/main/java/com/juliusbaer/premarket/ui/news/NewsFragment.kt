package com.juliusbaer.premarket.ui.news

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.NewsModel
import com.juliusbaer.premarket.ui.adapters.NewsAdapter
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.detailNews.NewsDetailFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.fragment_news.*
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_toolbar.*

class NewsFragment : BaseNFragment(R.layout.fragment_news), HasOfflinePlaceHolder, NavigationChild {
    companion object {
        const val TAG = "news"
    }

    override val titleResId: Int
        get() = R.string.news_overview

    private val viewModel by viewModels<NewsVM> { viewModelFactory }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar.inflateMenu(R.menu.call)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.call) {
                callToTrader()
                true
            } else {
                false
            }
        }
        initToolbar()
        screenTitle.setText(R.string.news_overview)

        recyclerView.setHasFixedSize(true)
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (positionStart == 0) recyclerView?.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        recyclerView.adapter?.unregisterAdapterDataObserver(adapterDataObserver)
        super.onDestroyView()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> populateNewsUI(it.data)
                is Resource.Failure -> {
                    it.data?.let { data -> populateNewsUI(data) }
                    if (!it.hasBeenHandled) parseError(it.e()!!)
                }
            }
        })
        viewModel.delNew.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> viewModel.loadNews()
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        viewModel.newsLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.loadNews()
        })
    }

    private fun populateNewsUI(data: List<NewsModel>) {
        val adapter = (recyclerView.adapter as? NewsAdapter)
                ?: NewsAdapter(object : NewsAdapter.NewsClickListener {
                    override fun onItemClick(id: Int) {
                        (activity as NavigationHost).openFragment(NewsDetailFragment.newInstance(id), null, true)
                    }

                    override fun onDeleteItem(id: Int) {
                        DeleteNewsDialog.newInstance(id).show(childFragmentManager, "deleteNews")
                    }
                }).apply {
                    registerAdapterDataObserver(adapterDataObserver)
                    recyclerView.adapter = this
                }
        adapter.submitList(data)

        val hasItems = data.isNotEmpty()
        recyclerView.isVisible = hasItems
        txtNoNews.isVisible = !hasItems
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    override fun onOffline() {
        if (recyclerView.adapter?.itemCount == 0) {
            super.onOffline()
        }
    }

    override fun onOnline() {
        doRequests(true)
    }

    private fun doRequests(isOnline: Boolean) {
        if (isOnline || isFirstStart) {
            progressDialog.show()
            viewModel.loadNews()
        }
    }

    private fun deleteNews(id: Int) {
        progressDialog.show()
        viewModel.newsDel(id)
    }

    class DeleteNewsDialog : DialogFragment() {
        companion object {
            private const val ARG_NEWS_ID = "newsId"

            fun newInstance(id: Int): DeleteNewsDialog {
                return DeleteNewsDialog().apply {
                    arguments = bundleOf(ARG_NEWS_ID to id)
                }
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            return AlertDialog.Builder(requireContext())
                    .setMessage(R.string.are_you_sure_delete_item_from_news)
                    .setPositiveButton(R.string.delete) { _, _ ->
                        (parentFragment as? NewsFragment)?.deleteNews(requireArguments().getInt(ARG_NEWS_ID))
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        }
    }
}
