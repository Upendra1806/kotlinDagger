package com.juliusbaer.premarket.ui.company.news

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
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.detailNews.NewsDetailFragment
import kotlinx.android.synthetic.main.fragment_company_news.*

class CompanyNewsFragment : BaseNFragment(R.layout.fragment_company_news) {
    private val viewModel by viewModels<CompanyNewsVM> { viewModelFactory }

    companion object {
        private const val EXTRA_COLLECTION_ID = "collectionId"

        fun newInstance(collectionId: Int) = CompanyNewsFragment().apply {
            arguments = Bundle().apply {
                putInt(EXTRA_COLLECTION_ID, collectionId)
            }
        }
    }

    private val collectionId by lazy {
        requireArguments().getInt(EXTRA_COLLECTION_ID)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
                is Resource.Success -> viewModel.getNewsById(collectionId)
                is Resource.Failure -> if (!it.hasBeenHandled) parseError(it.e()!!, true)
            }
        })
        viewModel.newsLiveData.observe(viewLifecycleOwner, Observer {
            viewModel.getNewsById(collectionId)
        })
    }

    private fun populateNewsUI(data: List<NewsModel>) {
        val adapter = (recyclerView.adapter as? NewsAdapter)?:NewsAdapter(object : NewsAdapter.NewsClickListener {
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

    override fun onOnline() {
        doRequests(true)
    }

    private fun doRequests(isOnline: Boolean) {
        if (isFirstStart || isOnline) {
            viewModel.getNewsById(collectionId)
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
                        (parentFragment as? CompanyNewsFragment)?.deleteNews(requireArguments().getInt(ARG_NEWS_ID))
                    }
                    .setNegativeButton(R.string.no, null)
                    .show()
        }
    }
}
