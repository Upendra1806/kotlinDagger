package com.juliusbaer.premarket.ui.company.topWarrants

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.juliusbaer.premarket.R
import com.juliusbaer.premarket.dataFlow.NetworkStateManager
import com.juliusbaer.premarket.models.responseModel.Resource
import com.juliusbaer.premarket.models.serverModels.WarrantModel
import com.juliusbaer.premarket.ui.adapters.WarrantsOnClickListener
import com.juliusbaer.premarket.ui.base.BaseNFragment
import com.juliusbaer.premarket.ui.base.HasOfflinePlaceHolder
import com.juliusbaer.premarket.ui.base.NavigationChild
import com.juliusbaer.premarket.ui.base.NavigationHost
import com.juliusbaer.premarket.ui.detailWarrant.WarrantDetailFragment
import com.juliusbaer.premarket.ui.fragments.extentions.initToolbar
import kotlinx.android.synthetic.main.header_app.*
import kotlinx.android.synthetic.main.include_company_warrants.*
import kotlinx.android.synthetic.main.include_toolbar.*

class CompanyTopWarrantsFragment : BaseNFragment(R.layout.company_top_warrants), HasOfflinePlaceHolder, NavigationChild {
    override val titleResId: Int
        get() = R.string.warrants
    private val viewModel by viewModels<CompanyTopWarrantsVM> { viewModelFactory }

    private lateinit var adapter: CompanyTopWarrantsAdapter

    companion object {
        private const val EXTRA_COLLECTION_ID = "collectionId"

        fun newInstance(collectionId: Int) = CompanyTopWarrantsFragment().apply {
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

        initToolbar()

        toolbar.inflateMenu(R.menu.call)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.call) {
                callToTrader()
                true
            } else {
                false
            }
        }
        screenTitle.setText(R.string.warrants)

        adapter = CompanyTopWarrantsAdapter(object : WarrantsOnClickListener {
            override fun onClick(warrant: WarrantModel) {
                (activity as? NavigationHost)?.openFragment(WarrantDetailFragment.newInstance(warrant.id, false, warrant), null, true)
            }
        })
        recyclerView.adapter = adapter
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.itemsLiveData.observe(viewLifecycleOwner, Observer {
            progressDialog.hide()
            when (it) {
                is Resource.Success -> populateWarrantsUI(it.data)
                is Resource.Failure -> {
                    it.data?.let { data -> populateWarrantsUI(data) }
                }
            }
        })
    }

    private fun populateWarrantsUI(listWarrants: List<WarrantModel>) {
        val hasItems = listWarrants.isNotEmpty()

        adapter.submitList(listWarrants)
        recyclerView.isVisible = hasItems
        txtNoWarrants.isVisible = !hasItems
    }

    override fun onResume() {
        super.onResume()

        doRequests(NetworkStateManager.isNetworkAvailable(requireContext()))
    }

    override fun onOnline() {
        doRequests(true)
    }

    override fun onOffline() {
        if (adapter.itemCount == 0) {
            super.onOffline()
        }
    }

    private fun doRequests(isOnline: Boolean) {
        if (isFirstStart || isOnline) {
            progressDialog.show()
            viewModel.getWarrantsTop(collectionId)
        }
    }
}
