package com.juliusbaer.premarket.ui.company.warrants

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.IDataManager
import com.juliusbaer.premarket.models.FilterModel
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.ui.adapters.WarrantsOnClickListener
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailFragment
import com.juliusbaer.premarket.ui.filter.FilterActivity
import com.juliusbaer.premarket.ui.filter.FilterFragment
import com.juliusbaer.premarket.ui.login.LoginActivity
import com.juliusbaer.premarket.ui.warrants.WarrantsAdapter
import com.juliusbaer.premarket.ui.warrants.WarrantsVM
import kotlinx.android.synthetic.main.company_warrants.*
import kotlinx.android.synthetic.main.include_company_warrants.*
import kotlinx.android.synthetic.main.include_warrants_filter.*
import javax.inject.Inject


open class CompanyWarrantsFragment(@LayoutRes layoutResId: Int = R.layout.company_warrants) : BaseNFragment(layoutResId) {
    @Inject
    lateinit var dataManager: IDataManager
    protected val viewModel by viewModels<WarrantsVM> { viewModelFactory }

    private var filter: FilterModel? = null
    private var listState: Parcelable? = null

    companion object {
        private const val ARG_COLLECTION_ID = "collectionId"
        private const val STATE_FILTER = "filter"
        private const val STATE_LIST = "listState"
        private const val FILTER_REQUEST_CODE = 1

        fun newInstance(collectionId: Int) = CompanyWarrantsFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_COLLECTION_ID, collectionId)
            }
        }
    }

    private val collectionId by lazy {
        arguments?.getInt(ARG_COLLECTION_ID)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        filter = savedInstanceState?.getParcelable(STATE_FILTER)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {
            listState = savedInstanceState.getParcelable(STATE_LIST)
        }
        txtFilterProducts.setOnClickListener {
            if (storage.isConfirmed()) {
                startActivityForResult(FilterActivity.newIntent(requireContext(), collectionId), FILTER_REQUEST_CODE)
            } else {
                startActivity(Intent(context, LoginActivity::class.java))
            }
        }
        filteredByLabel.setOnClickListener {
            startActivityForResult(FilterActivity.newIntent(requireContext(), collectionId, filter), FILTER_REQUEST_CODE)
        }
        filteredBy.setOnClickListener {
            startActivityForResult(FilterActivity.newIntent(requireContext(), collectionId, filter), FILTER_REQUEST_CODE)
        }

        showAllWarrants.setOnClickListener {
            spinner.isVisible = true

            viewModel.updateFilter(null)
        }
    }

    private val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            listState?.let {
                (recyclerView?.layoutManager as? LinearLayoutManager)?.onRestoreInstanceState(listState)
                listState = null
            } ?: run {
                if (positionStart == 0) recyclerView?.scrollToPosition(0)
            }
        }
    }

    private fun updateFilterLayoutVisibility(filter: FilterModel?) {
        val allWarrants = filter == null
        txtFilterProducts.isVisible = allWarrants

        filteredByLabel.isVisible = !allWarrants
        filteredBy.isVisible = !allWarrants
        filter?.let {
            val list = mutableListOf<String>()
            if (!it.underlyingTitle.isNullOrEmpty()) {
                list.add(it.underlyingTitle)
            }
            if (!it.contractOption.isNullOrEmpty()) {
                list.add(it.contractOption)
            }
            if (it.maturityStartDate != null || it.maturityEndDate != null) {
                list.add(getString(R.string.maturity))
            }
            if (it.strikePriceMin != null || it.strikePriceMax != null) {
                list.add(getString(R.string.strike_price_chf))
            }
            if (it.tradedVolume != null || it.tradedVolumeMax != null) {
                list.add(getString(R.string.min_traded_volume))
            }
            if (it.category?.size ?: 3 < 3) {
                list.add(getString(R.string.category))
            }
            filteredBy.text = list.joinToString(", ")
        }
        showAllWarrants.isVisible = !allWarrants
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.id = collectionId
        viewModel.filterLiveData.observe(viewLifecycleOwner, Observer {
            filter = it
            updateFilterLayoutVisibility(it)
        })
        viewModel.warrantsLiveData.observe(viewLifecycleOwner, Observer {
            spinner.isVisible = false
            populateWarrantsUI(it)
        })
        viewModel.networkStatusLiveData.observe(viewLifecycleOwner, Observer { throwable ->
            spinner.isVisible = false
            throwable?.let { parseError(it) }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            FILTER_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    listState = null
                    viewModel.updateFilter(data?.getParcelableExtra(FilterFragment.EXTRA_FILTER))
                }
                return
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroyView() {
        listState = (recyclerView.layoutManager as LinearLayoutManager).onSaveInstanceState()
        recyclerView.adapter?.unregisterAdapterDataObserver(adapterDataObserver)

        super.onDestroyView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        filter?.let { outState.putParcelable(STATE_FILTER, filter) }
        listState?.let { outState.putParcelable(STATE_LIST, listState) }
    }

    private fun populateWarrantsUI(data: PagedList<WarrantModel>) {
        val adapter = (recyclerView.adapter as? WarrantsAdapter)
                ?: WarrantsAdapter(object : WarrantsOnClickListener {
                    override fun onClick(warrant: WarrantModel) {
                        val fragment = WarrantDetailFragment.newInstance(warrant.id, false, warrant)
                        (activity as? NavigationHost)?.openFragment(fragment, null, true)
                    }
                }).apply {
                    registerAdapterDataObserver(adapterDataObserver)
                    recyclerView.adapter = this
                }
        val hasItems = data.isNotEmpty()
        adapter.submitList(data)
        recyclerView.isVisible = hasItems
        txtNoWarrants.isVisible = !hasItems
    }

    override fun onOnline() {
        doRequests(true)
    }

    @CallSuper
    protected open fun doRequests(isOnline: Boolean) {
        if (isOnline) {
            spinner.isVisible = true

            viewModel.retry()
        }
    }
}
